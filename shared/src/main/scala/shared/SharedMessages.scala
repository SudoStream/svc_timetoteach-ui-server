package shared

import scala.scalajs.js.Date
import scala.scalajs.js.Dictionary

object SharedMessages {
  def timeNow = new Date().toLocaleTimeString()

  def httpMainTitle = "Time To Teach"
}
