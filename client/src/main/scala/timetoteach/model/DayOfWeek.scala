package timetoteach.model

object DaysOfWeek {
  val values = Set(
    DayOfWeek("Monday"),
    DayOfWeek("Tuesday"),
    DayOfWeek("Wednesday"),
    DayOfWeek("Thursday"),
    DayOfWeek("Friday")
  )
}

case class DayOfWeek(value: String)