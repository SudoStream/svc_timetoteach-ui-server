package controllers

import javax.inject.Inject

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import controllers.UserSignupController.UserData
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
                                     cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  val logger = Logger
  private val postUrl = routes.UserSignupController.signup()

  //  def signup = deadbolt.WithAuthRequest()() { authRequest =>
  def signup = Action { implicit request: MessagesRequest[AnyContent] =>
    //    Future {
    val cookies = for {
      cookie <- request.cookies
      line = "name: '" + cookie.name + "',   value: '" + cookie.value + "'\n"
    } yield line

    logger.debug(s"-------------------------------\nrequest to signup: $cookies")

    val defaultValuesFromCookies = UserData(
      firstName = request.cookies.get("socialNetworkGivenName").get.value,
      familyName = request.cookies.get("socialNetworkFamilyName").get.value,
      emailAddress = request.cookies.get("socialNetworkEmail").get.value,
      picture = request.cookies.get("socialNetworkPicture").get.value,
      socialNetworkName = request.cookies.get("socialNetworkName").get.value,
      socialNetworkUserId = request.cookies.get("socialNetworkUserId").get.value
    )

    val errorFunction = { formWithErrors: Form[UserData] =>
      BadRequest(views.html.signup(formWithErrors, postUrl, defaultValuesFromCookies))
    }

    val successFunction = { data: UserData =>
      // This is the good case, where the form was successfully parsed as a Data.
      val theUser = TimeToTeachUser(
        firstName = data.firstName,
        familyName = data.familyName,
        emailAddress = data.emailAddress,
        picture = data.picture,
        socialNetworkName = data.socialNetworkName,
        socialNetworkUserId = data.socialNetworkUserId
        //        school = data.school
      )
      Redirect(routes.Application.profile()).flashing("info" -> "Signed Up!")
    }

    val formValidationResult = UserSignupController.userForm.bindFromRequest.fill(defaultValuesFromCookies)
    formValidationResult.fold(errorFunction, successFunction)
  }
  //  }
}

