package timetoteach.addnewclass

import org.scalajs.dom
import org.scalajs.dom.raw.HTMLButtonElement

object AddNewClassJsScreen {

  def addNewClassGroupsDiv(): Unit = {
    val addNewGroupButton = dom.document.getElementById("add-new-class-groups-div").asInstanceOf[HTMLButtonElement]
    if ( addNewGroupButton != null) {
      // If this button is clicked

      // Add a new line with drop downs etc
      // e.g. [Enter Group Name] / [Select Group Type] / [Select Group Level]
    } else {
      println("ERROR : There is no button called add-new-class-groups-div")
    }
  }

  def loadJavascript(): Unit = {
    addNewClassGroupsDiv()
  }

}
