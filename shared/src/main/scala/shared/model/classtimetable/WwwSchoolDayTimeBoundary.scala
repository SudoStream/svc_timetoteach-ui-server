package shared.model.classtimetable

trait SchoolDayTimeBoundary {

  def value: String = {
    val fullClassName = this.getClass.getName
    val arr = fullClassName.split('.')
    val baseClassname = arr.last
    val dashFormat = baseClassname.replaceAll("([A-Z])", "-$1")
    dashFormat.toLowerCase().drop(1)
  }

  val numberOfImplementations = 6
}

sealed case class SchoolDayStarts() extends SchoolDayTimeBoundary
sealed case class MorningBreakStarts() extends SchoolDayTimeBoundary
sealed case class MorningBreakEnds() extends SchoolDayTimeBoundary
sealed case class LunchStarts() extends SchoolDayTimeBoundary
sealed case class LunchEnds() extends SchoolDayTimeBoundary
sealed case class SchoolDayEnds() extends SchoolDayTimeBoundary
