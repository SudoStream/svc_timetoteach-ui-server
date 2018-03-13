package timetoteach.classtimetable

import org.scalajs.dom
import org.scalajs.dom.raw.HTMLDivElement
import timetoteach.classtimetable.ClassTimetableScreen.{addEventListenerForSubjectButtonsAddedToTimetable, classTimetable, generateHtmlForClassTimetable, launchAddSubjectToEmptySessionModalEventListeners}

trait ClassTimetableCommon
{

  def renderClassTimetable(): Unit =
  {
    simpleRenderClassTimetable()
    launchAddSubjectToEmptySessionModalEventListeners()
    addEventListenerForSubjectButtonsAddedToTimetable()
  }

  def simpleRenderClassTimetable(): Unit =
  {
    val theDaysSubjectsAsHtml = generateHtmlForClassTimetable(classTimetable)
    val allTheDaysDiv = dom.document.getElementById("all-the-days-rows").asInstanceOf[HTMLDivElement]
    allTheDaysDiv.innerHTML = theDaysSubjectsAsHtml
  }

}
