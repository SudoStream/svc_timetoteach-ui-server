package security

import be.objectify.deadbolt.scala.models.Subject
import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltHandler, DynamicResourceHandler}
import play.api.Logger
import play.api.mvc.{Request, Result, Results}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

/**
  *
  * @author Steve Chaloner (steve@objectify.be)
  */
class MyDeadboltHandler(dynamicResourceHandler: Option[DynamicResourceHandler] = None) extends DeadboltHandler {

  val logger = Logger("timetoteach")

  def beforeAuthCheck[A](request: Request[A]) = Future(None)

  override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] = {
    Future(dynamicResourceHandler.orElse(Some(new MyDynamicResourceHandler())))
  }

  override def getSubject[A](request: AuthenticatedRequest[A]): Future[Option[Subject]] = {
    // TODO: (1) Is the user valid? .... Actually this is in the LOGIN Part .. So need just ... ==>
    // TODO: (2) Is the tokenId valid session, time etc?
    println(s" -- Inside Deadbolt ${request.attrs.toString}")

    val socialNetworkNameCookieOption = request.cookies.get("socialNetworkName")
    val socialNetworkUserIdCookieOption = request.cookies.get("socialNetworkUserId")

    val timetoteachIdCookieOption = request.cookies.get("timetoteachId")

    if (timetoteachIdCookieOption.isDefined) {
      val timetoteachIdCookie = timetoteachIdCookieOption.get
      val timetoteachId = timetoteachIdCookie.value

      println(s"timeToTeachId Cookie $timetoteachId")
      // TODO: Check timetoteachId exists in database
      // If yes then subject exists else not
      // In NOT case need to redirect to signup page in UI

      //      Future(Some(new User("steve")))
      Future {
        None
      }
    } else {
      logger.warn("Subject not found in request")
      Future {
        None
      }
    }
  }

  def onAuthFailure[A](request: AuthenticatedRequest[A]): Future[Result] = {
    Future {
      Results.Forbidden(views.html.accessFailed())
    }
  }
}