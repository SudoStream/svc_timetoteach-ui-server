package timetoteach.dashboard

import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.Dynamic.global

object DashboardJsScreen
{

  def loadJavascript(): Unit =
  {
    global.console.log("Adding js for Dashboard Screen")

    val $ = js.Dynamic.global.$

    $(dom.document).ready(() => {
      dom.window.setTimeout(() => {
        $("[data-toggle=\"popover\"]").popover("show")
      }, 1000)

      dom.window.setTimeout(() => {
        $("[data-toggle=\"popover\"]").popover("hide")
      }, 10000)
    })



    //    $("[data-toggle=\"popover\"]").popover("show")
  }

}
