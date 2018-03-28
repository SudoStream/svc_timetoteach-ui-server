package timetoteach.planning.weekly

import duplicate.model.planning.{LessonSummary, LessonsThisWeek}
import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLDivElement}
import scalatags.JsDom.all._
import shared.util.PlanningHelper

import scala.collection.mutable
import scala.scalajs.js
import scala.scalajs.js.Dynamic.global

object CreatePlanForTheWeekJsScreen extends WeeklyPlansCommon {

  private var groupToSelectedEsOsAndBenchmarks: scala.collection.mutable.Map[String, scala.collection.mutable.Map[String, scala.collection.mutable.Map[String,
    (scala.collection.mutable.Set[String], scala.collection.mutable.Set[String])]]] = scala.collection.mutable.Map.empty

  private var eAndORowBackgroundNormalColor: Option[String] = None
  private var eAndORowForegroundNormalColor: Option[String] = None
  private var eAndORowBorderRadius: Option[String] = None

  private var currentlySelectedPlanningArea: Option[String] = None
  private var currentlySelectedPlanningAreaNice: Option[String] = None
  private var currentlySelectedLessonSummariesThisWeek: Option[List[LessonSummary]] = None

  def loadJavascript(): Unit = {
    global.console.log("Loading Create Plan For The Week Javascript")
    setMondayDateToCurrentlySelectedWeek()
    clickingAMondayWeekButtonUpdatesDates()
    mouseoverHighlightEandOsAndBenchmarks()
    clickOnEandO()
    clickOnBenchmark()
    planLessonsButton()
  }

  private def planLessonsButton(): Unit = {
    val allPlanLessonsButtons = dom.document.getElementsByClassName("create-weekly-plans-plan-lessons-button")
    val nodeListSize = allPlanLessonsButtons.length
    var index = 0
    while (index < nodeListSize) {
      val buttonElement = allPlanLessonsButtons(index).asInstanceOf[HTMLButtonElement]

      buttonElement.addEventListener("click", (e: dom.Event) => {
        currentlySelectedPlanningArea = None
        currentlySelectedPlanningAreaNice = None
        currentlySelectedLessonSummariesThisWeek = None

        val summariesDiv = dom.document.getElementById("create-weekly-plans-lessons-summaries").asInstanceOf[HTMLDivElement]
        while (summariesDiv.firstChild != null) {
          summariesDiv.removeChild(summariesDiv.firstChild)
        }

        val planningArea = buttonElement.getAttribute("data-planning-area")
        val planningAreaNice = buttonElement.getAttribute("data-planning-area-nice")

        currentlySelectedPlanningArea = Some(planningArea)
        currentlySelectedPlanningAreaNice = Some(planningAreaNice)

        dom.document.getElementById("create-weekly-plans-lesson-subject-name").innerHTML = currentlySelectedPlanningAreaNice.getOrElse("")
        dom.document.getElementById("create-weekly-plans-lesson-subject-name").setAttribute("data-subject-name", currentlySelectedPlanningArea.getOrElse(""))

        val classId = dom.window.localStorage.getItem("classId")
        val tttUserId = dom.window.localStorage.getItem("tttUserId")
        val lessonsThisWeekPickled = dom.window.localStorage.getItem("lessonsThisWeekPickled")

        import upickle.default._
        val lessonsThisWeek: LessonsThisWeek = read[LessonsThisWeek](PlanningHelper.decodeAnyNonFriendlyCharacters(lessonsThisWeekPickled))

        if (lessonsThisWeek.subjectToLessons.isDefinedAt(planningArea)) {
          currentlySelectedLessonSummariesThisWeek = Some(lessonsThisWeek.subjectToLessons(planningArea))

          val lessonTimes = for {
            lesson <- lessonsThisWeek.subjectToLessons(planningArea)
          } yield span(`class` := "text-muted")(
            small(s"${lesson.dayOfWeek.toLowerCase.capitalize}(${lesson.startTimeIso}-${lesson.endTimeIso})"))

          val child = dom.document.createElement("div")
          child.innerHTML = lessonTimes.mkString(", ")

          dom.document.getElementById("create-weekly-plans-lessons-summaries").appendChild(child)
          dom.document.getElementById("create-weekly-plans-number-of-lessons").innerHTML = lessonTimes.size.toString
        }
        val $ = js.Dynamic.global.$
        $("#create-weekly-plans-lesson-modal").modal("show", "backdrop: static", "keyboard : false")
      })

      index = index + 1
    }
  }

  private def setButtonDefaults(theDiv: HTMLDivElement): Unit = {
    theDiv.style.backgroundColor = eAndORowBackgroundNormalColor.getOrElse("white")
    theDiv.style.color = eAndORowForegroundNormalColor.getOrElse("grey")
    theDiv.style.borderRadius = eAndORowBorderRadius.getOrElse("0")
  }

