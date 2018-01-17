package timetoteach.addnewclass

import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLDivElement, HTMLInputElement}
import shared.model.classdetail.ClassDetails

import scala.collection.mutable.ListBuffer
import scala.scalajs.js.Dynamic.global
import scalatags.JsDom.all.{`class`, div, _}

object AddNewClassJsScreen {

  private var classDetails: Option[ClassDetails] = None
  private var className: Option[String] = None
  private var groupCounter = 0
  private var errorCount = 0
  private var errorMessages: collection.mutable.ListBuffer[String] = new ListBuffer[String]()


  def addNewClassGroupsDiv(): Unit = {
    val addNewGroupButton = dom.document.getElementById("add-new-class-groups-button").asInstanceOf[HTMLButtonElement]
    if (addNewGroupButton != null) {
      // If this button is clicked
      addNewGroupButton.addEventListener("click", (e: dom.Event) => {
        global.console.log("Add new group ...")
        groupCounter = groupCounter + 1

        // Add a new line with drop downs etc
        // e.g. [Enter Group Name] / [Select Group Type] / [Select Group Level]
        val newAddGroupRow = div(`class` := "row add-new-group-row")(
          div(`class` := "col-sm-3")(input(
            `type` := "text",
            `class` := "form-control",
            id := s"add-group-name-$groupCounter",
            `name` := s"add-group-name-$groupCounter",
            placeholder := "Enter group name"
          )),

          div(`class` := "col-sm-1"),

          div(`class` := "col-sm-3")(div(
            select(
              name := s"select-group-type-$groupCounter",
              id := s"select-group-type-$groupCounter",
              `class` := "btn btn-outline-info",
              option(value := "Select", "Select Group Type ... "),
              option(value := "Maths", "Maths"),
              option(value := "Literacy", "Literacy"),
              option(value := "Other", "Other")
            )
          )),

          div(`class` := "col-sm-3")(div(
            select(
              id := s"select-curriculum-level-$groupCounter",
              name := s"select-curriculum-level-$groupCounter",
              `class` := "btn btn-outline-info",
              option(value := "Select", "Select Curriculum Level ... "),
              option(value := "Early", "Early"),
              option(value := "First", "First"),
              option(value := "Second", "Second"),
              option(value := "Third", "Third"),
              option(value := "Fourth", "Fourth")
            )
          )),

          div(`class` := "col-sm-1")(
            button(
              id := s"delete-new-class-group-row-button-$groupCounter",
              `type` := "button",
              `class` := "close delete-new-class-group-row",
              attr("data-counter-value") := s"$groupCounter",
              attr("aria-label") := "delete")(
              span(attr("aria-hidden") := "true")(raw("&times;"))
            )
          )
        )

        val child = dom.document.createElement("div")
        child.innerHTML = newAddGroupRow.toString()

        val newGroupsDiv = dom.document.getElementById("add-new-class-groups-div").asInstanceOf[HTMLDivElement]
        newGroupsDiv.appendChild(child)
        addDeleteNewGroupRowBehaviour(s"delete-new-class-group-row-button-$groupCounter")
      })

    } else {
      println("ERROR : There is no button called add-new-class-groups-div")
    }
  }

  def addDeleteNewGroupRowBehaviour(deleteButtonId: String): Unit = {
    val deleteNewGroupRowButton = dom.document.getElementById(deleteButtonId).asInstanceOf[HTMLButtonElement]
    if (deleteNewGroupRowButton != null) {
      deleteNewGroupRowButton.addEventListener("click", (e: dom.Event) => {
        println(s"Delete row of new group ($deleteButtonId)...")
        if (deleteNewGroupRowButton != null) {
          deleteNewGroupRowButton.parentElement.parentElement.parentElement.removeChild(deleteNewGroupRowButton.parentElement.parentElement)
        }
      })
    }

  }

  def addErrorToPage(errorText: String): Unit = {
    errorCount = errorCount + 1
    if (!errorMessages.contains(errorText)) {
      errorMessages += errorText
      val errorsDiv = dom.document.getElementById("add-new-class-form-errors").asInstanceOf[HTMLDivElement]
      val newErrorDiv = div(`class` := "add-new-class-form-error alert alert-danger fade show text-center")(errorText)
      val newError = dom.document.createElement("div")
      newError.innerHTML = newErrorDiv.toString()
      errorsDiv.appendChild(newError)
    }
  }

  def validateNewClassName(newClassName: HTMLInputElement): Unit = {
    println(s"Validating new class name '${newClassName.value}'")
    if (newClassName.value.isEmpty) {
      newClassName.style.borderColor = "red"
      val errorsDiv = dom.document.getElementById("add-new-class-form-errors").asInstanceOf[HTMLDivElement]
      addErrorToPage("The new class name must not be empty")
    } else {
      newClassName.style.borderColor = "lightgreen"
      className = Some(newClassName.value)
    }
  }

  def clearFormErrors(): Unit = {
    errorCount = 0
    errorMessages.clear()
    val errorsDiv = dom.document.getElementById("add-new-class-form-errors").asInstanceOf[HTMLDivElement]
    while (errorsDiv.hasChildNodes()) {
      errorsDiv.removeChild(errorsDiv.lastChild)
    }
    errorsDiv.style.display = "none"
  }

  def showErrors(): Unit = {
    if (errorCount > 0) {
      val errorsDiv = dom.document.getElementById("add-new-class-form-errors").asInstanceOf[HTMLDivElement]
      errorsDiv.style.display = "block"
    }
  }

  def validateClassGroupName(groupName: HTMLInputElement): Unit = {
    println(s"Validating group name '${groupName.value}'")
    if (groupName.value.isEmpty) {
      groupName.style.borderColor = "red"
      val errorsDiv = dom.document.getElementById("add-new-class-form-errors").asInstanceOf[HTMLDivElement]
      addErrorToPage("Group names must not be empty")
    } else {
      groupName.style.borderColor = "lightgreen"
      className = Some(groupName.value)
    }
  }

  def validateClassGroups(): Unit = {
    var counter = 1
    while (counter <= groupCounter) {
      val groupName = dom.document.getElementById(s"add-group-name-$counter").asInstanceOf[HTMLInputElement]
      if ( groupName != null) {
        println(s"groupName: ${groupName.value}")
        validateClassGroupName(groupName)
        val selected = dom.document.getElementById(s"select-curriculum-level-$counter")
        // TODO
      }
      counter = counter + 1
    }
  }


  def saveButton(): Unit = {
    val saveNewClassButton = dom.document.getElementById("save-new-class-button").asInstanceOf[HTMLButtonElement]
    if (saveNewClassButton != null) {
      saveNewClassButton.addEventListener("click", (e: dom.Event) => {
        clearFormErrors()
        println("Saving new class ...")

        val newClassName = dom.document.getElementById("className").asInstanceOf[HTMLInputElement]
        validateNewClassName(newClassName)
        validateClassGroups()

        val newGroupsDiv = dom.document.getElementById("add-new-class-groups-div").asInstanceOf[HTMLDivElement]
        newGroupsDiv

        showErrors()
      })
    }
  }

  def loadJavascript(): Unit = {
    addNewClassGroupsDiv()
    saveButton()
  }

}
