package controllers.serviceproxies

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, Uri}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.google.inject.Singleton
import com.typesafe.config.ConfigFactory
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import duplicate.model.ClassDetails
import io.sudostream.timetoteach.kafka.serializing.systemwide.classes.ClassDetailsSerializer
import io.sudostream.timetoteach.kafka.serializing.systemwide.classtimetable.ClassTimetableSerializer
import play.api.Logger
import shared.model.classtimetable.{WWWClassTimetable, WwwClassId, WwwClassName}
import utils.ClassDetailsAvroConverter.convertPickledClassToAvro
import utils.ClassTimetableConverterToAvro.convertWwwClassTimeTableToAvro

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

@Singleton
class ClassTimetableWriterServiceProxyImpl {

  implicit val system: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(5 seconds)
  val logger: Logger.type = Logger
  private val config = ConfigFactory.load()
  val http = Http(system)
  private val classTimetableWriterServiceHostname = config.getString("services.classtimetable-writer-host")
  private val classTimetableWriterServicePort = config.getString("services.classtimetable-writer-port")
  private val minikubeRun: Boolean = sys.props.getOrElse("minikubeEnv", "false").toBoolean
  val protocol: String = if (classTimetableWriterServicePort.toInt > 9000 && !minikubeRun) "http" else "https"


  def upsertClassTimetables(userId: TimeToTeachUserId, wwwClassId: WwwClassId, wWWClassTimetable: WWWClassTimetable): Future[Boolean] = {
    logger.debug(s"upsertClassTimetables: ${userId.value}:${wwwClassId.value}:${wWWClassTimetable.toString}")

    val uriString = s"$protocol://$classTimetableWriterServiceHostname:$classTimetableWriterServicePort/api/classtimetables/${userId.value}/upsert"
    logger.debug(s"uri for upserting class timetable is $uriString")
    val classTimetableWriterServiceUri = Uri(uriString)

    val classTimetable = convertWwwClassTimeTableToAvro(userId.value, wwwClassId, wWWClassTimetable)

    val userClassTimetableSerializer = new ClassTimetableSerializer
    val classTimetableBytes = userClassTimetableSerializer.serialize("ignore", classTimetable)

    val postEditClassTimetableRequest = HttpRequest(HttpMethods.POST, uri = classTimetableWriterServiceUri, entity = classTimetableBytes)

    val badSslConfig = AkkaSSLConfig().mapSettings(s =>
      s.withLoose(s.loose.withDisableSNI(true))
        .withLoose(s.loose.withDisableHostnameVerification(true))
        .withLoose(s.loose.withAcceptAnyCertificate(true))
    )

    logger.info(s"ssl config = ${badSslConfig.toString}")
    val badCtx = Http().createClientHttpsContext(badSslConfig)

    val response: Future[HttpResponse] = http.singleRequest(postEditClassTimetableRequest, badCtx)

    response onComplete {
      case Success(httpResponse) => logger.info("Class Timetable Updated!")
      case Failure(ex) => logger.error(s"Issue updating Class Timetable: ${ex.getMessage}")
    }

    response map { httpResponse =>
      if (httpResponse.status.isSuccess()) true else false
    }
  }

  def upsertClass(userId: TimeToTeachUserId, newClass: ClassDetails): Future[Boolean] = {
    val uriString = s"$protocol://$classTimetableWriterServiceHostname:$classTimetableWriterServicePort/api/classes/${userId.value}/upsert"
    logger.debug(s"uri for upserting a class is $uriString")
    val classTimetableWriterServiceUri = Uri(uriString)

    val classDetailsSerializer = new ClassDetailsSerializer
    val newClassBytes = classDetailsSerializer.serialize("ignore", convertPickledClassToAvro(newClass))

    val postEditClassTimetableRequest = HttpRequest(HttpMethods.POST, uri = classTimetableWriterServiceUri, entity = newClassBytes)

    val badSslConfig = AkkaSSLConfig().mapSettings(s =>
      s.withLoose(s.loose.withDisableSNI(true))
        .withLoose(s.loose.withDisableHostnameVerification(true))
        .withLoose(s.loose.withAcceptAnyCertificate(true))
    )

    logger.info(s"ssl config = ${badSslConfig.toString}")
    val badCtx = Http().createClientHttpsContext(badSslConfig)

    val response: Future[HttpResponse] = http.singleRequest(postEditClassTimetableRequest, badCtx)

    response onComplete {
      case Success(httpResponse) => logger.info("New Class Created/Updated!")
      case Failure(ex) => logger.error(s"Problem when creating/updating Class Timetable: ${ex.getMessage}")
    }

    response map { httpResponse =>
      if (httpResponse.status.isSuccess()) true else false
    }
  }

  def deleteClass(userId: TimeToTeachUserId, classId: String): Future[HttpResponse] = {
    val uriString = s"$protocol://$classTimetableWriterServiceHostname:$classTimetableWriterServicePort" +
      s"/api/classes/${userId.value}/$classId"
    logger.debug(s"\n\nuri for deleting the class is $uriString")
    val classTimetableWriterServiceUri = Uri(uriString)
    val postEditClassTimetableRequest = HttpRequest(HttpMethods.DELETE, uri = classTimetableWriterServiceUri)

    val badSslConfig = AkkaSSLConfig().mapSettings(s =>
      s.withLoose(s.loose.withDisableSNI(true))
        .withLoose(s.loose.withDisableHostnameVerification(true))
        .withLoose(s.loose.withAcceptAnyCertificate(true))
    )

    logger.info(s"ssl config = ${badSslConfig.toString}")
    val badCtx = Http().createClientHttpsContext(badSslConfig)

    val eventualResponse: Future[HttpResponse] = http.singleRequest(postEditClassTimetableRequest, badCtx)

    eventualResponse onComplete {
      case Success(httpResponse) =>
        if (httpResponse.status.isSuccess()) {
          logger.info(s"Class $classId Deleted Successfully!")
        } else {
          logger.error("Failed to delete class $classId!")
        }
      case Failure(ex) => logger.error(s"Problem when deleting class $classId : ${ex.getMessage}")
    }

    eventualResponse
  }

}