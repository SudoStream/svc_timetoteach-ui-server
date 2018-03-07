package timetoteach.planning.termly

import org.scalajs.dom
import org.scalajs.dom.raw.HTMLButtonElement

import scala.scalajs.js
import scala.scalajs.js.Dynamic.global

object TermlyPlanningForClassAllSubjectsJsScreen
{
  def loadJavascript(): Unit =
  {
    global.console.log("Loading Termly Planning For Class All Subjects Javascript")
    downloadPdf()
  }

  def downloadPdf(): Unit =
  {
    val downloadPdfButton = dom.document.getElementById("download-termly-plan-pdf").asInstanceOf[HTMLButtonElement]
    downloadPdfButton.addEventListener("click", (e: dom.Event) => {
      val $ = js.Dynamic.global.$
      $("#generating-pdf").modal("show", "backdrop: static", "keyboard : false")
    })
  }
}
