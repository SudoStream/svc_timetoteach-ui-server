package shared.model.classtimetable

trait SchoolDayTimes {
  def createSchoolDayTimes(schoolDayTimesOption: Option[Map[SchoolDayTimeBoundary, String]]):
  Map[SchoolDayTimeBoundary, String] = {
    schoolDayTimesOption match {
      case Some(schoolDayTimesMap) =>
        if (schoolDayTimesMap.size != 6) {
          val errorMsg = s"Require 6 entries for dictionary, but there were ${schoolDayTimesMap.size} passed in, specifically '" +
            s"${schoolDayTimesMap.keys.mkString(" ")}'"
          throw new RuntimeException(errorMsg)
        }
        schoolDayTimesMap

      case None =>
        Map(
          SchoolDayStarts() -> "09:00",
          MorningBreakStarts() -> "10:30",
          MorningBreakEnds() -> "10:45",
          LunchStarts() -> "12:00",
          LunchEnds() -> "13:00",
          SchoolDayEnds() -> "15:00"
        )
    }
  }
}
