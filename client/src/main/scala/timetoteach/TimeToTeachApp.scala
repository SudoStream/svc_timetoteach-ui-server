package timetoteach

import timetoteach.screens.ClassTimetableScreen

import scala.scalajs.js

object TimeToTeachApp extends js.JSApp {

  override def main(): Unit = {
    ClassTimetableScreen.loadClassTimetableJavascript()
  }
}
