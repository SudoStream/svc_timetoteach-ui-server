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
import io.sudostream.timetoteach.messages.systemwide.model.User
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

  def isUserValid(timeToTeachUserIdWrapper: TimeToTeachUserId): Future[Boolean] = {
    val timeToTeachUserId = timeToTeachUserIdWrapper.value
    val protocol = if (userReaderServicePort.toInt > 9000) "http" else "https"
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
    logger.info(s"ssl config = ${badSslConfig.toString}")
    val badCtx = Http().createClientHttpsContext(badSslConfig)

    val responseFuture: Future[HttpResponse] = Http().singleRequest(req, badCtx)
    val eventualFuture: Future[Future[Boolean]] = responseFuture map {
      resp => processHttpResponse(resp)
    }

    eventualFuture.flatMap {
      res => res
    }
  }

  private def processHttpResponse(resp: HttpResponse): Future[Boolean] = {
    if (resp.status.isSuccess()) {
      val smallTimeout = 3000.millis
      val dataFuture = resp.entity.toStrict(smallTimeout) map {
        httpEntity =>
          httpEntity.getData()
      }
      val userExtractedFuture = dataFuture map {
        databytes =>
          deserialiseUser(databytes)
      }

      userExtractedFuture map {
        user => true
      }
    } else {
      logger.warn(s"Couldn't find user in time to teach : ${resp.toString()}")
      Future {
        false
      }
    }
  }

  private def deserialiseUser(databytes: ByteString) = {
    logger.debug("Deserialise User data bytes")
    val data = databytes.toList.toArray
    val reader = new SpecificDatumReader[User](User.SCHEMA$)
    val in: InputStream = new ByteArrayInputStream(data)
    val decoder: Decoder = new DecoderFactory().binaryDecoder(in, null)
    val user = new User()
    reader.read(user, decoder)
    user
  }

}
