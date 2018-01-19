package timetoteach.classesHome

import org.scalajs.dom
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLDivElement}

import scala.scalajs.js
import scalatags.JsDom
import scalatags.JsDom.all.{`class`, div, span, _}

object ClassHomeJsScreen {

  def loadJavascript(): Unit = {
    deleteClassBehaviour()
  }

  private var classIdToDelete: Option[String] = None
  private var classNameToDelete: Option[String] = None
  private var currentUserId: Option[String] = None

  def deleteClassBehaviour(): Unit = {
    val deleteTeacherClassButtons = dom.document.getElementsByClassName("delete-teacher-class-btn")
    val nodeListSize = deleteTeacherClassButtons.length
    var index = 0
    while (index < nodeListSize) {
      val deleteTeacherClassButton = deleteTeacherClassButtons(index).asInstanceOf[HTMLButtonElement]
      deleteTeacherClassButton.addEventListener("click", (e: dom.Event) => {
        val classId = deleteTeacherClassButton.getAttribute("data-class-id")
        val className = deleteTeacherClassButton.getAttribute("data-class-name")
        currentUserId = Some(deleteTeacherClassButton.getAttribute("data-user-id"))
        classIdToDelete = Some(classId)
        classNameToDelete = Some(className)
        println(s"Deleting the class $classId")

        addModalAreYouSureWantToDeleteClass()
        addYesDeleteTheClassBehaviour()

        val $ = js.Dynamic.global.$
        $("#modalAreYouSureWantToDeleteClass").modal("show", "backdrop: static", "keyboard : false")
        println("Can you see modal?")
      })
      index = index + 1
    }
  }

  def addYesDeleteTheClassBehaviour(): Unit = {
    val yesDeleteTheClassButton = dom.document.getElementById("yes-delete-the-class-btn").asInstanceOf[HTMLButtonElement]
    yesDeleteTheClassButton.addEventListener("click", (e: dom.Event) => {
      println(s"YES to deleting class ${classNameToDelete.getOrElse("[NO CLASS FOUND]")}")

      //TODO: call correct delete route from here

      val $ = js.Dynamic.global.$
      $("#modalAreYouSureWantToDeleteClass").modal("hide")
    })
  }

  def addModalAreYouSureWantToDeleteClass(): Unit = {
    removeAnyOlderModal()
    val child = dom.document.createElement("div")
    child.innerHTML = modalAreYouSureWantToDeleteClass().toString()
    val newGroupsDiv = dom.document.getElementById("divForModalAreYouSureWantToDeleteClass").asInstanceOf[Div]
    newGroupsDiv.appendChild(child)
  }

  private def removeAnyOlderModal() = {
    val oldModal = dom.document.getElementById("modalAreYouSureWantToDeleteClass").asInstanceOf[Div]
    if (oldModal != null) {
      oldModal.parentElement.removeChild(oldModal)
    }
  }

  def modalAreYouSureWantToDeleteClass(): JsDom.TypedTag[Div] = {
    div(`id` := "modalAreYouSureWantToDeleteClass", `class` := "modal", attr("tabindex") := "-1", role := "dialog")(
      div(`class` := "modal-dialog", role := "document")(
        div(`class` := "modal-content")(
          div(`class` := "modal-header")(
            h5(`class` := "modal-title")("Delete Class"),
            button(`type` := "button", `class` := "close", attr("data-dismiss") := "modal", attr("aria-label") := "Close")(
              span(attr("aria-hidden") := "true")(raw("&times;"))
            )
          ),
          div(`class` := "modal-body")(
            p("Are you sure you want to delete the class, ", strong(classNameToDelete.getOrElse("").toString), "?")
          ),
          div(`class` := "modal-footer")(
            button(`id`:= "yes-delete-the-class-btn",  `type` := "button", `class` := "btn btn-primary")("Yes"),
            button(`type` := "button", `class` := "btn btn-secondary", attr("data-dismiss") := "modal")("No")
          )
        )
      )
    )
  }

}
