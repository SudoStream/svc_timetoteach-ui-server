package controllers

import java.time.LocalTime
import javax.inject.Inject

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import com.typesafe.config.ConfigFactory
import controllers.serviceproxies.{SchoolReaderServiceProxyImpl, TimeToTeachUserId, UserReaderServiceProxyImpl, UserWriterServiceProxyImpl}
import io.sudostream.timetoteach.messages.systemwide.model.User
import models.timetoteach.classtimetable.SchoolDayTimes
import models.timetoteach.{CookieNames, TimeToTeachUser}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.mvc._
import security.MyDeadboltHandler
import shared.SharedMessages
import utils.TemplateUtils.getCookieStringFromRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Application @Inject()(userReader: UserReaderServiceProxyImpl,
                            userWriterServiceProxy: UserWriterServiceProxyImpl,
                            schoolsProxy: SchoolReaderServiceProxyImpl,
                            deadbolt: DeadboltActions,
                            handlers: HandlerCache,
                            actionBuilder: ActionBuilders) extends Controller {


  case class InitialUserPreferences(
                                     schoolStartTime: String,
                                     morningBreakStartTime: String,
                                     morningBreakEndTime: String,
                                     lunchStartTime: String,
                                     lunchEndTime: String,
                                     schoolEndTime: String,
                                     className: String,
                                     checkEarlyCurriculum: Boolean,
                                     checkFirstCurriculum: Boolean,
                                     checkSecondCurriculum: Boolean,
                                     checkThirdCurriculum: Boolean,
                                     checkFourthCurriculum: Boolean
                                   )

  val initialUserPreferencesForm = Form(
    mapping(
      "schoolStartTime" -> text,
      "morningBreakStartTime" -> text,
      "morningBreakEndTime" -> text,
      "lunchStartTime" -> text,
      "lunchEndTime" -> text,
      "schoolEndTime" -> text,
      "className" -> text,
      "checkEarlyCurriculum" -> boolean,
      "checkFirstCurriculum" -> boolean,
      "checkSecondCurriculum" -> boolean,
      "checkThirdCurriculum" -> boolean,
      "checkFourthCurriculum" -> boolean
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

    Future {
      Ok(views.html.classtimetable(new MyDeadboltHandler(userReader),
        userPictureUri,
        userFirstName,
        userFamilyName,
        defaultSchoolDayTimes)(authRequest))
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
    request.cookies foreach (cookie => logger.debug("UC name: '" + cookie.name + "',   value: '" + cookie.value + "'"))

    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, request)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, request)

    val errorFunction = { formWithErrors: Form[InitialUserPreferences] =>
      logger.error("ERROR : Oh dear ... " + formWithErrors.toString)
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

    val successFunction = { data: InitialUserPreferences =>
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

      //      val theUser = TimeToTeachUser(
      //        firstName = data.firstName,
      //        familyName = data.familyName,
      //        emailAddress = data.emailAddress,
      //        picture = theUserPictureUri,
      //        socialNetworkName = theSocialNetwork,
      //        socialNetworkUserId = theSocialNetworkUserId,
      //        schoolId = data.schoolId
      //      )
      //
      //      logger.debug(s"Creating a new user with values : ${theUser.toString}")
      //
      //      val timeToTeachUserIdFuture = userWriterServiceProxy.createNewUser(theUser)

      //      for {
      //        timeToTeachUserId <- timeToTeachUserIdFuture
      //      } yield {
      //        logger.debug(s"Time To Teach Id = '${timeToTeachUserId.value.toString}'")
      Future {
        Redirect(routes.Application.timeToTeachApp())
      }
      //      }
    }

    val formValidationResult = initialUserPreferencesForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

}
