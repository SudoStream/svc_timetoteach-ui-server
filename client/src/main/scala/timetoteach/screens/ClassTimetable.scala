package timetoteach.screens

import org.scalajs.dom
import org.scalajs.dom.Event
import org.scalajs.dom.raw._
import timetoteach.model.{Subject, Subjects}

import scala.scalajs.js

object ClassTimetable {

  var currentlySelectSubject: Option[Subject] = None
  var originalColour = ""

  def addEventListenerToDragDrop(): Unit = {
    val timetableSubjectButtons = dom.document.getElementsByClassName("subject")
    val nodeListSize = timetableSubjectButtons.length
    var index = 0

    while (index < nodeListSize) {
      val button = timetableSubjectButtons(index).asInstanceOf[HTMLButtonElement]

      button.addEventListener("dragover", (e: dom.Event) => {
        e.preventDefault()
      })

      button.addEventListener("drop", (e: dom.Event) => {
        e.preventDefault()
        e.currentTarget match {
          case subjectDiv: HTMLButtonElement =>
            val $ = js.Dynamic.global.$
            $("#addLessonsModal").modal("show")
          case _ =>
        }

      })
      index = index + 1
    }

  }

  def addEventListenerToDragstart(): Unit = {
    val timetableSubjectButtons = dom.document.getElementsByClassName("class-timetable-button")
    val nodeListSize = timetableSubjectButtons.length
    var index = 0
    while (index < nodeListSize) {
      val button = timetableSubjectButtons(index).asInstanceOf[HTMLButtonElement]
      button.addEventListener("dragstart", (e: dom.Event) => {
        setTheCurrentlySelectedSubject(e)
        setDisableValueOnAllTimetableButtonsTo(false)
      })

      button.addEventListener("dragend", (e: dom.Event) => {
        setTheCurrentlySelectedSubject(e)
        setDisableValueOnAllTimetableButtonsTo(true)
      })

      index = index + 1
    }
  }


  def setDisableValueOnAllTimetableButtonsTo(isDisabled: Boolean): Unit = {
    val timetableSlotButtons: NodeList = dom.document.getElementsByClassName("subject")
    val nodeListSize = timetableSlotButtons.length
    var index = 0
    while (index < nodeListSize) {
      val button = timetableSlotButtons(index).asInstanceOf[HTMLButtonElement]
      button.disabled = isDisabled
      button.style.borderColor = if (isDisabled) "lightgrey" else "yellow"
      index = index + 1
    }
  }

  def addListenerToAllSubjectButtons(): Unit = {
    for (subject <- Subjects.values) {
      val subjectElement = dom.document.getElementById(subject.value)
      subjectElement.addEventListener("click", (e: dom.Event) => {

        setTheCurrentlySelectedSubject(e)

        if (currentlySelectSubject.isEmpty) {
          setDisableValueOnAllTimetableButtonsTo(true)
        } else {
          setDisableValueOnAllTimetableButtonsTo(false)
        }

      })
    }

  }

  private def setTheCurrentlySelectedSubject(e: Event): Unit = {
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
  }
  private def resetDiv(subjectDiv: HTMLDivElement) = {
    val parent = subjectDiv.parentNode.asInstanceOf[HTMLDivElement]
    parent.style.border = ""
    parent.style.paddingLeft = "15px"
    parent.style.paddingRight = "15px"

    subjectDiv.style.textShadow = ""
    subjectDiv.style.color = originalColour
    subjectDiv.style.fontWeight = "normal"
    subjectDiv.style.fontSize = "medium"
  }
  private def selectThisElement(subjectDiv: HTMLDivElement, justSelectedSubject: Subject) = {

    val parent = subjectDiv.parentNode.asInstanceOf[HTMLDivElement]
    parent.style.borderLeft = "5px solid #fcfc00"
    parent.style.borderRight = "5px solid #fcfc00"
    parent.style.paddingLeft = "0"
    parent.style.paddingRight = "0"

    subjectDiv.style.textShadow = "1px 1px #222"
    originalColour = subjectDiv.style.color
    subjectDiv.style.color = "white"
    subjectDiv.style.fontWeight = "bold"
    subjectDiv.style.fontSize = "largest"
    currentlySelectSubject = Some(justSelectedSubject)
  }
  def loadClassTimetableJavascript(): Unit = {
    addEventListenerToDragstart()
    addEventListenerToDragDrop()
    addListenerToAllSubjectButtons()
  }

}
