package shared.model.classtimetable

import java.time.LocalTime

trait AllSessionsOfTheWeek extends SchoolDayTimes {

  def getSchoolDayTimes: Map[SchoolDayTimeBoundary, String]

  private def extractSessionHoursAndMinutes(key: SchoolDayTimeBoundary): (Int, Int) = {
    val schoolDayTime = getSchoolDayTimes.get(key)
    schoolDayTime match {
      case Some(time) =>
        val indexOfColon = time.indexOf(':')
        if (indexOfColon > 0) {
          val hours = Integer.parseInt(time.substring(0, indexOfColon))
          val minutes = Integer.parseInt(time.substring(indexOfColon + 1, indexOfColon + 3))
          (hours, minutes)
        } else throw new RuntimeException(s"Using key '$key' for school day times got time '$time' which is invalid")

      case _ => throw new RuntimeException(s"Key '$key' is not valid for school day times")
    }
  }
  private lazy val earlyMorningSessionStartsHoursMinutes = extractSessionHoursAndMinutes(SchoolDayStarts())
  private lazy val earlyMorningSessionEndsHoursMinutes = extractSessionHoursAndMinutes(MorningBreakStarts())
  private lazy val lateMorningSessionStartsHoursMinutes = extractSessionHoursAndMinutes(MorningBreakEnds())
  private lazy val lateMorningSessionEndsHoursMinutes = extractSessionHoursAndMinutes(LunchStarts())
  private lazy val afternoonSessionStartsHoursMinutes = extractSessionHoursAndMinutes(LunchEnds())
  private lazy val afternoonSessionEndsHoursMinutes = extractSessionHoursAndMinutes(SchoolDayEnds())

  private[classtimetable] val sessionsOfTheWeek: scala.collection.mutable.Map[SessionOfTheWeek, SessionBreakdown] = scala.collection.mutable.Map()
  sessionsOfTheWeek += (MondayEarlyMorningSession() ->
    SessionBreakdown(
      startTime = LocalTime.of(earlyMorningSessionStartsHoursMinutes._1, earlyMorningSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(earlyMorningSessionEndsHoursMinutes._1, earlyMorningSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (MondayLateMorningSession() ->
    SessionBreakdown(
      startTime = LocalTime.of(lateMorningSessionStartsHoursMinutes._1, lateMorningSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(lateMorningSessionEndsHoursMinutes._1, lateMorningSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (MondayAfternoonSession() ->
    SessionBreakdown(
      startTime = LocalTime.of(afternoonSessionStartsHoursMinutes._1, afternoonSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(afternoonSessionEndsHoursMinutes._1, afternoonSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (TuesdayEarlyMorningSession() ->
    SessionBreakdown(
      startTime = LocalTime.of(earlyMorningSessionStartsHoursMinutes._1, earlyMorningSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(earlyMorningSessionEndsHoursMinutes._1, earlyMorningSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (TuesdayLateMorningSession() ->
    SessionBreakdown(
      startTime = LocalTime.of(lateMorningSessionStartsHoursMinutes._1, lateMorningSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(lateMorningSessionEndsHoursMinutes._1, lateMorningSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (TuesdayAfternoonSession() ->
    SessionBreakdown(
      startTime = LocalTime.of(afternoonSessionStartsHoursMinutes._1, afternoonSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(afternoonSessionEndsHoursMinutes._1, afternoonSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (WednesdayEarlyMorningSession() ->
    SessionBreakdown(
      startTime = LocalTime.of(earlyMorningSessionStartsHoursMinutes._1, earlyMorningSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(earlyMorningSessionEndsHoursMinutes._1, earlyMorningSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (WednesdayLateMorningSession() ->
    SessionBreakdown(
      startTime = LocalTime.of(lateMorningSessionStartsHoursMinutes._1, lateMorningSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(lateMorningSessionEndsHoursMinutes._1, lateMorningSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (WednesdayAfternoonSession() ->
    SessionBreakdown(
      startTime = LocalTime.of(afternoonSessionStartsHoursMinutes._1, afternoonSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(afternoonSessionEndsHoursMinutes._1, afternoonSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (ThursdayEarlyMorningSession() ->
    SessionBreakdown(
      startTime = LocalTime.of(earlyMorningSessionStartsHoursMinutes._1, earlyMorningSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(earlyMorningSessionEndsHoursMinutes._1, earlyMorningSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (ThursdayLateMorningSession() ->
    SessionBreakdown(
      startTime = LocalTime.of(lateMorningSessionStartsHoursMinutes._1, lateMorningSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(lateMorningSessionEndsHoursMinutes._1, lateMorningSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (ThursdayAfternoonSession() ->
    SessionBreakdown(
      startTime = LocalTime.of(afternoonSessionStartsHoursMinutes._1, afternoonSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(afternoonSessionEndsHoursMinutes._1, afternoonSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (FridayEarlyMorningSession() ->
    SessionBreakdown(
      startTime = LocalTime.of(earlyMorningSessionStartsHoursMinutes._1, earlyMorningSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(earlyMorningSessionEndsHoursMinutes._1, earlyMorningSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (FridayLateMorningSession() ->
    SessionBreakdown(
      startTime = LocalTime.of(lateMorningSessionStartsHoursMinutes._1, lateMorningSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(lateMorningSessionEndsHoursMinutes._1, lateMorningSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (FridayAfternoonSession() ->
    SessionBreakdown(
      startTime = LocalTime.of(afternoonSessionStartsHoursMinutes._1, afternoonSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(afternoonSessionEndsHoursMinutes._1, afternoonSessionEndsHoursMinutes._2)
    ))

}
