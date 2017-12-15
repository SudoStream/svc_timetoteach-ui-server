package timetoteach.screens

import java.time.temporal.ChronoUnit.MINUTES

import org.scalajs.dom
import org.scalajs.dom.Event
import org.scalajs.dom.ext.Ajax.InputData
import org.scalajs.dom.raw._
import shared.model.classtimetable._
import shared.util.LocalTimeUtil

import scala.scalajs.js
import scala.scalajs.js.Dynamic.global
import scala.util.{Failure, Success}

object ClassTimetableScreen extends ClassTimetableScreenHtmlGenerator {

  val schoolDayStarts = dom.window.localStorage.getItem("schoolDayStarts")
  val morningBreakStarts = dom.window.localStorage.getItem("morningBreakStarts")
  val morningBreakEnds = dom.window.localStorage.getItem("morningBreakEnds")
  val lunchStarts = dom.window.localStorage.getItem("lunchStarts")
  val lunchEnds = dom.window.localStorage.getItem("lunchEnds")
  val schoolDayEnds = dom.window.localStorage.getItem("schoolDayEnds")

  val theClassTimetableName = dom.window.localStorage.getItem("timetableClassName")
  val theTimeToTeachUserId = dom.window.localStorage.getItem("timeToTeachUserId")

  val maybeSchoolTimetableTimes: Option[Map[SchoolDayTimeBoundary, String]] = if (
    schoolDayStarts.nonEmpty && schoolDayEnds.nonEmpty && morningBreakStarts.nonEmpty &&
      morningBreakEnds.nonEmpty && lunchStarts.nonEmpty && lunchEnds.nonEmpty) {
    Some(Map(
      SchoolDayStarts() -> schoolDayStarts,
      MorningBreakStarts() -> morningBreakStarts,
      MorningBreakEnds() -> morningBreakEnds,
      LunchStarts() -> lunchStarts,
      LunchEnds() -> lunchEnds,
      SchoolDayEnds() -> schoolDayEnds
    ))
  } else {
    None
  }


  var classTimetable: WWWClassTimetable = WWWClassTimetable(maybeSchoolTimetableTimes)
  override def getClassTimetable: WWWClassTimetable = classTimetable

