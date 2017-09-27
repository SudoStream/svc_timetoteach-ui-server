package controllers

import javax.inject.Inject

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import controllers.UserSignupController.UserData
import controllers.serviceproxies.UserWriterServiceProxyImpl
import models.timetoteach.{CookieNames, TimeToTeachUser}
import play.api.Logger
import play.api.data.Form
import play.api.mvc._

object UserSignupController {

  import play.api.data.Form
  import play.api.data.Forms._

  case class UserData(firstName: String,
                      familyName: String,
                      emailAddress: String
                     )

  val userForm = Form(
    mapping(
      "firstName" -> text,
      "familyName" -> text,
      "emailAddress" -> email
      //      "school" -> of[School]
    )(UserData.apply)(UserData.unapply)
  )


}

class UserSignupController @Inject()(deadbolt: DeadboltActions,
                                     handlers: HandlerCache,
                                     actionBuilder: ActionBuilders,
                                     cc: MessagesControllerComponents,
                                     userWriterServiceProxy: UserWriterServiceProxyImpl
                                    ) extends MessagesAbstractController(cc) {

  val logger: Logger = Logger
  private val postUrl = routes.UserSignupController.userCreated()

  def signup = Action { implicit request: MessagesRequest[AnyContent] =>
    request.cookies foreach (cookie => logger.debug("SU name: '" + cookie.name + "',   value: '" + cookie.value + "'"))

    val defaultValuesFromCookies: UserData = createUserDefaultValues(request)
    val initialForm = UserSignupController.userForm.bindFromRequest.fill(defaultValuesFromCookies)
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, request)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, request)

    Ok(views.html.signup(initialForm, postUrl, userPictureUri, userFirstName))
  }

  private def getCookieStringFromRequest(cookieKey: String, request: MessagesRequest[AnyContent]) : String = {
    request.cookies.get(cookieKey) match {
      case Some(cookie) => cookie.value
      case None => ""
    }
  }

  def userCreated = Action { implicit request: MessagesRequest[AnyContent] =>
    request.cookies foreach (cookie => logger.debug("UC name: '" + cookie.name + "',   value: '" + cookie.value + "'"))

    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, request)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, request)

    val errorFunction = { formWithErrors: Form[UserData] =>
      BadRequest(views.html.signup(formWithErrors, postUrl, userPictureUri, userFirstName))
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
        socialNetworkUserId = theSocialNetworkUserId
        //        school = data.school
      )

      logger.debug(s"Creating a new user with values : ${theUser.toString}")

      val timeToTeachUserId = userWriterServiceProxy.createNewUser(theUser)

      Redirect(routes.UserSignupController.signedUpCongrats())
        .withCookies(
          Cookie(CookieNames.timetoteachId, timeToTeachUserId.value))
        .bakeCookies()
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
      emailAddress = request.cookies.get(CookieNames.socialNetworkEmail).get.value
    )
  }

}

