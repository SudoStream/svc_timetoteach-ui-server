package timetoteach.planning.weekly

import org.scalajs.dom
import org.scalajs.dom.raw.HTMLButtonElement

import scala.scalajs.js
import scala.scalajs.js.Dynamic.global

object WeeklyPlanningJsScreen extends WeeklyPlansCommon {

  def loadJavascript(): Unit = {
    global.console.log("Loading Weekly Planning Javascript")
    setMondayDateToCurrentlySelectedWeek()
    clickingAMondayWeekButtonUpdatesDates()
    planThisWeekButton()
//    toShowOrNotShowPlanThisWeekButtonGivenTheWeekSelected()
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
