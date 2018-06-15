package timetoteach.planning.weekly

import duplicate.model.ClassDetails
import duplicate.model.planning._
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.ext.Ajax.InputData
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLDivElement}
import scalatags.JsDom.all.{div, id, _}
import shared.util.PlanningHelper
import upickle.default.write

import scala.scalajs.js
import scala.scalajs.js.Dynamic
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object WeeklyPlanningJsScreen extends WeeklyPlansCommon {

  global.toString // Just so implicit EC doesn't disappear when organise imports

  def loadJavascript(): Unit = {
    Dynamic.global.console.log("Loading Weekly Planning Javascript")
    setMondayDateToCurrentlySelectedWeek()
    clickingAMondayWeekButtonUpdatesDates()
    planThisWeekButton()
    showLessonPlanDetail()
    savePlanButton()
  }

  private def buildLessonSummary(maybePlan: Option[LessonPlan]): LessonSummary = {
    maybePlan match {
      case Some(lessonPlan) =>
        LessonSummary(
          lessonPlan.subject,
          lessonPlan.subjectAdditionalInfo,
          Some(lessonPlan.lessonDateIso),
          getDayOfWeek(new js.Date(lessonPlan.lessonDateIso)),
          lessonPlan.startTimeIso,
          lessonPlan.endTimeIso
        )
      case None => LessonSummary("", "", None, "", "", "")
    }
  }

  def buildLessonModalHtml(maybeLessonPlan: Option[LessonPlan]): Unit = {
    cleanupActivity("view-single-lesson-plan-modal-body")
    cleanupModalAdds()

    val index = 0
    val lessonSummary = buildLessonSummary(maybeLessonPlan)


    val theDiv = div(id := s"view-single-lesson-plan-main-lesson-container")(
      createLessonDataDiv(lessonSummary, index),
      createAddButton("create-weekly-plans-add-to-lesson-button-add-activity", "Activity", index, lessonSummary),
      createAddButton("create-weekly-plans-add-to-lesson-button-add-resource", "Resource", index, lessonSummary),
      createAddButton("create-weekly-plans-add-to-lesson-button-add-learning-intention", "Learning Intention", index, lessonSummary),
      createAddButton("create-weekly-plans-add-to-lesson-button-add-success-criteria", "Success Criteria", index, lessonSummary),
      createAddButton("create-weekly-plans-add-to-lesson-button-add-plenary", "Plenary", index, lessonSummary),
      createAddButton("create-weekly-plans-add-to-lesson-button-add-formative-assessment", "Formative Assessment", index, lessonSummary),
      createAddButton("create-weekly-plans-add-to-lesson-button-add-note", "Note", index, lessonSummary)
    )

    val child = dom.document.createElement("div")
    child.innerHTML = theDiv.toString()
    dom.document.getElementById("view-single-lesson-plan-modal-body").appendChild(child)
    clickingOnAddToLessonsButtons()
    Dynamic.global.console.log(s"[][a] ${groupIdsToName.toString()}")
    addLessonPlanDetailsFromSavedStatus()
    Dynamic.global.console.log(s"[][b] ${groupIdsToName.toString()}")
  }

  override def getGroupIdsToName: scala.collection.mutable.Map[String, String] = {
    Dynamic.global.console.log(s"!!!!!!!!!!!!!!!! Groups for subject built from Weekly Plan : ${groupIdsToName.toString}")
    groupIdsToName
  }

  private def buildGroupsMapForSubject(): Unit = {
    groupIdsToName.clear()

    val classDetailsPickled = dom.window.localStorage.getItem("classDetailsPickled")
    import upickle.default._
    val classDetails: ClassDetails = read[ClassDetails](PlanningHelper.decodeAnyNonFriendlyCharacters(classDetailsPickled))

    for (group <- classDetails.getSubjectGroups(maybeSelectedSubject.getOrElse("NO_SUBJECT"))) {
      groupIdsToName = groupIdsToName + (group.groupId.id -> group.groupName.name)
    }

    Dynamic.global.console.log(s"Groups for subject built : ${groupIdsToName.toString}")
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
        maybeSelectedSubjectAdditionalInfo = None
        maybeSelectedSubjectStartTimeIso = None
        maybeSelectedSubjectDayOfTheWeek = None

        maybeSelectedPlanningArea = Some(lessonPlanDiv.getAttribute("data-planning-area"))
        maybeSelectedSubject = Some(lessonPlanDiv.getAttribute("data-subject-name"))
        maybeSelectedSubjectAdditionalInfo = Some(lessonPlanDiv.getAttribute("data-subject-additional-info"))
        maybeSelectedSubjectStartTimeIso = Some(lessonPlanDiv.getAttribute("data-lesson-start-time"))
        maybeSelectedSubjectEndTimeIso = Some(lessonPlanDiv.getAttribute("data-lesson-end-time"))
        maybeSelectedSubjectDayOfTheWeek = Some(lessonPlanDiv.getAttribute("data-lesson-day-of-the-week"))
        buildGroupsMapForSubject()

        val fullWeeklyPlanOfLessonsPickled = dom.window.localStorage.getItem("fullWeeklyPlanOfLessonsPickled")
        import upickle.default._
        val fullWeeklyPlanOfLessons: FullWeeklyPlanOfLessons = read[FullWeeklyPlanOfLessons](PlanningHelper.decodeAnyNonFriendlyCharacters(fullWeeklyPlanOfLessonsPickled))

        dom.document.getElementById("view-single-lesson-plan-subject-name").innerHTML = s"${maybeSelectedSubject.getOrElse("")}"
        dom.document.getElementById("view-single-lesson-plan-subject-additional-info").innerHTML = s"${maybeSelectedSubjectAdditionalInfo.getOrElse("")}"
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
    Dynamic.global.console.log(s"Selected week button ${maybeSelectedWeekButton.toString}")
    maybeSelectedWeekButton match {
      case Some(selectedWeekButton) =>
        val mondayOfTheWeekDate = selectedWeekButton.getAttribute("data-selected-monday-date")
        val mondayJsDate = new js.Date(mondayOfTheWeekDate)
        val systemTodayDate = new js.Date(dom.window.localStorage.getItem("systemDateTodayIso"))
        Dynamic.global.console.log(s"System: ${systemTodayDate.toISOString()}, monday: ${mondayJsDate.toISOString()}")
        Dynamic.global.console.log(s"System: ${systemTodayDate.getTime()}, monday: ${mondayJsDate.getTime()}")
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

  private def createLessonPlan(weekBeginningIsoDate: String) : LessonPlan = {
    val index = 0

    val subject = maybeSelectedPlanningArea.getOrElse("NO_PLANNING_AREA")
    val subjectAdditionalInfo = maybeSelectedSubjectAdditionalInfo.getOrElse("NO_ADDITIONAL_INFO")
    val day = maybeSelectedSubjectDayOfTheWeek.getOrElse("NO_DAY")
    val lessonDateIso = getIsoDate(weekBeginningIsoDate, day)
    val startTime = maybeSelectedSubjectStartTimeIso.getOrElse("")
    val endTime = maybeSelectedSubjectEndTimeIso.getOrElse("")

    val activitiesPerGroup: Map[AttributeRowKey, List[String]] = extractLessonAttributesWithGroups("Activity", index)
    val resources: List[AttributeRowKey] = extractLessonAttributes("Resource", index)
    val learningIntentionsPerGroup: Map[AttributeRowKey, List[String]] = extractLessonAttributesWithGroups("LearningIntention", index)
    val successCriteriaPerGroup: Map[AttributeRowKey, List[String]] = extractLessonAttributesWithGroups("SuccessCriteria", index)
    val plenary: List[AttributeRowKey] = extractLessonAttributes("Plenary", index)
    val formativeAssessmentPerGroup: Map[AttributeRowKey, List[String]] = extractLessonAttributesWithGroups("FormativeAssessment", index)
    val notesBefore: List[AttributeRowKey] = extractLessonAttributes("Note", index)
    val notesAfter: List[AttributeRowKey] = Nil

    LessonPlan(
      subject,
      subjectAdditionalInfo,
      weekBeginningIsoDate,
      lessonDateIso,
      startTime,
      endTime,
      "",
      activitiesPerGroup,
      resources,
      learningIntentionsPerGroup,
      successCriteriaPerGroup,
      plenary,
      formativeAssessmentPerGroup,
      notesBefore,
      notesAfter
    )
  }

  private def savePlanButton(): Unit = {
    val saveSubjectWeeksPlanButton = dom.document.getElementById("view-single-lesson-plan-save-subject-plan")
    if (saveSubjectWeeksPlanButton != null) {
      saveSubjectWeeksPlanButton.addEventListener("click", (e: dom.Event) => {
        val subject = maybeSelectedPlanningArea.getOrElse("NO_SUBJECT")
        val classId = dom.window.localStorage.getItem("classId")
        val tttUserId = dom.window.localStorage.getItem("tttUserId")
        val weekBeginningIsoDate = currentlySelectMondayStartOfWeekDate.getOrElse("1970-01-01")
        Dynamic.global.console.log(s"Subject == $subject")
        Dynamic.global.console.log(s"classId == $classId")
        Dynamic.global.console.log(s"tttUserId == $tttUserId")
        Dynamic.global.console.log(s"weekBeginningIsoDate == $weekBeginningIsoDate")

        val lessonToSave: LessonPlan = createLessonPlan(weekBeginningIsoDate)
        Dynamic.global.console.log(s"lesson plan to save == ${lessonToSave.toString}")

        postSave(lessonToSave)
      })
    }

  }

  private def postSave(lessonPlanToSave: LessonPlan): Unit = {
    val lessonPlanPickled = PlanningHelper.encodeAnyJawnNonFriendlyCharacters(write[LessonPlan](lessonPlanToSave))
    val classId = dom.window.localStorage.getItem("classId")

    val theUrl = s"/saveSingleLessonPlan/$classId"
    val theHeaders = Map(
      "Content-Type" -> "application/x-www-form-urlencoded",
      "X-Requested-With" -> "Accept"
    )

    val tttUserId = dom.window.localStorage.getItem("tttUserId")
    val theData = InputData.str2ajax(s"singleLessonPlanPickled=$lessonPlanPickled&tttUserId=$tttUserId")

    Ajax.post(
      url = theUrl,
      headers = theHeaders,
      data = theData
    ).onComplete {
      case Success(xhr) =>
        val responseText = xhr.responseText
        println(s"response = '$responseText'")
        dom.window.setTimeout(() => {
          val $ = js.Dynamic.global.$
          $("#view-single-lesson-plan-modal").modal("hide")
          currentlySelectMondayStartOfWeekDate match {
            case Some(mondayIsoDate) =>
              dom.window.location.href = s"/weeklyViewOfWeeklyPlanning/$classId/$mondayIsoDate"
            case None =>
              dom.window.location.href = s"/weeklyViewOfWeeklyPlanning/$classId"
          }

        }, 10)
      case Failure(ex) =>
        dom.window.alert("Something went wrong with saving single lesson plans. Specifically : -" +
          s"\n\n${ex.toString}")
        val $ = js.Dynamic.global.$
        $("#view-single-lesson-plan-modal").modal("hide")
    }

  }


}
