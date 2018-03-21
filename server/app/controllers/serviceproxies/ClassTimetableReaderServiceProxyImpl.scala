package controllers.serviceproxies

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, Uri}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.google.inject.Singleton
import com.typesafe.config.ConfigFactory
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import controllers.planning.termly.TermPlansHelper
import duplicate.model.ClassDetails
import duplicate.model.planning.{LessonSummary, LessonSummaryOrdering, LessonsThisWeek}
import io.sudostream.timetoteach.kafka.serializing.systemwide.classes.ClassDetailsCollectionDeserializer
import io.sudostream.timetoteach.kafka.serializing.systemwide.classtimetable.ClassTimetableDeserializer
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.ClassTimetable
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.sessions.SessionOfTheDayWrapper
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.subjectdetail.SubjectDetailWrapper
import javax.inject.Inject
import models.timetoteach.TimeToTeachUserId
import play.api.Logger
import shared.model.classtimetable.{WWWClassTimetable, WwwClassId}
import utils.{ClassDetailsAvroConverter, ClassTimetableConverterToAvro}

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Sorting, Success}

@Singleton
class ClassTimetableReaderServiceProxyImpl @Inject()(schoolReader: SchoolReaderServiceProxyImpl) {

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

  val schoolsFuture = schoolReader.getAllSchoolsFuture

  def readAvroClassTimetable(userId: TimeToTeachUserId, wwwClassId: WwwClassId): Future[Option[ClassTimetable]] = {
    logger.debug(s"readClassTimetable: ${userId.toString}:${wwwClassId.value}")

    val uriString =
      s"$protocol://$classTimetableReaderServiceHostname:$classTimetableReaderServicePort/api/classtimetables?" +
        s"classId=${wwwClassId.value}&timeToTeachUserId=${userId.value}"
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
      extractAvroClassTimeTableFromHttpResponse(httpResponse)
    }

