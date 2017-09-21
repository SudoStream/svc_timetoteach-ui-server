package controllers

import javax.inject.Inject

import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import be.objectify.deadbolt.scala.cache.HandlerCache
import play.api.mvc._
import security.{HandlerKeys, MyDeadboltHandler}
import shared.SharedMessages

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Application @Inject()(deadbolt: DeadboltActions, handlers: HandlerCache, actionBuilder: ActionBuilders) extends Controller {

  def index = deadbolt.SubjectPresent()() { authRequest =>
    println(s"hello: ${authRequest.toString()}")
    Future {
      Ok(views.html.index(new MyDeadboltHandler, SharedMessages.itWorks)(authRequest))
    }
  }

  def health = Action {
    Ok
  }

  def profile = deadbolt.SubjectPresent()() { authRequest =>
    println(s"hmmmm: ${authRequest.subject.getOrElse("poop")}")
    Future {
      Ok(views.html.index(new MyDeadboltHandler, SharedMessages.itWorks)(authRequest))
    }
  }

  def view(foo: String, bar: String) = Action {
    Ok
  }

  def login = deadbolt.WithAuthRequest()() { authRequest =>
    println("Auth : " + authRequest.toString())
    Future {
      Ok(views.html.login())
    }
  }
}
