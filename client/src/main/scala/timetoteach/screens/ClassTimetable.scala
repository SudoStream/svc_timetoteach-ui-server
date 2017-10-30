package timetoteach.screens

import org.scalajs.dom
import org.scalajs.dom.Event
import org.scalajs.dom.raw._
import timetoteach.model.{Subject, Subjects}

import scala.scalajs.js

object ClassTimetable {

  var currentlySelectSubject: Option[Subject] = None
  var originalColour = ""

  def currentlyHidden(displayValue: String): Boolean = {
    displayValue match {
      case "block" => false
      case _ => true
    }
  }

  def preciseTimeToggler(): Unit = {
    val preciseTimeButton = dom.document.getElementById("precise-time-button").asInstanceOf[HTMLButtonElement]
    preciseTimeButton.addEventListener("click", (e: dom.Event) => {
      val preciseLessonTimeBody = dom.document.getElementById("precise-lesson-timing-body").asInstanceOf[HTMLDivElement]
      val currentDisplay = preciseLessonTimeBody.style.display
      preciseTimeButton.innerHTML = if (currentlyHidden(currentDisplay)) "Hide Precise Times" else "... Or Choose Precise Times"
      preciseLessonTimeBody.style.display = if (currentlyHidden(currentDisplay)) "block" else "none"
    })
  }

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
            $("#addLessonsModal").modal("show", "backdrop: static", "keyboard : false")
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

  def extractTotalMinutes(timeAsString: String): Int = {
    val indexOfColon = timeAsString.indexOf(':')
    if (indexOfColon > 0) {
      val hours = Integer.parseInt(timeAsString.substring(0, indexOfColon))
      val minutes = Integer.parseInt(timeAsString.substring(indexOfColon + 1, indexOfColon + 3))
      val isAm = if ((timeAsString takeRight 2).toLowerCase == "am") true else false
      val hoursInTotal = if (isAm) hours else hours + 12
      (hoursInTotal * 60) + minutes
    } else 0
  }

  def addMinutesToMinutesAndGetHoursAndMinutes(minutesOne: Int, minutesTwo: Int): (Int, Int) = {
    val totalMinutes = minutesOne + minutesTwo
    val hours = totalMinutes / 60
    val remainingMinutes = totalMinutes % 60
    (hours, remainingMinutes)
  }


  def addMinutesToTime(minutesToAdd: Int, currentTime: String): String = {
    val currentTotalMinutes = extractTotalMinutes(currentTime)
    val newTotalHoursAndMinutes = addMinutesToMinutesAndGetHoursAndMinutes(minutesToAdd, currentTotalMinutes)
    val hoursDisplay = if (newTotalHoursAndMinutes._1 > 12) newTotalHoursAndMinutes._1 - 12 else newTotalHoursAndMinutes._1
    val minutesDisplay = if (newTotalHoursAndMinutes._2 >= 10) newTotalHoursAndMinutes._2 else "0" + newTotalHoursAndMinutes._2
    val amOrPm = if (newTotalHoursAndMinutes._1 >= 12) "PM" else "AM"
    s"${hoursDisplay}:${minutesDisplay} ${amOrPm}"
  }

  def calculateEndTimeFromDuration(): Unit = {
    val durationRangeInput = dom.document.getElementById("lesson-duration").asInstanceOf[HTMLInputElement]
    durationRangeInput.addEventListener("change", (e: dom.Event) => {
      val lessonStartTime = dom.document.getElementById("timepicker1").asInstanceOf[HTMLInputElement].value
      val lessonDurationInMinutes = Integer.parseInt(durationRangeInput.value.stripMargin)
      val lessonEndTime = addMinutesToTime(lessonDurationInMinutes, lessonStartTime)
      dom.document.getElementById("timepicker2").asInstanceOf[HTMLInputElement].value = lessonEndTime
    })
  }

  def loadClassTimetableJavascript(): Unit = {
    calculateEndTimeFromDuration()
    preciseTimeToggler()
    addEventListenerToDragstart()
    addEventListenerToDragDrop()
    addListenerToAllSubjectButtons()
  }

}
