package timetoteach.planning.weekly

import duplicate.model.planning.FullWeeklyPlanOfLessons
import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLDivElement}
import shared.util.PlanningHelper

import scala.scalajs.js
import scala.scalajs.js.Dynamic.global

object WeeklyPlanningJsScreen extends WeeklyPlansCommon {

  private var maybeSelectedSubject: Option[String] = None
  private var maybeSelectedSubjectStartTimeIso: Option[String] = None
  private var maybeSelectedSubjectDayOfTheWeek: Option[String] = None


  def loadJavascript(): Unit = {
    global.console.log("Loading Weekly Planning Javascript")
    setMondayDateToCurrentlySelectedWeek()
    clickingAMondayWeekButtonUpdatesDates()
    planThisWeekButton()
    showLessonPlanDetail()
  }

  def showLessonPlanDetail(): Unit = {
    val allLessonsPlans = dom.document.getElementsByClassName("weekly-plan-subject-extent")
    val nodeListSize = allLessonsPlans.length
    var index = 0
    while (index < nodeListSize) {
      val lessonPlanDiv = allLessonsPlans(index).asInstanceOf[HTMLDivElement]
      lessonPlanDiv.addEventListener("click", (e: dom.Event) => {
        maybeSelectedSubject = None
        maybeSelectedSubjectStartTimeIso = None
        maybeSelectedSubjectDayOfTheWeek = None
        maybeSelectedSubject = Some(lessonPlanDiv.getAttribute("data-subject-name"))
        global.console.log(s"MaybeSubject : ${maybeSelectedSubject.toString}")
        maybeSelectedSubjectStartTimeIso = Some(lessonPlanDiv.getAttribute("data-lesson-start-time"))
        global.console.log(s"maybeSelectedSubjectStartTimeIso  : ${maybeSelectedSubjectStartTimeIso .toString}")
        maybeSelectedSubjectDayOfTheWeek = Some(lessonPlanDiv.getAttribute("data-lesson-day-of-the-week"))
        global.console.log(s"maybeSelectedSubjectDayOfTheWeek   : ${maybeSelectedSubjectDayOfTheWeek.toString}")

        val fullWeeklyPlanOfLessonsPickled = dom.window.localStorage.getItem("fullWeeklyPlanOfLessonsPickled")
        import upickle.default._
        val fullWeeklyPlanOfLessons: FullWeeklyPlanOfLessons = read[FullWeeklyPlanOfLessons](PlanningHelper.decodeAnyNonFriendlyCharacters(fullWeeklyPlanOfLessonsPickled))

        for (subject <- fullWeeklyPlanOfLessons.subjectToWeeklyPlanOfSubject.keys) {
          global.console.log(s"Subject : ${subject}")
        }

        val $ = js.Dynamic.global.$
        $("#view-single-lesson-plan-modal").modal("show", "backdrop: static", "keyboard : false")
      })

      lessonPlanDiv.addEventListener("mouseover", (e: dom.Event) => {
        lessonPlanDiv.style.boxShadow = "2px 2px #3e81ee"
      })

      lessonPlanDiv.addEventListener("mouseleave", (e: dom.Event) => {
        lessonPlanDiv.style.boxShadow = ""
      })

      index = index + 1
    }
  }

  override private[weekly] def toShowOrNotShowPlanThisWeekButtonGivenTheWeekSelected(): Unit = {
    val planThisWeekButton = dom.document.getElementById("weekly-planning-plan-this-week-button").asInstanceOf[HTMLButtonElement]
    val maybeSelectedWeekButton = getSelectedWeekButton()
    global.console.log(s"Selected week button ${maybeSelectedWeekButton.toString}")
    maybeSelectedWeekButton match {
      case Some(selectedWeekButton) =>
        val mondayOfTheWeekDate = selectedWeekButton.getAttribute("data-selected-monday-date")
        val mondayJsDate = new js.Date(mondayOfTheWeekDate)
        val systemTodayDate = new js.Date(dom.window.localStorage.getItem("systemDateTodayIso"))
        global.console.log(s"System: ${systemTodayDate.toISOString()}, monday: ${mondayJsDate.toISOString()}")
        global.console.log(s"System: ${systemTodayDate.getTime()}, monday: ${mondayJsDate.getTime()}")
        if (systemTodayDate.getTime() > mondayJsDate.getTime()) {
          planThisWeekButton.style.display = "none"
        } else {
          planThisWeekButton.style.display = "block"
        }

      case None =>
        planThisWeekButton.style.display = "block"
    }
  }

  private def planThisWeekButton(): Unit = {
    val planThisWeekButton = dom.document.getElementById("weekly-planning-plan-this-week-button").asInstanceOf[HTMLButtonElement]
    if (planThisWeekButton != null) {
      planThisWeekButton.addEventListener("click", (e: dom.Event) => {
        val classId = dom.window.localStorage.getItem("classId")

        currentlySelectMondayStartOfWeekDate match {
          case Some(mondayIsoDate) =>
            dom.window.location.href = s"/createPlanForTheWeek/$classId/$mondayIsoDate"
          case None =>
            dom.window.location.href = s"/createPlanForTheWeek/$classId"
        }

      })
    }
  }


}
