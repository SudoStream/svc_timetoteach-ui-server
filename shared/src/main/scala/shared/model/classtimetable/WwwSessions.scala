package shared.model.classtimetable

import java.time.LocalTime

object WwwSessions {
  val values = Set(
    "early-morning-session",
    "late-morning-session",
    "afternoon-session"
  )

  def createWwwSession(startTimeIso8601: String) : WwwSession = {
    val startTime = LocalTime.parse(startTimeIso8601)
    if ( startTime.isAfter(LocalTime.of(8,0)) && startTime.isBefore(LocalTime.of(9,45))) {
      WwwSession("early-morning-session")
    } else if ( startTime.isAfter(LocalTime.of(12,0)) && startTime.isBefore(LocalTime.of(14,0))) {
      WwwSession("afternoon-session")
    } else {
      WwwSession("late-morning-session")
    }
  }
}

case class WwwSession(value: String) {
  require(WwwSessions.values.count(_ == value) == 1)

  def niceValue: String = {
    value.split("-").map(_.capitalize).mkString(" ")
  }
}
