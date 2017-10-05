package example

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import shared.SharedMessages

import scala.scalajs.js

//noinspection TypeAnnotation
object ScalaJSExample extends js.JSApp {

  //  val myComponent =
  //    ScalaComponent.builder[Props]("MyComponent")
  //  |
  //
  //  val mainHeader =
  //    ScalaComponent.builder[String]("MainHeader")
  //      .render_P(name => <.h1("I Am The Header"))
  //      .build
  //

  val Hello =
    ScalaComponent.builder[String]("Hello")
      .render_P(name => <.div("Hello there ", name))
      .build


  ////////////////////////////////////////////////////
  case class Propsy(firstname: String, secondname: String)

  val HelloMessage = ScalaComponent.builder[Propsy]("HiMessage")
    .render_P((theProps) =>
        <.strong(s"Hello there ${theProps.firstname} ${theProps.secondname}"))
    .build

  val andy = HelloMessage(Propsy("Andhee", "Boil"))

  val mainDiv = ScalaComponent.builder[String]("MainDiv")
    .render_P(name =>
      <.div("I Am The Main Div",
        <.br,
        <.blockquote(andy,
          <.blockquote("What age are you?",
        <.h2("and h2 here!  " + SharedMessages.timeNow
        )))))
    .build

  val timeFn = () => mainDiv("toodles").renderIntoDOM(dom.document.getElementById("timetoteachApplicationRoot"))
  ////////////////////////////////////////////////////


  def main(): Unit = {
    dom.document.getElementById("scalajsShoutOut").textContent = SharedMessages.itWorks

    scala.scalajs.js.Dynamic.global.setInterval(timeFn, 1000)


    //    andy.renderIntoDOM(dom.document.getElementById("root"))
    //    mainDiv("toodles").renderIntoDOM(dom.document.getElementById("root"))
  }


}
