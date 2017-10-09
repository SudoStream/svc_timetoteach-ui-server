package timetoteach.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

import scala.scalajs.js.|

object Dashboard {

  case class Propsy(firstname: String, secondname: String)

  val myComponent =
    ScalaComponent.builder[Propsy]("MyComponent")
  |

  val NoArgs =
    ScalaComponent.builder[Unit]("No args")
      .renderStatic(<.div("Hello toodles!"))
      .build

  val SomethingElse = ScalaComponent.builder[Propsy]("Default hello")
    .render_P((elProps) =>
      <.strong(s"Hello there ${elProps.firstname} ${elProps.secondname}")
    ).build

}

