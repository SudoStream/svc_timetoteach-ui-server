package shared.model.classtimetable

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
  def shortValue : String = value.substring(0,3)
  def ordinalNumber: Int = {
    value match {
      case "Monday" => 1
      case "Tuesday" => 2
      case "Wednesday" => 3
      case "Thursday" => 4
      case "Friday" => 5
    }
  }
}