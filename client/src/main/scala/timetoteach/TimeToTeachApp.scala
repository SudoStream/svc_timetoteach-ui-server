package timetoteach

import org.scalajs.dom
import shared.SharedMessages
import timetoteach.components.Dashboard.Propsy

import scala.scalajs.js

object TimeToTeachApp extends js.JSApp {

  override def main(): Unit = {
    val dashboardRoot = dom.document.getElementById("timetoteachDashboard")
    dashboardRoot.textContent = "I'm back ... " + SharedMessages.itWorks
  }
}
