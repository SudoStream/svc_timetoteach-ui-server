package controllers.planning.termly

import javax.inject.Inject

import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltActions}
import be.objectify.deadbolt.scala.cache.HandlerCache
import controllers.serviceproxies.{TimeToTeachUserId, UserReaderServiceProxyImpl}
import models.timetoteach.CookieNames
import play.api.Logger
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents}
import security.MyDeadboltHandler
import utils.TemplateUtils.getCookieStringFromRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class TermlyPlansController @Inject()(
                                       cc: ControllerComponents,
                                       userReader: UserReaderServiceProxyImpl,
                                       handlers: HandlerCache,
                                       deadbolt: DeadboltActions) extends AbstractController(cc) {

  val logger: Logger = Logger

  def termlyPlans = deadbolt.SubjectPresent()() { authRequest : AuthenticatedRequest[AnyContent] =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)
    val tttUserId = getCookieStringFromRequest(CookieNames.timetoteachId, authRequest).getOrElse("NO ID")

    Future {
      Ok(views.html.planning.termly.termlyPlansHome(new MyDeadboltHandler(userReader),
        userPictureUri,
        userFirstName,
        userFamilyName,
        TimeToTeachUserId(tttUserId)
      ))
    }
  }

}
