package timetoteach.planning.weekly

import duplicate.model.CurriculumLevel
import duplicate.model.esandos._
import duplicate.model.planning._
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.ext.Ajax.InputData
import org.scalajs.dom.html.{Div, Input, LI, UList}
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLDivElement, HTMLElement, HTMLInputElement}
import org.scalajs.dom.svg.SVG
import scalatags.JsDom
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all.{`class`, attr, div, _}
import shared.util.PlanningHelper
import upickle.default.write

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.scalajs.js
import scala.scalajs.js.Dynamic.global
import scala.util.{Failure, Success}

object CreatePlanForTheWeekJsScreen extends WeeklyPlansCommon {

  private var groupToSelectedEsOsAndBenchmarks: scala.collection.mutable.Map[String, scala.collection.mutable.Map[String, scala.collection.mutable.Map[String,
    (scala.collection.mutable.Set[String], scala.collection.mutable.Set[String])]]] = scala.collection.mutable.Map.empty

  private var eAndORowBackgroundNormalColor: Option[String] = None
  private var eAndORowForegroundNormalColor: Option[String] = None
  private var eAndORowBorderRadius: Option[String] = None

  private var currentlySelectedPlanningArea: Option[String] = None
  private var currentlySelectedPlanningAreaNice: Option[String] = None
  private var currentlySelectedLessonSummariesThisWeek: Option[List[LessonSummary]] = None

  private var groupIdsToName: scala.collection.mutable.Map[String, String] = scala.collection.mutable.Map.empty

  def loadJavascript(): Unit = {
    global.console.log("Loading Create Plan For The Week Javascript")
    setMondayDateToCurrentlySelectedWeek()
    clickingAMondayWeekButtonUpdatesDates()
    mouseoverHighlightEandOsAndBenchmarks()
    clickOnEandO()
    clickOnBenchmark()
    planLessonsButton()
    deleteSingleRowFromClassPlan()
    resetValuesOnTabClick()
    saveSubjectWeeksPlanButton()
    addTickToSavedLessons()
  }

