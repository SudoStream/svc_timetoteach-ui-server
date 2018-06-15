package timetoteach.planning.weekly

import duplicate.model.planning.{AttributeRowKey, FullWeeklyPlanOfLessons, LessonPlan, LessonSummary}
import org.scalajs.dom
import org.scalajs.dom.html.{Div, Input}
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLDivElement, HTMLElement, HTMLInputElement}
import scalatags.JsDom
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all.{`class`, attr, div, _}
import shared.util.PlanningHelper

import scala.collection.mutable.ListBuffer
import scala.scalajs.js
import scala.scalajs.js.Dynamic

trait WeeklyPlansCommon {

  private[weekly] var maybeSelectedPlanningArea: Option[String] = None
  private[weekly] var maybeSelectedSubject: Option[String] = None
  private[weekly] var maybeSelectedSubjectAdditionalInfo: Option[String] = None
  private[weekly] var maybeSelectedSubjectStartTimeIso: Option[String] = None
  private[weekly] var maybeSelectedSubjectEndTimeIso: Option[String] = None
  private[weekly] var maybeSelectedSubjectDayOfTheWeek: Option[String] = None

  var defaultBackgroundColorOfWeekMondayButton = "ghostwhite"
  var defaultBorderColorOfWeekMondayButton = "grey"
  var defaultColorOfWeekMondayButton = "grey"
  var defaultFontSize = "0.7rem"

