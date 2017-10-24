package timetoteach.screens

import org.scalajs.dom
import org.scalajs.dom.raw.HTMLDivElement
import timetoteach.model.{Subject, Subjects}

object ClassTimetable {

  var currentlySelectSubject: Option[Subject] = None
  var orginalColour = ""

  def addListenerToAllSubjectButtons(): Unit = {
    for (subject <- Subjects.values) {
      val subjectElement = dom.document.getElementById(subject.value)
      subjectElement.addEventListener("click", (e: dom.Event) => {

        e.currentTarget match {
          case subjectDiv: HTMLDivElement =>
            val justSelectedSubject = Subject(subjectDiv.id)
            if (currentlySelectSubject.isDefined) {
              if (currentlySelectSubject.get == justSelectedSubject) {
                resetDiv(subjectDiv)
                currentlySelectSubject = None
              } else {
                val currentlySelectedDiv =
                  dom.document.getElementById(currentlySelectSubject.get.value).asInstanceOf[HTMLDivElement]
                resetDiv(currentlySelectedDiv)
                selectThisElement(subjectDiv, justSelectedSubject)
              }
            } else {
              selectThisElement(subjectDiv, justSelectedSubject)
            }

          case _ =>
        }

      })
    }

  }

  private def resetDiv(subjectDiv: HTMLDivElement) = {
    val parent = subjectDiv.parentNode.asInstanceOf[HTMLDivElement]
    parent.style.border = ""
    parent.style.paddingLeft = "15px"
    parent.style.paddingRight = "15px"

    subjectDiv.style.textShadow = ""
    subjectDiv.style.color = orginalColour
    subjectDiv.style.fontWeight = "normal"
    subjectDiv.style.fontSize = "medium"
  }
  private def selectThisElement(subjectDiv: HTMLDivElement, justSelectedSubject: Subject) = {

    val parent = subjectDiv.parentNode.asInstanceOf[HTMLDivElement]
    parent.style.border = "5px solid white"
    parent.style.paddingLeft = "0"
    parent.style.paddingRight = "0"

    subjectDiv.style.textShadow = "1px 1px #222"
    orginalColour = subjectDiv.style.color
    subjectDiv.style.color = "white"
    subjectDiv.style.fontWeight = "bold"
    subjectDiv.style.fontSize = "largest"
    currentlySelectSubject = Some(justSelectedSubject)
  }
  def loadClassTimetableJavascript(): Unit = {
    addListenerToAllSubjectButtons()
  }

}
