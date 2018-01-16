package timetoteach

import org.scalajs.dom
import timetoteach.addnewclass.AddNewClassJsScreen
import timetoteach.classtimetable.ClassTimetableScreen

import scala.scalajs.js

object TimeToTeachApp extends js.JSApp {

  override def main(): Unit = {
    val currentPathname = dom.document.location.pathname.toString
    if (currentPathname.contains("classtimetable")) {
      ClassTimetableScreen.loadClassTimetableJavascript()
    }
    if (currentPathname == "/addnewclass") {
      AddNewClassJsScreen.loadJavascript()
    }
  }

}
