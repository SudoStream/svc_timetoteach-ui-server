package controllers

import java.time.LocalTime
import javax.inject.Inject

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import com.typesafe.config.ConfigFactory
import controllers.serviceproxies.UserReaderServiceProxyImpl
import models.timetoteach.CookieNames
import models.timetoteach.classtimetable.SchoolDayTimes
import play.api.mvc._
import security.MyDeadboltHandler
import shared.SharedMessages
import utils.TemplateUtils.getCookieStringFromRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Application @Inject()(userReader: UserReaderServiceProxyImpl, deadbolt: DeadboltActions, handlers: HandlerCache, actionBuilder: ActionBuilders) extends Controller {

  private val config = ConfigFactory.load()
  private val showFrontPageSections = if (config.getString("feature.toggles.front-page-feature-sections") == "true") true else false

  val defaultSchoolDayTimes = SchoolDayTimes(
    schoolDayStarts = LocalTime.of(9, 0),
    morningBreakStarts = LocalTime.of(10, 30),
    morningBreakEnds = LocalTime.of(10, 45),
    lunchStarts = LocalTime.of(12, 0),
    lunchEnds = LocalTime.of(13, 0),
    schoolDayEnds = LocalTime.of(15, 0)
  )

  def index = Action {
    Ok(views.html.index(showFrontPageSections))
  }

  def health = Action {
    Ok
  }

  def loggedOutSuccessfully = Action {
    Ok(views.html.loggedOutSuccessfully(showFrontPageSections))
  }

  def profile = deadbolt.SubjectPresent()() { authRequest =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)

    Future {
      Ok(views.html.timetoteachDashboard(new MyDeadboltHandler(userReader),
        SharedMessages.httpMainTitle,
        userPictureUri,
        userFirstName,
        userFamilyName)(authRequest))
    }
  }

  def view(foo: String, bar: String) = Action {
    Ok
  }

  def login = deadbolt.WithAuthRequest()() { authRequest =>
    Future {
      Ok(views.html.login(false))
    }
  }

  def timeToTeachApp = deadbolt.SubjectPresent()() { authRequest =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)

    println(s"userFirstName =' ${userFirstName.getOrElse("OH DEAR NOT DEFINED")}")
    println(s"userPictureUri =' ${userPictureUri.getOrElse("OH DEAR NOT DEFINED")}")

    Future {
      Ok(views.html.timetoteachDashboard(new MyDeadboltHandler(userReader),
        SharedMessages.httpMainTitle,
        userPictureUri,
        userFirstName,
        userFamilyName)(authRequest))
    }
  }

  def classTimetable = deadbolt.SubjectPresent()() { authRequest =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)

    Future {
      Ok(views.html.classtimetable(new MyDeadboltHandler(userReader),
        userPictureUri,
        userFirstName,
        userFamilyName,
        defaultSchoolDayTimes)(authRequest))
    }
  }

  def weeklyPlanning = deadbolt.SubjectPresent()() { authRequest =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)

    Future {
      Ok(views.html.weeklyplanning(new MyDeadboltHandler(userReader), SharedMessages.httpMainTitle, userPictureUri, userFirstName, showFrontPageSections)(authRequest))
    }
  }

  def termlyPlanning = deadbolt.SubjectPresent()() { authRequest =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)

    Future {
      Ok(views.html.termlyplanning(new MyDeadboltHandler(userReader), SharedMessages.httpMainTitle, userPictureUri, userFirstName, showFrontPageSections)(authRequest))
    }
  }

}
