package timetoteach.manageClass

import duplicate.model._
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.ext.Ajax.InputData
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLDivElement, HTMLInputElement, HTMLSelectElement}
import scalatags.JsDom
import scalatags.JsDom.all.{`class`, div, _}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.scalajs.js
import scala.scalajs.js.Dynamic.global
import scala.util.{Failure, Success}

object ManageClassJsScreen
{
  var newClassName: Option[String] = None
  var newClassDescription: Option[String] = None
  var groupIdsToBeDeleted: scala.collection.mutable.Set[String] = scala.collection.mutable.Set.empty
  var newGroupsAdded: scala.collection.mutable.Set[String] = scala.collection.mutable.Set.empty

  var groupTypeCurrentlyAdding: Option[String] = None

  def loadJavascript(): Unit =
  {
    global.console.log("Adding js for Manage Class Screen")
    addDeleteGroupBehaviour()
    addNewGroupButtons()
    addNewGroupSaveButton()
    saveOnLostFocus()
    popovers()
  }

  def popovers(): Unit =
  {
    val groups = dom.document.getElementsByClassName("manage-new-group-row")
    val groupsCreated = groups.length
    global.console.log(s"Groups created == ${groupsCreated}")
    if (groupsCreated == 1) {
      val $ = js.Dynamic.global.$
      $(dom.document).ready(() => {
        dom.window.setTimeout(() => {
          $("[data-toggle=\"popover\"]").popover("show")
        }, 1000)

        dom.window.setTimeout(() => {
          $("[data-toggle=\"popover\"]").popover("hide")
        }, 20000)

      })
    }

  }

  private def createAlerts(errors: Seq[String]): JsDom.Modifier =
  {
    div()(
      for (error <- errors) yield div(`class` := "alert alert-danger", role := "alert")(p(error))
    )
  }

  def addNewGroupSaveButton(): Unit =
  {
    val addNewGroupSaveBtn = dom.document.getElementById("add-new-groups-save-button").asInstanceOf[HTMLButtonElement]
    if (addNewGroupSaveBtn != null) {
      addNewGroupSaveBtn.addEventListener("click", (e: dom.Event) => {
        groupTypeCurrentlyAdding match {
          case Some(groupValue) =>
            val errorsAlert = dom.document.getElementById("addNewGroupModalBodyErrorsAlert").asInstanceOf[HTMLDivElement]
            while (errorsAlert.firstChild != null) {
              errorsAlert.removeChild(errorsAlert.firstChild)
            }

            val ERRORS: scala.collection.mutable.Set[String] = scala.collection.mutable.Set.empty

            val groupName = dom.document.getElementById("add-group-name").asInstanceOf[HTMLInputElement].value
            if (groupName == null || groupName.isEmpty) {
              ERRORS += "Group Name should not be empty"
              dom.document.getElementById("add-group-name").asInstanceOf[HTMLInputElement].style.borderColor = "red"
            } else {
              dom.document.getElementById("add-group-name").asInstanceOf[HTMLInputElement].style.borderColor = "green"
            }

            val groupDescription = dom.document.getElementById("add-group-description").asInstanceOf[HTMLInputElement].value
            dom.document.getElementById("add-group-description").asInstanceOf[HTMLInputElement].style.borderColor = "green"

            val groupLevel = dom.document.getElementById("select-curriculum-level").asInstanceOf[HTMLSelectElement].value
            if (groupLevel == null || groupLevel.isEmpty || groupLevel == "Select") {
              ERRORS += "Must select a valid group level"
              dom.document.getElementById("select-curriculum-level").asInstanceOf[HTMLSelectElement].style.borderColor = "red"
            } else {
              dom.document.getElementById("select-curriculum-level").asInstanceOf[HTMLSelectElement].style.borderColor = "green"
            }

            if (ERRORS.isEmpty) {
              global.console.log(s"Creating group with values ... $groupName|$groupDescription|$groupValue|$groupLevel")
              val classId = dom.window.localStorage.getItem("classId")
              val groupId = GroupId(s"groupId_${java.util.UUID.randomUUID()}")
              val groupTypeModel = groupTypeFromString(groupValue)
              val groupNameModel = GroupName(groupName)
              val groupDescriptionModel = GroupDescription(groupDescription)
              val groupLevelModel = groupLevelFromString(groupLevel)

              createNewGroupRow(groupId, groupTypeModel, groupNameModel, groupDescriptionModel, groupLevelModel)

              newGroupsAdded.add(groupId.id)
              addDeleteGroupBehaviour()

              global.console.log("reload the javascript")

              val $ = js.Dynamic.global.$
              $("#addNewGroupModal").modal("hide")
              doSave()
            } else {
              showErrors(errorsAlert, ERRORS)
            }
          case None =>
            global.console.log("Error: No group type is currently set")
        }
      })
    } else {
      global.console.log("Error: Could not find button 'add-new-groups-save-button'")
    }
  }

