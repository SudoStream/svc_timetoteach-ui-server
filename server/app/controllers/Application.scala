package controllers

import javax.inject.Inject

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import controllers.serviceproxies.UserReaderServiceProxyImpl
import play.api.mvc._
import security.MyDeadboltHandler
import shared.SharedMessages

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
    Future {
      Ok(views.html.timetoteach(new MyDeadboltHandler(userReader), SharedMessages.itWorks)(authRequest))
    }
  }

  def view(foo: String, bar: String) = Action {
    Ok
  }

  def login = deadbolt.WithAuthRequest()() { authRequest =>
    Future {
      Ok(views.html.login())
    }
  }

  def timeToTeachApp = deadbolt.SubjectPresent()() { authRequest =>
    Future {
      Ok(views.html.timetoteach(new MyDeadboltHandler(userReader), SharedMessages.itWorks)(authRequest))
    }
  }

}
