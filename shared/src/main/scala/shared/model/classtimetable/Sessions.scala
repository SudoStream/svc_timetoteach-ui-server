package shared.model.classtimetable

object Sessions {
  val values = Set(
    "early-morning-session",
    "late-morning-session",
    "afternoon-session"
  )
}

case class Session(value: String) {
  require(Sessions.values.count(_ == value) == 1)
}
