package controllers

import java.util.Collections
import javax.inject.Inject

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload
import com.google.api.client.googleapis.auth.oauth2.{GoogleIdToken, GoogleIdTokenVerifier}
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import play.api.data.Forms._
import play.api.data._
import play.api.mvc.{Action, Controller, Cookie}

class SecurityController @Inject()(deadbolt: DeadboltActions, handlers: HandlerCache, actionBuilder: ActionBuilders) extends Controller {

  val userForm = Form(
    mapping(
      "idtoken" -> text
    )(TokenId.apply)(TokenId.unapply)
  )

  def tokensignin = Action { implicit request =>
    val tokenId = userForm.bindFromRequest.get
    println(s"\n\nOkay dokes ... Token Id = ${tokenId.value}\n\n")

    val verifier: GoogleIdTokenVerifier =
      new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance)
        .setAudience(Collections.singletonList("19438371353-1gljfancidnfrtjdt2ic22hd3ib4fab0.apps.googleusercontent.com"))
        .build()

    // (Receive idTokenString by HTTPS POST)

    val idToken: GoogleIdToken = verifier.verify(tokenId.value)
    if (idToken != null) {
      val payload: Payload = idToken.getPayload

      // Print user identifier
      val userId = payload.getSubject
      System.out.println("User ID: " + userId)

      // Get profile information from payload
      println(s"email: ${payload.getEmail}")
      println(s"email verified: ${payload.getEmailVerified.booleanValue}")
      println(s"name:  ${payload.get("name").toString}")
      println(s"picture: ${payload.get("picture").toString}")
      println(s"locale:  ${payload.get("locale").toString}")
      println(s"family name: ${payload.get("family_name").toString}")
      println(s"given name: ${payload.get("given_name").toString}")

      // Use or store profile information
      // ...
      Ok(payload.get("name").toString).withCookies(Cookie("timetoteachid","1234567")).bakeCookies()
    } else {
      System.out.println("Invalid ID token.")
      Unauthorized
    }
  }
}

case class TokenId(value: String)