    eventualFutureWwwClassTimetable.flatMap(res => res)
  }

  def readClassTimetable(userId: TimeToTeachUserId, wwwClassId: WwwClassId): Future[Option[WWWClassTimetable]] = {
    val avroTimetable = readAvroClassTimetable(userId, wwwClassId)
    convertToWwwVersion(avroTimetable)
  }

  def extractAvroClassTimeTableFromHttpResponse(httpResponse: HttpResponse): Future[Option[ClassTimetable]] = {
    if (httpResponse.status.isSuccess()) {
      val smallTimeout = 3000.millis
      val dataFuture = httpResponse.entity.toStrict(smallTimeout) map {
        httpEntity =>
          httpEntity.getData()
      }

      dataFuture map {
        databytes =>
          val bytesAsArray = databytes.toArray
          val classTimetableDeserializer = new ClassTimetableDeserializer
          Some(classTimetableDeserializer.deserialize("ignore", bytesAsArray))
      }
    } else {
      logger.warn(s"Issue finding class timetable : ${httpResponse.toString()}")
      Future {
        None
      }
    }
  }

  def convertToWwwVersion(futureTimetable: Future[Option[ClassTimetable]]): Future[Option[WWWClassTimetable]] = {
    futureTimetable map {
      case Some(timetable) => Some(ClassTimetableConverterToAvro.convertAvroClassTimeTableToWww(timetable))
      case None => None
    }
  }

  def extractClassesAssociatedWithTeacher(userId: TimeToTeachUserId): Future[List[ClassDetails]] = {
    logger.debug(s"extractClassesAssociatedWithTeacher: ${userId.toString}")

    val uriString =
      s"$protocol://$classTimetableReaderServiceHostname:$classTimetableReaderServicePort/api/classes/user/${userId.value}?" +
        s"timeToTeachUserId=${userId.value}"

    logger.debug(s"uri for extracting classes for teacher is $uriString")

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
        logger.debug("Classes bytes received for teacher")

      case Failure(ex) => logger.error(s"Issue updating Class Timetable: ${ex.getMessage}")
    }

    val eventualFutureClassDetails = response map { httpResponse =>
      extractClassesFromHttpResponse(httpResponse)
    }

    eventualFutureClassDetails.flatMap {
      res =>
        res
    }
  }

  def extractClassesFromHttpResponse(httpResponse: HttpResponse): Future[List[ClassDetails]] = {
    if (httpResponse.status.isSuccess()) {
      val smallTimeout = 3000.millis
      val dataFuture = httpResponse.entity.toStrict(smallTimeout) map {
        httpEntity =>
          httpEntity.getData()
      }
      val futureClassDetailsCollection = dataFuture map {
        databytes =>
          val bytesAsArray = databytes.toArray
          val classDetailsCollectionDeserializer = new ClassDetailsCollectionDeserializer
          classDetailsCollectionDeserializer.deserialize("ignore", bytesAsArray)
      }


      {
        for {
          schools <- schoolsFuture
        } yield futureClassDetailsCollection map {
          classDetailsCollection =>
            ClassDetailsAvroConverter.convertAvroClassDetailsCollectionToModel(classDetailsCollection,
              schools)
        }
      }.flatMap(res => res)
    } else {
      logger.warn(s"Issue finding classes for teacher : ${httpResponse.toString()}")
      Future {
        Nil
      }
    }
  }

  private def buildDayAwareSubjectDetailWrapper(sessions: List[SessionOfTheDayWrapper]):
  List[(String, SubjectDetailWrapper)] = {
    @tailrec
    def buildDayAwareSubjectDetailWrapperLoop(
                                               remainingList: List[SessionOfTheDayWrapper],
                                               currentResult: List[(String, SubjectDetailWrapper)]):
    List[(String, SubjectDetailWrapper)] = {
      if (remainingList.isEmpty) currentResult
      else {
        val nextValue = remainingList.head
        val newMiniList = nextValue.sessionOfTheDay.subjects.map(elem => (nextValue.sessionOfTheDay.dayOfTheWeek.toString, elem))
        val newList = newMiniList ::: currentResult
        buildDayAwareSubjectDetailWrapperLoop(remainingList.tail, newList)
      }
    }

    buildDayAwareSubjectDetailWrapperLoop(sessions, Nil)
  }

  private def createThisWeeksLessons(classTimetable: ClassTimetable): LessonsThisWeek = {
    @tailrec
    def createThisWeeksLessonsLoop(remainingSubjects: List[(String, SubjectDetailWrapper)],
                                   currentMap: Map[String, List[LessonSummary]]): Map[String, List[LessonSummary]] = {
      if (remainingSubjects.isEmpty) currentMap
      else {
        val nextSubjectName = TermPlansHelper.convertSubjectStringToSubjectName(
          remainingSubjects.head._2.subjectDetail.subjectName.toString).toString
        val nextSubjectLessonSummary = LessonSummary(
          subject = nextSubjectName,
          dayOfWeek = remainingSubjects.head._1,
          startTimeIso = remainingSubjects.head._2.subjectDetail.startTime.timeIso8601,
          endTimeIso = remainingSubjects.head._2.subjectDetail.endTime.timeIso8601
        )

        val nextMap: Map[String, List[LessonSummary]] = if (currentMap.isDefinedAt(nextSubjectName)) {
          val currentLessonSummaries = currentMap(nextSubjectName)
          val newList = nextSubjectLessonSummary :: currentLessonSummaries
          val newListAsArray = newList.toArray
          Sorting.quickSort(newListAsArray)(LessonSummaryOrdering)
          currentMap + (nextSubjectName -> newListAsArray.toList)
        } else {
          currentMap + (nextSubjectName -> List(nextSubjectLessonSummary))
        }

        createThisWeeksLessonsLoop(remainingSubjects.tail, nextMap)
      }
    }

    val subjectsOfTheWeek = buildDayAwareSubjectDetailWrapper(classTimetable.allSessionsOfTheWeek)

    val subjectsOfTheWeekMap = createThisWeeksLessonsLoop(subjectsOfTheWeek, Map())
    logger.debug(s"=+=+= subjectsOfTheWeekMap: ${subjectsOfTheWeekMap.toString()}")
    LessonsThisWeek(subjectsOfTheWeekMap)
  }

  def getThisWeeksLessons(userId: TimeToTeachUserId, wwwClassId: WwwClassId): Future[Option[LessonsThisWeek]] = {
    val futureMaybeClassTimetable = readAvroClassTimetable(userId, wwwClassId)
    for {
      maybeClassTimetable: Option[ClassTimetable] <- futureMaybeClassTimetable
      if maybeClassTimetable.isDefined
      lessonsThisWeek = createThisWeeksLessons(maybeClassTimetable.get)
    } yield Some(lessonsThisWeek)
  }

}
