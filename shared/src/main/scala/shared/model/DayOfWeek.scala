package shared.model

object DaysOfWeek {
  val values = Set(
    "Monday",
    "Tuesday",
    "Wednesday",
    "Thursday",
    "Friday"
  )
}

case class DayOfWeek(value: String) {
  require(DaysOfWeek.values.count(_ == value) == 1)
}