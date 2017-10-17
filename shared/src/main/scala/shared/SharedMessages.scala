package shared

import scala.scalajs.js.Date

object SharedMessages {
  def timeNow = new Date().toLocaleTimeString()

  def httpMainTitle = "Time To Teach"
}
