package timetoteach.planning.weekly

import scala.scalajs.js.Dynamic.global

object WeeklyPlanningJsScreen
{

  def loadJavascript(): Unit =
  {
    global.console.log("Loading Weekly Planning Javascript")
    simpleRenderClassTimetable()
  }

  private def simpleRenderClassTimetable(): Unit =
  {
//    val theDaysSubjectsAsHtml = generateHtmlForWeeklyPlanning(classTimetable)
//    val allTheDaysDiv = dom.document.getElementById("all-the-days-rows").asInstanceOf[HTMLDivElement]
//    allTheDaysDiv.innerHTML = theDaysSubjectsAsHtml
  }

}
