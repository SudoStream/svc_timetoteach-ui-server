package controllers

import java.time.LocalTime

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import com.typesafe.config.ConfigFactory
import controllers.serviceproxies._
import controllers.time.SystemTime
import io.sudostream.timetoteach.messages.systemwide.model.User
import javax.inject.Inject
import models.timetoteach.classtimetable.SchoolDayTimes
import models.timetoteach.{CookieNames, InitialUserPreferences, TimeToTeachUserId}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.mvc._
import security.MyDeadboltHandler
import shared.SharedMessages
import shared.model.classtimetable.WwwClassId
import shared.util.LocalTimeUtil
import utils.TemplateUtils.getCookieStringFromRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class Application @Inject()(userReader: UserReaderServiceProxyImpl,
                            userWriter: UserWriterServiceProxyImpl,
                            schoolsProxy: SchoolReaderServiceProxyImpl,
                            classTimetableReaderProxy: ClassTimetableReaderServiceProxyImpl,
                            schoolsReader: SchoolReaderServiceProxyImpl,
                            deadbolt: DeadboltActions,
                            handlers: HandlerCache,
                            systemTime: SystemTime,
                            actionBuilder: ActionBuilders) extends Controller {

  private val defaultSchoolDayTimes = SchoolDayTimes(
    schoolDayStarts = LocalTime.of(9, 0),
    morningBreakStarts = LocalTime.of(10, 30),
    morningBreakEnds = LocalTime.of(10, 45),
    lunchStarts = LocalTime.of(12, 0),
    lunchEnds = LocalTime.of(13, 0),
    schoolDayEnds = LocalTime.of(15, 0)
  )


  val initialUserPreferencesForm = Form(
    mapping(
      "schoolId" -> nonEmptyText,
      "schoolStartTime" -> nonEmptyText,
      "morningBreakStartTime" -> nonEmptyText,
      "morningBreakEndTime" -> nonEmptyText,
      "lunchStartTime" -> nonEmptyText,
      "lunchEndTime" -> nonEmptyText,
      "schoolEndTime" -> nonEmptyText
    )(InitialUserPreferences.apply)(InitialUserPreferences.unapply)
  )

  private val config = ConfigFactory.load()
  private val showFrontPageSections = if (config.getString("feature.toggles.front-page-feature-sections") == "true") true else false
  private val generalDevelopmentToggle = if (config.getString("feature.toggles.general-development-toggle") == "true") true else false

  val logger: Logger = Logger
  private val postInitialUserPreferencesUrl = routes.Application.initialPreferencesCreated()


  def index = Action {
    Ok(views.html.index(showFrontPageSections))
  }

  def health = Action {
    Ok
  }

  def privacy = Action {
    Ok(views.html.privacy())
  }

  def loggedOutSuccessfully = Action {
    Ok(views.html.loggedOutSuccessfully(showFrontPageSections))
  }

  def profile = deadbolt.SubjectPresent()() { authRequest =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)
    val eventualTodaysDate = systemTime.getToday()

    for {
      todaysDate <- eventualTodaysDate
    } yield Ok(views.html.timetoteachDashboard(new MyDeadboltHandler(userReader),
      SharedMessages.httpMainTitle,
      userPictureUri,
      userFirstName,
      userFamilyName,
      generalDevelopmentToggle,
      todaysDate)(authRequest))
  }

  def view(foo: String, bar: String) = Action {
    Ok
  }

  def login = deadbolt.WithAuthRequest()() { authRequest =>
    Future {
      Ok(views.html.login(false))
    }
  }

  def timeToTeachApp = deadbolt.SubjectPresent()() { authRequest =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)
    val timeToTeachUserId = getCookieStringFromRequest(CookieNames.timetoteachId, authRequest).getOrElse("NO TTT USER ID")

    val futureMaybeUser = userReader.getUserDetails(TimeToTeachUserId(timeToTeachUserId))
    val futureClassesAssociatedWithTeacher = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(
      TimeToTeachUserId(timeToTeachUserId))
    val futureUserPrefs = userReader.getUserPreferences(TimeToTeachUserId(timeToTeachUserId))

    val finalRoute = for {
      maybeUser <- futureMaybeUser
      if maybeUser.isDefined
      userInfo = maybeUser.get
      classesAssociatedWithTeacher <- futureClassesAssociatedWithTeacher
      userPrefs <- futureUserPrefs

      route = if (userInfo.userPreferences.isDefined) {
        logger.debug(s"???? classesAssociatedWithTeacher = ${classesAssociatedWithTeacher.size}")
        if (classesAssociatedWithTeacher.size == 1) {
          val theOneClass = classesAssociatedWithTeacher.head
          if (theOneClass.groups.isEmpty) {
            Future {
              Redirect(controllers.planning.classtimetable.routes.ClassTimetableController.gotoClass(theOneClass.id.id))
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
              readClassTimetable(TimeToTeachUserId(timeToTeachUserId), WwwClassId(theOneClass.id.id))

            for {
              maybeWwwClassTimetable <- wwwClassTimetableFuture
            } yield Redirect(controllers.planning.classtimetable.routes.ClassTimetableController.classTimetable(theOneClass.id.id))
          }
        } else {
          val userSchoolsWrappers = userInfo.schools
          val userSchoolIds = userSchoolsWrappers.map(theSchoolWrapper => theSchoolWrapper.school.id)
          val allSchools = schoolsReader.getAllSchools().getOrElse(Nil)


          Future {
            Redirect(controllers.planning.classtimetable.routes.ClassTimetableController.classesHome())
          }
        }
      } else {

        Future {
          Redirect(routes.Application.askInitialPreferences())
        }

      }
    } yield route

    finalRoute.flatMap(res => res)
  }

  def askInitialPreferences = deadbolt.SubjectPresent()() { authRequest =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)

    val eventualTodaysDate = systemTime.getToday()
    val timeToTeachUserId = getCookieStringFromRequest(CookieNames.timetoteachId, authRequest).getOrElse("NO_USER_ID")
    val userInfoFuture = userReader.getUserDetails(TimeToTeachUserId(timeToTeachUserId))

    for {
      todaysDate <- eventualTodaysDate
      userInfo <- userInfoFuture
      if userInfo.isDefined
    } yield Ok(views.html.askInitialPreferences(new MyDeadboltHandler(userReader),
      userPictureUri,
      userFirstName,
      userFamilyName,
      userInfo.get,
      postInitialUserPreferencesUrl,
      todaysDate
    )(authRequest))

  }

  def initialPreferencesCreated: Action[AnyContent] = deadbolt.SubjectPresent()() {
    implicit request =>
      val eventualTodaysDate = systemTime.getToday()
      val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, request)
      val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, request)

      val todaysDate = Await.result(eventualTodaysDate, 1.second)

      val errorFunction = {
        formWithErrors: Form[InitialUserPreferences] =>
          logger.error(s"${
            LocalTime.now.toString
          } : Form ERROR : Oh well ... " + formWithErrors.errors.toString())
          Future {
            BadRequest(views.html.askInitialPreferences(
              handler = new MyDeadboltHandler(userReader),
              userPictureUri = userPictureUri,
              userFirstName = userFirstName,
              userFamilyName = None,
              tttUser = new User(),
              postInitialPreferencesUrl = postInitialUserPreferencesUrl,
              todaysDate)
            )
          }
      }

      val successFunction = {
        newUserPreferences: InitialUserPreferences =>
          logger.info(s"${
            LocalTime.now.toString
          } : Form SUCCESS")
          logger.debug(s"${
            LocalTime.now.toString
          } : New User Prefs are - ${
            newUserPreferences.toString
          }")
          val cookies = request.cookies

          val theUserPictureUri = cookies.get(CookieNames.socialNetworkPicture) match {
            case Some(pictureCookie) => pictureCookie.value
            case None => ""
          }

          val theSocialNetwork = cookies.get(CookieNames.socialNetworkName) match {
            case Some(socialNetworkCookie) => socialNetworkCookie.value
            case None => ""
          }

          val theSocialNetworkUserId = cookies.get(CookieNames.socialNetworkUserId) match {
            case Some(socialUserIdCookie) => socialUserIdCookie.value
            case None => ""
          }

          val theTimeToTeachUserId = cookies.get(CookieNames.timetoteachId) match {
            case Some(userId) => userId.value
            case None => ""
          }

          val userPreferencesUpdated = userWriter.updateUserPreferences(TimeToTeachUserId(theTimeToTeachUserId), newUserPreferences)
          Await.result(userPreferencesUpdated, 1 seconds)

          Future {
            Redirect(routes.Application.timeToTeachApp())
          }
      }

      val formValidationResult = initialUserPreferencesForm.bindFromRequest
      formValidationResult.fold(errorFunction, successFunction)
  }

}