  private def showErrors(errorsAlert: HTMLDivElement, ERRORS: mutable.Set[String]): Unit =
  {
    val errorsDiv = dom.document.getElementById("addNewGroupModalBodyErrorsDiv").asInstanceOf[HTMLDivElement]
    val alertsMessage = div()(
      div()(
        p("There was a problem with some of the input details:"),
        createAlerts(ERRORS.toSeq)
      )
    )
    val child = dom.document.createElement("div")
    child.innerHTML = alertsMessage.toString()
    while (errorsAlert.firstChild != null) {
      errorsAlert.removeChild(errorsAlert.firstChild)
    }
    errorsAlert.appendChild(child)
    errorsDiv.style.display = "block"
  }

  def createNewGroupRow(groupId: GroupId,
                        groupType: GroupType,
                        groupName: GroupName,
                        groupDescription: GroupDescription,
                        groupLevel: CurriculumLevel): Unit =
  {
    val newAddGroupRow = div(`class` := "manage-new-group-row")(
      div(`class` := "form-row manage-class-group-row",
        attr("data-group-id") := s"${groupId.id}", attr("data-group-type") := s"${groupType.value.toLowerCase}")(

        div(`class` := "col-lg-3 in-app-menu-medium-and-up")(
          input(`id` := s"group-name-${groupId.id}", `type` := "text", `name` := "groupName",
            `class` := "form-control form-control-sm editable-input-button",
            `style` := "border-color:deepskyblue;",
            `placeholder` := s"${groupName.name}", `value` := s"${groupName.name}")()
        ),

        div(`class` := "col-lg-5 in-app-menu-medium-and-up")(
          input(`id` := s"group-description-${groupId.id}", `type` := "text", `name` := "groupDescription",
            `class` := "form-control form-control-sm editable-input-button",
            `style` := "border-color:deepskyblue;",
            `placeholder` := s"${groupDescription.name}", `value` := s"${groupDescription.name}")()
        ),

        div(`class` := "col-lg-3 in-app-menu-medium-and-up")(
          input(`id` := s"group-level-${groupId.id}", `type` := "text", `name` := "groupType",
            `class` := "form-control form-control-sm editable-input-button",
            `style` := "border-color:deepskyblue;",
            `placeholder` := s"${groupLevel.value.toLowerCase.capitalize.replace("level", "")}",
            `disabled` := "true")()
        )
        ,
        div(`class` := "col-lg-1 in-app-menu-medium-and-up")(
          button(`class` := "close delete-this-class-group", attr("aria-label") := "Close")(
            span(attr("aria-hidden") := "true")(raw("&times;"))
          )
        )
      )
    )

    val child = dom.document.createElement("div")
    child.innerHTML = newAddGroupRow.toString()

    val newGroupsDiv = dom.document.getElementById(s"${groupType.value.toLowerCase}-groups-section").asInstanceOf[HTMLDivElement]
    if (newGroupsDiv != null) {
      newGroupsDiv.appendChild(child)
    } else {
      global.console.log(s"ERROR: Could not find div '${groupType.value.toLowerCase}-groups-section'")
    }
  }

