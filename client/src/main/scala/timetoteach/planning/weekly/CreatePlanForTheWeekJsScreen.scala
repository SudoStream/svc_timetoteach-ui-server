package timetoteach.planning.weekly

import duplicate.model.esandos._
import duplicate.model.planning._
import duplicate.model.{CurriculumLevel, EarlyLevel}
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.ext.Ajax.InputData
import org.scalajs.dom.html.{Div, LI, UList}
import org.scalajs.dom.raw._
import org.scalajs.dom.svg.SVG
import scalatags.JsDom
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all.{`class`, attr, div, _}
import shared.util.PlanningHelper
import timetoteach.planning.weekly.WeeklyPlanningJsScreen.groupIdsToName
import upickle.default.write

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.scalajs.js
import scala.scalajs.js.Dynamic
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object CreatePlanForTheWeekJsScreen extends WeeklyPlansCommon {

  global.toString

  private var groupToSelectedEsOsAndBenchmarks: scala.collection.mutable.Map[String, scala.collection.mutable.Map[String, scala.collection.mutable.Map[String,
    (scala.collection.mutable.Set[String], scala.collection.mutable.Set[String])]]] = scala.collection.mutable.Map.empty
  private var groupToCompletedEsOsAndBenchmarks: scala.collection.mutable.Map[String, scala.collection.mutable.Map[String, scala.collection.mutable.Map[String,
    (scala.collection.mutable.Set[String], scala.collection.mutable.Set[String])]]] = scala.collection.mutable.Map.empty
  private var groupToNotStartedEsOsAndBenchmarks: scala.collection.mutable.Map[String, scala.collection.mutable.Map[String, scala.collection.mutable.Map[String,
    (scala.collection.mutable.Set[String], scala.collection.mutable.Set[String])]]] = scala.collection.mutable.Map.empty

  private var currentlySelectedPlanningAreaNice: Option[String] = None
  private var currentlySelectedLessonSummariesThisWeek: Option[List[LessonSummary]] = None


  def loadJavascript(): Unit = {
    Dynamic.global.console.log("Loading Create Plan For The Week Javascript")
    setMondayDateToCurrentlySelectedWeek()
    clickingAMondayWeekButtonUpdatesDates()
    mouseoverHighlightEandOsAndBenchmarks()
    clickOnEandO()
    clickOnBenchmark()
    planLessonsButton()
    deleteSingleRowFromClassPlan()
    resetValuesOnTabClick()
    saveSubjectWeeksPlanButton()
    saveEsOsBenchiesButton()
    addTickToSavedLessons()
    resetAllValuesToSaved()
    backToWeeklyViewButton()
  }

  private def setEsOsBenchmarksSummary(): Unit = {
    Dynamic.global.console.log(s"setEsOsBenchmarksSummary ... ${maybeSelectedPlanningArea.getOrElse("NOTHING_THERE")}")

    val esOsBenchSummariesDiv = dom.document.getElementById("es-and-os-and-benchmarks-summary").asInstanceOf[HTMLDivElement]
    while (esOsBenchSummariesDiv.firstChild != null) {
      esOsBenchSummariesDiv.removeChild(esOsBenchSummariesDiv.firstChild)
    }

    val subjectAndGroupKeys = groupToSelectedEsOsAndBenchmarks.keySet.filter {
      key =>
        val subjectAndGroupId = key.split("___")
        subjectAndGroupId(0) == maybeSelectedPlanningArea.getOrElse("NOTHING_THERE")
    }

    Dynamic.global.console.log(s"subjectAndGroupkeys  : $subjectAndGroupKeys")

  }

  private def getDateOfWeekAsNumber(date: String): Int = {
    new js.Date(date).getDay()
  }

  private def getDayOfWeekAsNumber(day: String): Int = {
    day.toUpperCase match {
      case "MONDAY" => 1
      case "TUESDAY" => 2
      case "WEDNESDAY" => 3
      case "THURSDAY" => 4
      case "FRIDAY" => 5
      case _ => 0
    }
  }


  def createTabbedWeeklyPlan(lessonSummary: LessonSummary, index: Int): JsDom.TypedTag[LI] = {
    val anchorClasses = if (index == 0) "nav-link active" else "nav-link"

    li(`class` := "nav-item")(
      a(`class` := anchorClasses,
        href := s"#tabbed-weekly-plans-tab-content_$index",
        id := s"tabbed-weekly-plans-tab-$index",
        attr("data-toggle") := "tab",
        attr("role") := "tab"
      )(
        lessonSummary.dayOfWeek.toLowerCase.capitalize,
        br,
        span(`class` := "text-muted")(
          small(s"(${lessonSummary.startTimeIso}-${lessonSummary.endTimeIso})"))
      )
    )
  }


  private def createTabbedContent(): JsDom.TypedTag[Div] = {
    val innerTabbedContent = for {
      (lessonSummary, index) <- currentlySelectedLessonSummariesThisWeek.getOrElse(Nil).zipWithIndex
      divClasses = if (index == 0) "tab-pane fade show active" else "tab-pane fade"
    } yield div(`class` := divClasses, id := s"tabbed-weekly-plans-tab-content_$index", attr("role") := "tabpanel")(
      createLessonDataDiv(lessonSummary, index),
      createAddButton("create-weekly-plans-add-to-lesson-button-add-activity", "Activity", index, lessonSummary),
      createAddButton("create-weekly-plans-add-to-lesson-button-add-resource", "Resource", index, lessonSummary),
      createAddButton("create-weekly-plans-add-to-lesson-button-add-learning-intention", "Learning Intention", index, lessonSummary),
      createAddButton("create-weekly-plans-add-to-lesson-button-add-success-criteria", "Success Criteria", index, lessonSummary),
      createAddButton("create-weekly-plans-add-to-lesson-button-add-plenary", "Plenary", index, lessonSummary),
      createAddButton("create-weekly-plans-add-to-lesson-button-add-formative-assessment", "Formative Assessment", index, lessonSummary),
      createAddButton("create-weekly-plans-add-to-lesson-button-add-note", "Note", index, lessonSummary)
    )

    div(`class` := "tab-content the-weekly-plans-modal-content-section")(
      innerTabbedContent
    )
  }

  private def buildTabbedWeekOfPlanning(lessonsThisWeek: LessonsThisWeek, planningArea: String): Unit = {
    val tabbedWeeklyPlansDiv = dom.document.getElementById("create-weekly-plans-modal-body").asInstanceOf[HTMLDivElement]
    while (tabbedWeeklyPlansDiv.hasChildNodes()) {
      tabbedWeeklyPlansDiv.removeChild(tabbedWeeklyPlansDiv.lastChild)
    }

    var count = 0
    val innerTabbedWeeklyPlansLIs = for (
      (lessonSummary, index) <- currentlySelectedLessonSummariesThisWeek.getOrElse(Nil).zipWithIndex
    ) yield createTabbedWeeklyPlan(lessonSummary, index)

    val tabbedWeeklyPlans: JsDom.TypedTag[UList] = ul(`class` := "nav nav-tabs")(innerTabbedWeeklyPlansLIs)
    val tabbedContent: JsDom.TypedTag[Div] = createTabbedContent()
    val tabsAndContent: JsDom.TypedTag[Div] = div(tabbedWeeklyPlans, tabbedContent)

    val child = dom.document.createElement("div")
    child.innerHTML = tabsAndContent.toString()
    dom.document.getElementById("create-weekly-plans-modal-body").appendChild(child)
  }

  def createSvgTick(): TypedTag[SVG] = {
    import scalatags.JsDom.TypedTag
    import scalatags.JsDom.svgTags.{svg, use}

    val tick: TypedTag[SVG] = svg(attr("version") := "1.1", `width` := "16", attr("viewbox") := "0 0 12 16",
      attr("class") := "octicon octicon-check float-right the-lesson-tick-done")(use(attr("xlink:href") := "#check")
    )

    tick
  }

  def subjectHasLessonsSaved(subjectName: String): Boolean = {
    val fullWeeklyPlanOfLessonsPickled = dom.window.localStorage.getItem("fullWeeklyPlanOfLessonsPickled")
    import upickle.default._
    val fullWeeklyPlanOfLessons: FullWeeklyPlanOfLessons = read[FullWeeklyPlanOfLessons](PlanningHelper.decodeAnyNonFriendlyCharacters(fullWeeklyPlanOfLessonsPickled))
    if (fullWeeklyPlanOfLessons.subjectToWeeklyPlanOfSubject.isDefinedAt(subjectName)) true else false
  }

  def addTickToSavedLessons(): Unit = {
    val weeklyTopLevelTabs = dom.document.getElementsByClassName("weekly-plans-top-level-tab")
    val nodeListSize = weeklyTopLevelTabs.length
    var index = 0
    while (index < nodeListSize) {
      val tabElement = weeklyTopLevelTabs(index).asInstanceOf[HTMLElement]
      val subjectName = tabElement.getAttribute("data-subject-area")
      if (subjectHasLessonsSaved(subjectName)) {
        val child = dom.document.createElement("span")
        child.innerHTML = span(`class` := "create-weekly-plans-subject-done-check-green")(createSvgTick()).toString()
        tabElement.appendChild(child)
      }

      index = index + 1
    }
  }

  def buttonElementClassType(buttonType: String): String = {
    buttonType match {
      case "Activity" => "create-weekly-plans-add-to-lesson-button-add-activity"
      case "Resource" => "create-weekly-plans-add-to-lesson-button-add-resource"
      case "Learning Intention" => "create-weekly-plans-add-to-lesson-button-add-learning-intention"
      case "Success Criteria" => "create-weekly-plans-add-to-lesson-button-add-success-criteria"
      case "Plenary" => "create-weekly-plans-add-to-lesson-button-add-plenary"
      case "Formative Assessment" => "create-weekly-plans-add-to-lesson-button-add-formative-assessment"
      case "Note" => "create-weekly-plans-add-to-lesson-button-add-note"
      case _ => ""
    }
  }

  override def getGroupIdsToName : scala.collection.mutable.Map[String, String] = {
    Dynamic.global.console.log(s"!!!!!!!!!!!!!!!! Groups for subject built from Create Plan : ${groupIdsToName.toString}")
    groupIdsToName
  }


  private def saveEsOsBenchiesButton(): Unit = {
    val saveEsOsBenchiesBtn = dom.document.getElementsByClassName("create-weekly-plans-save-esosbenchies-button")
    val nodeListSize = saveEsOsBenchiesBtn.length
    var index = 0
    while (index < nodeListSize) {
      val buttonElement = saveEsOsBenchiesBtn(index).asInstanceOf[HTMLButtonElement]

      buttonElement.addEventListener("click", (e: dom.Event) => {
        val subject = maybeSelectedPlanningArea.getOrElse("NO_SUBJECT")
        val classId = dom.window.localStorage.getItem("classId")
        val tttUserId = dom.window.localStorage.getItem("tttUserId")
        val weekBeginningIsoDate = currentlySelectMondayStartOfWeekDate.getOrElse("1970-01-01")
        val groupToEsOsBenchmarks: Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel] = populateGroupToEsOsBenchmarks()
        Dynamic.global.console.log(s"groupToEsOsBenchmarks == ${groupToEsOsBenchmarks.toString}")
        postSaveEsOsBenchies(
          WeeklyPlanOfOneSubject(
            tttUserId,
            classId,
            subject,
            weekBeginningIsoDate,
            groupToEsOsBenchmarks,
            Nil
          ),
          classId
        )
      })

      index = index + 1
    }
  }

  private def planLessonsButton(): Unit = {
    val allPlanLessonsButtons = dom.document.getElementsByClassName("create-weekly-plans-plan-lessons-button")
    val nodeListSize = allPlanLessonsButtons.length
    var index = 0
    while (index < nodeListSize) {
      val buttonElement = allPlanLessonsButtons(index).asInstanceOf[HTMLButtonElement]

      buttonElement.addEventListener("click", (e: dom.Event) => {
        maybeSelectedPlanningArea = None
        currentlySelectedPlanningAreaNice = None
        currentlySelectedLessonSummariesThisWeek = None

        val planningArea = buttonElement.getAttribute("data-planning-area")
        val planningAreaNice = buttonElement.getAttribute("data-planning-area-nice")

        maybeSelectedPlanningArea = Some(planningArea)
        currentlySelectedPlanningAreaNice = Some(planningAreaNice)

        dom.document.getElementById("create-weekly-plans-lesson-subject-name").innerHTML = currentlySelectedPlanningAreaNice.getOrElse("")
        dom.document.getElementById("create-weekly-plans-lesson-subject-name").setAttribute("data-subject-name", maybeSelectedPlanningArea.getOrElse(""))

        dom.document.getElementById("create-weekly-plans-lesson-modal-week-of").innerHTML = currentlySelectMondayStartOfWeekDate.getOrElse("")

        val classId = dom.window.localStorage.getItem("classId")
        val tttUserId = dom.window.localStorage.getItem("tttUserId")
        val lessonsThisWeekPickled = dom.window.localStorage.getItem("lessonsThisWeekPickled")

        import upickle.default._
        val lessonsThisWeek: LessonsThisWeek = read[LessonsThisWeek](PlanningHelper.decodeAnyNonFriendlyCharacters(lessonsThisWeekPickled))
        if (lessonsThisWeek.subjectToLessons.isDefinedAt(planningArea)) {
          currentlySelectedLessonSummariesThisWeek = Some(lessonsThisWeek.subjectToLessons(planningArea))
        }

        setEsOsBenchmarksSummary()
        buildTabbedWeekOfPlanning(lessonsThisWeek, planningArea)
        cleanupModalAdds()
        clickingOnAddToLessonsButtons()
        addLessonPlanDetailsFromSavedStatus()
        val $ = js.Dynamic.global.$
        $("#create-weekly-plans-lesson-modal").modal("show", "backdrop: static", "keyboard : false")
      })

      index = index + 1
    }
  }

  private def setButtonComplete(theDiv: HTMLDivElement): Unit = {
    theDiv.style.backgroundColor = "#e0dcff"
    theDiv.style.color = "#5060b0"
    theDiv.style.borderRadius = "7px"
  }

  private def setButtonStarted(theDiv: HTMLDivElement): Unit = {
    theDiv.style.backgroundColor = "#016dad"
    theDiv.style.color = "white"
    theDiv.style.borderRadius = "7px"
  }

  private def setButtonDefaults(theDiv: HTMLDivElement): Unit = {
    theDiv.style.backgroundColor = ""
    theDiv.style.color = "grey"
    theDiv.style.borderRadius = "0px"
  }

  def statusIs(theDiv: Div, statusCheck: String): Boolean = {
    val statusSpans = theDiv.getElementsByClassName("e-and-o-or-benchmark-status")
    val nodeListSize = statusSpans.length

    if (nodeListSize != 1) {
      false
    } else {
      val statusSpan = statusSpans(0).asInstanceOf[HTMLSpanElement]
      val status = statusSpan.innerHTML
      if (status == statusCheck) {
        true
      } else {
        false
      }
    }
  }

  def setStatusBadge(theDiv: Div, status: String, badge: String): Unit = {
    val statusSpans = theDiv.getElementsByClassName("e-and-o-or-benchmark-status")
    val nodeListSize = statusSpans.length
    if (nodeListSize == 1) {
      val statusSpan = statusSpans(0).asInstanceOf[HTMLSpanElement]
      Dynamic.global.console.log(s"status: $status, badge: $badge, currentval = '${statusSpan.innerHTML}'")
      statusSpan.classList.remove("badge-danger")
      statusSpan.classList.remove("badge-warning")
      statusSpan.classList.remove("badge-success")
      statusSpan.classList.add(badge)
      statusSpan.innerHTML = status
    }
  }

  private def safelyRemoveCodeFromGroup(
                                         group: scala.collection.mutable.Map[String, scala.collection.mutable.Map[String, scala.collection.mutable.Map[String,
                                           (scala.collection.mutable.Set[String], scala.collection.mutable.Set[String])]]],
                                         groupIdOrNot: String,
                                         curriculumSection: String,
                                         curriculumSubSection: String,
                                         code: String,
                                         eAndOs: Boolean
                                       ): Unit = {
    if (eAndOs) {
      if ((
        group.nonEmpty &&
          group.isDefinedAt(groupIdOrNot) &&
          group(groupIdOrNot).nonEmpty &&
          group(groupIdOrNot).isDefinedAt(curriculumSection) &&
          group(groupIdOrNot)(curriculumSection).isDefinedAt(curriculumSubSection)) &&
        group(groupIdOrNot)(curriculumSection)(curriculumSubSection)._1.contains(code)) {

        group(groupIdOrNot)(curriculumSection)(curriculumSubSection)._1.remove(code)
      }
    } else {
      if ((
        group.nonEmpty &&
          group.isDefinedAt(groupIdOrNot) &&
          group(groupIdOrNot).nonEmpty &&
          group(groupIdOrNot).isDefinedAt(curriculumSection) &&
          group(groupIdOrNot)(curriculumSection).isDefinedAt(curriculumSubSection)) &&
        group(groupIdOrNot)(curriculumSection)(curriculumSubSection)._2.contains(code)) {

        group(groupIdOrNot)(curriculumSection)(curriculumSubSection)._2.remove(code)
      }
    }
  }


  private def safelyAddCodeToGroup(
                                    group: scala.collection.mutable.Map[String, scala.collection.mutable.Map[String, scala.collection.mutable.Map[String,
                                      (scala.collection.mutable.Set[String], scala.collection.mutable.Set[String])]]],
                                    groupIdOrNot: String,
                                    curriculumSection: String,
                                    curriculumSubSection: String,
                                    code: String,
                                    eAndOs: Boolean
                                  ): scala.collection.mutable.Map[String, scala.collection.mutable.Map[String, scala.collection.mutable.Map[String,
    (scala.collection.mutable.Set[String], scala.collection.mutable.Set[String])]]] = {

    var newGroup: scala.collection.mutable.Map[String, scala.collection.mutable.Map[String, scala.collection.mutable.Map[String,
      (scala.collection.mutable.Set[String], scala.collection.mutable.Set[String])]]] = if (!group.isDefinedAt(groupIdOrNot)) {
      group + (groupIdOrNot -> mutable.Map.empty)
    } else group

    if (!newGroup(groupIdOrNot).isDefinedAt(curriculumSection)) {
      newGroup(groupIdOrNot) = newGroup(groupIdOrNot) + (curriculumSection -> mutable.Map.empty)
    }
    if (!newGroup(groupIdOrNot)(curriculumSection).isDefinedAt(curriculumSubSection)) {
      newGroup(groupIdOrNot)(curriculumSection)(curriculumSubSection) = (scala.collection.mutable.Set.empty, scala.collection.mutable.Set.empty)
    }

    if (eAndOs) {
      newGroup(groupIdOrNot)(curriculumSection)(curriculumSubSection)._1.remove(code)
      newGroup(groupIdOrNot)(curriculumSection)(curriculumSubSection)._1.add(code)
    } else {
      newGroup(groupIdOrNot)(curriculumSection)(curriculumSubSection)._2.remove(code)
      newGroup(groupIdOrNot)(curriculumSection)(curriculumSubSection)._2.add(code)
    }

    newGroup
  }

  def safelyCheckCode(
                       group: scala.collection.mutable.Map[String, scala.collection.mutable.Map[String, scala.collection.mutable.Map[String,
                         (scala.collection.mutable.Set[String], scala.collection.mutable.Set[String])]]],
                       groupIdOrNot: String,
                       curriculumSection: String,
                       curriculumSubSection: String,
                       code: String,
                       eAndOs: Boolean
                     ): Boolean = {

    if (group.nonEmpty &&
      group.isDefinedAt(groupIdOrNot) &&
      group(groupIdOrNot).nonEmpty &&
      group(groupIdOrNot).isDefinedAt(curriculumSection) &&
      group(groupIdOrNot)(curriculumSection).isDefinedAt(curriculumSubSection)) {

      if (eAndOs) {
        group(groupIdOrNot)(curriculumSection)(curriculumSubSection)._1.contains(code)
      } else {
        group(groupIdOrNot)(curriculumSection)(curriculumSubSection)._2.contains(code)
      }
    } else {
      false
    }
  }

  private def clickOnEandO_or_Benchmark(isEAndO: Boolean): Unit = {
    val className = if (isEAndO) "create-weekly-plans-es-and-os-row" else "create-weekly-plans-benchmark-row"
    val allEAndORows = dom.document.getElementsByClassName(className)
    val nodeListSize = allEAndORows.length
    var index = 0
    while (index < nodeListSize) {
      val theDiv = allEAndORows(index).asInstanceOf[HTMLDivElement]

      theDiv.addEventListener("click", (e: dom.Event) => {
        val groupIdOrNot = theDiv.getAttribute("data-group-id-or-not")
        val eAndOCode_or_Benchmark = if (isEAndO) theDiv.getAttribute("data-eando-code") else theDiv.getAttribute("data-benchmark")
        val curriculumSection = theDiv.getAttribute("data-curriculum-section")
        val curriculumSubSection = theDiv.getAttribute("data-curriculum-subsection")

        Dynamic.global.console.log(s"Selected E and O code '$eAndOCode_or_Benchmark'")

        if (statusIs(theDiv, "Started")) {
          setButtonComplete(theDiv)
          setStatusBadge(theDiv, "Complete", "badge-success")
          safelyRemoveCodeFromGroup(groupToSelectedEsOsAndBenchmarks, groupIdOrNot, curriculumSection, curriculumSubSection, eAndOCode_or_Benchmark, isEAndO)
          safelyRemoveCodeFromGroup(groupToNotStartedEsOsAndBenchmarks, groupIdOrNot, curriculumSection, curriculumSubSection, eAndOCode_or_Benchmark, isEAndO)
          groupToCompletedEsOsAndBenchmarks = safelyAddCodeToGroup(groupToCompletedEsOsAndBenchmarks, groupIdOrNot, curriculumSection, curriculumSubSection, eAndOCode_or_Benchmark, isEAndO)
        } else if (statusIs(theDiv, "Complete")) {
          setButtonDefaults(theDiv)
          setStatusBadge(theDiv, "Not Started", "badge-danger")
          safelyRemoveCodeFromGroup(groupToSelectedEsOsAndBenchmarks, groupIdOrNot, curriculumSection, curriculumSubSection, eAndOCode_or_Benchmark, isEAndO)
          safelyRemoveCodeFromGroup(groupToCompletedEsOsAndBenchmarks, groupIdOrNot, curriculumSection, curriculumSubSection, eAndOCode_or_Benchmark, isEAndO)
          groupToNotStartedEsOsAndBenchmarks = safelyAddCodeToGroup(groupToNotStartedEsOsAndBenchmarks, groupIdOrNot, curriculumSection, curriculumSubSection, eAndOCode_or_Benchmark, isEAndO)
        } else if (statusIs(theDiv, "Not Started")) {
          if (isEAndO) {
            selectEAndOCode(groupIdOrNot, eAndOCode_or_Benchmark, curriculumSection, curriculumSubSection)
          } else {
            selectBenchmark(groupIdOrNot, eAndOCode_or_Benchmark, curriculumSection, curriculumSubSection)
          }
          setStatusBadge(theDiv, "Started", "badge-warning")
          setButtonStarted(theDiv)
        } else {
          Dynamic.global.console.log(s"Unknown status.")
        }

        Dynamic.global.console.log(s"[][][][][][][] Current Not started ${groupToNotStartedEsOsAndBenchmarks.toString()}")
        Dynamic.global.console.log(s"[][][][][][][] Current Selected ${groupToSelectedEsOsAndBenchmarks.toString()}")
        Dynamic.global.console.log(s"[][][][][][][] Current Complete ${groupToCompletedEsOsAndBenchmarks.toString()}")
      })

      index = index + 1
    }
  }

  private def selectEAndOCode(groupIdOrNot: String, eAndOCode: String, curriculumSection: String, curriculumSubSection: String): Unit = {
    safelyRemoveCodeFromGroup(groupToNotStartedEsOsAndBenchmarks, groupIdOrNot, curriculumSection, curriculumSubSection, eAndOCode, true)
    safelyRemoveCodeFromGroup(groupToCompletedEsOsAndBenchmarks, groupIdOrNot, curriculumSection, curriculumSubSection, eAndOCode, true)

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
  }

  private def repaintTheEsAndOs(className: String): Unit = {
    repaintSelectedEsOsAndBenchies(className, true)
  }

  private def repaintTheBenchmarks(className: String): Unit = {
    repaintSelectedEsOsAndBenchies(className, false)
  }

  private def repaintSelectedEsOsAndBenchies(className: String, isEsAndOs: Boolean): Unit = {

    def checkCodeIsSelected(groupIdOrNot: String,
                            curriculumSection: String,
                            curriculumSubSection: String,
                            code: String
                           ): Boolean = {
      if (code == null || code == "" || code == "null") {
        false
      }
      if (isEsAndOs) {
        groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection)(curriculumSubSection)._1.contains(code)
      } else {
        groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection)(curriculumSubSection)._2.contains(code)
      }
    }

    val allEAndOAndBenchmarksRows = dom.document.getElementsByClassName(className)
    val nodeListSize = allEAndOAndBenchmarksRows.length
    var index = 0
    while (index < nodeListSize) {
      val theDiv = allEAndOAndBenchmarksRows(index).asInstanceOf[HTMLDivElement]

      val groupIdOrNot = theDiv.getAttribute("data-group-id-or-not")
      val codeToCheck = if (isEsAndOs) theDiv.getAttribute("data-eando-code") else theDiv.getAttribute("data-benchmark")
      val curriculumSection = theDiv.getAttribute("data-curriculum-section")
      val curriculumSubSection = theDiv.getAttribute("data-curriculum-subsection")

      if (
        (groupToSelectedEsOsAndBenchmarks.isDefinedAt(groupIdOrNot) &&
          groupToSelectedEsOsAndBenchmarks(groupIdOrNot).isDefinedAt(curriculumSection) &&
          groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection).isDefinedAt(curriculumSubSection) &&
          checkCodeIsSelected(groupIdOrNot, curriculumSection, curriculumSubSection, codeToCheck)
          ) ||
          statusIs(theDiv, "Started")
      ) {
        setButtonStarted(theDiv)
        setStatusBadge(theDiv, "Started", "badge-warning")
      } else if (statusIs(theDiv, "Complete")) {
        setButtonComplete(theDiv)
      } else {
        setButtonDefaults(theDiv)
      }

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
        theDiv.style.backgroundColor = "grey"
        theDiv.style.color = "white"
        theDiv.style.borderRadius = "7px"
      })

      theDiv.addEventListener("mouseleave", (e: dom.Event) => {
        val statusSpans = theDiv.getElementsByClassName("e-and-o-or-benchmark-status")
        val nodeListSize = statusSpans.length
        if (nodeListSize == 1) {
          val statusSpan = statusSpans(0).asInstanceOf[HTMLSpanElement]
          if (statusSpan.innerHTML == "Complete") {
            setButtonComplete(theDiv)
          } else if (statusSpan.innerHTML == "Started") {
            setButtonStarted(theDiv)
          } else {
            setButtonDefaults(theDiv)
          }
        }
      })

      index = index + 1
    }
  }

  private def clickOnEandO(): Unit = {
    clickOnEandO_or_Benchmark(true)
  }

  private def clickOnBenchmark(): Unit = {
    clickOnEandO_or_Benchmark(false)
  }

  private def selectBenchmark(groupIdOrNot: String, benchmarkValue: String, curriculumSection: String, curriculumSubSection: String): Boolean = {
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
  }



  private def isActive(element: HTMLElement): Boolean = element.classList.contains("active")

  private def getSelectedTab: Option[HTMLElement] = {
    val weeklyTopLevelTabs = dom.document.getElementsByClassName("weekly-plans-top-level-tab")
    val nodeListSize = weeklyTopLevelTabs.length
    var index = 0
    while (index < nodeListSize) {
      val tabElement: HTMLElement = weeklyTopLevelTabs(index).asInstanceOf[HTMLElement]
      if (isActive(tabElement)) {
        return Some(tabElement)
      }
      index = index + 1
    }
    None
  }

  private def buildGroupsMapForTabSelected(): Unit = {
    groupIdsToName.clear()
    val weeklyTopLevelTabs = dom.document.getElementsByClassName("weekly-plans-top-level-tab")
    val nodeListSize = weeklyTopLevelTabs.length
    var index = 0
    while (index < nodeListSize) {
      val tabElement = weeklyTopLevelTabs(index).asInstanceOf[HTMLElement]
      if (isActive(tabElement)) {
        val subject = tabElement.getAttribute("data-subject-area")
        val tabsWithGroupIds = dom.document.getElementsByClassName("tab-with-group-id")
        val theNodeListSize = tabsWithGroupIds.length
        var tabIndex = 0
        while (tabIndex < theNodeListSize) {
          val tabWithGroupIds = tabsWithGroupIds(tabIndex).asInstanceOf[HTMLElement]
          val tabSubject = tabWithGroupIds.getAttribute("data-tab-is-from-subject")
          if (subject == tabSubject) {
            val groupId = tabWithGroupIds.getAttribute("data-group-id")
            val groupName = tabWithGroupIds.getAttribute("data-group-name")
            groupIdsToName = groupIdsToName + (groupId -> groupName)
          }
          tabIndex = tabIndex + 1
        }
      }
      index = index + 1
    }
  }

  private def populateSelectedEsOsAndBenchmarksFromSaved(): Unit = {
    val fullWeeklyPlanOfLessonsPickled = dom.window.localStorage.getItem("fullWeeklyPlanOfLessonsPickled")
    import upickle.default._
    val fullWeeklyPlanOfLessons: FullWeeklyPlanOfLessons = read[FullWeeklyPlanOfLessons](PlanningHelper.decodeAnyNonFriendlyCharacters(fullWeeklyPlanOfLessonsPickled))

    val weeklyTopLevelTabs = dom.document.getElementsByClassName("weekly-plans-top-level-tab")
    val nodeListSize = weeklyTopLevelTabs.length
    var index = 0
    while (index < nodeListSize) {
      val tabElement = weeklyTopLevelTabs(index).asInstanceOf[HTMLElement]
      val subjectName = tabElement.getAttribute("data-subject-area")
      if (fullWeeklyPlanOfLessons.subjectToWeeklyPlanOfSubject.isDefinedAt(subjectName)) {
        val weeklyPlanOfSubject = fullWeeklyPlanOfLessons.subjectToWeeklyPlanOfSubject(subjectName)
        for (groupId <- weeklyPlanOfSubject.groupToEsOsBenchmarks.keys) {
          val groupIdWithSubject = s"${subjectName}___$groupId"
          Dynamic.global.console.log(s"populateSelectedEsOsAndBenchmarksFromSaved -> groupId: $groupIdWithSubject")
          val esOsAndBenchies = weeklyPlanOfSubject.groupToEsOsBenchmarks(groupId)
          for (section <- esOsAndBenchies.setSectionNameToSubSections.keys) {
            val subSectionToEsOsBenchies = esOsAndBenchies.setSectionNameToSubSections(section)
            for (subsection <- subSectionToEsOsBenchies.keys) {
              val eAndOSecSubection = subSectionToEsOsBenchies(subsection)
              for (eAndO <- eAndOSecSubection.eAndOs) {
                Dynamic.global.console.log(s"populateSelectedEsOsAndBenchmarksFromSaved() Adding ${eAndO.code}")
                selectEAndOCode(groupIdWithSubject, eAndO.code, section, subsection)
              }
              for (benchmark <- eAndOSecSubection.benchmarks) {
                selectBenchmark(groupIdWithSubject, benchmark.value, section, subsection)
              }
            }
          }
        }
      }

      index = index + 1
    }
  }


  private def populateCompletedEsOsFromSaved(): Unit = {
    val completedEsAndOsBenchmarksPickled = dom.window.localStorage.getItem("completedEsAndOsBenchmarksPickled")
    import upickle.default._
    val completedEsOsBenchies: CompletedEsAndOsByGroupBySubject = read[CompletedEsAndOsByGroupBySubject](PlanningHelper.decodeAnyNonFriendlyCharacters(completedEsAndOsBenchmarksPickled))
    val completedEsOsBenchiesMap = completedEsOsBenchies.completedEsAndOsByGroupBySubject

    val startedEsAndOsBenchmarksPickled = dom.window.localStorage.getItem("startedEsAndOsBenchmarksPickled")
    val startedEsOsBenchies: StartedEsAndOsByGroupBySubject = read[StartedEsAndOsByGroupBySubject](PlanningHelper.decodeAnyNonFriendlyCharacters(startedEsAndOsBenchmarksPickled))
    val startedEsOsBenchiesMap = startedEsOsBenchies.startedEsAndOsByGroupBySubject

    Dynamic.global.console.log(s"completedEsOsBenchiesMap : ${completedEsOsBenchiesMap.toString()}")
    Dynamic.global.console.log(s"startedEsOsBenchiesMap : ${startedEsOsBenchiesMap.toString()}")

    val allEAndORows = dom.document.getElementsByClassName("create-weekly-plans-es-and-os-row")
    val nodeListSize = allEAndORows.length
    var index = 0
    while (index < nodeListSize) {
      val theDiv = allEAndORows(index).asInstanceOf[HTMLDivElement]

      val divSubject = theDiv.getAttribute("data-group-id-or-not").split("___")(0)
      val divGroupId = theDiv.getAttribute("data-group-id-or-not").split("___")(1)
      val divSection = theDiv.getAttribute("data-curriculum-section")
      val divSubsection = theDiv.getAttribute("data-curriculum-subsection")
      val eAndOCode = theDiv.getAttribute("data-eando-code")

      if (completedEsOsBenchiesMap.isDefinedAt(divSubject) &&
        completedEsOsBenchiesMap(divSubject).isDefinedAt(divGroupId) &&
        completedEsOsBenchiesMap(divSubject)(divGroupId).isDefinedAt(divSection) &&
        completedEsOsBenchiesMap(divSubject)(divGroupId)(divSection).isDefinedAt(divSubsection)
      ) {
        val completedEsAndOs = completedEsOsBenchiesMap(divSubject)(divGroupId)(divSection)(divSubsection).eAndOs.map(elem => elem.code)
        if (completedEsAndOs.contains(eAndOCode)) {
          setStatusBadge(theDiv, "Complete", "badge-success")
          setButtonComplete(theDiv)
        }
      }
      if (startedEsOsBenchiesMap.isDefinedAt(divSubject) &&
        startedEsOsBenchiesMap(divSubject).isDefinedAt(divGroupId) &&
        startedEsOsBenchiesMap(divSubject)(divGroupId).isDefinedAt(divSection) &&
        startedEsOsBenchiesMap(divSubject)(divGroupId)(divSection).isDefinedAt(divSubsection)
      ) {
        val startedEsAndOs = startedEsOsBenchiesMap(divSubject)(divGroupId)(divSection)(divSubsection).eAndOs.map(elem => elem.code)
        if (startedEsAndOs.contains(eAndOCode)) {
          setStatusBadge(theDiv, "Started", "badge-warning")
          setButtonStarted(theDiv)
        }
      }
      index = index + 1
    }
  }


  private def populateCompletedBenchiesFromSaved(): Unit = {
    val completedEsAndOsBenchmarksPickled = dom.window.localStorage.getItem("completedEsAndOsBenchmarksPickled")
    import upickle.default._
    val completedEsOsBenchies: CompletedEsAndOsByGroupBySubject = read[CompletedEsAndOsByGroupBySubject](PlanningHelper.decodeAnyNonFriendlyCharacters(completedEsAndOsBenchmarksPickled))
    val completedEsOsBenchiesMap = completedEsOsBenchies.completedEsAndOsByGroupBySubject

    val startedEsAndOsBenchmarksPickled = dom.window.localStorage.getItem("startedEsAndOsBenchmarksPickled")
    val startedEsOsBenchies: StartedEsAndOsByGroupBySubject = read[StartedEsAndOsByGroupBySubject](PlanningHelper.decodeAnyNonFriendlyCharacters(startedEsAndOsBenchmarksPickled))
    val startedEsOsBenchiesMap = startedEsOsBenchies.startedEsAndOsByGroupBySubject

    val allEAndORows = dom.document.getElementsByClassName("create-weekly-plans-benchmark-row")
    val nodeListSize = allEAndORows.length
    var index = 0
    while (index < nodeListSize) {
      val theDiv = allEAndORows(index).asInstanceOf[HTMLDivElement]

      val divSubject = theDiv.getAttribute("data-group-id-or-not").split("___")(0)
      val divGroupId = theDiv.getAttribute("data-group-id-or-not").split("___")(1)
      val divSection = theDiv.getAttribute("data-curriculum-section")
      val divSubsection = theDiv.getAttribute("data-curriculum-subsection")
      val benchy = theDiv.getAttribute("data-benchmark")

      if (completedEsOsBenchiesMap.isDefinedAt(divSubject) &&
        completedEsOsBenchiesMap(divSubject).isDefinedAt(divGroupId) &&
        completedEsOsBenchiesMap(divSubject)(divGroupId).isDefinedAt(divSection) &&
        completedEsOsBenchiesMap(divSubject)(divGroupId)(divSection).isDefinedAt(divSubsection)
      ) {
        val completedEsAndOs = completedEsOsBenchiesMap(divSubject)(divGroupId)(divSection)(divSubsection).benchmarks.map(elem => elem.value)

        if (completedEsAndOs.contains(benchy)) {
          setStatusBadge(theDiv, "Complete", "badge-success")
          setButtonComplete(theDiv)
        }
      }
      if (startedEsOsBenchiesMap.isDefinedAt(divSubject) &&
        startedEsOsBenchiesMap(divSubject).isDefinedAt(divGroupId) &&
        startedEsOsBenchiesMap(divSubject)(divGroupId).isDefinedAt(divSection) &&
        startedEsOsBenchiesMap(divSubject)(divGroupId)(divSection).isDefinedAt(divSubsection)
      ) {
        val startedBenchmarks = startedEsOsBenchiesMap(divSubject)(divGroupId)(divSection)(divSubsection).benchmarks.map(elem => elem.value)
        if (startedBenchmarks.contains(benchy)) {
          setStatusBadge(theDiv, "Started", "badge-warning")
          setButtonStarted(theDiv)
        }
      }
      index = index + 1
    }
  }

  private def resetValuesOnTabClick(): Unit = {
    val $ = js.Dynamic.global.$
    $("a[data-toggle=\"tab\"]").on("shown.bs.tab", (e: dom.Event) => {
      resetAllValuesToSaved()
      Dynamic.global.console.log(s"The groups are ... ${groupIdsToName.toString()}")
    })
  }

  private def resetAllValuesToSaved(): Unit = {
    groupToSelectedEsOsAndBenchmarks.clear()
    groupToCompletedEsOsAndBenchmarks.clear()
    groupToNotStartedEsOsAndBenchmarks.clear()
    populateSelectedEsOsAndBenchmarksFromSaved()
    populateCompletedEsOsFromSaved()
    populateCompletedBenchiesFromSaved()
    repaintTheEsAndOs("create-weekly-plans-es-and-os-row")
    repaintTheBenchmarks("create-weekly-plans-benchmark-row")

    buildGroupsMapForTabSelected()
    maybeSelectedPlanningArea = None
    currentlySelectedPlanningAreaNice = None
    currentlySelectedLessonSummariesThisWeek = None

    val maybeSelectedTab = getSelectedTab
    Dynamic.global.console.log(s"Selected tab = ${maybeSelectedTab.toString}")
    maybeSelectedTab match {
      case Some(element) =>
        maybeSelectedPlanningArea = Some(element.getAttribute("data-subject-area"))
      case None =>
    }

    setEsOsBenchmarksSummary()
  }

  private def createEandOSetSubSection(esAndOsPlusBenchmarksForSubsection: (mutable.Set[String], mutable.Set[String])): EandOSetSubSection = {
    val esAndOs: List[EandO] = if (esAndOsPlusBenchmarksForSubsection._1.isEmpty) Nil else {
      for {
        esAndOsCode <- esAndOsPlusBenchmarksForSubsection._1
      } yield EandO(esAndOsCode, Nil)
    }.toList

    val benchmarks: List[Benchmark] = if (esAndOsPlusBenchmarksForSubsection._2.isEmpty) Nil else {
      for {
        benchmark <- esAndOsPlusBenchmarksForSubsection._2
      } yield Benchmark(benchmark)
    }.toList

    EandOSetSubSection(
      "",
      esAndOs,
      benchmarks
    )
  }

  private def createSetSectionNameToSubSectionsMap(sectionName: String, subsectionToEsAndOs:
  mutable.Map[String, (mutable.Set[String], mutable.Set[String])]): Map[String, Map[String, EandOSetSubSection]] = {
    val mutableMap: mutable.Map[String, EandOSetSubSection] = mutable.Map.empty

    if (subsectionToEsAndOs.isEmpty) Map() else {

      for (subsectionName <- subsectionToEsAndOs.keys) {
        if (subsectionToEsAndOs.isDefinedAt(subsectionName)) {
          val esAndOsPlusBenchmarksForSubsection = subsectionToEsAndOs(subsectionName)
          val esOsSetSubSection: EandOSetSubSection = createEandOSetSubSection(esAndOsPlusBenchmarksForSubsection)
          mutableMap += (subsectionName -> esOsSetSubSection)
        }
      }

      if (mutableMap.isEmpty) Map() else Map(sectionName -> mutableMap.toMap)
    }

  }

  private def extractCurriculumLevel(eAndO: EandO): CurriculumLevel = {
    CurriculumLevel.createCurriculumLevelFromEAndOCode(eAndO.code)
  }

  private def extractCurriculumLevel(setSectionNameToSubSectionsMap: Map[String, Map[String, EandOSetSubSection]]): CurriculumLevel = {
    if (setSectionNameToSubSectionsMap.isEmpty) EarlyLevel else {
      val levels = for {
        sectionName <- setSectionNameToSubSectionsMap.keys
        subsectionName <- setSectionNameToSubSectionsMap(sectionName).keys
        eAndOCodes = setSectionNameToSubSectionsMap(sectionName)(subsectionName).eAndOs
        headCode = if (eAndOCodes.nonEmpty) eAndOCodes.head else EandO("NOPE", Nil)
        level = extractCurriculumLevel(headCode)
      } yield level

      levels.head
    }
  }

  private def convertStringsToListEandO(strings: List[String]): List[EandO] = {
    strings.map(elem => EandO(elem, Nil))
  }

  private def convertStringsToListBenchmark(strings: List[String]): List[Benchmark] = {
    strings.map(elem => Benchmark(elem))
  }

  private def convertToImmutableMapEsOsBenchies(
                                                 mutableMap: mutable.Map[String, (mutable.Set[String], mutable.Set[String])]
                                               ): Map[String, EandOSetSubSection] = {
    @tailrec
    def loop(remainingKeys: List[String],
             currentMap: Map[String, EandOSetSubSection]
            ): Map[String, EandOSetSubSection] = {
      if (remainingKeys.isEmpty) currentMap
      else {
        loop(
          remainingKeys.tail,
          currentMap + (remainingKeys.head ->
            EandOSetSubSection(
              "",
              convertStringsToListEandO(mutableMap(remainingKeys.head)._1.toSet.toList),
              convertStringsToListBenchmark(mutableMap(remainingKeys.head)._2.toSet.toList)
            ))
        )
      }
    }

    loop(mutableMap.keys.toList, Map())
  }

  private def buildSectionToSubsectionMap(
                                           sectionToSubsectionToEsOsAndBenchies: mutable.Map[String, mutable.Map[String, (mutable.Set[String], mutable.Set[String])]],
                                           sections: List[String]
                                         ): Map[String, Map[String, EandOSetSubSection]] = {
    @tailrec
    def loop(remainingSections: List[String],
             currentMap: Map[String, Map[String, EandOSetSubSection]]): Map[String, Map[String, EandOSetSubSection]] = {
      if (remainingSections.isEmpty) currentMap
      else {
        val nextSection = remainingSections.head
        val mutableSubSectionToEsOsBenchies = sectionToSubsectionToEsOsAndBenchies(nextSection)
        val immutableSubSectionToEsOsBenchies = convertToImmutableMapEsOsBenchies(mutableSubSectionToEsOsBenchies)
        loop(remainingSections.tail, currentMap + (nextSection -> immutableSubSectionToEsOsBenchies))
      }
    }

    loop(sections, Map())
  }

  private def populateGroupToEsOsBenchmarks(): Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel] = {

    {
      for {
        planningAreaAndGroupIdKey <- groupToSelectedEsOsAndBenchmarks.keys
        groupId = planningAreaAndGroupIdKey.split("___")(1)
        if groupToSelectedEsOsAndBenchmarks.isDefinedAt(planningAreaAndGroupIdKey)
        sectionToSubsectionMap = buildSectionToSubsectionMap(
          groupToSelectedEsOsAndBenchmarks(planningAreaAndGroupIdKey),
          groupToSelectedEsOsAndBenchmarks(planningAreaAndGroupIdKey).keys.toList
        )
        curriculumLevel: CurriculumLevel = extractCurriculumLevel(sectionToSubsectionMap)
      } yield (groupId, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel(
        curriculumLevel,
        CurriculumArea.createCurriculumAreaFromString(maybeSelectedPlanningArea.getOrElse("NO_SUBJECT")),
        sectionToSubsectionMap
      ))
    }.toMap

  }

  private def extractGroupIds(peerNode: HTMLElement): List[String] = {
    var groupIds = new ListBuffer[String]()

    val divLevelNodes = peerNode.childNodes
    val divLevelNodesSize = divLevelNodes.length
    var divLevelNodeIdx = 0
    while (divLevelNodeIdx < divLevelNodesSize) {
      val theNode = divLevelNodes(divLevelNodeIdx).asInstanceOf[HTMLElement]
      if (theNode.className.contains("create-weekly-plans-lesson-modal-select-groups")) {

        ////////////////////////////////////////////////////////////////////////////////
        val inputLevelNodes = theNode.childNodes
        val inputLevelNodesSize = inputLevelNodes.length
        var inputLevelNodeIdx = 0
        while (inputLevelNodeIdx < inputLevelNodesSize) {
          val theNode = inputLevelNodes(inputLevelNodeIdx).asInstanceOf[HTMLElement]
          if (theNode.className.contains("group-on-off")) {
            val groupInputElement = theNode.asInstanceOf[HTMLInputElement]
            if (groupInputElement.checked) {
              val groupId = groupInputElement.getAttribute("data-group-id")
              Dynamic.global.console.log(s"Adding group id $groupId")
              groupIds += groupId
            }
          }
          inputLevelNodeIdx = inputLevelNodeIdx + 1
        }
        ////////////////////////////////////////////////////////////////////////////////

      }
      divLevelNodeIdx = divLevelNodeIdx + 1
    }

    groupIds.toList
  }

  private def extractLessonAttributes(attributeType: String, tabIndexToLookFor: Int): List[AttributeRowKey] = {
    var lessonAttributes = new ListBuffer[AttributeRowKey]()

    val inputAttributes = dom.document.getElementsByClassName(s"input-attribute-${attributeType.replace(" ", "")}")
    val nodeListSize = inputAttributes.length
    var nodeIndex = 0
    while (nodeIndex < nodeListSize) {
      val inputLessonAttributes = inputAttributes(nodeIndex).asInstanceOf[HTMLInputElement]
      val tabIndex = inputLessonAttributes.getAttribute("data-tab-index").toInt
      if (tabIndex == tabIndexToLookFor) {
        val attributeText = inputLessonAttributes.value
        val attributeOrderValue = inputLessonAttributes.getAttribute("data-attribute-order-value").toInt
        lessonAttributes += AttributeRowKey(attributeText, attributeOrderValue)
      }
      nodeIndex = nodeIndex + 1
    }

    lessonAttributes.toList
  }

  private def extractLessonAttributesWithGroups(attributeType: String, tabIndexToLookFor: Int): Map[AttributeRowKey, List[String]] = {
    var activityToGroups = scala.collection.mutable.Map[AttributeRowKey, List[String]]()

    val inputAttributes = dom.document.getElementsByClassName(s"input-attribute-${attributeType.replace(" ", "")}")
    val nodeListSize = inputAttributes.length
    var nodeIndex = 0
    while (nodeIndex < nodeListSize) {
      val inputLessonAttributes = inputAttributes(nodeIndex).asInstanceOf[HTMLInputElement]
      val tabIndex = inputLessonAttributes.getAttribute("data-tab-index").toInt
      if (tabIndex == tabIndexToLookFor) {
        val activityText = inputLessonAttributes.value
        val attributeOrderValue = inputLessonAttributes.getAttribute("data-attribute-order-value").toInt

        val peerNodes = inputLessonAttributes.parentNode.childNodes
        val peerNodeSize = peerNodes.length
        var peerNodeIndex = 0
        while (peerNodeIndex < peerNodeSize) {
          val peerNode = peerNodes(peerNodeIndex).asInstanceOf[HTMLElement]
          if (peerNode != null && peerNode.toString != "undefined" && peerNode.className.contains("form-row")) {
            val groupIds: List[String] = extractGroupIds(peerNode)
            activityToGroups += (
              AttributeRowKey(activityText, attributeOrderValue) ->
                groupIds)
          } else {
            activityToGroups += (AttributeRowKey(activityText, attributeOrderValue) -> Nil)
          }
          peerNodeIndex = peerNodeIndex + 1
        }
      }
      nodeIndex = nodeIndex + 1
    }

    Dynamic.global.console.log(s"Text Values for ${attributeType} Map == ${activityToGroups.toString}")
    activityToGroups.toMap
  }

  private def getIsoDate(weekStartIsoDate: String, dayOfWeek: String): String = {
    val jsDate = new js.Date(weekStartIsoDate)
    val date = dayOfWeek.toUpperCase match {
      case "MONDAY" => jsDate
      case "TUESDAY" =>
        jsDate.setDate(jsDate.getDate() + 1)
      case "WEDNESDAY" =>
        jsDate.setDate(jsDate.getDate() + 2)
      case "THURSDAY" =>
        jsDate.setDate(jsDate.getDate() + 3)
      case "FRIDAY" =>
        jsDate.setDate(jsDate.getDate() + 4)
      case somethingElse =>
        Dynamic.global.console.log(s"ERROR: Do not recognise day '$dayOfWeek'")
        jsDate
    }
    jsDate.toISOString().split("T")(0)
  }

  private def createOneWeekLessonSummary(weekBeginningIsoDate: String): List[LessonPlan] = {
    var lessonPlansForTheWeek = new ListBuffer[LessonPlan]()

    val lessonSummaryDivs = dom.document.getElementsByClassName("data-lesson-summary-for-lesson-div")
    val nodeListSize = lessonSummaryDivs.length
    var index = 0
    while (index < nodeListSize) {
      val lessonSummaryDiv = lessonSummaryDivs(index).asInstanceOf[HTMLDivElement]

      val tabIndex = lessonSummaryDiv.getAttribute("data-tab-index")

      val subject = lessonSummaryDiv.getAttribute("data-lesson-summary-subject")
      val subjectAdditionalInfo = lessonSummaryDiv.getAttribute("data-lesson-summary-subject-additional-info")
      val day = lessonSummaryDiv.getAttribute("data-lesson-summary-day")
      val lessonDateIso = getIsoDate(weekBeginningIsoDate, day)
      val startTime = lessonSummaryDiv.getAttribute("data-lesson-summary-start-time")
      val endTime = lessonSummaryDiv.getAttribute("data-lesson-summary-end-time")

      val activitiesPerGroup: Map[AttributeRowKey, List[String]] = extractLessonAttributesWithGroups("Activity", index)
      val resources: List[AttributeRowKey] = extractLessonAttributes("Resource", index)
      val learningIntentionsPerGroup: Map[AttributeRowKey, List[String]] = extractLessonAttributesWithGroups("LearningIntention", index)
      val successCriteriaPerGroup: Map[AttributeRowKey, List[String]] = extractLessonAttributesWithGroups("SuccessCriteria", index)
      val plenary: List[AttributeRowKey] = extractLessonAttributes("Plenary", index)
      val formativeAssessmentPerGroup: Map[AttributeRowKey, List[String]] = extractLessonAttributesWithGroups("FormativeAssessment", index)
      val notesBefore: List[AttributeRowKey] = extractLessonAttributes("Note", index)
      val notesAfter: List[AttributeRowKey] = Nil

      lessonPlansForTheWeek += LessonPlan(
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

      index = index + 1
    }

    Dynamic.global.console.log(s"Lesson plans to save ... ${lessonPlansForTheWeek.toString}")
    lessonPlansForTheWeek.toList
  }

  private def saveSubjectWeeksPlanButton(): Unit = {
    val saveSubjectWeeksPlanButton = dom.document.getElementById("create-weekly-plans-save-subject-plan")
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
        Dynamic.global.console.log(s"groupToSelectedEsOsAndBenchmarks == ${groupToSelectedEsOsAndBenchmarks.toString}")

        val groupToEsOsBenchmarks: Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel] = populateGroupToEsOsBenchmarks()
        Dynamic.global.console.log(s"groupToEsOsBenchmarks == ${groupToEsOsBenchmarks.toString}")

        val lessons: List[LessonPlan] = createOneWeekLessonSummary(weekBeginningIsoDate)
        Dynamic.global.console.log(s"lessonSummary == ${lessons.toString}")

        postSave(
          WeeklyPlanOfOneSubject(
            tttUserId,
            classId,
            subject,
            weekBeginningIsoDate,
            groupToEsOsBenchmarks,
            lessons
          ),
          classId
        )
      })
    }
  }

  private def getCurrentTabIdString(): String = {
    getSelectedTab match {
      case Some(currentTab) => s"#${currentTab.id}"
      case None => ""
    }
  }

  private def postSave(subjectWeeklyPlan: WeeklyPlanOfOneSubject, classId: String): Unit = {
    val subjectWeeklyPlansPickled = PlanningHelper.encodeAnyJawnNonFriendlyCharacters(write[WeeklyPlanOfOneSubject](subjectWeeklyPlan))
    val completedEsOsBenchiesPickled = PlanningHelper.encodeAnyJawnNonFriendlyCharacters(write[CompletedEsAndOsByGroup](createCompletedEsAndOsByGroup()))
    val notStartedEsOsBenchiesPickled = PlanningHelper.encodeAnyJawnNonFriendlyCharacters(write[NotStartedEsAndOsByGroup](createNotStartedEsAndOsByGroup()))

    Dynamic.global.console.log(s"Not Started: ${groupToNotStartedEsOsAndBenchmarks.toString()}")
    Dynamic.global.console.log(s"Selected: ${groupToSelectedEsOsAndBenchmarks.toString()}")
    Dynamic.global.console.log(s"Completed: ${groupToCompletedEsOsAndBenchmarks.toString()}")
    Dynamic.global.console.log(s"Pickled, this == $subjectWeeklyPlansPickled")
    Dynamic.global.console.log(s"Pickled Completed, this == $completedEsOsBenchiesPickled")
    Dynamic.global.console.log(s"Pickled NotStarted, this == $notStartedEsOsBenchiesPickled")

    import scala.concurrent.ExecutionContext.Implicits.global
    val theUrl = s"/savePlanForTheWeek/$classId"
    val theHeaders = Map(
      "Content-Type" -> "application/x-www-form-urlencoded",
      "X-Requested-With" -> "Accept"
    )
    val theData = InputData.str2ajax(s"subjectWeeklyPlansPickled=$subjectWeeklyPlansPickled&" +
      s"notStarted=$notStartedEsOsBenchiesPickled&completed=$completedEsOsBenchiesPickled")

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
          $("#create-weekly-plans-lesson-modal").modal("hide")
          currentlySelectMondayStartOfWeekDate match {
            case Some(mondayIsoDate) =>
              dom.window.location.href = s"/createPlanForTheWeek/$classId/$mondayIsoDate"
            case None =>
              dom.window.location.href = s"/createPlanForTheWeek/$classId"
          }

        }, 10)
      case Failure(ex) =>
        dom.window.alert("Something went wrong with saving group termly plans. Specifically : -" +
          s"\n\n${ex.toString}")
        val $ = js.Dynamic.global.$
        $("#create-weekly-plans-lesson-modal").modal("hide")
    }

  }

  private def createCompletedEsAndOsByGroup(): CompletedEsAndOsByGroup = {
    val innerMap: Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel] = if (groupToCompletedEsOsAndBenchmarks.isEmpty) Map() else {
      {
        for {
          planningAreaAndGroupIdKey <- groupToCompletedEsOsAndBenchmarks.keys
          groupId = planningAreaAndGroupIdKey.split("___")(1)

          if groupToCompletedEsOsAndBenchmarks.isDefinedAt(planningAreaAndGroupIdKey)
          sectionName <- groupToCompletedEsOsAndBenchmarks(planningAreaAndGroupIdKey).keys
          setSectionNameToSubSectionsMap = createSetSectionNameToSubSectionsMap(sectionName, groupToCompletedEsOsAndBenchmarks(planningAreaAndGroupIdKey)(sectionName))
          curriculumLevel: CurriculumLevel = extractCurriculumLevel(setSectionNameToSubSectionsMap)
        } yield (groupId, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel(
          curriculumLevel,
          CurriculumArea.createCurriculumAreaFromString(maybeSelectedPlanningArea.getOrElse("NO_SUBJECT")),
          setSectionNameToSubSectionsMap
        ))
      }.toMap
    }
    CompletedEsAndOsByGroup(innerMap)
  }

  private def createNotStartedEsAndOsByGroup(): NotStartedEsAndOsByGroup = {
    val innerMap: Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel] = if (groupToNotStartedEsOsAndBenchmarks.isEmpty) Map() else {
      {
        for {
          planningAreaAndGroupIdKey <- groupToNotStartedEsOsAndBenchmarks.keys
          groupId = planningAreaAndGroupIdKey.split("___")(1)

          if groupToNotStartedEsOsAndBenchmarks.isDefinedAt(planningAreaAndGroupIdKey)
          sectionName <- groupToNotStartedEsOsAndBenchmarks(planningAreaAndGroupIdKey).keys
          setSectionNameToSubSectionsMap = createSetSectionNameToSubSectionsMap(sectionName, groupToNotStartedEsOsAndBenchmarks(planningAreaAndGroupIdKey)(sectionName))
          curriculumLevel: CurriculumLevel = extractCurriculumLevel(setSectionNameToSubSectionsMap)
        } yield (groupId, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel(
          curriculumLevel,
          CurriculumArea.createCurriculumAreaFromString(maybeSelectedPlanningArea.getOrElse("NO_SUBJECT")),
          setSectionNameToSubSectionsMap
        ))
      }.toMap
    }
    NotStartedEsAndOsByGroup(innerMap)
  }


  private def postSaveEsOsBenchies(subjectWeeklyPlan: WeeklyPlanOfOneSubject, classId: String): Unit = {
    val subjectWeeklyPlansPickled = PlanningHelper.encodeAnyJawnNonFriendlyCharacters(write[WeeklyPlanOfOneSubject](subjectWeeklyPlan))
    val completedEsOsBenchiesPickled = PlanningHelper.encodeAnyJawnNonFriendlyCharacters(write[CompletedEsAndOsByGroup](createCompletedEsAndOsByGroup()))
    val notStartedEsOsBenchiesPickled = PlanningHelper.encodeAnyJawnNonFriendlyCharacters(write[NotStartedEsAndOsByGroup](createNotStartedEsAndOsByGroup()))

    Dynamic.global.console.log(s"Not Started: ${groupToNotStartedEsOsAndBenchmarks.toString()}")
    Dynamic.global.console.log(s"Selected: ${groupToSelectedEsOsAndBenchmarks.toString()}")
    Dynamic.global.console.log(s"Completed: ${groupToCompletedEsOsAndBenchmarks.toString()}")
    Dynamic.global.console.log(s"Pickled, this == $subjectWeeklyPlansPickled")
    Dynamic.global.console.log(s"Pickled Completed, this == $completedEsOsBenchiesPickled")
    Dynamic.global.console.log(s"Pickled NotStarted, this == $notStartedEsOsBenchiesPickled")

    val theUrl = s"/saveEsOsBenchiesForTheWeek/$classId"
    val theHeaders = Map(
      "Content-Type" -> "application/x-www-form-urlencoded",
      "X-Requested-With" -> "Accept"
    )
    val theData = InputData.str2ajax(s"subjectWeeklyPlansPickled=$subjectWeeklyPlansPickled&" +
      s"notStarted=$notStartedEsOsBenchiesPickled&completed=$completedEsOsBenchiesPickled")



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
          $("#create-weekly-plans-lesson-modal").modal("hide")
          currentlySelectMondayStartOfWeekDate match {
            case Some(mondayIsoDate) =>
              dom.window.location.href = s"/createPlanForTheWeek/$classId/$mondayIsoDate"
            case None =>
              dom.window.location.href = s"/createPlanForTheWeek/$classId"
          }

        }, 10)
      case Failure(ex) =>
        dom.window.alert("Something went wrong with saving group termly plans. Specifically : -" +
          s"\n\n${ex.toString}")
    }

  }


  private def backToWeeklyViewButton(): Unit = {
    val planThisWeekButton = dom.document.getElementById("back-to-weekly-view-button").asInstanceOf[HTMLButtonElement]
    if (planThisWeekButton != null) {
      planThisWeekButton.addEventListener("click", (e: dom.Event) => {
        val classId = dom.window.localStorage.getItem("classId")

        currentlySelectMondayStartOfWeekDate match {
          case Some(mondayIsoDate) =>
            dom.window.location.href = s"/weeklyViewOfWeeklyPlanning/$classId/$mondayIsoDate"
          case None =>
            dom.window.location.href = s"/weeklyViewOfWeeklyPlanning/$classId"
        }

      })
    }
  }


}



