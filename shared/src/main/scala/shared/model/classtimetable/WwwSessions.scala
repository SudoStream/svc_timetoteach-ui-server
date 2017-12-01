package shared.model.classtimetable

object WwwSessions {
  val values = Set(
    "early-morning-session",
    "late-morning-session",
    "afternoon-session"
  )
}

case class WwwSession(value: String) {
  require(WwwSessions.values.count(_ == value) == 1)

  def niceValue: String = {
    value.split("-").map(_.capitalize).mkString(" ")
  }
}