  def addNewGroupButtons(): Unit =
  {
    addNewMathsGroupButton("add-new-maths-groups-button", "Mathematics")
    addNewMathsGroupButton("add-new-reading-groups-button", "Reading")
    addNewMathsGroupButton("add-new-writing-groups-button", "Writing")
    addNewMathsGroupButton("add-new-spelling-groups-button", "Spelling")
  }

  def clearAnyModalValuesFirst(): Unit =
  {
    val groupNameInput = dom.document.getElementById("add-group-name").asInstanceOf[HTMLInputElement]
    if (groupNameInput != null) {
      groupNameInput.value = ""
    }

    val groupDescriptionInput = dom.document.getElementById("add-group-description").asInstanceOf[HTMLInputElement]
    if (groupDescriptionInput != null) {
      groupDescriptionInput.value = ""
    }

    val groupLevelInput = dom.document.getElementById("select-curriculum-level").asInstanceOf[HTMLSelectElement]
    if (groupLevelInput != null) {
      groupLevelInput.selectedIndex = 0
      groupLevelInput.value = "Select"
    }

  }

  def addNewMathsGroupButton(elementId: String, groupType: String): Unit =
  {
    val addNewMathsGroupBtn = dom.document.getElementById(elementId).asInstanceOf[HTMLButtonElement]
    if (addNewMathsGroupBtn != null) {
      addNewMathsGroupBtn.addEventListener("click", (e: dom.Event) => {
        val modalHeader = dom.document.getElementById("addNewGroupModalHeader")
        if (modalHeader != null) {
          modalHeader.innerHTML = s"Add New ${groupType.capitalize} Group"
          groupTypeCurrentlyAdding = Some(groupType)


          clearAnyModalValuesFirst()
          val $ = js.Dynamic.global.$
          $("#addNewGroupModal").modal("show", "backdrop: static", "keyboard : false")
        } else {
          global.console.log("WARNING: Could not find button 'add-new-maths-groups-button'")
        }
      })
    } else {
      global.console.log(s"WARNING: Could not find button $elementId")
    }
  }

  def saveOnLostFocus(): Unit =
  {
    val editableInputElems = dom.document.getElementsByClassName("editable-input-button")
    val nodeListSize = editableInputElems.length
    var index = 0
    while (index < nodeListSize) {
      global.console.log("Adding Focus out behaviour")
      val editableInputElement = editableInputElems(index).asInstanceOf[HTMLInputElement]
      editableInputElement.addEventListener("focusout", (e: dom.Event) => {
        global.console.log("Focus out!")
        doSave()
      })
      index = index + 1
    }
  }

  def addDeleteGroupBehaviour(): Unit =
  {
    val deleteThisGroupButton = dom.document.getElementsByClassName("delete-this-class-group")
    val nodeListSize = deleteThisGroupButton.length
    var index = 0
    while (index < nodeListSize) {
      val theDeleteButton = deleteThisGroupButton(index).asInstanceOf[HTMLButtonElement]

      theDeleteButton.addEventListener("click", (e: dom.Event) => {
        val theRowDiv = theDeleteButton.parentNode.parentNode.asInstanceOf[HTMLDivElement]
        val groupId = theRowDiv.getAttribute("data-group-id")
        global.console.log(s"Deleting group with id '$groupId'")
        if (groupId != null) {
          groupIdsToBeDeleted.add(groupId)
        } else {
          global.console.log("ERROR: Could not find group id to delete")
        }

        theRowDiv.parentNode.removeChild(theRowDiv)
        doSave()
      })

      index = index + 1
    }
  }

  def valueOrPlaceholder(input: HTMLInputElement): String =
  {
    if (input.value != null && input.value.nonEmpty) {
      input.value
    } else {
      input.placeholder
    }
  }

  def groupTypeFromString(groupTypeStr: String): GroupType =
  {
    groupTypeStr.toLowerCase match {
      case "mathematics" => MathsGroupType
      case "reading" => ReadingGroupType
      case "writing" => WritingGroupType
      case "spelling" => SpellingGroupType
      case _ => OtherGroupType
    }
  }

  def groupLevelFromString(groupLevelStr: String): CurriculumLevel =
  {
    groupLevelStr.toLowerCase match {
      case "early" => EarlyLevel
      case "first" => FirstLevel
      case "second" => SecondLevel
      case "third" => ThirdLevel
      case "fourth" => FourthLevel
    }
  }

