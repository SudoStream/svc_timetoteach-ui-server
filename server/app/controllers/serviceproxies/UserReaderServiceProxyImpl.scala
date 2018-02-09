package controllers.serviceproxies

import java.io.{ByteArrayInputStream, InputStream}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.stream.ActorMaterializer
import akka.util.{ByteString, Timeout}
import com.google.inject.Singleton
import com.typesafe.config.ConfigFactory
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import io.sudostream.timetoteach.messages.systemwide.model.{User, UserPreferences}
import models.timetoteach.TimeToTeachUserId
import org.apache.avro.io.{Decoder, DecoderFactory}
import org.apache.avro.specific.SpecificDatumReader
import play.api.Logger

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

@Singleton
class UserReaderServiceProxyImpl {

  implicit val system: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(5 seconds)
  val logger: Logger.type = Logger
  private val config = ConfigFactory.load()
  private val userReaderServiceHostname = config.getString("services.user-service-host")
  private val userReaderServicePort = config.getString("services.user-service-port")
  private val minikubeRun : Boolean =  sys.props.getOrElse("minikubeEnv","false").toBoolean
  val protocol: String = if (userReaderServicePort.toInt > 9000 && !minikubeRun ) "http" else "https"

  def getUser(timeToTeachUserIdWrapper: TimeToTeachUserId): Future[HttpResponse] = {
    val timeToTeachUserId = timeToTeachUserIdWrapper.value
    val userServiceUri =
      Uri(s"$protocol://$userReaderServiceHostname:$userReaderServicePort/api/user?" +
        s"timeToTeachUserId=$timeToTeachUserId")
    logger.debug(s"Sending request to '${userServiceUri.toString()}'")
    val req = HttpRequest(GET, uri = userServiceUri)

    val badSslConfig = AkkaSSLConfig().mapSettings(s =>
      s.withLoose(s.loose.withDisableSNI(true))
        .withLoose(s.loose.withDisableHostnameVerification(true))
        .withLoose(s.loose.withAcceptAnyCertificate(true))
    )

    val badCtx = Http().createClientHttpsContext(badSslConfig)

    Http().singleRequest(req, badCtx)
  }

  def isUserValid(timeToTeachUserIdWrapper: TimeToTeachUserId): Future[Boolean] = {
    val eventualFuture: Future[Future[Boolean]] = getUser(timeToTeachUserIdWrapper) map {
      resp => processHttpResponseIsUserValid(resp)
    }

    eventualFuture.flatMap {
      res => res
    }
  }

  def getUserDetails(timeToTeachUserIdWrapper: TimeToTeachUserId): Future[Option[User]] = {
    val eventualOptionFuture: Future[Future[Option[User]]] = getUser(timeToTeachUserIdWrapper) map {
      resp => processHttpResponseGetUser(resp)
    }

    eventualOptionFuture flatMap {
      res =>
        res
    }
  }

  def getUserPreferences(timeToTeachUserIdWrapper: TimeToTeachUserId): Future[Option[UserPreferences]] = {
    val eventualOptionFuture: Future[Future[Option[User]]] = getUser(timeToTeachUserIdWrapper) map {
      resp => processHttpResponseGetUser(resp)
    }

    val futureOptionUser = eventualOptionFuture flatMap {
      res => res
    }

    futureOptionUser map {
      case Some(user) =>
        logger.debug(s"User Prefs we have here are : ${user.userPreferences.toString}")
        user.userPreferences
      case None => None
    }
  }

  private def extractUserFromResponse(resp: HttpResponse): Future[Option[User]] = {
    val smallTimeout = 3000.millis
    val dataFuture = resp.entity.toStrict(smallTimeout) map {
      httpEntity =>
        httpEntity.getData()
    }
    dataFuture map {
      databytes =>
        deserialiseUser(databytes)
    }
  }

  private def processHttpResponseIsUserValid(resp: HttpResponse): Future[Boolean] = {
    if (resp.status.isSuccess()) {
      extractUserFromResponse(resp) map {
        user => true
      }
    } else {
      logger.warn(s"Couldn't find user in time to teach : ${resp.toString()}")
      Future {
        false
      }
    }
  }

  private def processHttpResponseGetUser(resp: HttpResponse): Future[Option[User]] = {
    if (resp.status.isSuccess()) {
      extractUserFromResponse(resp)
    } else {
      logger.warn(s"Couldn't find user in time to teach : ${resp.toString()}")
      Future {
        None
      }
    }
  }


  private def deserialiseUser(databytes: ByteString): Option[User] = {
    try {
      logger.debug("Deserialise User data bytes")
      val data = databytes.toList.toArray
      val reader = new SpecificDatumReader[User](User.SCHEMA$)
      val in: InputStream = new ByteArrayInputStream(data)
      val decoder: Decoder = new DecoderFactory().binaryDecoder(in, null)
      val user = new User()
      reader.read(user, decoder)
      Some(user)
    } catch {
      case _: Throwable => None
    }
  }

}
