package shared

import scala.scalajs.js.Date

object SharedMessages {
  def itWorks = "It works, wayhey!"

  def timeNow = new Date().toLocaleTimeString()
}
