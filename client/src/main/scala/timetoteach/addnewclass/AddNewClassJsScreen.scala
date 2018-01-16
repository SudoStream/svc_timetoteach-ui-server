package timetoteach.addnewclass

import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLDivElement}

import scala.scalajs.js.Dynamic.global
import scalatags.JsDom.all.{`class`, div, _}

object AddNewClassJsScreen {

  private var currentGroups = List()
  private var groupCounter = 0

  def addNewClassGroupsDiv(): Unit = {
    val addNewGroupButton = dom.document.getElementById("add-new-class-groups-button").asInstanceOf[HTMLButtonElement]
    if (addNewGroupButton != null) {
      // If this button is clicked
      addNewGroupButton.addEventListener("click", (e: dom.Event) => {
        global.console.log("Add new group ...")

        // Add a new line with drop downs etc
        // e.g. [Enter Group Name] / [Select Group Type] / [Select Group Level]
        val newAddGroupRow = div(`class` := "row add-new-group-row")(
          div(`class` := "col-sm-4")(input(
            `type` := "text",
            `class` := "form-control",
            `name` := s"addGroup${currentGroups.size + 1}",
            placeholder := s"Enter group name (${currentGroups.size + 1})"
          )),
          div(`class` := "col-sm-4")(div("hello2")),
          div(`class` := "col-sm-4")(div("hello3"))
        )

        val newGroupsDiv = dom.document.getElementById("add-new-class-groups-div").asInstanceOf[HTMLDivElement]
        newGroupsDiv.innerHTML = newGroupsDiv.innerHTML + newAddGroupRow.toString()
      })


    } else {
      println("ERROR : There is no button called add-new-class-groups-div")
    }
  }

  def loadJavascript(): Unit = {
    addNewClassGroupsDiv()
  }

}
