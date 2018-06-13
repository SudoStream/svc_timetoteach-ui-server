package controllers

import javax.inject.Inject
import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import controllers.UserSignupController.UserData
import controllers.email.SignupEmailService
import controllers.serviceproxies.{SchoolReaderServiceProxyImpl, UserReaderServiceProxyImpl, UserWriterServiceProxyImpl}
import models.timetoteach._
import play.api.Logger
import play.api.data.Form
import play.api.mvc._
import tttutils.StringUtils.noWhiteSpaceAtAll
import tttutils.TemplateUtils.getCookieStringFromRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
                                     schoolsProxy: SchoolReaderServiceProxyImpl,
                                     userReader: UserReaderServiceProxyImpl,
                                     signupEmailService: SignupEmailService
                                    ) extends MessagesAbstractController(cc) {

  val logger: Logger = Logger
  private val postUrl = routes.UserSignupController.userCreated()


  def signupStep1 = deadbolt.WithAuthRequest()() { authRequest =>
    Future {
      Ok(views.html.login(true))
    }
  }

  def signupStep2: Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    request.cookies foreach (cookie => logger.debug("SU name: '" + cookie.name + "',   value: '" + cookie.value + "'"))

    val defaultValuesFromCookies: UserData = createUserDefaultValues(request)
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, request)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, request)
    val schoolsFuture = schoolsProxy.getAllSchoolsFuture

    for {
      schools <- schoolsFuture
    } yield {
      Ok(views.html.signupNew(defaultValuesFromCookies, postUrl, userPictureUri.getOrElse(""), userFirstName.getOrElse(""), schools))
    }
  }

  def userCreated: Action[AnyContent] = Action.async { implicit request =>
    request.cookies foreach (cookie => logger.debug("UC name: '" + cookie.name + "',   value: '" + cookie.value + "'"))

    val defaultValuesFromCookies: UserData = createUserDefaultValues(request)
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, request).getOrElse("")
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, request).getOrElse("")
    val schoolsFuture = schoolsProxy.getAllSchoolsFuture

    val errorFunction = { formWithErrors: Form[UserData] =>
      logger.error("ERROR : Oh dear ... " + formWithErrors.toString)
      for {
        schools <- schoolsFuture
      } yield {
        BadRequest(views.html.signupNew(defaultValuesFromCookies, postUrl, userPictureUri, userFirstName, schools))
      }
    }

    val successFunction = { data: UserData =>
      logger.info(s"School info is as follows : '${data.schoolId}'")

      Future {
        Redirect(routes.UserSignupController.creatingNewUser())
          .withCookies(
            Cookie(CookieNames.userDataFirstName, noWhiteSpaceAtAll(data.firstName)),
            Cookie(CookieNames.userDataFamilyName, noWhiteSpaceAtAll(data.familyName)),
            Cookie(CookieNames.userDataEmailAddress, noWhiteSpaceAtAll(data.emailAddress)),
            Cookie(CookieNames.userDataSchoolId, noWhiteSpaceAtAll(data.schoolId))
          )
      }

    }

    val formValidationResult = UserSignupController.userForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

  def creatingNewUser(): Action[AnyContent] = Action.async { implicit request =>
    val schoolsFuture = schoolsProxy.getAllSchoolsFuture
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, request).getOrElse("")
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, request).getOrElse("")
    val firstName = request.cookies.get(CookieNames.userDataFirstName).get.value
    val familyName = request.cookies.get(CookieNames.userDataFamilyName).get.value
    val emailAddress = request.cookies.get(CookieNames.userDataEmailAddress).get.value
    val schoolId = request.cookies.get(CookieNames.userDataSchoolId).get.value

    if (schoolId == null || schoolId.isEmpty) {
      for {
        schools <- schoolsFuture
      } yield {
        BadRequest(views.html.signupNew(createUserDefaultValues(request), postUrl, userPictureUri, userFirstName, schools))
      }
    } else {
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
        firstName = firstName,
        familyName = familyName,
        emailAddress = emailAddress,
        picture = theUserPictureUri,
        socialNetworkName = theSocialNetwork,
        socialNetworkUserId = theSocialNetworkUserId,
        schoolId = schoolId
      )

      logger.debug(s"Creating a new user with values : ${theUser.toString}")

      val timeToTeachUserIdFuture = userWriterServiceProxy.createNewUser(theUser)

      signupEmailService.sendEmail(theUser)

      for {
        timeToTeachUserId <- timeToTeachUserIdFuture
      } yield {
        logger.debug(s"Time To Teach Id = '${timeToTeachUserId.value.toString}'")
        Redirect(routes.UserSignupController.signedUpCongrats())
          .withCookies(
            Cookie(CookieNames.timetoteachId, noWhiteSpaceAtAll(timeToTeachUserId.value)))
          .discardingCookies(
            DiscardingCookie(CookieNames.userDataFirstName),
            DiscardingCookie(CookieNames.userDataFamilyName),
            DiscardingCookie(CookieNames.userDataEmailAddress),
            DiscardingCookie(CookieNames.userDataSchoolId)
          )
          .bakeCookies()
      }
    }
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

