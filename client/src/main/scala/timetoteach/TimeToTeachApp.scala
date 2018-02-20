package timetoteach

import org.scalajs.dom
import timetoteach.addnewclass.AddNewClassJsScreen
import timetoteach.classesHome.ClassHomeJsScreen
import timetoteach.classtimetable.ClassTimetableScreen
import timetoteach.planning.ClassLevelPlanningJsScreen
import timetoteach.signup.SignupJs

import scala.scalajs.js
import scala.scalajs.js.Dynamic.global

object TimeToTeachApp extends js.JSApp {

  override def main(): Unit = {

    val currentPathname = dom.document.location.pathname.toString
    if (currentPathname.contains("classtimetable")) {
      ClassTimetableScreen.loadClassTimetableJavascript()
    }
    if (currentPathname == "/addnewclass") {
      AddNewClassJsScreen.loadJavascript()
    }
    if (currentPathname == "/classes") {
      ClassHomeJsScreen.loadJavascript()
    }
    if (currentPathname == "/signupsteptwo") {
      SignupJs.loadJavascript()
    }

    if (dom.document.location.toString.contains("termlyplanningforclassatgrouplevel")) {
      ClassLevelPlanningJsScreen.loadJavascript()
    }
    if (dom.document.location.toString.contains("termlyplanningforclassatclasslevel")) {
      ClassLevelPlanningJsScreen.loadJavascript()
    }

  }

}
