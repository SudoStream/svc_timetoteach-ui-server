package controllers

import javax.inject.Inject

import be.objectify.deadbolt.scala.DeadboltActions
import play.api.mvc._
import security.MyDeadboltHandler
import shared.SharedMessages

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Application @Inject()(deadbolt: DeadboltActions) extends Controller {

  def index = deadbolt.WithAuthRequest()() { authRequest =>
    Future {
      Ok(views.html.index(new MyDeadboltHandler, SharedMessages.itWorks)(authRequest))
    }
  }

  def health = Action {
    Ok
  }

  def profile = Action {
    Ok
  }

  def view(foo: String, bar: String) = Action {
    Ok
  }

  def login = Action {
    Ok(views.html.login())
  }
}
