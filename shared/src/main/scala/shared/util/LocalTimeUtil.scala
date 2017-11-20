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

  def isMorningTime(timeString: String): Boolean = {
    val amOrPm = timeString takeRight 2
    if (amOrPm.toUpperCase == "PM") false else true
  }

  def convertStringTimeToLocalTime(timeString: String): Option[LocalTime] = {
    val maybeHoursAndMinutes = extractHoursAndMinutesFromString(timeString)

    maybeHoursAndMinutes match {
      case Some(hoursAndMinutes) =>
        val hoursIn24HourFormat = hoursAndMinutes._1 + (if (isMorningTime(timeString)) 0 else 12)
        Some(LocalTime.of(hoursIn24HourFormat, hoursAndMinutes._2))
      case None => None
    }
  }

  def get12HourAmPmFromLocalTime(time: LocalTime): String = {
    val hours = if (time.getHour > 12) time.getHour - 12 else time.getHour
    val minutes = if (time.getMinute >= 10) time.getMinute else "0" + time.getMinute.toString
    val amOrPm = if (time.getHour >= 12) "PM" else "AM"
    s"$hours:$minutes $amOrPm"
  }

}
