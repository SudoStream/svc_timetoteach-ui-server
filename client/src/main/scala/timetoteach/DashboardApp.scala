package timetoteach

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import shared.SharedMessages
import timetoteach.components.Dashboard.Propsy

import scala.scalajs.js

object DashboardApp extends js.JSApp {

  val hiii = <.div(components.Dashboard.SomethingElse(new Propsy("andy","boyle")))

  val timeFn = () => hiii.renderIntoDOM(dom.document.getElementById("timetoteachDashboard"))

  override def main(): Unit = {
    val dashboardRoot = dom.document.getElementById("timetoteachDashboard")
    dashboardRoot.textContent = "I'm back ... " + SharedMessages.itWorks

  }
}
