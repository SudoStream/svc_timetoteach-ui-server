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
import controllers.time.SystemTime
import duplicate.model.ClassDetails
import io.sudostream.timetoteach.messages.systemwide.model
import io.sudostream.timetoteach.messages.systemwide.model.{User, UserPreferences}
import models.timetoteach.classtimetable.SchoolDayTimes
import models.timetoteach.{CookieNames, School, TimeToTeachUserId}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.mvc._
import security.MyDeadboltHandler
import shared.model.classtimetable.WwwClassId
import shared.util.{LocalTimeUtil, PlanningHelper}
import utils.ClassTimetableConverterToAvro.convertJsonClassTimetableToWwwClassTimetable
import utils.SchoolConverter
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
                                         termService: TermServiceProxy,
                                         systemTime: SystemTime,
                                         actionBuilder: ActionBuilders) extends AbstractController(cc)
  with ClassTimetableControllerHelper {

  implicit val system: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(5.seconds)
  private val config = ConfigFactory.load()
  private val userServiceHostname = config.getString("services.user-service-host")
  private val userServicePort = config.getString("services.user-service-port")
  private val timeToTeachFacebookId = config.getString("login.details.facebook.timetoteach-facebook-id")
  private val timeToTeachFacebookSecret = config.getString("login.details.facebook.timetoteach-facebook-secret")
  private val logger: Logger.type = Logger

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
      "classId" -> text,
      "tttUserId" -> text
    )(ClassTimeTableJson.apply)(ClassTimeTableJson.unapply)
  )

  case class ClassTimeTableJson(classTimetable: String, classId: String, tttUserId: String)

  def classTimetableSave: Action[AnyContent] = Action.async { implicit request =>
    val classTimetableFormBound = classTimetableForm.bindFromRequest.get
    logger.debug(s"Class Timetable As Json = ${classTimetableFormBound.classTimetable}")
    logger.debug(s"Class Id As Json = ${classTimetableFormBound.classId}")
    logger.debug(s"TTT User Id = ${classTimetableFormBound.tttUserId}")

    val wwwClassTimetable = convertJsonClassTimetableToWwwClassTimetable(PlanningHelper.decodeAnyNonFriendlyCharacters(classTimetableFormBound.classTimetable))
    val res = classTimetableWriter.upsertClassTimetables(
      TimeToTeachUserId(classTimetableFormBound.tttUserId),
      WwwClassId(classTimetableFormBound.classId),
      wwwClassTimetable
    )

    for {
      done <- res
    } yield Ok("Saved class timetable!")
  }


  def classTimetable(classId: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest =>
    val eventualTodaysDate = systemTime.getToday
    val (userPictureUri: Option[String], userFirstName: Option[String], userFamilyName: Option[String], tttUserId: String) = extractCommonHeaders(authRequest)
    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(tttUserId))
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

    def printToLog(number: Int, something: Any): Int = {
      println(s"++++++++++++++++++++++++++++++ cough $number.toString ========= ${something.toString}")
      1
    }

    for {
      todaysDate <- eventualTodaysDate
      classes <- eventualClasses
      classDetailsList = classes.filter(theClass => theClass.id.id == classId)
      maybeClassDetails: Option[ClassDetails] = classDetailsList.headOption
      cough = printToLog(1, maybeClassDetails)
      if maybeClassDetails.isDefined
      classDetails = maybeClassDetails.get

      futureMaybeCurrentSchoolTerm = termService.currentSchoolTerm(SchoolConverter.convertLocalAuthorityStringToAvroVersion(classDetails.schoolDetails.localAuthority))

      schoolDayTimes <- futureSchoolDayTimes
      wwwClassTimetableFuture = classTimetableReaderProxy.
        readClassTimetable(TimeToTeachUserId(tttUserId), WwwClassId(classDetails.id.id))
      maybeWwwClassTimetable <- wwwClassTimetableFuture

      maybeCurrentSchoolTerm <- futureMaybeCurrentSchoolTerm
      cough2 = printToLog(2, maybeCurrentSchoolTerm )
      if maybeCurrentSchoolTerm.isDefined
    } yield {
      Ok(views.html.planning.classtimetables.classtimetable(new MyDeadboltHandler(userReader),
        userPictureUri,
        userFirstName,
        userFamilyName,
        schoolDayTimes,
        maybeWwwClassTimetable,
        TimeToTeachUserId(tttUserId),
        classDetails,
        maybeCurrentSchoolTerm.get,
        todaysDate
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
    val eventualTodaysDate = systemTime.getToday

    {
      for {
        classesAssociatedWithTeacher <- classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(tttUserId))
        todaysDate <- eventualTodaysDate
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
          allSchools.filter(school => userSchoolIds.contains(school.id)),
          todaysDate
        ))
      }
    }.flatMap(res => res)
  }


  def addNewClass(): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest =>
    val (userPictureUri: Option[String], userFirstName: Option[String], userFamilyName: Option[String], tttUserId: String) = extractCommonHeaders(authRequest)
    val futureMaybeUser = userReader.getUserDetails(TimeToTeachUserId(tttUserId))
    val eventualTodaysDate = systemTime.getToday

    {
      for {
        classesAssociatedWithTeacher <- classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(tttUserId))
        todaysDate <- eventualTodaysDate
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
          allUserSchools,
          todaysDate
        ))
      }
    }.flatMap(res => res)
  }

  def gotoClass(classId: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest =>
    val (userPictureUri: Option[String], userFirstName: Option[String], userFamilyName: Option[String], tttUserId: String) = extractCommonHeaders(authRequest)
    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(tttUserId))
    val futureUserPrefs = userReader.getUserPreferences(TimeToTeachUserId(tttUserId))

    {
      for {
        classes <- eventualClasses
        classDetailsList = classes.filter(theClass => theClass.id.id == classId)
        maybeClassDetails = classDetailsList.headOption
        if maybeClassDetails.isDefined
        classDetails = maybeClassDetails.get

        userPrefs <- futureUserPrefs

        route = if (classDetails.groups.isEmpty) {
          Future {
            Redirect(controllers.planning.classtimetable.routes.ClassTimetableController.manageClass(classDetails.id.id))
          }
        } else {
          val schoolDayTimes = userPrefs match {
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

          val wwwClassTimetableFuture = classTimetableReaderProxy.
            readClassTimetable(TimeToTeachUserId(tttUserId), WwwClassId(classDetails.id.id))

          for {
            maybeWwwClassTimetable <- wwwClassTimetableFuture
          } yield Redirect(controllers.planning.classtimetable.routes.ClassTimetableController.classTimetable(classDetails.id.id))
        }
      } yield route
    }.flatMap(res => res)
  }


  def manageClass(classId: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest =>
    val (userPictureUri: Option[String], userFirstName: Option[String], userFamilyName: Option[String], tttUserId: String) = extractCommonHeaders(authRequest)
    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(tttUserId))
    val eventualTodaysDate = systemTime.getToday

    for {
      classes <- eventualClasses
      classDetailsList = classes.filter(theClass => theClass.id.id == classId)
      maybeClassDetails = classDetailsList.headOption
      if maybeClassDetails.isDefined
      classDetails = maybeClassDetails.get
      futureMaybeCurrentSchoolTerm = termService.currentSchoolTerm(SchoolConverter.convertLocalAuthorityStringToAvroVersion(classDetails.schoolDetails.localAuthority))
      maybeCurrentSchoolTerm <- futureMaybeCurrentSchoolTerm
      if maybeCurrentSchoolTerm.isDefined
      todaysDate <- eventualTodaysDate
    } yield {
      Ok(views.html.planning.classtimetables.manageClass(new MyDeadboltHandler(userReader),
        userPictureUri,
        userFirstName,
        userFamilyName,
        TimeToTeachUserId(tttUserId),
        classDetails,
        maybeCurrentSchoolTerm.get,
        todaysDate
      ))
    }
  }

  def editClass: Action[AnyContent] = Action.async { implicit request =>
    val newClassFormBound = newClassForm.bindFromRequest.get
    logger.debug(s"Edit Class Pickled = #${newClassFormBound.newClassPickled}#")
    logger.debug(s"TTT User Id = ${newClassFormBound.tttUserId}")

    import upickle.default._
    val editedClass = read[ClassDetails](newClassFormBound.newClassPickled)

    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(
      TimeToTeachUserId(newClassFormBound.tttUserId)
    )

    val upserted = {
      for {
        classes <- eventualClasses
        classDetailsList = classes.filter(theClass => theClass.id.id == editedClass.id.id)
        maybeClassDetails = classDetailsList.headOption
        if maybeClassDetails.isDefined
        currentClass = maybeClassDetails.get
        mergedClass = mergeEditedClassWithCurrent(currentClass, editedClass)
      } yield classTimetableWriter.upsertClass(
        TimeToTeachUserId(newClassFormBound.tttUserId),
        mergedClass.asInstanceOf[ClassDetails]
      )
    }.flatMap(res => res)

    for {
      done <- upserted
    } yield Ok("Created new class!")


  }

  def saveNewClass: Action[AnyContent] = Action.async { implicit request =>
    val newClassFormBound = newClassForm.bindFromRequest.get
    logger.debug(s"New Class Pickled = #${newClassFormBound.newClassPickled}#")
    logger.debug(s"TTT User Id = ${newClassFormBound.tttUserId}")

    import upickle.default._
    val newClassDetails = read[ClassDetails](newClassFormBound.newClassPickled)

    logger.debug(s"New Class Unpickled = ${newClassDetails.toString}")
    val upserted = classTimetableWriter.upsertClass(
      TimeToTeachUserId(newClassFormBound.tttUserId),
      newClassDetails.asInstanceOf[ClassDetails]
    )

    for {
      done <- upserted
    } yield Ok("Created new class!")
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