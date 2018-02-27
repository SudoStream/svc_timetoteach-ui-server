package timetoteach.manageClass

import duplicate.model._
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.ext.Ajax.InputData
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLDivElement, HTMLInputElement}

import scala.collection.mutable.ListBuffer
import scala.scalajs.js.Dynamic.global
import scala.util.{Failure, Success}

object ManageClassJsScreen
{
  var newClassName: Option[String] = None
  var newClassDescription: Option[String] = None
  var groupIdsToBeDeleted: scala.collection.mutable.Set[String] = scala.collection.mutable.Set.empty
  // var groupsEdited: scala.collection.mutable.Map[String, ] = scala.collection.mutable.Set.empty
  // var newGroupsAdded:

  def loadJavascript(): Unit =
  {
    global.console.log("Adding js for Manage Class Screen")
    addDeleteGroupBehaviour()
    saveButton()
    makeSaveButtonEnabledIfStateHasChanged()
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
