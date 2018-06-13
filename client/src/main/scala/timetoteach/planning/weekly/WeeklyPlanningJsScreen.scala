package timetoteach.planning.weekly

import duplicate.model.planning.{FullWeeklyPlanOfLessons, LessonPlan, WeeklyPlanOfOneSubject}
import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLDivElement}
import shared.util.PlanningHelper

import scala.scalajs.js
import scala.scalajs.js.Dynamic.global

object WeeklyPlanningJsScreen extends WeeklyPlansCommon {

  private var maybeSelectedPlanningArea: Option[String] = None
  private var maybeSelectedSubject: Option[String] = None
  private var maybeSelectedSubjectStartTimeIso: Option[String] = None
  private var maybeSelectedSubjectEndTimeIso: Option[String] = None
  private var maybeSelectedSubjectDayOfTheWeek: Option[String] = None


  def loadJavascript(): Unit = {
    global.console.log("Loading Weekly Planning Javascript")
    setMondayDateToCurrentlySelectedWeek()
    clickingAMondayWeekButtonUpdatesDates()
    planThisWeekButton()
    showLessonPlanDetail()
  }

  def buildLessonModalHtml(maybeLessonPlan: Option[LessonPlan]): Unit = {
    dom.document.getElementById("view-single-lesson-plan-modal-body").innerHTML = s"Will fill in " +
      s"details for ${maybeLessonPlan.toString()}"
  }

  def buildNoLessonModalHtml(subject: String): Unit = {
    dom.document.getElementById("view-single-lesson-plan-modal-body").innerHTML =
      s"Looks like there is no plan currently for this $subject lesson."
  }

  def findSelectedLessonPlan(subject: WeeklyPlanOfOneSubject): Option[LessonPlan] = {
    val selectedPlanningArea = maybeSelectedPlanningArea.getOrElse("NOPE")
    val selectedLessonStartTime = maybeSelectedSubjectStartTimeIso.getOrElse("NOPE")
    val selectedLessonDayOfWeek = maybeSelectedSubjectDayOfTheWeek.getOrElse("NOPE")

    def loop(remainingLessons: List[LessonPlan]): Option[LessonPlan] = {
      if (remainingLessons.isEmpty) None
      else {
        val nextLesson = remainingLessons.head
        val jsLessonDate = new js.Date(nextLesson.lessonDateIso)
        if (
          nextLesson.subject == selectedPlanningArea &&
            nextLesson.startTimeIso == selectedLessonStartTime &&
            getDayOfWeek(jsLessonDate) == selectedLessonDayOfWeek
        ) Some(nextLesson)
        else loop(remainingLessons.tail)
      }
    }

    loop(subject.lessons)
  }

  def showLessonPlanDetail(): Unit = {
    val allLessonsPlans = dom.document.getElementsByClassName("weekly-plan-subject-extent")
    val nodeListSize = allLessonsPlans.length
    var index = 0
    while (index < nodeListSize) {
      val lessonPlanDiv = allLessonsPlans(index).asInstanceOf[HTMLDivElement]
      lessonPlanDiv.addEventListener("click", (e: dom.Event) => {
        maybeSelectedPlanningArea = None
        maybeSelectedSubject = None
        maybeSelectedSubjectStartTimeIso = None
        maybeSelectedSubjectDayOfTheWeek = None

        maybeSelectedPlanningArea = Some(lessonPlanDiv.getAttribute("data-planning-area"))
        maybeSelectedSubject = Some(lessonPlanDiv.getAttribute("data-subject-name"))
        maybeSelectedSubjectStartTimeIso = Some(lessonPlanDiv.getAttribute("data-lesson-start-time"))
        maybeSelectedSubjectEndTimeIso = Some(lessonPlanDiv.getAttribute("data-lesson-end-time"))
        maybeSelectedSubjectDayOfTheWeek = Some(lessonPlanDiv.getAttribute("data-lesson-day-of-the-week"))

        val fullWeeklyPlanOfLessonsPickled = dom.window.localStorage.getItem("fullWeeklyPlanOfLessonsPickled")
        import upickle.default._
        val fullWeeklyPlanOfLessons: FullWeeklyPlanOfLessons = read[FullWeeklyPlanOfLessons](PlanningHelper.decodeAnyNonFriendlyCharacters(fullWeeklyPlanOfLessonsPickled))

        dom.document.getElementById("view-single-lesson-plan-subject-name").innerHTML = s"${maybeSelectedSubject.getOrElse("")}"
        dom.document.getElementById("view-single-lesson-plan-day-of-week").innerHTML = s"${maybeSelectedSubjectDayOfTheWeek.getOrElse("").toLowerCase.capitalize},"
        dom.document.getElementById("view-single-lesson-plan-start-time").innerHTML = s"${maybeSelectedSubjectStartTimeIso.getOrElse("")}"
        dom.document.getElementById("view-single-lesson-plan-end-time").innerHTML = s"${maybeSelectedSubjectEndTimeIso.getOrElse("")}"

        if (fullWeeklyPlanOfLessons.subjectToWeeklyPlanOfSubject.isDefinedAt(maybeSelectedPlanningArea.getOrElse("NO_PLANNING_AREA"))) {
          val selectedLessonPlan = findSelectedLessonPlan(fullWeeklyPlanOfLessons.subjectToWeeklyPlanOfSubject(maybeSelectedPlanningArea.getOrElse("NO_PLANNING_AREA")))
          buildLessonModalHtml(selectedLessonPlan)
        } else {
          buildNoLessonModalHtml(maybeSelectedPlanningArea.getOrElse("NO_PLANNING_AREA"))
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