  var currentlySelectedSession: Option[WwwSession] = None
  var currentlySelectedSubject: Option[WwwSubjectName] = None
  var lastSelectedSubject: Option[WwwSubjectName] = None
  var currentlySelectedDayOfWeek: Option[WwwDayOfWeek] = None
  var longerClickSubjectSelected = false
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
      preciseTimeTogglerBehaviour(preciseTimeButton)
    })
  }

  private def preciseTimeTogglerBehaviour(preciseTimeButton: HTMLButtonElement): Unit = {
    val preciseLessonTimeBody = dom.document.getElementById("precise-lesson-timing-body").asInstanceOf[HTMLDivElement]
    val currentDisplay = preciseLessonTimeBody.style.display
    preciseTimeButton.innerHTML = if (currentlyHidden(currentDisplay)) "Hide Precise Times" else "... Or Choose Precise Times"
    preciseLessonTimeBody.style.display = if (currentlyHidden(currentDisplay)) "block" else "none"
    setDecentTimesForPreciseModal()
  }

  private def preciseTimeTogglerBehaviourOff(preciseTimeButton: HTMLButtonElement): Unit = {
    val preciseLessonTimeBody = dom.document.getElementById("precise-lesson-timing-body").asInstanceOf[HTMLDivElement]
    preciseTimeButton.innerHTML = "... Or Choose Precise Times"
    preciseLessonTimeBody.style.display = "none"
    setDecentTimesForPreciseModal()
  }

  private def addAdditionalInfoToSubject(maybeOkBehaviour: Option[(WwwSubjectDetail, WwwSessionOfTheWeek)]): Unit = {
    for {
      okBehaviour <- maybeOkBehaviour
      subjectSelected = okBehaviour._1
      sessionOfTheWeek = okBehaviour._2
    } {
      val additionalInfoInput = dom.document.getElementById("input-for-additional-info").asInstanceOf[HTMLInputElement]
      additionalInfoInput.addEventListener("keypress", (e: dom.Event) => {
        e match {
          case event: KeyboardEvent =>
            if (event.keyCode == 13) {
              updatedSubject(subjectSelected, sessionOfTheWeek)
              setRemoveBehaviourTupleToNone()
            }
          case _ =>
        }
      })
    }

  }


  private def setDecentTimesForPreciseModal(): Unit = {
    for {
      subjectName <- this.lastSelectedSubject
      session <- this.currentlySelectedSession
      dayOfWeek <- this.currentlySelectedDayOfWeek
    } {
      val inputStartTime = dom.document.getElementById("timepicker1").asInstanceOf[HTMLInputElement]
      val inputEndTime = dom.document.getElementById("timepicker2").asInstanceOf[HTMLInputElement]
      val maybeEarliestStartTime =
        classTimetable.getFirstAvailableTimeSlot(WwwSessionOfTheWeek.createSessionOfTheWeek(dayOfWeek, session))

      inputStartTime.value = if (maybeEarliestStartTime.isDefined) {
        val start = LocalTimeUtil.get12HourAmPmFromLocalTime(maybeEarliestStartTime.get.startTime)
        global.console.log(s"StartTime : $start")
        start
      } else "09:00 AM"

      inputEndTime.value = if (maybeEarliestStartTime.isDefined) {
        val end = maybeEarliestStartTime.get.startTime.plusMinutes(40)
        LocalTimeUtil.get12HourAmPmFromLocalTime(end)
      } else "09:40 AM"
    }
  }

  def submitLessonDuration(): Unit = {
    val submitLessonDuration = dom.document.getElementById("submit-lesson-duration").asInstanceOf[HTMLButtonElement]
    submitLessonDuration.addEventListener("click", (e: dom.Event) => {

      val startTimeStringProposed = dom.document.getElementById("timepicker1").asInstanceOf[HTMLInputElement].value
      val maybeStartTimeProposed = LocalTimeUtil.convertStringTimeToLocalTime(startTimeStringProposed)

      val endTimeStringProposed = dom.document.getElementById("timepicker2").asInstanceOf[HTMLInputElement].value
      val maybeEndTimeProposed = LocalTimeUtil.convertStringTimeToLocalTime(endTimeStringProposed)

      println(s"Proposed start time: ${maybeStartTimeProposed.get.toString}")
      println(s"Proposed end time: ${maybeEndTimeProposed.get.toString}")

      val maybeTimeSlot = for {
        startTime <- maybeStartTimeProposed
        endTime <- maybeEndTimeProposed
      } yield WwwTimeSlot(startTime, endTime)

      val maybeSessionOfTheWeek: Option[WwwSessionOfTheWeek] = extractSelectedSessionOfTheWeek
      val maybeSubjectSession = for {
        sessionOfTheWeek <- maybeSessionOfTheWeek
        sessionTimeSlot <- classTimetable.getTimeSlotForSession(sessionOfTheWeek)
        selectedSubject <- lastSelectedSubject
        timeSlot <- maybeTimeSlot
        subjectDetail = WwwSubjectDetail(selectedSubject, timeSlot)
      } yield (subjectDetail, sessionOfTheWeek)

      global.console.log(s"maybeSubjectSession = ${maybeSubjectSession.toString}")
      maybeSubjectSession match {
        case Some(subjectSession) =>
          if (classTimetable.addSubject(subjectSession._1, subjectSession._2)) {
            renderClassTimetable()
          } else {
            global.alert("Issue: Not enough space to add subject")
          }
          val $ = js.Dynamic.global.$
          $("#addLessonsModal").modal("hide")
          lastSelectedSubject = None
          if (longerClickSubjectSelected) {
            setDisableValueOnAllTimetableButtonsTo(false)
          } else {
            setDisableValueOnAllTimetableButtonsTo(true)
          }
        case None =>
          global.alert("Unable to add subject to session")
          global.console.error(s"Error adding subject to session")
      }

    })
  }

  def fillAsMuchAsPossibleOfSession(): Unit = {
    val oneThirdFillSubjectButton = dom.document.getElementById("fill-rest-session-button").asInstanceOf[HTMLButtonElement]
    oneThirdFillSubjectButton.addEventListener("click", (e: dom.Event) => {
      addingSubjectGeneralBehaviour(Right(AsMuchAsPossible()))
    })
  }

  case class AsMuchAsPossible()

  private def addingSubjectGeneralBehaviour(eitherFractionOrAsMuchAsPossible: Either[Fraction, AsMuchAsPossible]) = {
    val maybeSessionOfTheWeek: Option[WwwSessionOfTheWeek] = extractSelectedSessionOfTheWeek
    val maybeSubjectDetailAndSessionOfTheWeek = for {
      sessionOfTheWeek <- maybeSessionOfTheWeek
      sessionTimeSlot <- classTimetable.getTimeSlotForSession(sessionOfTheWeek)
      selectedSubject <- lastSelectedSubject
      timeSlot <- eitherFractionOrAsMuchAsPossible match {
        case Right(asMuchAsPoss) => classTimetable.getFirstAvailableTimeSlot(maybeSessionOfTheWeek)
        case Left(fraction) => classTimetable.getFirstAvailableTimeSlot(sessionOfTheWeek, fraction)
      }
      subjectDetail = WwwSubjectDetail(selectedSubject, timeSlot)
    } yield (subjectDetail, sessionOfTheWeek)

    maybeSubjectDetailAndSessionOfTheWeek match {
      case Some(subjectDetailAndSessionOfTheWeek) =>
        global.console.log(s"subjectDetailAndSessionOfTheWeek: ${subjectDetailAndSessionOfTheWeek.toString}")
        if (classTimetable.addSubject(subjectDetailAndSessionOfTheWeek._1, subjectDetailAndSessionOfTheWeek._2)) {
          renderClassTimetable()
        } else {
          global.alert("Error : Could not add subject. Perhaps not enough space to add subject")
        }
        val $ = js.Dynamic.global.$
        $("#addLessonsModal").modal("hide")
        lastSelectedSubject = None
        if (longerClickSubjectSelected) {
          setDisableValueOnAllTimetableButtonsTo(false)
        } else {
          setDisableValueOnAllTimetableButtonsTo(true)
        }
      case None =>
        global.alert("Unable to add subject to session")
        global.console.error(s"Error adding subject to session")
    }
  }

  def addSubjectToPartlyFillSession(): Unit = {
    def fractionBehaviourForButton(fraction: Fraction) = {
      addingSubjectGeneralBehaviour(Left(fraction))
    }

    val oneThirdFillSubjectButton = dom.document.getElementById("add-subject-to-one-third-session-button").asInstanceOf[HTMLButtonElement]
    oneThirdFillSubjectButton.addEventListener("click", (e: dom.Event) => {
      fractionBehaviourForButton(OneThird())
    })
    val halfFillSubjectButton = dom.document.getElementById("add-subject-to-half-session-button").asInstanceOf[HTMLButtonElement]
    halfFillSubjectButton.addEventListener("click", (e: dom.Event) => {
      fractionBehaviourForButton(OneHalf())
    })
    val twoThirdsFillSubjectButton = dom.document.getElementById("add-subject-to-two-thirds-session-button").asInstanceOf[HTMLButtonElement]
    twoThirdsFillSubjectButton.addEventListener("click", (e: dom.Event) => {
      fractionBehaviourForButton(TwoThirds())
    })
    val fillSubjectButton = dom.document.getElementById("add-subject-to-fill-session-button").asInstanceOf[HTMLButtonElement]
    fillSubjectButton.addEventListener("click", (e: dom.Event) => {
      fractionBehaviourForButton(Whole())
    })

  }

  private def extractSelectedSessionOfTheWeek = {
    val theSessionOfTheWeek = {
      for {
        day <- currentlySelectedDayOfWeek
        session <- currentlySelectedSession
      } yield WwwSessionOfTheWeek.createSessionOfTheWeek(day, session)
    }.flatten
    theSessionOfTheWeek
  }
  def modalButtonsBehaviour(): Unit = {
    fillAsMuchAsPossibleOfSession()
    addSubjectToPartlyFillSession()
  }

  def launchAddSubjectToEmptySessionModalEventListeners(): Unit = {
    def popupSubjectTimesModal(e: Event): Unit = {
      e.preventDefault()
      e.currentTarget match {
        case buttonTarget: HTMLButtonElement =>
          val buttonTargetText = dom.document.getElementById("addLessonsModalLabel").innerHTML

          val timetableSession = buttonTarget.getAttribute("data-timetable-session")

          currentlySelectedSession = timetableSession match {
            case sessionName: String => if (WwwSessions.values.contains(sessionName)) Some(WwwSession(sessionName)) else None
            case _ => None
          }

          val dayOfTheWeek = buttonTarget.getAttribute("data-day-of-the-week")
          currentlySelectedDayOfWeek = dayOfTheWeek match {
            case dayName: String => if (WwwDaysOfWeek.values.contains(dayName)) Some(WwwDayOfWeek(dayName)) else None
            case _ => None
          }

          val subjectSessionDayOption = for {
            subjectSelected <- currentlySelectedSubject
            sessionSelected <- currentlySelectedSession
            dayOfWeekSelected <- currentlySelectedDayOfWeek
            subjectNice = subjectSelected.value.replace("subject-", "").replaceAll("-", " ").split(' ').map(_.capitalize).mkString(" ")
            sessionNice = sessionSelected.value.replaceAll("-", " ").split(' ').map(_.capitalize).mkString(" ")
          } yield {
            (subjectNice, sessionNice, dayOfWeekSelected.value)
          }

          if (subjectSessionDayOption.isDefined) {
            val $ = js.Dynamic.global.$
            $("#addLessonsModal").modal("show", "backdrop: static", "keyboard : false")
            dom.document.getElementById("addLessonsModalLabel").innerHTML
              = s"Add <strong>${subjectSessionDayOption.get._1}</strong>" +
              s"<br>To ${subjectSessionDayOption.get._3} ${subjectSessionDayOption.get._2}"
            this.lastSelectedSubject = currentlySelectedSubject
          } else {
            scala.scalajs.js.Dynamic.global.alert("There was an error selecting subject and session.\n\n" +
              s"Subjected Selected = '${currentlySelectedSubject.getOrElse("NO SUBJECT SELECTED")}'\n" +
              s"Session Selected = '${currentlySelectedSession.getOrElse("NO SESSION SELECTED")}'\n"
            )
          }

        case _ =>
      }

      val preciseTimeButton = dom.document.getElementById("precise-time-button").asInstanceOf[HTMLButtonElement]
      preciseTimeTogglerBehaviourOff(preciseTimeButton)
    }

    val timetableSubjectButtons = dom.document.getElementsByClassName("subject-empty")
    val nodeListSize = timetableSubjectButtons.length
    var index = 0
    while (index < nodeListSize) {
      val button = timetableSubjectButtons(index).asInstanceOf[HTMLButtonElement]

      button.addEventListener("dragover", (e: dom.Event) => {
        e.preventDefault()
      })

      button.addEventListener("drop", (e: dom.Event) => {
        popupSubjectTimesModal(e)
      })

      button.addEventListener("click", (e: dom.Event) => {
        longerClickSubjectSelected = true
        popupSubjectTimesModal(e)
      })

      index = index + 1
    }

  }


  def getDayContainerRow(button: HTMLButtonElement): HTMLDivElement = {
    def rowSearcher(currentHtmlElement: HTMLElement): HTMLDivElement = {
      val allClassesForElement = currentHtmlElement.classList
      if (allClassesForElement.contains("dayoftheweek-row")) {
        currentHtmlElement.asInstanceOf[HTMLDivElement]
      } else {
        rowSearcher(currentHtmlElement.parentElement)
      }
    }

    rowSearcher(button)
  }

  def addEventListenerForSubjectButtonsAddedToTimetable(): Unit = {
    val timetableSubjectButtons = dom.document.getElementsByClassName("non-empty-subject")
    val nodeListSize = timetableSubjectButtons.length
    var index = 0
    while (index < nodeListSize) {
      val button = timetableSubjectButtons(index).asInstanceOf[HTMLButtonElement]
      button.addEventListener("click", (e: dom.Event) => {
        val subjectCode = button.getAttribute("data-subject-code")
        val day = button.getAttribute("data-day-of-the-week")
        val timetableSession = button.getAttribute("data-timetable-session")
        val startTime = button.getAttribute("data-lesson-start-time")
        val endTime = button.getAttribute("data-lesson-end-time")

        val dayContainerRow = getDayContainerRow(button)
        val currentSubjectSummary = dom.document.getElementById("subject-summary-in-timetable").asInstanceOf[HTMLDivElement]
        if (currentSubjectSummary != null) {
          val $ = js.Dynamic.global.$
          $("#subject-summary-in-timetable").collapse()
        }

        val maybeTimeSlot = for {
          startTimeAsLocal <- LocalTimeUtil.convertStringTimeToLocalTime(startTime)
          endTimeAsLocal <- LocalTimeUtil.convertStringTimeToLocalTime(endTime)
          timeSlot = WwwTimeSlot(startTimeAsLocal, endTimeAsLocal)
        } yield timeSlot

        global.console.log(s"maybeTimeSlot := $maybeTimeSlot")

        val maybeAdditionalInfo = for {
          sessionOfTheWeek <- WwwSessionOfTheWeek.createSessionOfTheWeek(WwwDayOfWeek(day), WwwSession(timetableSession))
          timeSlot <- maybeTimeSlot
          additionalInfo <- classTimetable.getAdditionalInfoForSubject(
            WwwSubjectDetail(WwwSubjectName(subjectCode), timeSlot),
            sessionOfTheWeek)
        } yield additionalInfo

        global.console.log(s"maybeAdditionalInfo := $maybeAdditionalInfo")

        val subjectSummaryAndOptions = createSubjectSummary(subjectCode, startTime, endTime,
          timetableSession, day,
          maybeAdditionalInfo.getOrElse(""))

        if (getRemoveBehaviourTuple.isDefined) {
          if (currentSubjectSummary != null) {
            dayContainerRow.removeChild(currentSubjectSummary)
          }
          dayContainerRow.appendChild(subjectSummaryAndOptions.render)
        }
        addRemoveBehaviour(getRemoveBehaviourTuple)
        addOKSubjectBehaviour(getRemoveBehaviourTuple)
        addAdditionalInfoToSubject(getRemoveBehaviourTuple)

        global.console.log(s"Subject: $subjectCode\n" +
          s"Start Time: $startTime\n" +
          s"End Time: $endTime\n" +
          s"Session: $timetableSession\n" +
          s"Day: $day\n"
        )
      })

      index = index + 1
    }

  }

  def addRemoveBehaviour(maybeRemoveBehaviour: Option[(WwwSubjectDetail, WwwSessionOfTheWeek)]): Unit = {
    for {
      removeBehaviour <- maybeRemoveBehaviour
      subjectToRemove = removeBehaviour._1
      sessionOfTheWeek = removeBehaviour._2
    } {
      val removeSubjectButton = dom.document.getElementById("remove-lesson-from-timetable-button").asInstanceOf[HTMLButtonElement]
      removeSubjectButton.addEventListener("click", (e: dom.Event) => {
        val removedOkay = getClassTimetable.removeSubject(subjectToRemove, sessionOfTheWeek)
        renderClassTimetable()
      })
    }
  }

  def addOKSubjectBehaviour(maybeOkBehaviour: Option[(WwwSubjectDetail, WwwSessionOfTheWeek)]): Unit = {
    for {
      okBehaviour <- maybeOkBehaviour
      subjectSelected = okBehaviour._1
      sessionOfTheWeek = okBehaviour._2
    } {
      val okSubjectButton = dom.document.getElementById("ok-update-for-timetable-button").asInstanceOf[HTMLButtonElement]
      okSubjectButton.addEventListener("click", (e: dom.Event) => {
        updatedSubject(subjectSelected, sessionOfTheWeek)
      })
    }
  }

  private def updatedSubject(subjectSelected: WwwSubjectDetail, sessionOfTheWeek: WwwSessionOfTheWeek): Unit = {
    val additionalInfo = dom.document.getElementById("input-for-additional-info").asInstanceOf[HTMLInputElement].value
    global.console.log(s"Editing subject to have additional info: $additionalInfo")

    val editedSubject = WwwSubjectDetail(
      subjectSelected.subject,
      subjectSelected.timeSlot,
      additionalInfo
    )

    classTimetable.editSubject(editedSubject, sessionOfTheWeek)
    dom.document.getElementById("subject-summary-in-timetable").asInstanceOf[HTMLButtonElement].style.display = "none"
    setRemoveBehaviourTupleToNone()
    renderClassTimetable()
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
    val timetableSlotButtons: NodeList = dom.document.getElementsByClassName("subject-empty")
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
    for (subject <- WwwSubjects.values) {
      if (subject != "subject-empty") {
        val subjectElement = dom.document.getElementById(subject)
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

  }

  private def setTheCurrentlySelectedSubject(e: Event): Unit = {
    e.currentTarget match {
      case subjectDiv: HTMLDivElement =>
        val justSelectedSubject = WwwSubjectName(subjectDiv.id)
        if (currentlySelectedSubject.isDefined) {
          if (currentlySelectedSubject.get == justSelectedSubject) {
            currentlySelectedSubject = None
            longerClickSubjectSelected = false
            resetDiv(subjectDiv)
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

  private def resetDiv(subjectDiv: HTMLDivElement): Unit = {
    val parent = subjectDiv.parentNode.asInstanceOf[HTMLDivElement]
    parent.style.border = ""
    parent.style.paddingLeft = "15px"
    parent.style.paddingRight = "15px"

    subjectDiv.style.textShadow = ""
    subjectDiv.style.color = originalColour
    subjectDiv.style.fontWeight = "normal"
    subjectDiv.style.fontSize = "medium"
  }
  private def selectThisElement(subjectDiv: HTMLDivElement, justSelectedSubject: WwwSubjectName): Unit = {

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
    s"$hoursDisplay:$minutesDisplay $amOrPm"
  }

  def subtractMinutesToTime(minutesToSubtract: Int, currentTime: String): String = {
    val currentTotalMinutes = extractTotalMinutes(currentTime)
    val newTotalHoursAndMinutes = subtractMinutesToMinutesAndGetHoursAndMinutes(minutesToSubtract, currentTotalMinutes)
    val hoursDisplay = if (newTotalHoursAndMinutes._1 > 12) newTotalHoursAndMinutes._1 - 12 else newTotalHoursAndMinutes._1
    val minutesDisplay = if (newTotalHoursAndMinutes._2 >= 10) newTotalHoursAndMinutes._2 else "0" + newTotalHoursAndMinutes._2
    val amOrPm = if (newTotalHoursAndMinutes._1 >= 12) "PM" else "AM"
    s"$hoursDisplay:$minutesDisplay $amOrPm"
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

  def calculateDurationFromNewEndTime(): Unit = {
    def updateTheDurationWhenEndChanges(lessonEndTimeInput: HTMLInputElement): Unit = {
      val lessonStartTimeString = dom.document.getElementById("timepicker1").asInstanceOf[HTMLInputElement].value
      val lessonEndTimeString = lessonEndTimeInput.value

      val newDuration = for {
        startTime <- LocalTimeUtil.convertStringTimeToLocalTime(lessonStartTimeString)
        endTime <- LocalTimeUtil.convertStringTimeToLocalTime(lessonEndTimeString)
      } yield MINUTES.between(startTime, endTime).toString

      val currentDuration = dom.document.getElementById("lesson-duration").asInstanceOf[HTMLInputElement].value
      dom.document.getElementById("lesson-duration").asInstanceOf[HTMLInputElement].value = newDuration.getOrElse(currentDuration)

      val lessonOutput = js.Dynamic.global.document.getElementById("lesson-duration-output")
      lessonOutput.value = newDuration.getOrElse(currentDuration)
    }

    val lessonEndTimeInput = dom.document.getElementById("timepicker2").asInstanceOf[HTMLInputElement]
    lessonEndTimeInput.addEventListener("input", (e: dom.Event) => {
      updateTheDurationWhenEndChanges(lessonEndTimeInput)
    })
    lessonEndTimeInput.addEventListener("keydown", (e: dom.Event) => {
      updateTheDurationWhenEndChanges(lessonEndTimeInput)
    })
    lessonEndTimeInput.addEventListener("keyup", (e: dom.Event) => {
      updateTheDurationWhenEndChanges(lessonEndTimeInput)
    })
  }

  def saveCancelClearBehaviour(): Unit = {
    val clearSubjectsButton = dom.document.getElementById("clear-subjects-from-class-timetable-button").asInstanceOf[HTMLButtonElement]
    clearSubjectsButton.addEventListener("click", (e: dom.Event) => {
      global.console.log("Clearing the subjects...")
      classTimetable.clearWholeTimetable()
      renderClassTimetable()
    })

    val saveClassTimetableButton = dom.document.getElementById("save-class-timetable-button").asInstanceOf[HTMLButtonElement]
    saveClassTimetableButton.addEventListener("click", (e: dom.Event) => {
      global.console.log(s"Saving the class timetable... ${classTimetable.toString}")

      import dom.ext.Ajax

      import scala.concurrent.ExecutionContext.Implicits.global
      val theUrl = "/classtimetablesave"
      val theHeaders = Map(
        "Content-Type" -> "application/x-www-form-urlencoded",
        "X-Requested-With" -> "Accept"
      )
      val theClassTimetableName = dom.window.localStorage.getItem("timetableClassName")
      val theTimeToTeachUserId = dom.window.localStorage.getItem("timeToTeachUserId")
      val theData = InputData.str2ajax(s"classTimetable=${classTimetable.toString}&" +
        s"className=$theClassTimetableName&tttUserId=$theTimeToTeachUserId")

      Ajax.post(
        url = theUrl,
        headers = theHeaders,
        data = theData
      ).onComplete {
        case Success(xhr) =>
          val hello = xhr.responseText
          println(s"hello === $hello")
          dom.window.location.href = "/app";
        case Failure(ex) =>
          dom.window.alert("Something went wrong with saving class timetable. Specificically : -" +
            s"\n\n${ex.toString}")
      }
    })

  }

  def toggleTheSubjectsAside(): Unit = {
    val subjectAsideToggle =
      dom.document.getElementById("class-timetable-subject-aside-toggle-button").asInstanceOf[HTMLButtonElement]

    subjectAsideToggle.addEventListener("click", (e: dom.Event) => {
      val subjectAside = dom.document.getElementById("class-timetable-subject-aside").asInstanceOf[HTMLButtonElement]
      subjectAside.style.display = "none"

      val showSubjectsDiv = dom.document.getElementById("show-subjects-button-div").asInstanceOf[HTMLDivElement]
      showSubjectsDiv.style.display = "block"
      showSubjectsDiv.addEventListener("click", (e: dom.Event) => {
        val subjectAside = dom.document.getElementById("class-timetable-subject-aside").asInstanceOf[HTMLButtonElement]
        subjectAside.style.display = "block"
        showSubjectsDiv.style.display = "none"
      })

    })
  }


  def loadClassTimetableJavascript(): Unit = {
    calculateDurationFromNewEndTime()
    calculateEndTimeFromNewStartTime()
    calculateEndTimeFromDuration()
    preciseTimeToggler()
    addEventListenerToDragstart()
    launchAddSubjectToEmptySessionModalEventListeners()
    addListenerToAllSubjectButtons()
    modalButtonsBehaviour()
    saveCancelClearBehaviour()
    toggleTheSubjectsAside()
    submitLessonDuration()
  }

  def renderClassTimetable(): Unit = {
    val theDaysSubjectsAsHtml = generateHtmlForClassTimetable(classTimetable)
    val allTheDaysDiv = dom.document.getElementById("all-the-days-rows").asInstanceOf[HTMLDivElement]
    allTheDaysDiv.innerHTML = theDaysSubjectsAsHtml
    launchAddSubjectToEmptySessionModalEventListeners()
    addEventListenerForSubjectButtonsAddedToTimetable()
  }

}
