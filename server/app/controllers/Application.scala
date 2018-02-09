package controllers

import java.time.LocalTime
import javax.inject.Inject

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import com.typesafe.config.ConfigFactory
import controllers.serviceproxies._
import io.sudostream.timetoteach.messages.systemwide.model.User
import models.timetoteach.{CookieNames, InitialUserPreferences, TimeToTeachUserId}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.mvc._
import security.MyDeadboltHandler
import shared.SharedMessages
import utils.TemplateUtils.getCookieStringFromRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class Application @Inject()(userReader: UserReaderServiceProxyImpl,
                            userWriter: UserWriterServiceProxyImpl,
                            schoolsProxy: SchoolReaderServiceProxyImpl,
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

    Future {
      Ok(views.html.timetoteachDashboard(new MyDeadboltHandler(userReader),
        SharedMessages.httpMainTitle,
        userPictureUri,
        userFirstName,
        userFamilyName,
        generalDevelopmentToggle)(authRequest))
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
                userFamilyName,
                generalDevelopmentToggle)(authRequest))

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


  def weeklyPlanning = deadbolt.SubjectPresent()() { authRequest =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)

    Future {
      Ok(views.html.weeklyplanning(new MyDeadboltHandler(userReader), SharedMessages.httpMainTitle, userPictureUri, userFirstName, showFrontPageSections)(authRequest))
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
