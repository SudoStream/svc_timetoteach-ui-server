package controllers

import javax.inject.Inject

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import controllers.UserSignupController.UserData
import controllers.serviceproxies.{SchoolReaderServiceProxyImpl, UserWriterServiceProxyImpl}
import models.timetoteach._
import play.api.Logger
import play.api.data.Form
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

object UserSignupController {

  import play.api.data.Form
  import play.api.data.Forms._

  case class UserData(firstName: String,
                      familyName: String,
                      emailAddress: String,
                      schoolId: String
                     )

  val userForm = Form(
    mapping(
      "First Name" -> text,
      "Family Name" -> text,
      "Email Address" -> email,
      "School Id" -> text
    )(UserData.apply)(UserData.unapply)
  )


}


class UserSignupController @Inject()(deadbolt: DeadboltActions,
                                     handlers: HandlerCache,
                                     actionBuilder: ActionBuilders,
                                     cc: MessagesControllerComponents,
                                     userWriterServiceProxy: UserWriterServiceProxyImpl,
                                     schoolsProxy: SchoolReaderServiceProxyImpl
                                    ) extends MessagesAbstractController(cc) {

  val logger: Logger = Logger
  private val postUrl = routes.UserSignupController.userCreated()


  def signup = Action.async { implicit request: MessagesRequest[AnyContent] =>
    request.cookies foreach (cookie => logger.debug("SU name: '" + cookie.name + "',   value: '" + cookie.value + "'"))

    val defaultValuesFromCookies: UserData = createUserDefaultValues(request)
    val initialForm = UserSignupController.userForm.bindFromRequest.fill(defaultValuesFromCookies)
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, request)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, request)
    val schoolsFuture = schoolsProxy.getAllSchoolsFuture

    for {
      schools <- schoolsFuture
    } yield {
      Ok(views.html.signup(initialForm, postUrl, userPictureUri, userFirstName, schools))
    }
  }


  private def getCookieStringFromRequest(cookieKey: String, request: MessagesRequest[AnyContent]): String = {
    request.cookies.get(cookieKey) match {
      case Some(cookie) => cookie.value
      case None => ""
    }
  }

  def userCreated = Action.async { implicit request: MessagesRequest[AnyContent] =>
    request.cookies foreach (cookie => logger.debug("UC name: '" + cookie.name + "',   value: '" + cookie.value + "'"))

    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, request)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, request)
    val schoolsFuture = schoolsProxy.getAllSchoolsFuture

    val errorFunction = { formWithErrors: Form[UserData] =>
      logger.error("ERROR : Oh dear ... " + formWithErrors.toString)
      for {
        schools <- schoolsFuture
      } yield {
        BadRequest(views.html.signup(formWithErrors, postUrl, userPictureUri, userFirstName, schools))
      }
    }

    val successFunction = { data: UserData =>
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

      val theUser = TimeToTeachUser(
        firstName = data.firstName,
        familyName = data.familyName,
        emailAddress = data.emailAddress,
        picture = theUserPictureUri,
        socialNetworkName = theSocialNetwork,
        socialNetworkUserId = theSocialNetworkUserId,
        schoolId = data.schoolId
      )

      logger.debug(s"Creating a new user with values : ${theUser.toString}")

      val timeToTeachUserIdFuture = userWriterServiceProxy.createNewUser(theUser)

      for {
        timeToTeachUserId <- timeToTeachUserIdFuture
      } yield {
        Redirect(routes.UserSignupController.signedUpCongrats())
          .withCookies(
            Cookie(CookieNames.timetoteachId, timeToTeachUserId.value))
          .bakeCookies()
      }
    }

    val formValidationResult = UserSignupController.userForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

  def signedUpCongrats = Action { implicit request: MessagesRequest[AnyContent] =>
    logger.debug(s"userCreated: ${request.attrs}")
    Ok(views.html.signedupcongrats())
  }

  private def createUserDefaultValues(request: MessagesRequest[AnyContent]): UserData = {
    UserData(
      firstName = request.cookies.get(CookieNames.socialNetworkGivenName).get.value,
      familyName = request.cookies.get(CookieNames.socialNetworkFamilyName).get.value,
      emailAddress = request.cookies.get(CookieNames.socialNetworkEmail).get.value,
      schoolId = ""
    )
  }

}
