package timetoteach.planning.weekly

import timetoteach.classtimetable.ClassTimetableCommon

import scala.scalajs.js.Dynamic.global

object WeeklyPlanningJsScreen extends ClassTimetableCommon
{

  def loadJavascript(): Unit =
  {
    global.console.log("Loading Weekly Planning Javascript")
    simpleRenderClassTimetable()
  }

}
