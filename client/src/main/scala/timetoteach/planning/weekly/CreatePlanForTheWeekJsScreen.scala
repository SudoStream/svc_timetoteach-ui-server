package timetoteach.planning.weekly

import duplicate.model.CurriculumLevel
import duplicate.model.esandos._
import duplicate.model.planning.{LessonSummary, LessonsThisWeek, WeeklyPlanOfOneSubject}
import org.scalajs.dom
import org.scalajs.dom.html.{Div, LI, UList}
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLDivElement, HTMLElement}
import scalatags.JsDom
import scalatags.JsDom.all.{`class`, div, _}
import shared.util.PlanningHelper
import upickle.default.write

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
    deleteSingleRowFromClassPlan()
    resetValuesOnTabClick()
    saveSubjectWeeksPlanButton()
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

  private def createAddButton(buttonIdRoot: String, buttonDescription: String, index: Int): List[JsDom.TypedTag[Div]] = {
    val addDetailsDiv = div(`id` := s"$buttonIdRoot-div-$index", `class` := s"$buttonIdRoot-div")
    val buttonDiv = div(`class` := "row")(
      button(id := s"$buttonIdRoot-$index", `class` := s"$buttonIdRoot btn btn-sm btn-success create-weekly-plans-add-to-lesson-button",
        attr("data-button-index") := index)(
        s"+ $buttonDescription"
      )
    )

    List(addDetailsDiv, buttonDiv)
  }

  private def createTabbedContent(): JsDom.TypedTag[Div] = {
    val innerTabbedContent = for {
      (lessonSummary, index) <- currentlySelectedLessonSummariesThisWeek.getOrElse(Nil).zipWithIndex
      divClasses = if (index == 0) "tab-pane fade show active" else "tab-pane fade"
    } yield div(`class` := divClasses, id := s"tabbed-weekly-plans-tab-content_$index", attr("role") := "tabpanel")(
      createAddButton("create-weekly-plans-add-to-lesson-button-add-activity", "Activity", index),
      createAddButton("create-weekly-plans-add-to-lesson-button-add-resource", "Resource", index),

      createAddButton("create-weekly-plans-add-to-lesson-button-add-learning-intention", "Learning Intention", index),
      createAddButton("create-weekly-plans-add-to-lesson-button-add-success-criteria", "Success Criteria", index),
      createAddButton("create-weekly-plans-add-to-lesson-button-add-plenary", "Plenary", index),
      createAddButton("create-weekly-plans-add-to-lesson-button-add-formative-assessment", "Formative Assessment", index),
      createAddButton("create-weekly-plans-add-to-lesson-button-add-note", "Note", index)
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
    val tabsAndContent = div(tabbedWeeklyPlans, tabbedContent)

    val child = dom.document.createElement("div")
    child.innerHTML = tabsAndContent.toString()
    dom.document.getElementById("create-weekly-plans-modal-body").appendChild(child)
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

    global.console.log(s"There are $nodeListSize buttons with the class = $buttonElementId")

    var index = 0
    while (index < nodeListSize) {
      val addActivityButton = addActivityButtons(index).asInstanceOf[HTMLButtonElement]

      addActivityButton.addEventListener("click", (e: dom.Event) => {
        global.console.log(s"groups: ${
          groupToSelectedEsOsAndBenchmarks.keys.toString()
        }")

        val groupNamesToGroupIds = for {
          group <- groupToSelectedEsOsAndBenchmarks.keys
          subjectAndGroupId = group.split("___")
          groupId = subjectAndGroupId(1)
          groupName = dom.window.localStorage.getItem(s"$groupId")
          if groupName != null
          uniqIdForRow = java.util.UUID.randomUUID().toString
        } yield (groupName, groupId, uniqIdForRow)

        val groupsAsCheckboxes: Seq[JsDom.TypedTag[Div]] = {
          for (groupNameToGroupId <- groupNamesToGroupIds) yield div(`class` :=
            "custom-control custom-checkbox create-weekly-plans-lesson-modal-select-groups")(
            input(`id` := s"group-checkbox-${
              groupNameToGroupId._2
            }-${
              groupNameToGroupId._3
            }", `name` := s"group-checkbox-${
              groupNameToGroupId._2
            }-${
              groupNameToGroupId._3
            }",
              `type` := "checkbox", `class` := "custom-control-input", `value` := "On"),
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
        }.toSeq

        val groupsAsCheckboxesInContainer = if (groupNamesToGroupIds.nonEmpty && applyToGroups) {
          div(`class` := "form-row")(
            span(`class` := "create-weekly-plans-lesson-modal-select-groups")(small("Applies to which groups: ")),
            groupsAsCheckboxes
          )
        } else {
          div()
        }

        val newActivityRow = form()(
          div(`class` := "form-group")(
            button(`class` := "close create-weekly-plans-lesson-modal-delete-this-row", attr("aria-label") := "Close")(
              span(attr("aria-hidden") := "true")(raw("&times;"))
            ),
            fieldset()(
              legend(buttonNameType),
              input(`type` := "text", `class` := "form-control form-control-sm", placeholder := s"Enter $buttonNameType"),
              groupsAsCheckboxesInContainer
            )
          )
        )

        val child = dom.document.createElement("div")
        child.innerHTML = newActivityRow.toString

        val buttonIndex = addActivityButton.getAttribute("data-button-index")
        val newGroupsDiv = dom.document.getElementById(s"$buttonElementId-div-$buttonIndex").asInstanceOf[HTMLDivElement]
        newGroupsDiv.appendChild(child)

        deleteSingleRowFromClassPlan()
      })


      index = index + 1
    }
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

  private def resetValuesOnTabClick(): Unit = {
    val $ = js.Dynamic.global.$
    $("a[data-toggle=\"tab\"]").on("shown.bs.tab", (e: dom.Event) => {
      groupToSelectedEsOsAndBenchmarks.clear()
      repaintTheEsAndOs("create-weekly-plans-es-and-os-row")
      repaintTheEsAndOs("create-weekly-plans-eobenchmark-row")
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

        val groupToEsOsBenchmarks = populateGroupToEsOsBenchmarks()
        global.console.log(s"groupToEsOsBenchmarks == ${groupToEsOsBenchmarks.toString}")


        //        postSave()
      })
    }
  }

  private def postSave(subjectWeeklyPlan: WeeklyPlanOfOneSubject): Unit = {
    val subjectWeeklyPlansPickled = PlanningHelper.encodeAnyJawnNonFriendlyCharacters(write[WeeklyPlanOfOneSubject](subjectWeeklyPlan))
    global.console.log(s"Pickled, this == $subjectWeeklyPlansPickled")

    //    import scala.concurrent.ExecutionContext.Implicits.global
    //    val theUrl = s"/termlysaveplanningforsubjectandgroup/$classId/$subject/$groupId"
    //    val theHeaders = Map(
    //      "Content-Type" -> "application/x-www-form-urlencoded",
    //      "X-Requested-With" -> "Accept"
    //    )
    //    val theData = InputData.str2ajax(s"subjectWeeklyPlansPickled=$subjectWeeklyPlansPickled")
    //
    //    Ajax.post(
    //      url = theUrl,
    //      headers = theHeaders,
    //      data = theData
    //    ).onComplete {
    //      case Success(xhr) =>
    //        val responseText = xhr.responseText
    //        println(s"response = '$responseText'")
    //        dom.window.setTimeout(() => {
    //          println(s"lets goto group planning overview")
    //          dom.window.location.href = s"/termlyoverviewforcurriculumareaandgroup/$classId/$subject/$groupId"
    //        }, 10)
    //      case Failure(ex) =>
    //        dom.window.alert("Something went wrong with saving group termly plans. Specifically : -" +
    //          s"\n\n${ex.toString}")
    //    }

  }

}