  def buildGroups(): List[Group] =
  {
    var updatedGroups: collection.mutable.ListBuffer[Group] = new ListBuffer[Group]()

    val manageClassGroupRow = dom.document.getElementsByClassName("manage-class-group-row")
    val nodeListSize = manageClassGroupRow.length
    var index = 0
    while (index < nodeListSize) {
      val theManageClassGroupRowDiv = manageClassGroupRow(index).asInstanceOf[HTMLDivElement]

      val groupId = GroupId(theManageClassGroupRowDiv.getAttribute("data-group-id"))
      val groupName: GroupName = GroupName(valueOrPlaceholder(
        dom.document.getElementById(s"group-name-${groupId.id}").asInstanceOf[HTMLInputElement])
      )
      val groupDescription: GroupDescription = GroupDescription(valueOrPlaceholder(
        dom.document.getElementById(s"group-description-${groupId.id}").asInstanceOf[HTMLInputElement])
      )

      val groupType: GroupType = groupTypeFromString(theManageClassGroupRowDiv.getAttribute("data-group-type"))
      val groupLevel: CurriculumLevel = groupLevelFromString(valueOrPlaceholder(
        dom.document.getElementById(s"group-level-${groupId.id}").asInstanceOf[HTMLInputElement])
      )

      println(s"Group Id: ${groupId.id}, GroupName: ${groupName.name}, GroupDescription: ${groupDescription.name}, " +
        s"Type: ${groupType.value}, level: ${groupLevel.value}")

      updatedGroups += Group(
        groupId,
        groupName,
        groupDescription,
        groupType,
        groupLevel
      )

      index = index + 1
    }

    println(s"All groups ===> ${updatedGroups.toString}")
    updatedGroups.toList
  }

  private def doSave() =
  {
    val $ = js.Dynamic.global.$
    $("#doing-stuff").modal("show", "backdrop: static", "keyboard : false")

    println("Saving edits to class ...")
    val tttUserId = dom.window.localStorage.getItem("timeToTeachUserId")
    val classId = dom.window.localStorage.getItem("classId")
    val className = valueOrPlaceholder(dom.document.getElementById("className").asInstanceOf[HTMLInputElement])
    val classDescription = valueOrPlaceholder(dom.document.getElementById("classDescription").asInstanceOf[HTMLInputElement])
    val groups: List[Group] = buildGroups()
    println(s"timeToTeachUserId = '$tttUserId'\n" +
      s"classId = '$classId'\n" +
      s"ACTION: Removing ${groupIdsToBeDeleted.size} groups from class\n" +
      s"New class name? ${newClassName.isDefined}\n" +
      s"New class description? ${newClassDescription.isDefined}")

    println(s"All the groups : ${groups.toString}")

    val newClassDetails = ClassDetails(
      id = ClassId(classId),
      schoolDetails = null,
      className = ClassName(className),
      classDescription = ClassDescription(classDescription),
      groups = groups,
      classTeachersWithWriteAccess = List(tttUserId),
      subjectToMaybeGroupMap = Map()
    )

    import upickle.default.{ReadWriter => RW, _}
    val classDetailsPickled = write[ClassDetails](newClassDetails)
    println(s"Edited Class Details Pickled: ###$classDetailsPickled###")
    saveEditedClass(classDetailsPickled)
    popovers()
  }

  def saveEditedClass(classDetailsPickled: String): Any =
  {
    import scala.concurrent.ExecutionContext.Implicits.global

    val theUrl = "/editclass"
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
        dom.window.setTimeout(() => {
          val $ = js.Dynamic.global.$
          $("#doing-stuff").modal("hide")
          println(s"lets goto group planning overview")
          val classId = dom.window.localStorage.getItem("classId")
          dom.window.location.href = s"/manageclass/$classId"
        }, 10)
      case Failure(ex) =>
        dom.window.alert("Something went wrong with creating new class. Specifically : -" +
          s"\n\n${ex.toString}")
    }
  }


}
