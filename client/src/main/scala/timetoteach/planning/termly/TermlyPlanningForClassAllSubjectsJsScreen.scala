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
    popovers()
  }

  private def downloadPdf(): Unit =
  {
    val downloadPdfButton = dom.document.getElementById("download-termly-plan-pdf").asInstanceOf[HTMLButtonElement]
    downloadPdfButton.addEventListener("click", (e: dom.Event) => {
      val $ = js.Dynamic.global.$
      $("#generating-pdf").modal("show", "backdrop: static", "keyboard : false")
    })
  }

  private def popovers(): Unit =
  {
    val $ = js.Dynamic.global.$

    $(dom.document).ready(() => {
      dom.window.setTimeout(() => {
        $("[data-toggle=\"popover\"]").popover("show")
      }, 1000)

      dom.window.setTimeout(() => {
        $("[data-toggle=\"popover\"]").popover("hide")
      }, 10000)

    })

  }

}
