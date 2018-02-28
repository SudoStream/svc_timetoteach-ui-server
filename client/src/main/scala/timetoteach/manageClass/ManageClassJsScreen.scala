package timetoteach.manageClass

import duplicate.model._
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.ext.Ajax.InputData
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLDivElement, HTMLInputElement, HTMLSelectElement}
import scalatags.JsDom

import scala.collection.mutable.ListBuffer
import scala.scalajs.js
import scala.scalajs.js.Dynamic.global
import scala.util.{Failure, Success}
import scalatags.JsDom.all.{`class`, div, _}
import timetoteach.addnewclass.AddNewClassJsScreen.groupCounter

import scala.collection.mutable

object ManageClassJsScreen
{
  var newClassName: Option[String] = None
  var newClassDescription: Option[String] = None
  var groupIdsToBeDeleted: scala.collection.mutable.Set[String] = scala.collection.mutable.Set.empty
  // var groupsEdited: scala.collection.mutable.Map[String, ] = scala.collection.mutable.Set.empty
  // var newGroupsAdded:

  var groupTypeCurrentlyAdding: Option[String] = None

  def loadJavascript(): Unit =
  {
    global.console.log("Adding js for Manage Class Screen")
    addDeleteGroupBehaviour()
    saveButton()
    makeSaveButtonEnabledIfStateHasChanged()
    addNewGroupButtons()
    addNewGroupSaveButton()
  }

  private def createAlerts(errors: Seq[String]): JsDom.Modifier =
  {
    div()(
     for(error <- errors) yield div(`class` := "alert alert-danger", role := "alert")(p(error))
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

            global.console.log(s"Creating group with values ... $groupName|$groupDescription|$groupValue|$groupLevel")

            if (ERRORS.nonEmpty) {
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
          case None =>
            global.console.log("Error: No group type is currently set")
        }
      })
    } else {
      global.console.log("Error: Could not find button 'add-new-groups-save-button'")
    }
  }

  def createNewGroupRow(): Unit =
  {
    //    <div class="manage-new-group-row">
    //      <div class="form-row manage-class-group-row" data-group-id="@group.groupId.id" data-group-type="spelling">
    //
    //        <div class="row in-app-menu-small-devices in-app-menu-small-devices-row">
    //          <div class="col-sm-12 text-muted"><small>Group Name</small></div>
    //          <div class="col-sm-12 ">
    //            <input type="text" name="groupName" class="form-control form-control-sm" disabled="true"
    //                   placeholder="@{
    //                                                    group.groupName.name
    //                                                }">
    //            </div>
    //          </div>
    //          <div class="col-lg-3 in-app-menu-medium-and-up">
    //            <input id="group-name-@group.groupId.id" type="text" name="groupName" class="form-control form-control-sm editable-input-button"
    //                   placeholder="@group.groupName.name" value="@group.groupName.name">
    //            </div>
    //
    //
    //            <div class="row in-app-menu-small-devices in-app-menu-small-devices-row">
    //              <div class="col-sm-12 text-muted"><small>Description</small></div>
    //              <div class="col-sm-12 ">
    //                <input type="text" name="groupDescription" class="form-control form-control-sm" disabled="true"
    //                       placeholder="@{
    //                                                    group.groupDescription.name
    //                                                }">
    //                </div>
    //              </div>
    //              <div class="col-lg-5 in-app-menu-medium-and-up">
    //                <input id="group-description-@group.groupId.id" type="text" name="groupDescription" class="form-control form-control-sm editable-input-button"
    //                       placeholder="@group.groupDescription.name" value="@group.groupDescription.name">
    //                </div>
    //
    //
    //                <div class="row in-app-menu-small-devices in-app-menu-small-devices-row">
    //                  <div class="col-sm-12 text-muted"><small>Level</small></div>
    //                  <div class="col-sm-12 ">
    //                    <input type="text" name="groupDescription" class="form-control form-control-sm" disabled="true"
    //                           placeholder="@{
    //                                                    group.groupLevel.value.toLowerCase.capitalize.replace("level", "")
    //    }">
    //    </div>
    //    </div>
    //    <div class="col-lg-2 in-app-menu-medium-and-up">
    //      <input id="group-level-@group.groupId.id" type="text" name="groupType" class="form-control form-control-sm" disabled="true"
    //             placeholder="@group.groupLevel.value.toLowerCase.capitalize.replace("level", "")">
    //    </div>
    //
    //    <div class="row in-app-menu-small-devices in-app-menu-small-devices-row">
    //      <br>
    //        <div class="col-sm-6 ">
    //          <button type="button" class="close  delete-this-class-group" aria-label="Close">
    //            <span aria-hidden="true">&times;</span>
    //          </button>
    //        </div>
    //      </div>
    //      <div class="col-lg-1 in-app-menu-medium-and-up">
    //        <button type="button" class="close delete-this-class-group" aria-label="Close">
    //          <span aria-hidden="true">&times;</span>
    //        </button>
    //      </div>
    //      <div class="col-lg-1 in-app-menu-medium-and-up"></div>
    //
    //    </div>
    //    </div>

  }

  def addNewGroupButtons(): Unit =
  {
    //    option(value := "Mathematics", "Mathematics"),
    //    option(value := "Reading", "Reading"),
    //    option(value := "Writing", "Writing"),
    //    option(value := "Spelling", "Spelling")

    addNewMathsGroupButton("add-new-maths-groups-button", "Mathematics")
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


  def makeSaveButtonEnabledIfStateHasChanged(): Unit =
  {
    val editableInputElems = dom.document.getElementsByClassName("editable-input-button")
    val nodeListSize = editableInputElems.length
    var index = 0
    while (index < nodeListSize) {
      val editableInputElement = editableInputElems(index).asInstanceOf[HTMLInputElement]
      editableInputElement.addEventListener("click", (e: dom.Event) => {
        val saveEditsButton = dom.document.getElementById("save-class-edit-changes-button").asInstanceOf[HTMLButtonElement]
        saveEditsButton.disabled = false
      })
      index = index + 1
    }

    ////

    val saveEditsButton = dom.document.getElementById("save-class-edit-changes-button").asInstanceOf[HTMLButtonElement]
    if (groupIdsToBeDeleted.nonEmpty) {
      saveEditsButton.disabled = false
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
        makeSaveButtonEnabledIfStateHasChanged()
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

  def saveButton(): Unit =
  {
    val saveEditsToClassButton = dom.document.getElementById("save-class-edit-changes-button").asInstanceOf[HTMLButtonElement]
    if (saveEditsToClassButton != null) {
      saveEditsToClassButton.addEventListener("click", (e: dom.Event) => {
        saveEditsToClassButton.disabled = true
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
      })
    }
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
      case Failure(ex) =>
        dom.window.alert("Something went wrong with creating new class. Specifically : -" +
          s"\n\n${ex.toString}")
    }
  }


}
