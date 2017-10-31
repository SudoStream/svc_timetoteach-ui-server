package timetoteach.screens

import org.scalajs.dom
import org.scalajs.dom.Event
import org.scalajs.dom.raw._
import timetoteach.model._

import scala.scalajs.js

object ClassTimetable {

  var currentlySelectedSession: Option[Session] = None
  var currentlySelectedSubject: Option[Subject] = None
  var currentlySelectedDayOfWeek: Option[DayOfWeek] = None
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
          case buttonTarget: HTMLButtonElement =>
            val buttonTargetText = dom.document.getElementById("addLessonsModalLabel").innerHTML

            val timetableSession = buttonTarget.getAttribute("data-timetable-session")
            currentlySelectedSession = timetableSession match {
              case sessionName: String => if (Sessions.values.contains(Session(sessionName))) Some(Session(sessionName)) else None
              case _ => None
            }

            val dayOfTheWeek = buttonTarget.getAttribute("data-day-of-the-week")
            currentlySelectedDayOfWeek = dayOfTheWeek match {
              case dayName: String => if (DaysOfWeek.values.contains(DayOfWeek(dayName))) Some(DayOfWeek(dayName)) else None
              case _ => None
            }

            val subjectSessionDayOption = for {
              subjectSelected <- currentlySelectedSubject
              sessionSelected <- currentlySelectedSession
              dayOfWeekSelected <- currentlySelectedDayOfWeek
              subjectNice = subjectSelected.value.replace("subject-","").replaceAll("-"," ").split(' ').map( _.capitalize).mkString(" ")
              sessionNice = sessionSelected.value.replaceAll("-"," ")
            } yield {
              (subjectNice, sessionNice, dayOfWeekSelected.value)
            }

            if (subjectSessionDayOption.isDefined) {
              val $ = js.Dynamic.global.$
              $("#addLessonsModal").modal("show", "backdrop: static", "keyboard : false")
              dom.document.getElementById("addLessonsModalLabel").innerHTML
                = s"Add <strong>${subjectSessionDayOption.get._1}</strong> to ${subjectSessionDayOption.get._3} ${subjectSessionDayOption.get._2}"
            } else {
              scala.scalajs.js.Dynamic.global.alert("There was an error selecting subject and session.\n\n" +
                s"Subjected Selected = '${currentlySelectedSubject.getOrElse("NO SUBJECT SELECTED")}'\n" +
                s"Session Selected = '${currentlySelectedSession.getOrElse("NO SESSION SELECTED")}'\n"
              )
            }
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

        if (currentlySelectedSubject.isEmpty) {
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
        if (currentlySelectedSubject.isDefined) {
          if (currentlySelectedSubject.get == justSelectedSubject) {
            resetDiv(subjectDiv)
            currentlySelectedSubject = None
          } else {
            val currentlySelectedDiv =
              dom.document.getElementById(currentlySelectedSubject.get.value).asInstanceOf[HTMLDivElement]
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
    currentlySelectedSubject = Some(justSelectedSubject)
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

  def subtractMinutesToMinutesAndGetHoursAndMinutes(minutesToSubract: Int, minutesTwo: Int): (Int, Int) = {
    val totalMinutes = minutesTwo - minutesToSubract
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

  def subtractMinutesToTime(minutesToSubtract: Int, currentTime: String): String = {
    val currentTotalMinutes = extractTotalMinutes(currentTime)
    val newTotalHoursAndMinutes = subtractMinutesToMinutesAndGetHoursAndMinutes(minutesToSubtract, currentTotalMinutes)
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

  def calculateEndTimeFromNewStartTime(): Unit = {
    def updateTheEndTimeWhenStartChanges(lessonStartTimeInput: Element): Unit = {
      val lessonDuration = dom.document.getElementById("lesson-duration").asInstanceOf[HTMLInputElement].value
      val lessonDurationInMinutes = Integer.parseInt(lessonDuration.stripMargin)
      val lessonEndTime = addMinutesToTime(lessonDurationInMinutes, lessonStartTimeInput.asInstanceOf[HTMLInputElement].value)
      dom.document.getElementById("timepicker2").asInstanceOf[HTMLInputElement].value = lessonEndTime
    }

    val lessonStartTimeInput = dom.document.getElementById("timepicker1")
    lessonStartTimeInput.addEventListener("input", (e: dom.Event) => {
      updateTheEndTimeWhenStartChanges(lessonStartTimeInput)
    })
    lessonStartTimeInput.addEventListener("keydown", (e: dom.Event) => {
      updateTheEndTimeWhenStartChanges(lessonStartTimeInput)
    })
    lessonStartTimeInput.addEventListener("keyup", (e: dom.Event) => {
      updateTheEndTimeWhenStartChanges(lessonStartTimeInput)
    })
  }

  def calculateEndTimeFromNewEndTime(): Unit = {
    def updateTheEndTimeWhenEndChanges(lessonStartTimeInput: Element): Unit = {
      val lessonDuration = dom.document.getElementById("lesson-duration").asInstanceOf[HTMLInputElement].value
      val lessonDurationInMinutes = Integer.parseInt(lessonDuration.stripMargin)
      val lessonEndTime = addMinutesToTime(lessonDurationInMinutes, lessonStartTimeInput.asInstanceOf[HTMLInputElement].value)
      dom.document.getElementById("timepicker1").asInstanceOf[HTMLInputElement].value = lessonEndTime
    }

    val lessonEndTimeInput = dom.document.getElementById("timepicker2")
    lessonEndTimeInput.addEventListener("input", (e: dom.Event) => {
      updateTheEndTimeWhenEndChanges(lessonEndTimeInput)
    })
    lessonEndTimeInput.addEventListener("keydown", (e: dom.Event) => {
      updateTheEndTimeWhenEndChanges(lessonEndTimeInput)
    })
    lessonEndTimeInput.addEventListener("keyup", (e: dom.Event) => {
      updateTheEndTimeWhenEndChanges(lessonEndTimeInput)
    })
  }

  def loadClassTimetableJavascript(): Unit = {
    calculateEndTimeFromNewEndTime()
    calculateEndTimeFromNewStartTime()
    calculateEndTimeFromDuration()
    preciseTimeToggler()
    addEventListenerToDragstart()
    addEventListenerToDragDrop()
    addListenerToAllSubjectButtons()
  }

}
