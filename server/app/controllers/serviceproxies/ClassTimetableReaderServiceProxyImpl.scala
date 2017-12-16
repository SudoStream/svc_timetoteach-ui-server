package controllers.serviceproxies

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, Uri}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.google.inject.Singleton
import com.typesafe.config.ConfigFactory
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import io.sudostream.timetoteach.kafka.serializing.systemwide.classtimetable.ClassTimetableDeserializer
import io.sudostream.timetoteach.kafka.serializing.systemwide.model.SchoolsDeserializer
import play.api.Logger
import shared.model.classtimetable.{WWWClassTimetable, WwwClassName}
import utils.ClassTimetableConverterToAvro

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

@Singleton
class ClassTimetableReaderServiceProxyImpl {
  implicit val system: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(5 seconds)
  val logger: Logger.type = Logger
  private val config = ConfigFactory.load()
  val http = Http(system)
  private val classTimetableReaderServiceHostname = config.getString("services.classtimetable-reader-host")
  private val classTimetableReaderServicePort = config.getString("services.classtimetable-reader-port")
  private val minikubeRun: Boolean = sys.props.getOrElse("minikubeEnv", "false").toBoolean
  val protocol: String = if (classTimetableReaderServicePort.toInt > 9000 && !minikubeRun) "http" else "https"

  def readClassTimetable(userId: TimeToTeachUserId, wwwClassName: WwwClassName): Future[Option[WWWClassTimetable]] = {
    logger.debug(s"readClassTimetable: ${userId.toString}:${wwwClassName.value}")

    val uriString =
      s"$protocol://$classTimetableReaderServiceHostname:$classTimetableReaderServicePort/api/classtimetables?" +
        s"className=${wwwClassName.value}&timeToTeachUserId=${userId.value}"
    logger.debug(s"uri for upserting class timetable is $uriString")

    val classTimetableReaderServiceUri = Uri(uriString)

    val wwwClassTimetableRequest = HttpRequest(HttpMethods.GET, uri = classTimetableReaderServiceUri)

    val badSslConfig = AkkaSSLConfig().mapSettings(s =>
      s.withLoose(s.loose.withDisableSNI(true))
        .withLoose(s.loose.withDisableHostnameVerification(true))
        .withLoose(s.loose.withAcceptAnyCertificate(true))
    )

    logger.info(s"ssl config = ${badSslConfig.toString}")
    val badCtx = Http().createClientHttpsContext(badSslConfig)

    val response: Future[HttpResponse] = http.singleRequest(wwwClassTimetableRequest, badCtx)

    response onComplete {
      case Success(httpResponse) =>
        logger.debug("Class Timetable bytes received")

      case Failure(ex) => logger.error(s"Issue updating Class Timetable: ${ex.getMessage}")
    }

    val eventualFutureWwwClassTimetable = response map { httpResponse =>
      extractClassTimeTableFromHttpResponse(httpResponse)
    }

    eventualFutureWwwClassTimetable.flatMap {
      res =>
        res
    }
  }

  def extractClassTimeTableFromHttpResponse(httpResponse: HttpResponse): Future[Option[WWWClassTimetable]] = {
    if (httpResponse.status.isSuccess()) {
      val smallTimeout = 3000.millis
      val dataFuture = httpResponse.entity.toStrict(smallTimeout) map {
        httpEntity =>
          httpEntity.getData()
      }
      val futureTimetable = dataFuture map {
        databytes =>
          val bytesAsArray = databytes.toArray
          val classTimetableDeserializer = new ClassTimetableDeserializer
          classTimetableDeserializer.deserialize("ignore", bytesAsArray)
      }

      futureTimetable map {
        fTimetable =>
          Some(ClassTimetableConverterToAvro.convertAvroClassTimeTableToWww(fTimetable))
      }
    } else {
      logger.warn(s"Issue finding class timatable : ${httpResponse.toString()}")
      Future {
        None
      }
    }
  }


}
