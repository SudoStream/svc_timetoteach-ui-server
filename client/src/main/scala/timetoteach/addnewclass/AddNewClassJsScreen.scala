package timetoteach.addnewclass

import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLDivElement, HTMLInputElement}
import shared.model.classdetail.ClassDetails

import scala.scalajs.js.Dynamic.global
import scalatags.JsDom.all.{`class`, div, _}

object AddNewClassJsScreen {

  private var currentGroups = List[ClassDetails]()
  private var groupCounter = 0

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
            `name` := s"add-group-name-$groupCounter",
            placeholder := "Enter group name"
          )),

          div(`class` := "col-sm-1"),

          div(`class` := "col-sm-3")(div(
            select(name := s"select-group-type-$groupCounter",
              `class` := "btn btn-outline-info",
              option(value := "Select", "Select Group Type ... "),
              option(value := "Maths", "Maths"),
              option(value := "Literacy", "Literacy"),
              option(value := "Other", "Other")
            )
          )),

          div(`class` := "col-sm-3")(div(
            select(name := s"select-curriculum-level-$groupCounter",
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
        addDeleteNewGroupRowBehaviour()
      })

    } else {
      println("ERROR : There is no button called add-new-class-groups-div")
    }
  }

  def addDeleteNewGroupRowBehaviour(): Unit = {
    var counter = 1
    while (counter <= groupCounter) {
      val deleteNewGroupRowButton =
        dom.document.getElementById(s"delete-new-class-group-row-button-$counter").asInstanceOf[HTMLButtonElement]

      if (deleteNewGroupRowButton != null) {
        deleteNewGroupRowButton.addEventListener("click", (e: dom.Event) => {
          println("Delete row of new group ...")
          val rowToDelete = deleteNewGroupRowButton.parentElement.parentElement.parentElement
          val newGroupsDiv = dom.document.getElementById("add-new-class-groups-div").asInstanceOf[HTMLDivElement]
          newGroupsDiv.removeChild(rowToDelete)
          addDeleteNewGroupRowBehaviour()
        })
      }
      counter = counter + 1
    }

  }

  def addErrorToPage(errorText: String): Unit = {
    val errorsDiv = dom.document.getElementById("add-new-class-form-errors").asInstanceOf[HTMLDivElement]
    val newErrorDiv = div(`class` := "add-new-class-form-error alert alert-danger fade show text-center")(p(errorText))
    val newError = dom.document.createElement("div")
    newError.innerHTML = newErrorDiv.toString()
    errorsDiv.appendChild(newError)
  }

  def validateNewClassName(newClassName: HTMLInputElement): Unit = {
    println(s"Validating new class name '${newClassName.value}'")
    if (newClassName.value.isEmpty) {
      newClassName.style.borderColor = "red"
      val errorsDiv = dom.document.getElementById("add-new-class-form-errors").asInstanceOf[HTMLDivElement]
      addErrorToPage("The new class name must not be empty")
    } else {
      newClassName.style.borderColor = "lightgreen"
    }
  }

  def clearFormErrors(): Unit = {
    val errorsDiv = dom.document.getElementById("add-new-class-form-errors").asInstanceOf[HTMLDivElement]
    while (errorsDiv.hasChildNodes()) {
      errorsDiv.removeChild(errorsDiv.lastChild)
    }
  }


  def saveButton(): Unit = {
    clearFormErrors()

    val saveNewClassButton = dom.document.getElementById("save-new-class-button").asInstanceOf[HTMLButtonElement]
    if (saveNewClassButton != null) {
      saveNewClassButton.addEventListener("click", (e: dom.Event) => {
        println("Saving new class ...")

        val newClassName = dom.document.getElementById("className").asInstanceOf[HTMLInputElement]
        validateNewClassName(newClassName)


        val newGroupsDiv = dom.document.getElementById("add-new-class-groups-div").asInstanceOf[HTMLDivElement]
        newGroupsDiv
      })
    }
  }

  def loadJavascript(): Unit = {
    addNewClassGroupsDiv()
    saveButton()
  }

}
