package shared.model

object Sessions {
  val values = Set(
    Session("early-morning-session"),
    Session("late-morning-session"),
    Session("afternoon-session")
  )
}

case class Session(value: String)