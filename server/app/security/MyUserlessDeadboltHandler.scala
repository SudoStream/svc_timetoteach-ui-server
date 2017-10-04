package security

import javax.inject.Inject

import be.objectify.deadbolt.scala.AuthenticatedRequest
import be.objectify.deadbolt.scala.models.Subject
import controllers.serviceproxies.UserReaderServiceProxyImpl

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  *
  * @author Steve Chaloner (steve@objectify.be)
  */
class MyUserlessDeadboltHandler @Inject()(userReader: UserReaderServiceProxyImpl) extends MyDeadboltHandler(userReader) {
  override def getSubject[A](request: AuthenticatedRequest[A]): Future[Option[Subject]] = Future(None)
}