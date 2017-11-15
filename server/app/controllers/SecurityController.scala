package controllers

import java.io.{ByteArrayInputStream, InputStream}
import java.util.Collections
import javax.inject.Inject

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpsConnectionContext}
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.{ByteString, Timeout}
import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload
import com.google.api.client.googleapis.auth.oauth2.{GoogleIdToken, GoogleIdTokenVerifier}
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.typesafe.config.ConfigFactory
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import io.sudostream.timetoteach.messages.systemwide.model.User
import models.timetoteach.CookieNames
import org.apache.avro.io.{Decoder, DecoderFactory}
import org.apache.avro.specific.SpecificDatumReader
import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.mvc.{Cookie, _}
import play.libs.ws.WS

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

class SecurityController @Inject()(ws: WSClient,
                                   deadbolt: DeadboltActions,
                                   handlers: HandlerCache,
                                   actionBuilder: ActionBuilders
                                  ) extends Controller {

  implicit val system: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(5 seconds)
  private val config = ConfigFactory.load()
  private val userServiceHostname = config.getString("services.user-service-host")
  private val userServicePort = config.getString("services.user-service-port")
  val logger = Logger

  val userForm = Form(
    mapping(
      "idtoken" -> text
    )(TokenId.apply)(TokenId.unapply)
  )

  def tokensignin = Action.async { implicit request =>
    val tokenId = userForm.bindFromRequest.get
    logger.debug(s"Token Id = ${tokenId.value}")

    val idToken: GoogleIdToken = verifyToken(tokenId)
    if (idToken != null) {
      checkTokenAgainstUserService(idToken)
    } else {
      invalidToken
    }
  }

  def signout = Action.async { implicit request =>
    logger.debug("Signing out now!")

    Future {
      Ok("Signed Out")
        .discardingCookies(
          DiscardingCookie(CookieNames.timetoteachId),
          DiscardingCookie(CookieNames.socialNetworkFamilyName),
          DiscardingCookie(CookieNames.socialNetworkGivenName),
          DiscardingCookie(CookieNames.socialNetworkName),
          DiscardingCookie(CookieNames.socialNetworkUserId),
          DiscardingCookie(CookieNames.socialNetworkEmail),
          DiscardingCookie(CookieNames.socialNetworkPicture)
        ).bakeCookies()
    }

  }

  private def invalidToken: Future[Status] = {
    logger.warn("Token was null after being processed by google verified. Invalid ID token.")
    Future {
      Unauthorized
    }
  }

  private def checkTokenAgainstUserService(idToken: GoogleIdToken): Future[Result] = {
    val payload: Payload = idToken.getPayload
    printDebugInfo(payload)

    val protocol = if (userServicePort.toInt > 9000) "http" else "https"
    val userServiceUri =
      Uri(s"$protocol://$userServiceHostname:$userServicePort/api/user?" +
        s"socialNetworkName=GOOGLE&socialNetworkUserId=${payload.getSubject}")
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

    val eventualFuture: Future[Future[Result]] = responseFuture map {
      resp => processHttpResponse(resp, payload)
    }

    eventualFuture.flatMap {
      res => res
    }
  }

  private def verifyToken(tokenId: TokenId) = {
    val verifier: GoogleIdTokenVerifier =
      new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance)
        .setAudience(Collections.singletonList("473457256422-khclkcv9b7u7ecu6q7nntq9460ko3mo4.apps.googleusercontent.com"))
        .build()
    val idToken: GoogleIdToken = verifier.verify(tokenId.value)
    idToken
  }

  private def printDebugInfo(payload: Payload) {
    logger.debug("User ID: " + payload.getSubject)
    logger.debug(s"email: ${payload.getEmail}")
    logger.debug(s"email verified: ${payload.getEmailVerified.booleanValue}")
    logger.debug(s"name:  ${payload.get("name").toString}")
    logger.debug(s"picture: ${payload.get("picture").toString}")
    logger.debug(s"locale:  ${payload.get("locale").toString}")
    logger.debug(s"family name: ${payload.get("family_name").toString}")
    logger.debug(s"given name: ${payload.get("given_name").toString}")
  }

  private[controllers] def processHttpResponse(resp: HttpResponse,
                                               payload: Payload): Future[Result] = {
    if (resp.status.isSuccess()) {
      logger.info(s"Success status for request.")
      val smallTimeout = 3000.millis
      //     val dataFuture: Future[ByteString] = resp.entity.toStrict(smallTimeout).map {
      val dataFuture = resp.entity.toStrict(smallTimeout) map {
        httpEntity =>
          httpEntity.getData()
      }
      val userExtractedFuture = dataFuture map {
        databytes =>
          deserialiseUser(databytes)
      }

      userExtractedFuture map {
        user =>
          Ok(payload.get("name").toString)
            .withCookies(
              Cookie(CookieNames.timetoteachId, user.timeToTeachId),
              Cookie(CookieNames.socialNetworkName, "GOOGLE"),
              Cookie(CookieNames.socialNetworkUserId, payload.getSubject),
              Cookie(CookieNames.socialNetworkEmail, payload.getEmail),
              Cookie(CookieNames.socialNetworkPicture, payload.get("picture").toString),
              Cookie(CookieNames.socialNetworkFamilyName, payload.get("family_name").toString),
              Cookie(CookieNames.socialNetworkGivenName, payload.get("given_name").toString)
            )
            .bakeCookies()
      }
    } else {
      logger.warn(s"Couldn't find user in time to teach : ${resp.toString()}")
      Future {
        NotFound(payload.get("name").toString)
          .discardingCookies(DiscardingCookie(CookieNames.timetoteachId))
          .withCookies(
            Cookie(CookieNames.socialNetworkFamilyName, payload.get("family_name").toString),
            Cookie(CookieNames.socialNetworkGivenName, payload.get("given_name").toString),
            Cookie(CookieNames.socialNetworkName, "GOOGLE"),
            Cookie(CookieNames.socialNetworkUserId, payload.getSubject),
            Cookie(CookieNames.socialNetworkEmail, payload.getEmail),
            Cookie(CookieNames.socialNetworkPicture, payload.get("picture").toString)
          )
          .bakeCookies()
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

case class TokenId(value: String)