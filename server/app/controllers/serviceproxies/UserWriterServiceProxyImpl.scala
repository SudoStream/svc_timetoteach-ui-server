package controllers.serviceproxies

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.google.inject.Singleton
import com.typesafe.config.ConfigFactory
import models.timetoteach.TimeToTeachUser
import play.api.Logger

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

@Singleton
class UserWriterServiceProxyImpl {

  implicit val system: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(5 seconds)
  val logger: Logger.type = Logger
  private val config = ConfigFactory.load()
  private val userServiceHostname = config.getString("services.user-service-host")
  private val userServicePort = config.getString("services.user-service-port")


  def createNewUser(user: TimeToTeachUser): Future[TimeToTeachUserId] = {

    // TODO: request user-writer service to add user

    Future {
      TimeToTeachUserId("TODO: This should be a real result back from the user writer service")
    }
  }
}

case class TimeToTeachUserId(value: String)