  private def clickOnEandO(): Unit = {
    val allEAndORows = dom.document.getElementsByClassName("create-weekly-plans-es-and-os-row")
    val nodeListSize = allEAndORows.length
    var index = 0
    while (index < nodeListSize) {
      val theDiv = allEAndORows(index).asInstanceOf[HTMLDivElement]

      theDiv.addEventListener("click", (e: dom.Event) => {
        val groupIdOrNot = theDiv.getAttribute("data-group-id-or-not")
        val eAndOCode = theDiv.getAttribute("data-eando-code")
        val curriculumSection = theDiv.getAttribute("data-curriculum-section")
        val curriculumSubSection = theDiv.getAttribute("data-curriculum-subsection")

        global.console.log(s"Selected E and O code '$eAndOCode'")

        if ((
          groupToSelectedEsOsAndBenchmarks.nonEmpty &&
            groupToSelectedEsOsAndBenchmarks.isDefinedAt(groupIdOrNot) &&
            groupToSelectedEsOsAndBenchmarks(groupIdOrNot).nonEmpty &&
            groupToSelectedEsOsAndBenchmarks(groupIdOrNot).isDefinedAt(curriculumSection) &&
            groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection).isDefinedAt(curriculumSubSection)) &&
          groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection)(curriculumSubSection)._1.contains(eAndOCode)) {
          groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection)(curriculumSubSection)._1.remove(eAndOCode)
          setButtonDefaults(theDiv)
        } else {
          if (!groupToSelectedEsOsAndBenchmarks.isDefinedAt(groupIdOrNot)) {
            groupToSelectedEsOsAndBenchmarks = groupToSelectedEsOsAndBenchmarks + (groupIdOrNot -> mutable.Map.empty)
          }
          if (!groupToSelectedEsOsAndBenchmarks(groupIdOrNot).isDefinedAt(curriculumSection)) {
            groupToSelectedEsOsAndBenchmarks(groupIdOrNot) = groupToSelectedEsOsAndBenchmarks(groupIdOrNot) + (curriculumSection -> mutable.Map.empty)
          }
          if (!groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection).isDefinedAt(curriculumSubSection)) {
            groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection)(curriculumSubSection) = (scala.collection.mutable.Set.empty, scala.collection.mutable.Set.empty)
          }

          groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection)(curriculumSubSection)._1.add(eAndOCode)
          global.console.log(s"Should be now .. values of Es and Os are ... ${groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection)(curriculumSubSection)._1.toString()}")
          theDiv.style.backgroundColor = "#016ecd"
          theDiv.style.color = "white"
          theDiv.style.borderRadius = "7px"
        }
      })


      index = index + 1
    }
  }


  private def mouseoverHighlightEandOsAndBenchmarks(): Unit = {
    val allEAndOAndBenchmarkRows = dom.document.getElementsByClassName("create-weekly-plans-eobenchmark-row")
    val nodeListSize = allEAndOAndBenchmarkRows.length
    var index = 0
    while (index < nodeListSize) {
      val theDiv = allEAndOAndBenchmarkRows(index).asInstanceOf[HTMLDivElement]

      theDiv.addEventListener("mouseover", (e: dom.Event) => {
        if (eAndORowBackgroundNormalColor.isEmpty) {
          eAndORowBackgroundNormalColor = Some(theDiv.style.backgroundColor)
        }
        if (eAndORowForegroundNormalColor.isEmpty) {
          eAndORowForegroundNormalColor = Some(theDiv.style.color)
        }
        if (eAndORowBorderRadius.isEmpty) {
          eAndORowBorderRadius = Some(theDiv.style.borderRadius)
        }

        val groupIdOrNot = theDiv.getAttribute("data-group-id-or-not")
        val eAndOCode = theDiv.getAttribute("data-eando-code")
        val benchmarkValue = theDiv.getAttribute("data-benchmark")
        val curriculumSection = theDiv.getAttribute("data-curriculum-section")
        val curriculumSubSection = theDiv.getAttribute("data-curriculum-subsection")

        if ((eAndOCode != null && eAndOCode.nonEmpty &&
          (
            groupToSelectedEsOsAndBenchmarks.isEmpty ||
              !groupToSelectedEsOsAndBenchmarks.isDefinedAt(groupIdOrNot) ||
              groupToSelectedEsOsAndBenchmarks(groupIdOrNot).isEmpty ||
              !groupToSelectedEsOsAndBenchmarks(groupIdOrNot).isDefinedAt(curriculumSection) ||
              !groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection).isDefinedAt(curriculumSubSection) ||
              !groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection)(curriculumSubSection)._1.contains(eAndOCode))) ||
          (benchmarkValue != null && benchmarkValue.nonEmpty &&
            (
              groupToSelectedEsOsAndBenchmarks.isEmpty ||
                !groupToSelectedEsOsAndBenchmarks.isDefinedAt(groupIdOrNot) ||
                groupToSelectedEsOsAndBenchmarks(groupIdOrNot).isEmpty ||
                !groupToSelectedEsOsAndBenchmarks(groupIdOrNot).isDefinedAt(curriculumSection) ||
                !groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection).isDefinedAt(curriculumSubSection) ||
                !groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection)(curriculumSubSection)._2.contains(benchmarkValue)))) {
          theDiv.style.backgroundColor = "grey"
          theDiv.style.color = "white"
          theDiv.style.borderRadius = "7px"
        }
      })

      theDiv.addEventListener("mouseleave", (e: dom.Event) => {
        val groupIdOrNot = theDiv.getAttribute("data-group-id-or-not")
        val eAndOCode = theDiv.getAttribute("data-eando-code")
        val benchmarkValue = theDiv.getAttribute("data-benchmark")
        val curriculumSection = theDiv.getAttribute("data-curriculum-section")
        val curriculumSubSection = theDiv.getAttribute("data-curriculum-subsection")
        if ((eAndOCode != null && eAndOCode.nonEmpty &&
          (
            groupToSelectedEsOsAndBenchmarks.isEmpty ||
              !groupToSelectedEsOsAndBenchmarks.isDefinedAt(groupIdOrNot) ||
              groupToSelectedEsOsAndBenchmarks(groupIdOrNot).isEmpty ||
            !groupToSelectedEsOsAndBenchmarks(groupIdOrNot).isDefinedAt(curriculumSection) ||
            !groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection).isDefinedAt(curriculumSubSection) ||
            !groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection)(curriculumSubSection)._1.contains(eAndOCode))) ||
          (benchmarkValue != null && benchmarkValue.nonEmpty &&
            (groupToSelectedEsOsAndBenchmarks.isEmpty ||
              !groupToSelectedEsOsAndBenchmarks.isDefinedAt(groupIdOrNot) ||
              groupToSelectedEsOsAndBenchmarks(groupIdOrNot).isEmpty ||
              !groupToSelectedEsOsAndBenchmarks(groupIdOrNot).isDefinedAt(curriculumSection) ||
              !groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection).isDefinedAt(curriculumSubSection) ||
              !groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection)(curriculumSubSection)._2.contains(benchmarkValue)))) {
          theDiv.style.backgroundColor = eAndORowBackgroundNormalColor.getOrElse("white")
          theDiv.style.color = eAndORowForegroundNormalColor.getOrElse("grey")
          theDiv.style.borderRadius = eAndORowBorderRadius.getOrElse("0")
        }
      })


      index = index + 1
    }
  }

  private def clickOnBenchmark(): Unit = {
    val allBenchmarkRows = dom.document.getElementsByClassName("create-weekly-plans-benchmark-row")
    val nodeListSize = allBenchmarkRows.length
    var index = 0
    while (index < nodeListSize) {
      val theDiv = allBenchmarkRows(index).asInstanceOf[HTMLDivElement]

      theDiv.addEventListener("click", (e: dom.Event) => {
        val groupIdOrNot = theDiv.getAttribute("data-group-id-or-not")
        val benchmarkValue = theDiv.getAttribute("data-benchmark")
        val curriculumSection = theDiv.getAttribute("data-curriculum-section")
        val curriculumSubSection = theDiv.getAttribute("data-curriculum-subsection")

        global.console.log(s"Selected the Benchmark '$benchmarkValue'||$curriculumSection||$curriculumSubSection")
        if ((
            groupToSelectedEsOsAndBenchmarks.nonEmpty &&
              groupToSelectedEsOsAndBenchmarks.isDefinedAt(groupIdOrNot) &&
              groupToSelectedEsOsAndBenchmarks(groupIdOrNot).nonEmpty &&
              groupToSelectedEsOsAndBenchmarks(groupIdOrNot).isDefinedAt(curriculumSection) &&
              groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection).isDefinedAt(curriculumSubSection)) &&
          groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection)(curriculumSubSection)._2.contains(benchmarkValue)) {
          groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection)(curriculumSubSection)._2.remove(benchmarkValue)
          setButtonDefaults(theDiv)
        } else {
          if (!groupToSelectedEsOsAndBenchmarks.isDefinedAt(groupIdOrNot)) {
            groupToSelectedEsOsAndBenchmarks = groupToSelectedEsOsAndBenchmarks + (groupIdOrNot -> mutable.Map.empty)
          }
          if (!groupToSelectedEsOsAndBenchmarks(groupIdOrNot).isDefinedAt(curriculumSection)) {
            groupToSelectedEsOsAndBenchmarks(groupIdOrNot) = groupToSelectedEsOsAndBenchmarks(groupIdOrNot) + (curriculumSection -> mutable.Map.empty)
          }
          if (!groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection).isDefinedAt(curriculumSubSection)) {
            groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection)(curriculumSubSection) = (scala.collection.mutable.Set.empty, scala.collection.mutable.Set.empty)
          }

          groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection)(curriculumSubSection)._2.add(benchmarkValue)
          theDiv.style.backgroundColor = "#016ecd"
          theDiv.style.color = "white"
          theDiv.style.borderRadius = "7px"
        }
      })

      index = index + 1
    }
  }


}


