package controllers.planning.classtimetable

import java.time.LocalTime
import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{ActionBuilders, AuthenticatedRequest, DeadboltActions}
import com.typesafe.config.ConfigFactory
import controllers.serviceproxies._
import duplicate.model.ClassDetails
import io.sudostream.timetoteach.messages.systemwide.model
import io.sudostream.timetoteach.messages.systemwide.model.{User, UserPreferences}
import models.timetoteach.classtimetable.SchoolDayTimes
import models.timetoteach.{CookieNames, School}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.mvc._
import security.MyDeadboltHandler
import shared.model.classtimetable.WwwClassName
import shared.util.LocalTimeUtil
import utils.ClassTimetableConverterToAvro.convertJsonClassTimetableToWwwClassTimetable
import utils.TemplateUtils.getCookieStringFromRequest

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class ClassTimetableController @Inject()(classTimetableWriter: ClassTimetableWriterServiceProxyImpl,
                                         userReader: UserReaderServiceProxyImpl,
                                         classTimetableReaderProxy: ClassTimetableReaderServiceProxyImpl,
                                         schoolsReader: SchoolReaderServiceProxyImpl,
                                         cc: ControllerComponents,
                                         deadbolt: DeadboltActions,
                                         handlers: HandlerCache,
                                         actionBuilder: ActionBuilders) extends AbstractController(cc) {

  implicit val system: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(5.seconds)
  private val config = ConfigFactory.load()
  private val userServiceHostname = config.getString("services.user-service-host")
  private val userServicePort = config.getString("services.user-service-port")
  private val timeToTeachFacebookId = config.getString("login.details.facebook.timetoteach-facebook-id")
  private val timeToTeachFacebookSecret = config.getString("login.details.facebook.timetoteach-facebook-secret")
  val logger: Logger.type = Logger

  val defaultSchoolDayTimes = SchoolDayTimes(
    schoolDayStarts = LocalTime.of(9, 0),
    morningBreakStarts = LocalTime.of(10, 30),
    morningBreakEnds = LocalTime.of(10, 45),
    lunchStarts = LocalTime.of(12, 0),
    lunchEnds = LocalTime.of(13, 0),
    schoolDayEnds = LocalTime.of(15, 0)
  )

  val newClassForm = Form(
    mapping(
      "newClassPickled" -> text,
      "tttUserId" -> text
    )(NewClassJson.apply)(NewClassJson.unapply)
  )

  case class NewClassJson(newClassPickled: String, tttUserId: String)

  val classTimetableForm = Form(
    mapping(
      "classTimetable" -> text,
      "className" -> text,
      "tttUserId" -> text
    )(ClassTimeTableJson.apply)(ClassTimeTableJson.unapply)
  )

  case class ClassTimeTableJson(classTimetable: String, className: String, tttUserId: String)

  def classTimetableSave: Action[AnyContent] = Action.async { implicit request =>
    val classTimetableFormBound = classTimetableForm.bindFromRequest.get
    logger.debug(s"Class Timetable As Json = ${classTimetableFormBound.classTimetable}")
    logger.debug(s"Class Name As Json = ${classTimetableFormBound.className}")
    logger.debug(s"TTT User Id = ${classTimetableFormBound.tttUserId}")

    val wwwClassTimetable = convertJsonClassTimetableToWwwClassTimetable(classTimetableFormBound.classTimetable)
    classTimetableWriter.upsertClassTimetables(
      TimeToTeachUserId(classTimetableFormBound.tttUserId),
      WwwClassName(classTimetableFormBound.className),
      wwwClassTimetable
    )

    Future {
      Ok("Saved class timetable!")
    }
  }


  def classTimetable: Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest =>
    val (userPictureUri: Option[String], userFirstName: Option[String], userFamilyName: Option[String], tttUserId: String) = extractCommonHeaders(authRequest)

    val futureUserPrefs: Future[Option[UserPreferences]] = userReader.getUserPreferences(TimeToTeachUserId(tttUserId))

    val futureSchoolDayTimes: Future[SchoolDayTimes] = futureUserPrefs map {
      case Some(userPrefs) =>
        val schoolTimes = userPrefs.allSchoolTimes.head
        logger.debug(s"classTimetable() : schoolTimes : ${schoolTimes.toString}")
        SchoolDayTimes(
          schoolDayStarts = LocalTimeUtil.convertStringTimeToLocalTime(schoolTimes.schoolStartTime).getOrElse(
            defaultSchoolDayTimes.schoolDayStarts),
          morningBreakStarts = LocalTimeUtil.convertStringTimeToLocalTime(schoolTimes.morningBreakStartTime).getOrElse(
            defaultSchoolDayTimes.morningBreakStarts),
          morningBreakEnds = LocalTimeUtil.convertStringTimeToLocalTime(schoolTimes.morningBreakEndTime).getOrElse(
            defaultSchoolDayTimes.morningBreakEnds),
          lunchStarts = LocalTimeUtil.convertStringTimeToLocalTime(schoolTimes.lunchStartTime).getOrElse(
            defaultSchoolDayTimes.lunchStarts),
          lunchEnds = LocalTimeUtil.convertStringTimeToLocalTime(schoolTimes.lunchEndTime).getOrElse(
            defaultSchoolDayTimes.lunchEnds),
          schoolDayEnds = LocalTimeUtil.convertStringTimeToLocalTime(schoolTimes.schoolEndTime).getOrElse(
            defaultSchoolDayTimes.schoolDayEnds)
        )
      case None => defaultSchoolDayTimes
    }

    val futureClassName: Future[String] = futureUserPrefs map {
      case Some(userPrefs) => userPrefs.allSchoolTimes.head.userTeachesTheseClasses.head.className
      case None => "<No Class Name>"
    }

    for {
      schoolDayTimes <- futureSchoolDayTimes
      className <- futureClassName
      wwwClassTimetableFuture = classTimetableReaderProxy.
        readClassTimetable(TimeToTeachUserId(tttUserId), WwwClassName(className))
      maybeWwwClassTimetable <- wwwClassTimetableFuture
    } yield {
      Ok(views.html.planning.classtimetables.classtimetable(new MyDeadboltHandler(userReader),
        userPictureUri,
        userFirstName,
        userFamilyName,
        schoolDayTimes,
        maybeWwwClassTimetable,
        className,
        TimeToTeachUserId(tttUserId)
      )(authRequest))
    }
  }

  private def extractCommonHeaders(authRequest: AuthenticatedRequest[AnyContent]) = {
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)
    val tttUserId = getCookieStringFromRequest(CookieNames.timetoteachId, authRequest).getOrElse("NO ID")
    (userPictureUri, userFirstName, userFamilyName, tttUserId)
  }

  def classesHome: Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest =>
    val (userPictureUri: Option[String], userFirstName: Option[String], userFamilyName: Option[String], tttUserId: String) = extractCommonHeaders(authRequest)
    val futureMaybeUser = userReader.getUserDetails(TimeToTeachUserId(tttUserId))

    {
      for {
        classesAssociatedWithTeacher <- classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(tttUserId))
        maybeUser <- futureMaybeUser
        user: User = maybeUser match {
          case Some(theUser) => theUser
          case None => null
        }
        userSchoolsWrappers = user.schools
        userSchoolIds = userSchoolsWrappers.map(theSchoolWrapper => theSchoolWrapper.school.id)
        allSchools = schoolsReader.getAllSchools().getOrElse(Nil)
      } yield Future {
        Ok(views.html.planning.classtimetables.classesHome(new MyDeadboltHandler(userReader),
          userPictureUri,
          userFirstName,
          userFamilyName,
          TimeToTeachUserId(tttUserId),
          classesAssociatedWithTeacher,
          allSchools.filter(school => userSchoolIds.contains(school.id))
        ))
      }
    }.flatMap(res => res)
  }


  def addNewClass(): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest =>
    val (userPictureUri: Option[String], userFirstName: Option[String], userFamilyName: Option[String], tttUserId: String) = extractCommonHeaders(authRequest)
    val futureMaybeUser = userReader.getUserDetails(TimeToTeachUserId(tttUserId))

    {
      for {
        classesAssociatedWithTeacher <- classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(tttUserId))
        maybeUser <- futureMaybeUser
        user: User = maybeUser match {
          case Some(theUser) => theUser
          case None => null
        }
        userSchools: List[model.School] = user.schools.map { schoolWrapper => schoolWrapper.school }
        userSchoolIds = userSchools.map {
          theSchool =>
            if (theSchool.id == "b3a608f1-eb84-437d-844e-3af547cde77c") {
              logger.debug(s"the user school id ==> '${theSchool.id}'")
            }
            theSchool.id
        }
        allTheSchools: List[School] = schoolsReader.getAllSchools().getOrElse(Nil)
        allUserSchools: List[School] = allTheSchools.filter { eachSchool => userSchoolIds.contains(eachSchool.id) }
      } yield Future {
        Ok(views.html.planning.classtimetables.addNewClass(new MyDeadboltHandler(userReader),
          userPictureUri,
          userFirstName,
          userFamilyName,
          TimeToTeachUserId(tttUserId),
          allUserSchools
        ))
      }
    }.flatMap(res => res)
  }

  def saveNewClass: Action[AnyContent] = Action.async { implicit request =>
    val newClassFormBound = newClassForm.bindFromRequest.get
    logger.debug(s"New Class Pickled = #${newClassFormBound.newClassPickled}#")
    logger.debug(s"TTT User Id = ${newClassFormBound.tttUserId}")

    import upickle.default._
    val newClassDetails = read[ClassDetails](newClassFormBound.newClassPickled)

    logger.debug(s"New Class Unpickled = ${newClassDetails.toString}")
    classTimetableWriter.upsertClass(
      TimeToTeachUserId(newClassFormBound.tttUserId),
      newClassDetails.asInstanceOf[ClassDetails]
    )

    Future {
      Ok("Created new class!")
    }
  }

  def deleteClass(tttUserId: String, classId: String): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"request for deleteClass: ${request.toString()}")
    logger.debug(s"Attempting to delete class '$classId' for user '$tttUserId'")

    val eventualResponse = classTimetableWriter.deleteClass(TimeToTeachUserId(tttUserId), classId)

    eventualResponse.onComplete {
      case Success(httpResponse) =>
        if (httpResponse.status.isSuccess()) {
          logger.info(s"Succees Deleting class '$classId' for user '$tttUserId'")
        } else {
          logger.error(s"Problem class '$classId' for user '$tttUserId'")
        }
      case Failure(ex) =>
        logger.error(s"Failed Response Deleting class '$classId' for user '$tttUserId'. Exception is ${ex.getMessage}")
    }

    for {
      response <- eventualResponse
    } yield Status(response.status.intValue())
  }

}