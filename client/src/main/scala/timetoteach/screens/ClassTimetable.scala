package timetoteach.screens

import org.scalajs.dom
import org.scalajs.dom.raw
import org.scalajs.dom.raw.HTMLDivElement
import timetoteach.model.{Subject, Subjects}

object ClassTimetable {

  var currentlySelectSubject: Option[Subject] = None

  def addListenerToAllSubjectButtons(): Unit = {
    for (subject <- Subjects.values) {
      val subjectElement = dom.document.getElementById(subject.value)
      subjectElement.addEventListener("click", (e: dom.Event) => {

        e.currentTarget match {
          case subjectDiv: HTMLDivElement =>
            subjectDiv.style.textShadow = "1px 1px #222"
            subjectDiv.style.borderColor = "white"
            subjectDiv.style.border = "bold"
            subjectDiv.style.color = "white"
            subjectDiv.style.fontWeight = "bold"
            subjectDiv.style.fontSize = "largest"

          case _ =>
        }

      })
    }
  }

  def loadClassTimetableJavascript(): Unit = {
    addListenerToAllSubjectButtons()
  }

}
