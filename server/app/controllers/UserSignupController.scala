package controllers

import javax.inject.Inject

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import controllers.UserSignupController.UserData
import controllers.serviceproxies.UserWriterServiceProxyImpl
import models.timetoteach.TimeToTeachUser
import play.api.Logger
import play.api.data.Form
import play.api.mvc._

object UserSignupController {

  import play.api.data.Form
  import play.api.data.Forms._

  case class UserData(firstName: String,
                      familyName: String,
                      emailAddress: String,
                      picture: String,
                      socialNetworkName: String,
                      socialNetworkUserId: String
                     )

  val userForm = Form(
    mapping(
      "firstName" -> text,
      "familyName" -> text,
      "emailAddress" -> email,
      "picture" -> text,
      "socialNetworkName" -> text,
      "socialNetworkUserId" -> text
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

    request.cookies foreach (cookie => logger.debug("name: '" + cookie.name + "',   value: '" + cookie.value + "'"))
    val defaultValuesFromCookies: UserData = createUserDefaultValues(request)

    val initialForm = UserSignupController.userForm.bindFromRequest.fill(defaultValuesFromCookies)
    Ok(views.html.signup(initialForm, postUrl, defaultValuesFromCookies))
  }


  def userCreated = Action { implicit request: MessagesRequest[AnyContent] =>
    val defaultValuesFromCookies: UserData = createUserDefaultValues(request)

    val errorFunction = { formWithErrors: Form[UserData] =>
      BadRequest(views.html.signup(formWithErrors, postUrl, defaultValuesFromCookies))
    }

    val successFunction = { data: UserData =>
      val theUser = TimeToTeachUser(
        firstName = data.firstName,
        familyName = data.familyName,
        emailAddress = data.emailAddress,
        picture = data.picture,
        socialNetworkName = data.socialNetworkName,
        socialNetworkUserId = data.socialNetworkUserId
        //        school = data.school
      )

      logger.debug(s"Creating a new user with values : ${theUser.toString}")

      val timeToTeachUserId = userWriterServiceProxy.createNewUser(theUser)

      Redirect(routes.UserSignupController.signedUpCongrats())
        .withCookies(
          Cookie("timetoteachId", timeToTeachUserId.value))
        .bakeCookies()
    }

    val formValidationResult = UserSignupController.userForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

  def signedUpCongrats = Action { implicit request: MessagesRequest[AnyContent] =>
    logger.debug(s"userCreated: ${request.attrs}")
    Ok(views.html.signedupcongrats())
  }

  private def createUserDefaultValues(request: MessagesRequest[AnyContent]) = {
    val defaultValuesFromCookies = UserData(
      firstName = request.cookies.get("socialNetworkGivenName").get.value,
      familyName = request.cookies.get("socialNetworkFamilyName").get.value,
      emailAddress = request.cookies.get("socialNetworkEmail").get.value,
      picture = request.cookies.get("socialNetworkPicture").get.value,
      socialNetworkName = request.cookies.get("socialNetworkName").get.value,
      socialNetworkUserId = request.cookies.get("socialNetworkUserId").get.value
    )
    defaultValuesFromCookies
  }

}

