package security

import be.objectify.deadbolt.scala.models.Subject
import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltHandler, DynamicResourceHandler}
import com.typesafe.config.ConfigFactory
import controllers.serviceproxies.{TimeToTeachUserId, UserReaderServiceProxyImpl}
import models.User
import play.api.Logger
import play.api.mvc.{Request, Result, Results}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

/**
  *
  * @author Steve Chaloner (steve@objectify.be)
  */
class MyDeadboltHandler(userReader: UserReaderServiceProxyImpl, dynamicResourceHandler: Option[DynamicResourceHandler] = None) extends DeadboltHandler {

  val logger = Logger("timetoteach")
  private val config = ConfigFactory.load()
  private val showFrontPageSections = if (config.getString("feature.toggles.front-page-feature-sections") == "true") true else false

  def beforeAuthCheck[A](request: Request[A]) = Future(None)

  override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] = {
    Future(dynamicResourceHandler.orElse(Some(new MyDynamicResourceHandler())))
  }

  override def getSubject[A](request: AuthenticatedRequest[A]): Future[Option[Subject]] = {
    logger.debug(s" -- Inside Deadbolt ${request.attrs.toString}")
    val socialNetworkNameCookieOption = request.cookies.get("socialNetworkName")
    val socialNetworkUserIdCookieOption = request.cookies.get("socialNetworkUserId")

    val timetoteachIdCookieOption = request.cookies.get("timetoteachId")

    if (timetoteachIdCookieOption.isDefined) {
      val timetoteachIdCookie = timetoteachIdCookieOption.get
      val timetoteachId = timetoteachIdCookie.value

      logger.debug(s"timeToTeachId Cookie $timetoteachId")
      val isUserValidFuture = userReader.isUserValid(TimeToTeachUserId(timetoteachId))

      val eventualSubjectOption = isUserValidFuture map {
        case true => Future {
          logger.debug("user is valid")
          Some(new User(timetoteachId))
        }
        case false => Future {
          logger.debug("user is not valid")
          None
        }
      }

      eventualSubjectOption flatMap { fut => fut }
    } else {
      logger.warn("Subject not found in request")
      Future {
        None
      }
    }
  }

  def onAuthFailure[A](request: AuthenticatedRequest[A]): Future[Result] = {
    Future {
      Results.Forbidden(views.html.accessFailed(showFrontPageSections))
    }
  }
}
