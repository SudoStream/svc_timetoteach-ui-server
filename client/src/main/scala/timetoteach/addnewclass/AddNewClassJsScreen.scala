package timetoteach.addnewclass

import org.scalajs.dom
import org.scalajs.dom.ext.Ajax.InputData
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLDivElement, HTMLInputElement, HTMLSelectElement}
import shared.model.classdetail._

import scala.collection.mutable.ListBuffer
import scala.scalajs.js.Dynamic.global
import scala.util.{Failure, Success}
import scalatags.JsDom.all.{`class`, div, _}
import dom.ext.Ajax

object AddNewClassJsScreen {

  private var classDetails: Option[ClassDetails] = None
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

  def validateNewClassName(newClassName: HTMLInputElement): Option[String] = {
    println(s"Validating new class name '${newClassName.value}'")
    if (newClassName.value.isEmpty) {
      newClassName.style.borderColor = "red"
      val errorsDiv = dom.document.getElementById("add-new-class-form-errors").asInstanceOf[HTMLDivElement]
      addErrorToPage("The new class name must not be empty")
      None
    } else {
      newClassName.style.borderColor = "lightgreen"
      Some(newClassName.value)
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

  def validateClassGroupName(groupName: HTMLInputElement): Option[String] = {
    println(s"Validating group name '${groupName.value}'")
    if (groupName.value.isEmpty) {
      groupName.style.borderColor = "red"
      val errorsDiv = dom.document.getElementById("add-new-class-form-errors").asInstanceOf[HTMLDivElement]
      addErrorToPage("Group names must not be empty")
      None
    } else {
      groupName.style.borderColor = "lightgreen"
      Some(groupName.value)
    }
  }

  def validateSelection(selectedElement: HTMLSelectElement, errorMsg: String): Option[String] = {
    if (selectedElement.value.contains("...") || selectedElement.value == "Select") {
      selectedElement.style.borderColor = "red"
      addErrorToPage(errorMsg)
      None
    } else {
      selectedElement.style.borderColor = "lightgreen"
      Some(selectedElement.value)
    }
  }

  def validateClassGroups(): Option[List[Group]] = {
    var counter = 1
    val maybeGroups: ListBuffer[Option[Group]] = new ListBuffer[Option[Group]]()
    while (counter <= groupCounter) {
      val groupNameInputElement = dom.document.getElementById(s"add-group-name-$counter").asInstanceOf[HTMLInputElement]
      if (groupNameInputElement != null) {
        val maybeGroup = for {
          groupName <- validateClassGroupName(groupNameInputElement)

          selectedGroupType = dom.document.getElementById(s"select-group-type-$counter").asInstanceOf[HTMLSelectElement]
          groupTypeMaybe <- validateSelection(selectedGroupType, "Need to select a group type, e.g. Maths, Literacy etc")
          groupType = {
            println(s"groupTypeMaybe = '$groupTypeMaybe'")
            groupTypeMaybe match {
              case "Maths" => MathsGroupType
              case "Literacy" => LiteracyGroupType
              case _ => OtherGroupType
            }
          }

          selectedCurriculumLevel = dom.document.getElementById(s"select-curriculum-level-$counter").asInstanceOf[HTMLSelectElement]
          groupLevelMaybe <- validateSelection(selectedCurriculumLevel, "Need to select a curriculum level, e.g. Early, First etc")
          groupLevel = {
            println(s"groupLevelMaybe = '$groupLevelMaybe'")
            groupLevelMaybe match {
              case "Early" => EarlyLevel
              case "First" => FirstLevel
              case "Second" => SecondLevel
              case "Third" => ThirdLevel
              case "Fourth" => FourthLevel
            }
          }
        } yield Group(GroupId(s"groupId_${java.util.UUID.randomUUID()}"), GroupName(groupName), groupType, groupLevel)

        maybeGroups += maybeGroup
      }
      counter = counter + 1
    }

    val groups = {
      for {
        group <- maybeGroups
        if group.isDefined
        groupYes <- group
      } yield groupYes
    }.toList

    if (groups.isEmpty) {
      None
    } else {
      Some(groups)
    }
  }

  def saveNewClass(classDetailsPickled: String): Any = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val theUrl = "/savenewclass"
    val theHeaders = Map(
      "Content-Type" -> "application/x-www-form-urlencoded",
      "X-Requested-With" -> "Accept"
    )
    val theTimeToTeachUserId = dom.window.localStorage.getItem("timeToTeachUserId")
    val theData = InputData.str2ajax(s"newClassPickled=$classDetailsPickled&tttUserId=$theTimeToTeachUserId")

    Ajax.post(
      url = theUrl,
      headers = theHeaders,
      data = theData
    ).onComplete {
      case Success(xhr) =>
        val responseText = xhr.responseText
        println(s"response = '$responseText'")
        dom.window.location.href = "/classes";
      case Failure(ex) =>
        dom.window.alert("Something went wrong with creating new class. Specifically : -" +
          s"\n\n${ex.toString}")
    }

  }

  def saveButton(): Unit = {
    val saveNewClassButton = dom.document.getElementById("save-new-class-button").asInstanceOf[HTMLButtonElement]
    if (saveNewClassButton != null) {
      saveNewClassButton.addEventListener("click", (e: dom.Event) => {
        clearFormErrors()
        println("Saving new class ...")

        val newClassName = dom.document.getElementById("className").asInstanceOf[HTMLInputElement]
        val classDetails = for {
          className <- validateNewClassName(newClassName)
          groups <- validateClassGroups()
        } yield ClassDetails(
          ClassId(s"classId_${java.util.UUID.randomUUID()}"),
          ClassName(className),
          groups,
          Nil
        )

        if (errorCount > 0) {
          showErrors()
        } else {
          println(s"Class Details: ${classDetails.toString}")
          import upickle.default._
          val classDetailsPickled = write(classDetails)
          println(s"Class Details Pickled: $classDetailsPickled")
          saveNewClass(classDetailsPickled)
        }
      })
    }
  }

  def loadJavascript(): Unit = {
    addNewClassGroupsDiv()
    saveButton()
  }

}
