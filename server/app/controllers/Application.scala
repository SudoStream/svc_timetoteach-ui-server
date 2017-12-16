package controllers

import java.time.LocalTime
import javax.inject.Inject

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import com.typesafe.config.ConfigFactory
import controllers.serviceproxies._
import io.sudostream.timetoteach.messages.systemwide.model.{User, UserPreferences}
import models.timetoteach.{CookieNames, InitialUserPreferences}
import models.timetoteach.classtimetable.SchoolDayTimes
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.mvc._
import security.MyDeadboltHandler
import shared.SharedMessages
import shared.model.classtimetable.WwwClassName
import shared.util.LocalTimeUtil
import utils.TemplateUtils.getCookieStringFromRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class Application @Inject()(userReader: UserReaderServiceProxyImpl,
                            userWriter: UserWriterServiceProxyImpl,
                            schoolsProxy: SchoolReaderServiceProxyImpl,
                            classTimetableReaderProxy: ClassTimetableReaderServiceProxyImpl,
                            deadbolt: DeadboltActions,
                            handlers: HandlerCache,
                            actionBuilder: ActionBuilders) extends Controller {


  val initialUserPreferencesForm = Form(
    mapping(
      "schoolId" -> nonEmptyText,
      "schoolStartTime" -> nonEmptyText,
      "morningBreakStartTime" -> nonEmptyText,
      "morningBreakEndTime" -> nonEmptyText,
      "lunchStartTime" -> nonEmptyText,
      "lunchEndTime" -> nonEmptyText,
      "schoolEndTime" -> nonEmptyText,
      "className" -> nonEmptyText,
      "checkEarlyCurriculum" -> text,
      "checkFirstCurriculum" -> text,
      "checkSecondCurriculum" -> text,
      "checkThirdCurriculum" -> text,
      "checkFourthCurriculum" -> text
    )(InitialUserPreferences.apply)(InitialUserPreferences.unapply)
  )

  private val config = ConfigFactory.load()
  private val showFrontPageSections = if (config.getString("feature.toggles.front-page-feature-sections") == "true") true else false

  val logger: Logger = Logger
  private val postInitialUserPreferencesUrl = routes.Application.initialPreferencesCreated()

  val defaultSchoolDayTimes = SchoolDayTimes(
    schoolDayStarts = LocalTime.of(9, 0),
    morningBreakStarts = LocalTime.of(10, 30),
    morningBreakEnds = LocalTime.of(10, 45),
    lunchStarts = LocalTime.of(12, 0),
    lunchEnds = LocalTime.of(13, 0),
    schoolDayEnds = LocalTime.of(15, 0)
  )

  def index = Action {
    Ok(views.html.index(showFrontPageSections))
  }

  def health = Action {
    Ok
  }

  def loggedOutSuccessfully = Action {
    Ok(views.html.loggedOutSuccessfully(showFrontPageSections))
  }

  def profile = deadbolt.SubjectPresent()() { authRequest =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)

    Future {
      Ok(views.html.timetoteachDashboard(new MyDeadboltHandler(userReader),
        SharedMessages.httpMainTitle,
        userPictureUri,
        userFirstName,
        userFamilyName)(authRequest))
    }
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

    println(s"userFirstName =' ${userFirstName.getOrElse("OH DEAR NOT DEFINED")}")
    println(s"userPictureUri =' ${userPictureUri.getOrElse("OH DEAR NOT DEFINED")}")

    val timeToTeachUserId = getCookieStringFromRequest(CookieNames.timetoteachId, authRequest)
    println(s"timetoteachId = '${timeToTeachUserId}'")
    timeToTeachUserId match {
      case Some(userId) =>
        val userInfoFuture = userReader.getUserDetails(TimeToTeachUserId(userId))

        userInfoFuture map {
          case Some(userInfo) =>
            if (userInfo.userPreferences.isDefined) {

              println("Initial user preferences already defined.")
              Ok(views.html.timetoteachDashboard(new MyDeadboltHandler(userReader),
                SharedMessages.httpMainTitle,
                userPictureUri,
                userFirstName,
                userFamilyName)(authRequest))

            } else {
              // TODO: Redirect

              println("Initial user preferences not yet defined. Lets grab them now")
              Ok(views.html.askInitialPreferences(new MyDeadboltHandler(userReader),
                userPictureUri,
                userFirstName,
                userFamilyName,
                userInfo,
                postInitialUserPreferencesUrl
              )(authRequest))
            }

          case None =>
            NotFound(s"Tim To Teach User Id not found: ${timeToTeachUserId.toString}")
        }

      case None =>
        Future {
          NotFound(s"Tim To Teach User Id not found: ${timeToTeachUserId.toString}")
        }
    }
  }

  def askInitialPreferences = deadbolt.SubjectPresent()() { authRequest =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)

    val timeToTeachUserId = getCookieStringFromRequest(CookieNames.timetoteachId, authRequest)
    timeToTeachUserId match {
      case Some(userId) =>
        val userInfoFuture = userReader.getUserDetails(TimeToTeachUserId(userId))
        userInfoFuture map {
          case Some(userInfo) =>
            Ok(views.html.askInitialPreferences(new MyDeadboltHandler(userReader),
              userPictureUri,
              userFirstName,
              userFamilyName,
              userInfo,
              postInitialUserPreferencesUrl
            )(authRequest))
        }
      case None =>
        Future {
          NotFound(s"Time To Teach User Id not found: ${timeToTeachUserId.toString}")
        }
    }
  }

  def classTimetable = deadbolt.SubjectPresent()() { authRequest =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)
    val tttUserId = getCookieStringFromRequest(CookieNames.timetoteachId, authRequest).getOrElse("NO ID")

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
      Ok(views.html.classtimetable(new MyDeadboltHandler(userReader),
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

  def weeklyPlanning = deadbolt.SubjectPresent()() { authRequest =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)

    Future {
      Ok(views.html.weeklyplanning(new MyDeadboltHandler(userReader), SharedMessages.httpMainTitle, userPictureUri, userFirstName, showFrontPageSections)(authRequest))
    }
  }

  def termlyPlanning = deadbolt.SubjectPresent()() { authRequest =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)

    Future {
      Ok(views.html.termlyplanning(new MyDeadboltHandler(userReader), SharedMessages.httpMainTitle, userPictureUri, userFirstName, showFrontPageSections)(authRequest))
    }
  }


  def initialPreferencesCreated: Action[AnyContent] = deadbolt.SubjectPresent()() { implicit request =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, request)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, request)

    val errorFunction = { formWithErrors: Form[InitialUserPreferences] =>
      logger.error(s"${LocalTime.now.toString} : Form ERROR : Oh well ... " + formWithErrors.errors.toString())
      Future {
        BadRequest(views.html.askInitialPreferences(
          handler = new MyDeadboltHandler(userReader),
          userPictureUri = userPictureUri,
          userFirstName = userFirstName,
          userFamilyName = None,
          tttUser = new User(),
          postInitialPreferencesUrl = postInitialUserPreferencesUrl))
      }
    }

    val successFunction = { newUserPreferences: InitialUserPreferences =>
      logger.info(s"${LocalTime.now.toString} : Form SUCCESS")
      logger.debug(s"${LocalTime.now.toString} : New User Prefs are - ${newUserPreferences.toString}")
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
