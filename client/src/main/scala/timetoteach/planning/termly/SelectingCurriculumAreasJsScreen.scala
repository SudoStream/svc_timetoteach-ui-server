package timetoteach.planning.termly

import org.scalajs.dom
import org.scalajs.dom.raw.HTMLDivElement

import scala.scalajs.js.Dynamic.global

object SelectingCurriculumAreasJsScreen
{

  def loadJavascript(): Unit =
  {
    global.console.log("Loading Selecting Curriculum Areas Javascript")
//    popovers()
  }

  private def popovers(): Unit =
  {
    val alert = dom.document.getElementById("es-and-os-alert").asInstanceOf[HTMLDivElement]
    if (alert != null) {
      dom.window.setTimeout(() => {
        alert.style.display = "block"
      }, 600)
    }
  }

}
