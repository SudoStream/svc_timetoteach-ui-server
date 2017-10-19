package timetoteach

import org.scalajs.dom
import org.scalajs.dom.raw.HTMLInputElement
import timetoteach.screens.ClassTimetable

import scala.scalajs.js

object TimeToTeachApp extends js.JSApp {

  val child = dom.document.createElement("div")
  child.textContent = "Hi from Scala-js-dom"
  child.id = "inputToAddLessonMonday-class-begin-end"

  override def main(): Unit = {

    ClassTimetable.loadClassTimetableJavascript()

    val classTimetableSectionRoot = dom.document.getElementById("class-timetable-section")
    //    classTimetableSectionRoot.textContent = "I'm back ... " + SharedMessages.httpMainTitle

    val inputMondayLesson = dom.document.getElementById("inputToAddLessonMonday")
    inputMondayLesson.addEventListener("click", (e: dom.Event) => {

      e.currentTarget match {
        case input: HTMLInputElement =>
          if (input.checked) {
            val inputWrapper = dom.document.getElementById("inputToAddLessonMonday-wrapper")
            inputWrapper.appendChild(child)
          } else {
            val inputWrapper = dom.document. getElementById("inputToAddLessonMonday-wrapper")
            inputWrapper.removeChild(child)
          }
        case _ => dom.window.alert("Nope")
      }

    })


  }
}