  private def setEsOsBenchmarksSummary(): Unit = {
    global.console.log(s"setEsOsBenchmarksSummary ... ${currentlySelectedPlanningArea.getOrElse("NOTHING_THERE")}")

    val esOsBenchSummariesDiv = dom.document.getElementById("es-and-os-and-benchmarks-summary").asInstanceOf[HTMLDivElement]
    while (esOsBenchSummariesDiv.firstChild != null) {
      esOsBenchSummariesDiv.removeChild(esOsBenchSummariesDiv.firstChild)
    }

    val subjectAndGroupKeys = groupToSelectedEsOsAndBenchmarks.keySet.filter {
      key =>
        val subjectAndGroupId = key.split("___")
        subjectAndGroupId(0) == currentlySelectedPlanningArea.getOrElse("NOTHING_THERE")
    }

    global.console.log(s"subjectAndGroupkeys  : $subjectAndGroupKeys")

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

  private def createAddButton(buttonIdRoot: String, buttonDescription: String, index: Int, lessonSummary: LessonSummary): List[JsDom.TypedTag[Div]] = {
    val addDetailsDiv = div(`id` := s"$buttonIdRoot-div-$index", `class` := s"$buttonIdRoot-div")
    val buttonDiv = div(`class` := "row")(
      button(id := s"$buttonIdRoot-$index", `class` := s"$buttonIdRoot btn btn-sm btn-success create-weekly-plans-add-to-lesson-button",
        attr("data-subject-name") := lessonSummary.subject,
        attr("data-lesson-start-time") := lessonSummary.startTimeIso,
        attr("data-lesson-day-of-the-week") := lessonSummary.dayOfWeek,
        attr("data-attribute-type") := buttonDescription,
        attr("data-tab-index") := index)(
        s"+ $buttonDescription"
      )
    )

    List(addDetailsDiv, buttonDiv)
  }

  private def createLessonDataDiv(lessonSummary: LessonSummary, tabIndex: Int): JsDom.TypedTag[Div] = {
    div(
      `class` := "data-lesson-summary-for-lesson-div",
      attr("data-tab-index") := tabIndex,
      attr("data-lesson-summary-subject") := lessonSummary.subject,
      attr("data-lesson-summary-subject-additional-info") := lessonSummary.subjectAdditionalInfo,
      attr("data-lesson-summary-day") := lessonSummary.dayOfWeek,
      attr("data-lesson-summary-start-time") := lessonSummary.startTimeIso,
      attr("data-lesson-summary-end-time") := lessonSummary.endTimeIso
    )()
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

  def getDayOfWeek(date: js.Date): String = {
    date.getDay() match {
      case 1 => "MONDAY"
      case 2 => "TUESDAY"
      case 3 => "WEDNESDAY"
      case 4 => "THURSDAY"
      case 5 => "FRIDAY"
      case 6 => "SATURDAY"
      case 0 => "SUNDAY"
      case _ => "MONDAY"
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

  def addAttributeDetailsForGroup(subject: String, lessonDate: String, lessonStartTime: String, attributeType: String,
                                  attributesPerGroup: Map[String, List[String]]): Unit = {
    val allPlanLessonsButtons = dom.document.getElementsByClassName("create-weekly-plans-add-to-lesson-button")
    val nodeListSize = allPlanLessonsButtons.length
    var index = 0
    while (index < nodeListSize) {
      val buttonElement = allPlanLessonsButtons(index).asInstanceOf[HTMLButtonElement]
      val dataSubject = buttonElement.getAttribute("data-subject-name")
      val dataLessonStartTime = buttonElement.getAttribute("data-lesson-start-time")
      val dataLessonDayOfWeek = buttonElement.getAttribute("data-lesson-day-of-the-week")
      val dataAttributeType = buttonElement.getAttribute("data-attribute-type")
      val jsLessonDate = new js.Date(lessonDate)

      global.console.log(s"attributesPerGroup ${attributesPerGroup.toString()}")

      if (dataSubject == subject &&
        dataLessonStartTime == lessonStartTime &&
        dataLessonDayOfWeek == getDayOfWeek(jsLessonDate) &&
        dataAttributeType == attributeType &&
        attributesPerGroup.nonEmpty
      ) {
        for (attrValue <- attributesPerGroup.keys) {
          val tabIndex = buttonElement.getAttribute("data-tab-index")
          addAttributeRow(
            buttonElementClassType(dataAttributeType),
            dataAttributeType,
            true,
            tabIndex,
            Some(attrValue),
            Some(attributesPerGroup(attrValue))
          )
        }
      }

      index = index + 1
    }
  }

  def addAttributeDetails(subject: String, lessonDate: String, lessonStartTime: String, attributeType: String, attributeValues: List[String]): Unit = {
    val allPlanLessonsButtons = dom.document.getElementsByClassName("create-weekly-plans-add-to-lesson-button")
    val nodeListSize = allPlanLessonsButtons.length
    var index = 0
    while (index < nodeListSize) {
      val buttonElement = allPlanLessonsButtons(index).asInstanceOf[HTMLButtonElement]
      val dataSubject = buttonElement.getAttribute("data-subject-name")
      val dataLessonStartTime = buttonElement.getAttribute("data-lesson-start-time")
      val dataLessonDayOfWeek = buttonElement.getAttribute("data-lesson-day-of-the-week")
      val dataAttributeType = buttonElement.getAttribute("data-attribute-type")
      val jsLessonDate = new js.Date(lessonDate)

      if (dataSubject == subject &&
        dataLessonStartTime == lessonStartTime &&
        dataLessonDayOfWeek == getDayOfWeek(jsLessonDate) &&
        dataAttributeType == attributeType &&
        attributeValues.nonEmpty
      ) {
        for (attrValue <- attributeValues) {
          val tabIndex = buttonElement.getAttribute("data-tab-index")
          addAttributeRow(buttonElementClassType(dataAttributeType), dataAttributeType, false, tabIndex, Some(attrValue), None)
        }
      }

      index = index + 1
    }
  }

  def addLessonPlanDetailsFromSavedStatus(): Unit = {
    val fullWeeklyPlanOfLessonsPickled = dom.window.localStorage.getItem("fullWeeklyPlanOfLessonsPickled")
    import upickle.default._
    val fullWeeklyPlanOfLessons: FullWeeklyPlanOfLessons = read[FullWeeklyPlanOfLessons](PlanningHelper.decodeAnyNonFriendlyCharacters(fullWeeklyPlanOfLessonsPickled))
    if (fullWeeklyPlanOfLessons.subjectToWeeklyPlanOfSubject.isDefinedAt(currentlySelectedPlanningArea.getOrElse("Nope"))) {
      val weeklyPlanOfSubject = fullWeeklyPlanOfLessons.subjectToWeeklyPlanOfSubject(currentlySelectedPlanningArea.getOrElse("Nope"))
      for (lesson <- weeklyPlanOfSubject.lessons) {
        addAttributeDetailsForGroup(lesson.subject, lesson.lessonDateIso, lesson.startTimeIso, "Activity", lesson.activitiesPerGroup )
        addAttributeDetails(lesson.subject, lesson.lessonDateIso, lesson.startTimeIso, "Resource", lesson.resources)
        addAttributeDetailsForGroup(lesson.subject, lesson.lessonDateIso, lesson.startTimeIso, "Learning Intention", lesson.learningIntentionsPerGroup)
        addAttributeDetailsForGroup(lesson.subject, lesson.lessonDateIso, lesson.startTimeIso, "Success Criteria", lesson.successCriteriaPerGroup)
        addAttributeDetails(lesson.subject, lesson.lessonDateIso, lesson.startTimeIso, "Plenary", lesson.plenary)
        addAttributeDetailsForGroup(lesson.subject, lesson.lessonDateIso, lesson.startTimeIso, "Formative Assessment", lesson.formativeAssessmentPerGroup)
        addAttributeDetails(lesson.subject, lesson.lessonDateIso, lesson.startTimeIso, "Note", lesson.notesBefore)
      }
    }
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

        val planningArea = buttonElement.getAttribute("data-planning-area")
        val planningAreaNice = buttonElement.getAttribute("data-planning-area-nice")

        currentlySelectedPlanningArea = Some(planningArea)
        currentlySelectedPlanningAreaNice = Some(planningAreaNice)

        dom.document.getElementById("create-weekly-plans-lesson-subject-name").innerHTML = currentlySelectedPlanningAreaNice.getOrElse("")
        dom.document.getElementById("create-weekly-plans-lesson-subject-name").setAttribute("data-subject-name", currentlySelectedPlanningArea.getOrElse(""))

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

  private def setButtonDefaults(theDiv: HTMLDivElement): Unit = {
    global.console.log(s"eAndORowBackgroundNormalColor = $eAndORowBackgroundNormalColor.toString")
    theDiv.style.backgroundColor = eAndORowBackgroundNormalColor.getOrElse("f6f6f6")
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
          global.console.log(s"Should be now .. values of Es and Os are ... ${
            groupToSelectedEsOsAndBenchmarks(groupIdOrNot)(curriculumSection)(curriculumSubSection)._1.toString()
          }")
          theDiv.style.backgroundColor = "#016ecd"
          theDiv.style.color = "white"
          theDiv.style.borderRadius = "7px"
        }
      })


      index = index + 1
    }
  }

  private def repaintTheEsAndOs(className: String): Unit = {
    val allEAndOAndBenchmarksRows = dom.document.getElementsByClassName(className)
    val nodeListSize = allEAndOAndBenchmarksRows.length
    var index = 0
    while (index < nodeListSize) {
      val theDiv = allEAndOAndBenchmarksRows(index).asInstanceOf[HTMLDivElement]
      theDiv.style.backgroundColor = eAndORowBackgroundNormalColor.getOrElse("f6f6f6")
      theDiv.style.color = eAndORowForegroundNormalColor.getOrElse("grey")
      theDiv.style.borderRadius = eAndORowBorderRadius.getOrElse("0")
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
          theDiv.style.backgroundColor = eAndORowBackgroundNormalColor.getOrElse("f6f6f6")
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

  private def clickingOnAddToLessonsButtons(): Unit = {
    addButtonClickBehaviour("create-weekly-plans-add-to-lesson-button-add-activity", "Activity", true)
    addButtonClickBehaviour("create-weekly-plans-add-to-lesson-button-add-resource", "Resource", false)
    addButtonClickBehaviour("create-weekly-plans-add-to-lesson-button-add-learning-intention", "Learning Intention", true)
    addButtonClickBehaviour("create-weekly-plans-add-to-lesson-button-add-success-criteria", "Success Criteria", true)
    addButtonClickBehaviour("create-weekly-plans-add-to-lesson-button-add-plenary", "Plenary", false)
    addButtonClickBehaviour("create-weekly-plans-add-to-lesson-button-add-formative-assessment", "Formative Assessment", false)
    addButtonClickBehaviour("create-weekly-plans-add-to-lesson-button-add-note", "Note", false)
  }


  private def cleanupModalAdds(): Unit = {
    cleanupActivity("create-weekly-plans-add-to-lesson-button-add-activity-div")
    cleanupActivity("create-weekly-plans-add-to-lesson-button-add-resource-div")
    cleanupActivity("create-weekly-plans-add-to-lesson-button-add-learning-intention-div")
    cleanupActivity("create-weekly-plans-add-to-lesson-button-add-success-criteria-div")
    cleanupActivity("create-weekly-plans-add-to-lesson-button-add-plenary-div")
    cleanupActivity("create-weekly-plans-add-to-lesson-button-add-formative-assessment-div")
    cleanupActivity("create-weekly-plans-add-to-lesson-button-add-note-div")
  }

  private def cleanupActivity(elementId: String): Unit = {
    val activityDiv = dom.document.getElementById(elementId).asInstanceOf[HTMLDivElement]
    while (activityDiv != null && activityDiv.hasChildNodes()) {
      activityDiv.removeChild(activityDiv.lastChild)
    }
  }

  private def addButtonClickBehaviour(buttonElementId: String, buttonNameType: String, applyToGroups: Boolean): Unit = {
    val addActivityButtons = dom.document.getElementsByClassName(buttonElementId)
    val nodeListSize = addActivityButtons.length

    var index = 0
    while (index < nodeListSize) {
      val addActivityButton = addActivityButtons(index).asInstanceOf[HTMLButtonElement]

      addActivityButton.addEventListener("click", (e: dom.Event) => {
        global.console.log(s"groups: ${
          groupIdsToName.keys.toString()
        }")

        val tabIndex = addActivityButton.getAttribute("data-tab-index")
        addAttributeRow(buttonElementId, buttonNameType, applyToGroups, tabIndex, None, None)
      })

      index = index + 1
    }
  }

  private def addAttributeRow(buttonElementId: String,
                              buttonNameType: String,
                              applyToGroups: Boolean,
                              tabIndex: String,
                              attributeText: Option[String],
                              maybeGroupIds: Option[List[String]]
                             ): Unit = {


    val groupNamesToGroupIds = for {
      groupId <- groupIdsToName.keys
      groupName = groupIdsToName(groupId)
      if groupName != null
      uniqIdForRow = java.util.UUID.randomUUID().toString
    } yield (groupName, groupId, uniqIdForRow)


    def createGroupInputDiv(groupNameToGroupId: (String, String, String)): TypedTag[Div] = {
      val groupId = groupNameToGroupId._2

      global.console.log(s"createGroupInputDiv1 : ${maybeGroupIds.toString}")
      global.console.log(s"createGroupInputDiv2 : ${groupId}")
      val realInput: TypedTag[Input] = if (maybeGroupIds.isDefined && maybeGroupIds.get.contains(groupId)) {
        input(`id` := s"group-checkbox-${
          groupNameToGroupId._2
        }-${
          groupNameToGroupId._3
        }", `name` := s"group-checkbox-${
          groupNameToGroupId._2
        }-${
          groupNameToGroupId._3
        }",
          `type` := "checkbox",
          attr("data-group-id") := groupNameToGroupId._2,
          `class` := "custom-control-input group-on-off", `value` := "On", checked := true)
      } else {
        input(`id` := s"group-checkbox-${
          groupNameToGroupId._2
        }-${
          groupNameToGroupId._3
        }", `name` := s"group-checkbox-${
          groupNameToGroupId._2
        }-${
          groupNameToGroupId._3
        }",
          `type` := "checkbox",
          attr("data-group-id") := groupNameToGroupId._2,
          `class` := "custom-control-input group-on-off", `value` := "On")
      }

      div(`class` :=
        "custom-control custom-checkbox create-weekly-plans-lesson-modal-select-groups")(
        realInput,

        input(`name` := s"group-checkbox-${
          groupNameToGroupId._2
        }-${
          groupNameToGroupId._3
        }", `type` := "hidden", `value` := "Off"),

        label(`class` := "custom-control-label", `for` := s"group-checkbox-${
          groupNameToGroupId._2
        }-${
          groupNameToGroupId._3
        }"),

        span(`class` := "custom-control-description")(s"${
          groupNameToGroupId._1
        }")
      )

    }

    val groupsAsCheckboxes: Seq[TypedTag[Div]] = {
      for (groupNameToGroupId <- groupNamesToGroupIds) yield createGroupInputDiv(groupNameToGroupId)
    }.toSeq

    val groupsAsCheckboxesInContainer = if (groupNamesToGroupIds.nonEmpty && applyToGroups) {
      div(`class` := "form-row")(
        span(`class` := "create-weekly-plans-lesson-modal-span-select-groups")(small("Applies to which groups: ")),
        groupsAsCheckboxes
      )
    } else {
      div()
    }

    val theInput = attributeText match {
      case Some(inputText) => input(`type` := "text", `class` := s"form-control form-control-sm input-attribute-${buttonNameType.replace(" ", "")}",
        attr("data-tab-index") := tabIndex, placeholder := s"Enter $buttonNameType", value := s"${attributeText.getOrElse("")}")
      case None => input(`type` := "text", `class` := s"form-control form-control-sm input-attribute-${buttonNameType.replace(" ", "")}",
        attr("data-tab-index") := tabIndex, placeholder := s"Enter $buttonNameType")
    }

    val newAttributeRow = form()(
      div(`class` := "form-group")(
        button(`class` := "close create-weekly-plans-lesson-modal-delete-this-row", attr("aria-label") := "Close")(
          span(attr("aria-hidden") := "true")(raw("&times;"))
        ),
        fieldset()(
          legend(buttonNameType), theInput, groupsAsCheckboxesInContainer
        )
      )
    )

    val child = dom.document.createElement("div")
    child.innerHTML = newAttributeRow.toString

    val newGroupsDiv = dom.document.getElementById(s"$buttonElementId-div-$tabIndex").asInstanceOf[Div]
    newGroupsDiv.appendChild(child)

    deleteSingleRowFromClassPlan()
  }
  private def deleteSingleRowFromClassPlan(): Unit = {
    val deleteThisGroupButton = dom.document.getElementsByClassName("create-weekly-plans-lesson-modal-delete-this-row")
    val nodeListSize = deleteThisGroupButton.length
    var index = 0
    while (index < nodeListSize) {
      val theDeleteButton = deleteThisGroupButton(index).asInstanceOf[HTMLButtonElement]

      theDeleteButton.addEventListener("click", (e: dom.Event) => {
        val theRowDiv = theDeleteButton.parentNode.parentNode.parentNode.asInstanceOf[HTMLElement]
        val theParent = theRowDiv.parentNode
        if (theRowDiv != null && theParent != null) {
          theParent.removeChild(theRowDiv)
        }
      })

      index = index + 1
    }

  }

  private def isActive(element: HTMLElement): Boolean = element.classList.contains("active")

  private def resetValuesOnTabClick(): Unit = {
    val $ = js.Dynamic.global.$
    $("a[data-toggle=\"tab\"]").on("shown.bs.tab", (e: dom.Event) => {
      groupToSelectedEsOsAndBenchmarks.clear()
      repaintTheEsAndOs("create-weekly-plans-es-and-os-row")
      repaintTheEsAndOs("create-weekly-plans-eobenchmark-row")


      groupIdsToName.clear()
      val weeklyTopLevelTabs = dom.document.getElementsByClassName("weekly-plans-top-level-tab")
      val nodeListSize = weeklyTopLevelTabs.length
      var index = 0
      while (index < nodeListSize) {
        val tabElement = weeklyTopLevelTabs(index).asInstanceOf[HTMLElement]

        if (isActive(tabElement)) {
          // TODO: andy - add group ids to the map()
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

          // 2 Set the Group Ids Map to Group Name
        }

        index = index + 1
      }

      global.console.log(s"The groups are ... ${groupIdsToName.toString()}")

    })


  }


  private def createEandOSetSubSection(esAndOsPlusBenchmarksForSubsection: (mutable.Set[String], mutable.Set[String])): EandOSetSubSection = {
    val esAndOs: List[EandO] = {
      for {
        esAndOsCode <- esAndOsPlusBenchmarksForSubsection._1
      } yield EandO(esAndOsCode, Nil)
    }.toList

    val benchmarks: List[Benchmark] = {
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

    for (subsectionName <- subsectionToEsAndOs.keys) {
      val esAndOsPlusBenchmarksForSubsection = subsectionToEsAndOs(subsectionName)
      val esOsSetSubSection: EandOSetSubSection = createEandOSetSubSection(esAndOsPlusBenchmarksForSubsection)
      mutableMap += (subsectionName -> esOsSetSubSection)
    }

    Map(sectionName -> mutableMap.toMap)
  }

  private def extractCurriculumLevel(eAndO: EandO): CurriculumLevel = {
    CurriculumLevel.createCurriculumLevelFromEAndOCode(eAndO.code)
  }

  private def extractCurriculumLevel(setSectionNameToSubSectionsMap: Map[String, Map[String, EandOSetSubSection]]): CurriculumLevel = {
    global.console.log(s"setSectionNameToSubSectionsMap = ${setSectionNameToSubSectionsMap}")
    val levels = for {
      sectionName <- setSectionNameToSubSectionsMap.keys
      subsectionName <- setSectionNameToSubSectionsMap(sectionName).keys
      eAndOCodes = setSectionNameToSubSectionsMap(sectionName)(subsectionName).eAndOs
      log1 = global.console.log(s"eAndOCodes = ${eAndOCodes.toString}")
      if eAndOCodes.nonEmpty
      level = extractCurriculumLevel(eAndOCodes.head)
    } yield level

    levels.head
  }

  private def populateGroupToEsOsBenchmarks(): Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel] = {
    {
      for {
        planningAreaAndGroupIdKey <- groupToSelectedEsOsAndBenchmarks.keys
        groupId = planningAreaAndGroupIdKey.split("___")(1)

        sectionName <- groupToSelectedEsOsAndBenchmarks(planningAreaAndGroupIdKey).keys
        setSectionNameToSubSectionsMap = createSetSectionNameToSubSectionsMap(sectionName, groupToSelectedEsOsAndBenchmarks(planningAreaAndGroupIdKey)(sectionName))
        curriculumLevel: CurriculumLevel = extractCurriculumLevel(setSectionNameToSubSectionsMap)
      } yield (groupId, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel(
        curriculumLevel,
        CurriculumArea.createCurriculumAreaFromString(currentlySelectedPlanningArea.getOrElse("NO_SUBJECT")),
        setSectionNameToSubSectionsMap
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
              global.console.log(s"Adding group id $groupId")
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

  private def extractLessonAttributes(attributeType: String, tabIndexToLookFor: Int): List[String] = {
    var lessonAttributes = new ListBuffer[String]()

    val inputAttributes = dom.document.getElementsByClassName(s"input-attribute-${attributeType.replace(" ", "")}")
    val nodeListSize = inputAttributes.length
    var nodeIndex = 0
    while (nodeIndex < nodeListSize) {
      val inputLessonAttributes = inputAttributes(nodeIndex).asInstanceOf[HTMLInputElement]
      val tabIndex = inputLessonAttributes.getAttribute("data-tab-index").toInt
      if (tabIndex == tabIndexToLookFor) {
        val attributeText = inputLessonAttributes.value
        lessonAttributes += attributeText
      }
      nodeIndex = nodeIndex + 1
    }

    lessonAttributes.toList
  }

  private def extractLessonAttributesWithGroups(attributeType: String, tabIndexToLookFor: Int): Map[String, List[String]] = {
    var activityToGroups = scala.collection.mutable.Map[String, List[String]]()

    val inputAttributes = dom.document.getElementsByClassName(s"input-attribute-${attributeType.replace(" ", "")}")
    val nodeListSize = inputAttributes.length
    var nodeIndex = 0
    while (nodeIndex < nodeListSize) {
      val inputLessonAttributes = inputAttributes(nodeIndex).asInstanceOf[HTMLInputElement]
      val tabIndex = inputLessonAttributes.getAttribute("data-tab-index").toInt
      if (tabIndex == tabIndexToLookFor) {
        val activityText = inputLessonAttributes.value

        val peerNodes = inputLessonAttributes.parentNode.childNodes
        val peerNodeSize = peerNodes.length
        var peerNodeIndex = 0
        while (peerNodeIndex < peerNodeSize) {
          val peerNode = peerNodes(peerNodeIndex).asInstanceOf[HTMLElement]
          if (peerNode != null && peerNode.toString != "undefined" && peerNode.className.contains("form-row")) {
            val groupIds: List[String] = extractGroupIds(peerNode)
            activityToGroups += (activityText -> groupIds)
          } else {
            activityToGroups += (activityText -> Nil)
          }
          peerNodeIndex = peerNodeIndex + 1
        }
      }
      nodeIndex = nodeIndex + 1
    }

    global.console.log(s"Text Values for ${attributeType} Map == ${activityToGroups.toString}")
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
        global.console.log(s"ERROR: Do not recognise day '$dayOfWeek'")
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

      val activitiesPerGroup: Map[String, List[String]] = extractLessonAttributesWithGroups("Activity", index)
      val resources: List[String] = extractLessonAttributes("Resource", index)
      val learningIntentionsPerGroup: Map[String, List[String]] = extractLessonAttributesWithGroups("LearningIntention", index)
      val successCriteriaPerGroup: Map[String, List[String]] = extractLessonAttributesWithGroups("SuccessCriteria", index)
      val plenary: List[String] = extractLessonAttributes("Plenary", index)
      val formativeAssessmentPerGroup: Map[String, List[String]] = extractLessonAttributesWithGroups("FormativeAssessment", index)
      val notesBefore: List[String] = extractLessonAttributes("Note", index)
      val notesAfter: List[String] = Nil

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

    global.console.log(s"Lesson plans to save ... ${lessonPlansForTheWeek.toString}")
    lessonPlansForTheWeek.toList
  }

  private def saveSubjectWeeksPlanButton(): Unit = {
    val saveSubjectWeeksPlanButton = dom.document.getElementById("create-weekly-plans-save-subject-plan")
    if (saveSubjectWeeksPlanButton != null) {
      saveSubjectWeeksPlanButton.addEventListener("click", (e: dom.Event) => {

        val subject = currentlySelectedPlanningArea.getOrElse("NO_SUBJECT")
        val classId = dom.window.localStorage.getItem("classId")
        val tttUserId = dom.window.localStorage.getItem("tttUserId")
        val weekBeginningIsoDate = currentlySelectMondayStartOfWeekDate.getOrElse("1970-01-01")
        global.console.log(s"Subject == $subject")
        global.console.log(s"classId == $classId")
        global.console.log(s"tttUserId == $tttUserId")
        global.console.log(s"weekBeginningIsoDate == $weekBeginningIsoDate")
        global.console.log(s"groupToSelectedEsOsAndBenchmarks == ${groupToSelectedEsOsAndBenchmarks.toString}")

        val groupToEsOsBenchmarks: Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel] = populateGroupToEsOsBenchmarks()
        global.console.log(s"groupToEsOsBenchmarks == ${groupToEsOsBenchmarks.toString}")

        val lessons: List[LessonPlan] = createOneWeekLessonSummary(weekBeginningIsoDate)
        global.console.log(s"lessonSummary == ${lessons.toString}")

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

  private def postSave(subjectWeeklyPlan: WeeklyPlanOfOneSubject, classId: String): Unit = {
    val subjectWeeklyPlansPickled = PlanningHelper.encodeAnyJawnNonFriendlyCharacters(write[WeeklyPlanOfOneSubject](subjectWeeklyPlan))
    global.console.log(s"Pickled, this == $subjectWeeklyPlansPickled")

    import scala.concurrent.ExecutionContext.Implicits.global
    val theUrl = s"/savePlanForTheWeek/$classId"
    val theHeaders = Map(
      "Content-Type" -> "application/x-www-form-urlencoded",
      "X-Requested-With" -> "Accept"
    )
    val theData = InputData.str2ajax(s"subjectWeeklyPlansPickled=$subjectWeeklyPlansPickled")

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

}