  private[weekly] def getIsoDate(weekStartIsoDate: String, dayOfWeek: String): String = {
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


  private[weekly] var groupIdsToName: scala.collection.mutable.Map[String, String] = scala.collection.mutable.Map.empty
  def getGroupIdsToName: scala.collection.mutable.Map[String, String]

  private[weekly] var currentlySelectMondayStartOfWeekDate: Option[String] = None

  private[weekly] def setMondayDateToCurrentlySelectedWeek(): Unit = {
    currentlySelectMondayStartOfWeekDate = None
    val allMondayButtons = dom.document.getElementsByClassName("template-weekly-planning-mondays-actual-monday-date-btn")
    val nodeListSize = allMondayButtons.length
    var index = 0
    while (index < nodeListSize) {
      val buttonElement = allMondayButtons(index).asInstanceOf[HTMLButtonElement]
      val isSelected = buttonElement.getAttribute("data-is-currently-selected")
      if (isSelected == "true") {
        val mondayDateIso = buttonElement.getAttribute("data-selected-monday-date")
        currentlySelectMondayStartOfWeekDate = Some(mondayDateIso)
        setSelectedButton(buttonElement)
      }
      index = index + 1
    }
    if (currentlySelectMondayStartOfWeekDate.isDefined) {
      Dynamic.global.console.log(s"Setting current Monday to ${currentlySelectMondayStartOfWeekDate.get}")
      updateDaysOfTheWeeksDate()
    } else {
      Dynamic.global.console.log(s"ERROR: There looks to be no Monday set!")
    }
  }

  private[weekly] def addAttributeRow(buttonElementId: String,
                                      buttonNameType: String,
                                      applyToGroups: Boolean,
                                      tabIndex: String,
                                      attributeText: Option[String],
                                      maybeGroupIds: Option[List[String]],
                                      maybeOrderNumber: Option[Int]
                                     ): Unit = {

    Dynamic.global.console.log(s"<><> groupIdsToName : ${getGroupIdsToName.toString()}")

    val groupNamesToGroupIds = for {
      groupId <- getGroupIdsToName.keys
      groupName = getGroupIdsToName(groupId)
      if groupName != null
      uniqIdForRow = java.util.UUID.randomUUID().toString
    } yield (groupName, groupId, uniqIdForRow)


    def createGroupInputDiv(groupNameToGroupId: (String, String, String)): TypedTag[Div] = {
      val groupId = groupNameToGroupId._2

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

    Dynamic.global.console.log(s"groupNamesToGroupIds : ${groupNamesToGroupIds.toString()}")

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

    val inputAttributeClass = s"input-attribute-${buttonNameType.replace(" ", "")}"
    val theInput = attributeText match {
      case Some(inputText) =>
        if (buttonNameType == "Activity") {
          textarea(`type` := "text", `class` := s"form-control form-control-sm $inputAttributeClass", rows := "5",
            attr("data-tab-index") := tabIndex, attr("data-attribute-order-value") := getOrderNumber(maybeOrderNumber, tabIndex.toInt, inputAttributeClass),
            placeholder := s"Enter $buttonNameType - '${attributeText.toString}' ", value := s"${attributeText.getOrElse("")}")(s"${attributeText.getOrElse(s"Enter $buttonNameType")}")
        } else {
          input(`type` := "text", `class` := s"form-control form-control-sm $inputAttributeClass",
            attr("data-tab-index") := tabIndex, attr("data-attribute-order-value") := getOrderNumber(maybeOrderNumber, tabIndex.toInt, inputAttributeClass),
            placeholder := s"Enter $buttonNameType", value := s"${attributeText.getOrElse("")}")
        }
      case None =>
        if (buttonNameType == "Activity") {
          textarea(`type` := "text", `class` := s"form-control form-control-sm $inputAttributeClass", rows := "5",
            attr("data-tab-index") := tabIndex, attr("data-attribute-order-value") := getOrderNumber(maybeOrderNumber, tabIndex.toInt, inputAttributeClass),
            placeholder := s"Enter $buttonNameType")
        } else {
          input(`type` := "text", `class` := s"form-control form-control-sm $inputAttributeClass",
            attr("data-tab-index") := tabIndex, attr("data-attribute-order-value") := getOrderNumber(maybeOrderNumber, tabIndex.toInt, inputAttributeClass),
            placeholder := s"Enter $buttonNameType")
        }
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

  private[weekly] def deleteSingleRowFromClassPlan(): Unit = {
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


  private[weekly] def getOrderNumber(maybeOrderNumber: Option[Int],
                                     tabIndex: Int,
                                     inputAttributeClass: String
                                    ): Int = {
    maybeOrderNumber match {
      case Some(orderNumber) => orderNumber
      case None => generateOrderNumber(tabIndex, inputAttributeClass)
    }
  }

  def generateOrderNumber(tabIndexToSearch: Int,
                          inputAttributeClass: String): Int = {
    val attributeClassInputs = dom.document.getElementsByClassName(inputAttributeClass)
    val nodeListSize = attributeClassInputs.length

    var currentOrderNumMax = 0
    var index = 0
    while (index < nodeListSize) {
      val element = attributeClassInputs(index).asInstanceOf[HTMLElement]
      val tabIndexOnElement = element.getAttribute("data-tab-index").toInt
      if (tabIndexOnElement == tabIndexToSearch) {
        val orderNum = element.getAttribute("data-attribute-order-value").toInt
        if (orderNum > currentOrderNumMax) {
          currentOrderNumMax = orderNum
        }
      }
      index = index + 1
    }

    currentOrderNumMax + 1
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

  def createLessonDataDiv(lessonSummary: LessonSummary, tabIndex: Int): JsDom.TypedTag[Div] = {
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

  def createAddButton(buttonIdRoot: String, buttonDescription: String, index: Int, lessonSummary: LessonSummary): List[JsDom.TypedTag[Div]] = {
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

  def clickingOnAddToLessonsButtons(): Unit = {
    addButtonClickBehaviour("create-weekly-plans-add-to-lesson-button-add-activity", "Activity", true)
    addButtonClickBehaviour("create-weekly-plans-add-to-lesson-button-add-resource", "Resource", false)
    addButtonClickBehaviour("create-weekly-plans-add-to-lesson-button-add-learning-intention", "Learning Intention", true)
    addButtonClickBehaviour("create-weekly-plans-add-to-lesson-button-add-success-criteria", "Success Criteria", true)
    addButtonClickBehaviour("create-weekly-plans-add-to-lesson-button-add-plenary", "Plenary", false)
    addButtonClickBehaviour("create-weekly-plans-add-to-lesson-button-add-formative-assessment", "Formative Assessment", false)
    addButtonClickBehaviour("create-weekly-plans-add-to-lesson-button-add-note", "Note", false)
  }

  def cleanupModalAdds(): Unit = {
    cleanupActivity("create-weekly-plans-add-to-lesson-button-add-activity-div")
    cleanupActivity("create-weekly-plans-add-to-lesson-button-add-resource-div")
    cleanupActivity("create-weekly-plans-add-to-lesson-button-add-learning-intention-div")
    cleanupActivity("create-weekly-plans-add-to-lesson-button-add-success-criteria-div")
    cleanupActivity("create-weekly-plans-add-to-lesson-button-add-plenary-div")
    cleanupActivity("create-weekly-plans-add-to-lesson-button-add-formative-assessment-div")
    cleanupActivity("create-weekly-plans-add-to-lesson-button-add-note-div")
  }

  def cleanupActivity(elementId: String): Unit = {
    val activityDiv = dom.document.getElementById(elementId).asInstanceOf[HTMLDivElement]
    while (activityDiv != null && activityDiv.hasChildNodes()) {
      activityDiv.removeChild(activityDiv.lastChild)
    }
  }

  def addButtonClickBehaviour(buttonClassName: String, buttonNameType: String, applyToGroups: Boolean): Unit = {
    val addActivityButtons = dom.document.getElementsByClassName(buttonClassName)
    val nodeListSize = addActivityButtons.length

    var index = 0
    while (index < nodeListSize) {
      val addActivityButton = addActivityButtons(index).asInstanceOf[HTMLButtonElement]

      addActivityButton.addEventListener("click", (e: dom.Event) => {
        Dynamic.global.console.log(s"groups: ${
          getGroupIdsToName.keys.toString()
        }")

        val tabIndex = addActivityButton.getAttribute("data-tab-index")
        addAttributeRow(buttonClassName, buttonNameType, applyToGroups, tabIndex, None, None, None)
      })

      index = index + 1
    }
  }


  private def setSelectedButton(buttonElement: HTMLButtonElement) = {
    buttonElement.setAttribute("data-is-currently-selected", "true")
    buttonElement.style.backgroundColor = "white"
    buttonElement.style.borderColor = "green"
    buttonElement.style.fontSize = "0.8rem"
    buttonElement.style.color = "green"
    buttonElement.style.fontWeight = "bold"
  }

  private def updateDaysOfTheWeeksDate(): Unit = {
    currentlySelectMondayStartOfWeekDate match {
      case Some(dateOfTheMonday) =>
        val allDaysOfTheWeek = dom.document.getElementsByClassName("weekly-plan-dayoftheweek-specific-date")
        val nodeListSize = allDaysOfTheWeek.length
        var index = 0
        while (index < nodeListSize) {
          val mondayJsDate = new js.Date(dateOfTheMonday)
          val dayOfTheWeekDiv = allDaysOfTheWeek(index).asInstanceOf[HTMLDivElement]
          val dayOfWeek = dayOfTheWeekDiv.getAttribute("data-day-of-the-week")
          dayOfTheWeekDiv.innerHTML = generateNiceDate(dayOfWeek, mondayJsDate)
          index = index + 1
        }
      case None =>
    }
  }

  private def generateNiceDate(dayOfWeek: String, jsDateOfMonday: js.Date): String = {
    dayOfWeek.toUpperCase match {
      case "MONDAY" =>
        s"${jsDateOfMonday.getDate()} ${getMonthFromNumber(jsDateOfMonday.getMonth())}"
      case "TUESDAY" =>
        jsDateOfMonday.setDate(jsDateOfMonday.getDate() + 1)
        s"${jsDateOfMonday.getDate()} ${getMonthFromNumber(jsDateOfMonday.getMonth())}"
      case "WEDNESDAY" =>
        jsDateOfMonday.setDate(jsDateOfMonday.getDate() + 2)
        s"${jsDateOfMonday.getDate()} ${getMonthFromNumber(jsDateOfMonday.getMonth())}"
      case "THURSDAY" =>
        jsDateOfMonday.setDate(jsDateOfMonday.getDate() + 3)
        s"${jsDateOfMonday.getDate()} ${getMonthFromNumber(jsDateOfMonday.getMonth())}"
      case "FRIDAY" =>
        jsDateOfMonday.setDate(jsDateOfMonday.getDate() + 4)
        s"${jsDateOfMonday.getDate()} ${getMonthFromNumber(jsDateOfMonday.getMonth())}"
      case somethingElse =>
        Dynamic.global.console.log(s"ERROR: Do not recognise day '$dayOfWeek'")
        ""
    }
  }

  private def getMonthFromNumber(month: Int): String = {
    month match {
      case 0 => "January"
      case 1 => "February"
      case 2 => "March"
      case 3 => "April"
      case 4 => "May"
      case 5 => "June"
      case 6 => "July"
      case 7 => "August"
      case 8 => "September"
      case 9 => "October"
      case 10 => "November"
      case 11 => "December"
      case somethingElse =>
        Dynamic.global.console.log(s"ERROR: Do not recognise month code '${month}'")
        ""
    }
  }

  private[weekly] def getSelectedWeekButton(): Option[HTMLButtonElement] = {
    val allMondayButtons = dom.document.getElementsByClassName("template-weekly-planning-mondays-actual-monday-date-btn")
    val nodeListSize = allMondayButtons.length
    var index = 0
    while (index < nodeListSize) {
      val buttonElement = allMondayButtons(index).asInstanceOf[HTMLButtonElement]
      val buttonSelected = buttonElement.getAttribute("data-is-currently-selected").toBoolean
      if (buttonSelected) {
        return Some(buttonElement)
      }

      index = index + 1
    }
    None
  }

  private[weekly] def toShowOrNotShowPlanThisWeekButtonGivenTheWeekSelected(): Unit = {
    val planThisWeekButton = dom.document.getElementById("weekly-planning-plan-this-week-button").asInstanceOf[HTMLButtonElement]
    if (planThisWeekButton != null) {
      planThisWeekButton.style.display = "none"
    }
  }

  private[weekly] def clickingAMondayWeekButtonUpdatesDates(): Unit = {
    val allMondayButtons = dom.document.getElementsByClassName("template-weekly-planning-mondays-actual-monday-date-btn")
    val nodeListSize = allMondayButtons.length
    var index = 0
    while (index < nodeListSize) {
      val buttonElement = allMondayButtons(index).asInstanceOf[HTMLButtonElement]
      buttonElement.addEventListener("click", (e: dom.Event) => {
        setAllWeeklyMondayButtonsToDefault()
        setSelectedButton(buttonElement)
        setMondayDateToCurrentlySelectedWeek()
        //        toShowOrNotShowPlanThisWeekButtonGivenTheWeekSelected()

        val currentPathname = dom.document.location.pathname.toString
        currentlySelectMondayStartOfWeekDate match {
          case Some(mondaydate) =>
            val classId = dom.window.localStorage.getItem("classId")
            if (currentPathname.contains("createPlanForTheWeek")) {
              dom.window.location.href = s"/createPlanForTheWeek/$classId/$mondaydate"
            } else {
              dom.window.location.href = s"/weeklyViewOfWeeklyPlanning/$classId/$mondaydate"
            }
        }

      })
      index = index + 1
    }
  }

  private def setAllWeeklyMondayButtonsToDefault(): Unit = {
    val allMondayButtons = dom.document.getElementsByClassName("template-weekly-planning-mondays-actual-monday-date-btn")
    val nodeListSize = allMondayButtons.length
    var index = 0
    while (index < nodeListSize) {
      val buttonElement = allMondayButtons(index).asInstanceOf[HTMLButtonElement]
      buttonElement.setAttribute("data-is-currently-selected", "false")
      buttonElement.style.backgroundColor = defaultBackgroundColorOfWeekMondayButton
      buttonElement.style.borderColor = defaultBorderColorOfWeekMondayButton
      buttonElement.style.color = defaultBorderColorOfWeekMondayButton
      buttonElement.style.fontSize = defaultFontSize
      buttonElement.style.fontWeight = "normal"
      index = index + 1
    }
  }

  private[weekly] def addLessonPlanDetailsFromSavedStatus(): Unit = {
    val fullWeeklyPlanOfLessonsPickled = dom.window.localStorage.getItem("fullWeeklyPlanOfLessonsPickled")
    Dynamic.global.console.log("addLessonPlanDetailsFromSavedStatus 1")
    import upickle.default._
    Dynamic.global.console.log("addLessonPlanDetailsFromSavedStatus 2")
    val fullWeeklyPlanOfLessons: FullWeeklyPlanOfLessons = read[FullWeeklyPlanOfLessons](PlanningHelper.decodeAnyNonFriendlyCharacters(fullWeeklyPlanOfLessonsPickled))
    Dynamic.global.console.log(s"addLessonPlanDetailsFromSavedStatus 3a : ${fullWeeklyPlanOfLessons.subjectToWeeklyPlanOfSubject.keys.toString()}")
    Dynamic.global.console.log(s"addLessonPlanDetailsFromSavedStatus 3b : ${maybeSelectedPlanningArea.getOrElse("Nope")}")

    if (fullWeeklyPlanOfLessons.subjectToWeeklyPlanOfSubject.isDefinedAt(maybeSelectedPlanningArea.getOrElse("Nope"))) {
      Dynamic.global.console.log("addLessonPlanDetailsFromSavedStatus 4")
      val weeklyPlanOfSubject = fullWeeklyPlanOfLessons.subjectToWeeklyPlanOfSubject(maybeSelectedPlanningArea.getOrElse("Nope"))
      for (lesson: LessonPlan <- weeklyPlanOfSubject.lessons) {
        addAttributeDetailsForGroup(lesson.subject, lesson.lessonDateIso, lesson.startTimeIso, "Activity", lesson.activitiesPerGroup)
        addAttributeDetails(lesson.subject, lesson.lessonDateIso, lesson.startTimeIso, "Resource", lesson.resources)
        addAttributeDetailsForGroup(lesson.subject, lesson.lessonDateIso, lesson.startTimeIso, "Learning Intention", lesson.learningIntentionsPerGroup)
        addAttributeDetailsForGroup(lesson.subject, lesson.lessonDateIso, lesson.startTimeIso, "Success Criteria", lesson.successCriteriaPerGroup)
        addAttributeDetails(lesson.subject, lesson.lessonDateIso, lesson.startTimeIso, "Plenary", lesson.plenary)
        addAttributeDetailsForGroup(lesson.subject, lesson.lessonDateIso, lesson.startTimeIso, "Formative Assessment", lesson.formativeAssessmentPerGroup)
        addAttributeDetails(lesson.subject, lesson.lessonDateIso, lesson.startTimeIso, "Note", lesson.notesBefore)
      }
    }
  }

  def addAttributeDetailsForGroup(subject: String, lessonDate: String, lessonStartTime: String, attributeType: String,
                                  attributesPerGroup: Map[AttributeRowKey, List[String]]): Unit = {
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
        attributesPerGroup.nonEmpty
      ) {

        for (attrValue <- attributesPerGroup.keys.toList.sortBy(elem => elem.attributeOrderNumber)) {
          val tabIndex = buttonElement.getAttribute("data-tab-index")
          if(attributeType == "Activity"){
            Dynamic.global.console.log(s"addAttributeDetailsForGroup 3 : ${tabIndex}")
          }

          addAttributeRow(
            buttonElementClassType(dataAttributeType),
            dataAttributeType,
            true,
            tabIndex,
            Some(attrValue.attributeValue),
            Some(attributesPerGroup(attrValue)),
            Some(attrValue.attributeOrderNumber)
          )
        }
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

  def addAttributeDetails(subject: String, lessonDate: String, lessonStartTime: String, attributeType: String, attributeValues: List[AttributeRowKey]): Unit = {
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
        for (attrValue <- attributeValues.sortBy(elem => elem.attributeOrderNumber)) {
          val tabIndex = buttonElement.getAttribute("data-tab-index")
          addAttributeRow(buttonElementClassType(dataAttributeType), dataAttributeType, false, tabIndex, Some(attrValue.attributeValue), None, Some(attrValue.attributeOrderNumber))
        }
      }

      index = index + 1
    }
  }

  private[weekly] def extractLessonAttributesWithGroups(attributeType: String, tabIndexToLookFor: Int): Map[AttributeRowKey, List[String]] = {
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


  private[weekly] def extractLessonAttributes(attributeType: String, tabIndexToLookFor: Int): List[AttributeRowKey] = {
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

}
