package timetoteach.planning

import org.scalajs.dom
import org.scalajs.dom.raw.HTMLDivElement

import scala.scalajs.js.Dynamic.global

object ClassGroupPlanningJsScreen {

  var eAndORowBackgroundNormalColor : Option[String] = None
  var eAndORowForegroundNormalColor : Option[String] = None
  var eAndORowBorderRadius : Option[String] = None

  def loadJavascript(): Unit = {
    global.console.log("Loading ClassGroupPlanningJsScreen javascript")
    mouseoverEandOs()
  }

  def mouseoverEandOs(): Unit = {
    val allEAndORows = dom.document.getElementsByClassName("termly-plans-eobenchmark-row")
    val nodeListSize = allEAndORows.length
    var index = 0
    while (index < nodeListSize) {
      val theDiv = allEAndORows(index).asInstanceOf[HTMLDivElement]

      theDiv.addEventListener("mouseover", (e: dom.Event) => {
        if (eAndORowBackgroundNormalColor.isEmpty) {
          eAndORowBackgroundNormalColor = Some(theDiv.style.backgroundColor)
        }
        if (eAndORowForegroundNormalColor.isEmpty){
          eAndORowForegroundNormalColor = Some(theDiv.style.color)
        }
        if (eAndORowBorderRadius.isEmpty) {
          eAndORowBorderRadius = Some(theDiv.style.borderRadius)
        }

        theDiv.style.backgroundColor = "grey"
        theDiv.style.color = "white"
        theDiv.style.borderRadius = "7px"
      })

      theDiv.addEventListener("mouseleave", (e: dom.Event) => {
        theDiv.style.backgroundColor = eAndORowBackgroundNormalColor.getOrElse("white")
        theDiv.style.color = eAndORowForegroundNormalColor.getOrElse("grey")
        theDiv.style.borderRadius = eAndORowBorderRadius.getOrElse("0")
      })


      index = index + 1
    }


  }

}
