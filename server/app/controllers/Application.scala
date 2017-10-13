package controllers

import javax.inject.Inject

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import controllers.serviceproxies.UserReaderServiceProxyImpl
import models.timetoteach.CookieNames
import play.api.mvc._
import security.MyDeadboltHandler
import shared.SharedMessages
import utils.TemplateUtils.getCookieStringFromRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Application @Inject()(userReader: UserReaderServiceProxyImpl, deadbolt: DeadboltActions, handlers: HandlerCache, actionBuilder: ActionBuilders) extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def health = Action {
    Ok
  }

  def loggedOutSuccessfully = Action {
    Ok(views.html.loggedOutSuccessfully())
  }

  def profile = deadbolt.SubjectPresent()() { authRequest =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)

    Future {
      Ok(views.html.timetoteachDashboard(new MyDeadboltHandler(userReader), SharedMessages.itWorks, userPictureUri, userFirstName)(authRequest))
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

    println(s"userFirstName =' ${userFirstName.getOrElse("OH DEAR NOT DEFINED")}")
    println(s"userPictureUri =' ${userPictureUri.getOrElse("OH DEAR NOT DEFINED")}")

    Future {
      Ok(views.html.timetoteachDashboard(new MyDeadboltHandler(userReader), SharedMessages.itWorks, userPictureUri, userFirstName)(authRequest))
    }
  }

  def classTimetable = deadbolt.SubjectPresent()() { authRequest =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)

    Future {
      Ok(views.html.classtimetable(new MyDeadboltHandler(userReader), SharedMessages.itWorks, userPictureUri, userFirstName)(authRequest))
    }
  }

  def weeklyPlanning = deadbolt.SubjectPresent()() { authRequest =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)

    Future {
      Ok(views.html.weeklyplanning(new MyDeadboltHandler(userReader), SharedMessages.itWorks, userPictureUri, userFirstName)(authRequest))
    }
  }

  def termlyPlanning = deadbolt.SubjectPresent()() { authRequest =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)

    Future {
      Ok(views.html.termlyplanning(new MyDeadboltHandler(userReader), SharedMessages.itWorks, userPictureUri, userFirstName)(authRequest))
    }
  }

}
