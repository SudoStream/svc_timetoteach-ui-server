package timetoteach.planning.weekly

import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLDivElement}

import scala.scalajs.js
import scala.scalajs.js.Dynamic.global

object WeeklyPlanningJsScreen {

  private var currentlySelectMondayStartOfWeekDate: Option[String] = None

  var defaultBackgroundColorOfWeekMondayButton = "ghostwhite"
  var defaultBorderColorOfWeekMondayButton = "grey"
  var defaultColorOfWeekMondayButton = "grey"
  var defaultFontSize = "1rem"

  def loadJavascript(): Unit = {
    global.console.log("Loading Weekly Planning Javascript")
    simpleRenderClassTimetable()
    setMondayDateToCurrentlySelectedWeek()
    clickingAMondayWeekButtonUpdatesDates()
    planThisWeekButton()
    toShowOrNotShowPlanThisWeekButtonGivenTheWeekSelected()
  }

  private def getSelectedWeekButton(): Option[HTMLButtonElement] = {
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

  private def toShowOrNotShowPlanThisWeekButtonGivenTheWeekSelected(): Unit = {
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
        dom.window.location.href = s"/createPlanForTheWeek/$classId"
      })
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

  private def clickingAMondayWeekButtonUpdatesDates(): Unit = {
    val allMondayButtons = dom.document.getElementsByClassName("template-weekly-planning-mondays-actual-monday-date-btn")
    val nodeListSize = allMondayButtons.length
    var index = 0
    while (index < nodeListSize) {
      val buttonElement = allMondayButtons(index).asInstanceOf[HTMLButtonElement]
      buttonElement.addEventListener("click", (e: dom.Event) => {
        setAllWeeklyMondayButtonsToDefault()
        setSelectedButton(buttonElement)
        setMondayDateToCurrentlySelectedWeek()
        toShowOrNotShowPlanThisWeekButtonGivenTheWeekSelected()
      })
      index = index + 1
    }

  }

  private def setSelectedButton(buttonElement: HTMLButtonElement) = {
    buttonElement.setAttribute("data-is-currently-selected", "true")
    buttonElement.style.backgroundColor = "white"
    buttonElement.style.borderColor = "green"
    buttonElement.style.fontSize = "1.1rem"
    buttonElement.style.color = "green"
    buttonElement.style.fontWeight = "bold"
  }

  private def setMondayDateToCurrentlySelectedWeek(): Unit  = {
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
      global.console.log(s"Setting current Monday to ${currentlySelectMondayStartOfWeekDate.get}")
      updateDaysOfTheWeeksDate()
    } else {
      global.console.log(s"ERROR: There looks to be no Monday set!")
    }
  }

  private def getMonthFromNumber(month: Int): String  = {
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
        global.console.log(s"ERROR: Do not recognise month code '${month}'")
        ""
    }
  }

  private def generateNiceDate(dayOfWeek: String, jsDateOfMonday: js.Date): String  = {
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
        global.console.log(s"ERROR: Do not recognise day '$dayOfWeek'")
        ""
    }
  }

  private def updateDaysOfTheWeeksDate(): Unit  = {
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

  private def simpleRenderClassTimetable(): Unit  = {
    //    val theDaysSubjectsAsHtml = generateHtmlForWeeklyPlanning(classTimetable)
    //    val allTheDaysDiv = dom.document.getElementById("all-the-days-rows").asInstanceOf[HTMLDivElement]
    //    allTheDaysDiv.innerHTML = theDaysSubjectsAsHtml
  }

}
