package shared.util

import java.time.LocalTime

object LocalTimeUtil {

  def extractHoursAndMinutesFromString(timeString: String): Option[(Int, Int)] = {
    val indexOfColon = timeString.indexOf(':')
    if (indexOfColon > 0) {
      val hours = Integer.parseInt(timeString.substring(0, indexOfColon))
      val minutes = Integer.parseInt(timeString.substring(indexOfColon + 1, indexOfColon + 3))
      Some((hours, minutes))
    } else None
  }

  def convertStringTimeToLocalTime(timeString: String): Option[LocalTime] = {
    val maybeHoursAndMinutes = extractHoursAndMinutesFromString(timeString)
    maybeHoursAndMinutes match {
      case Some(hoursAndMinutes) =>
        Some(LocalTime.of(hoursAndMinutes._1, hoursAndMinutes._2))
      case None => None
    }
  }

  def get12HourAmPmFromLocalTime(time: LocalTime) : String = {
    val hours = if ( time.getHour > 12 ) time.getHour - 12 else time.getHour
    val minutes = if ( time.getMinute >= 10 ) time.getMinute else "0" + time.getMinute.toString
    val amOrPm = if ( time.getHour >= 12 ) "PM" else "AM"
    s"$hours:$minutes $amOrPm"
  }

}
