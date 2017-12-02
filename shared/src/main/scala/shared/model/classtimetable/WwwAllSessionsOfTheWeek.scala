package shared.model.classtimetable

import java.time.LocalTime

trait WwwAllSessionsOfTheWeek extends WwwSchoolDayTimes {

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

  private[classtimetable] val sessionsOfTheWeek: scala.collection.mutable.Map[WwwSessionOfTheWeek, WwwSessionBreakdown] = scala.collection.mutable.Map()
  sessionsOfTheWeek += (MondayEarlyMorningSession() ->
    WwwSessionBreakdown(
      MondayEarlyMorningSession(),
      startTime = LocalTime.of(earlyMorningSessionStartsHoursMinutes._1, earlyMorningSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(earlyMorningSessionEndsHoursMinutes._1, earlyMorningSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (MondayLateMorningWwwSession() ->
    WwwSessionBreakdown(
      MondayLateMorningWwwSession(),
      startTime = LocalTime.of(lateMorningSessionStartsHoursMinutes._1, lateMorningSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(lateMorningSessionEndsHoursMinutes._1, lateMorningSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (MondayAfternoonWwwSession() ->
    WwwSessionBreakdown(
      MondayAfternoonWwwSession(),
      startTime = LocalTime.of(afternoonSessionStartsHoursMinutes._1, afternoonSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(afternoonSessionEndsHoursMinutes._1, afternoonSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (TuesdayEarlyMorningWwwSession() ->
    WwwSessionBreakdown(
      TuesdayEarlyMorningWwwSession(),
      startTime = LocalTime.of(earlyMorningSessionStartsHoursMinutes._1, earlyMorningSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(earlyMorningSessionEndsHoursMinutes._1, earlyMorningSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (TuesdayLateMorningWwwSession() ->
    WwwSessionBreakdown(
      TuesdayLateMorningWwwSession(),
      startTime = LocalTime.of(lateMorningSessionStartsHoursMinutes._1, lateMorningSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(lateMorningSessionEndsHoursMinutes._1, lateMorningSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (TuesdayAfternoonWwwSession() ->
    WwwSessionBreakdown(
      TuesdayAfternoonWwwSession(),
      startTime = LocalTime.of(afternoonSessionStartsHoursMinutes._1, afternoonSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(afternoonSessionEndsHoursMinutes._1, afternoonSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (WednesdayEarlyMorningWwwSession() ->
    WwwSessionBreakdown(
      WednesdayEarlyMorningWwwSession(),
      startTime = LocalTime.of(earlyMorningSessionStartsHoursMinutes._1, earlyMorningSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(earlyMorningSessionEndsHoursMinutes._1, earlyMorningSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (WednesdayLateMorningWwwSession() ->
    WwwSessionBreakdown(
      WednesdayLateMorningWwwSession(),
      startTime = LocalTime.of(lateMorningSessionStartsHoursMinutes._1, lateMorningSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(lateMorningSessionEndsHoursMinutes._1, lateMorningSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (WednesdayAfternoonWwwSession() ->
    WwwSessionBreakdown(
      WednesdayAfternoonWwwSession(),
      startTime = LocalTime.of(afternoonSessionStartsHoursMinutes._1, afternoonSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(afternoonSessionEndsHoursMinutes._1, afternoonSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (ThursdayEarlyMorningWwwSession() ->
    WwwSessionBreakdown(
      ThursdayEarlyMorningWwwSession(),
      startTime = LocalTime.of(earlyMorningSessionStartsHoursMinutes._1, earlyMorningSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(earlyMorningSessionEndsHoursMinutes._1, earlyMorningSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (ThursdayLateMorningWwwSession() ->
    WwwSessionBreakdown(
      ThursdayLateMorningWwwSession(),
      startTime = LocalTime.of(lateMorningSessionStartsHoursMinutes._1, lateMorningSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(lateMorningSessionEndsHoursMinutes._1, lateMorningSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (ThursdayAfternoonWwwSession() ->
    WwwSessionBreakdown(
      ThursdayAfternoonWwwSession(),
      startTime = LocalTime.of(afternoonSessionStartsHoursMinutes._1, afternoonSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(afternoonSessionEndsHoursMinutes._1, afternoonSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (FridayEarlyMorningWwwSession() ->
    WwwSessionBreakdown(
      FridayEarlyMorningWwwSession(),
      startTime = LocalTime.of(earlyMorningSessionStartsHoursMinutes._1, earlyMorningSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(earlyMorningSessionEndsHoursMinutes._1, earlyMorningSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (FridayLateMorningWwwSession() ->
    WwwSessionBreakdown(
      FridayLateMorningWwwSession(),
      startTime = LocalTime.of(lateMorningSessionStartsHoursMinutes._1, lateMorningSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(lateMorningSessionEndsHoursMinutes._1, lateMorningSessionEndsHoursMinutes._2)
    ))
  sessionsOfTheWeek += (FridayAfternoonWwwSession() ->
    WwwSessionBreakdown(
      FridayAfternoonWwwSession(),
      startTime = LocalTime.of(afternoonSessionStartsHoursMinutes._1, afternoonSessionStartsHoursMinutes._2),
      endTime = LocalTime.of(afternoonSessionEndsHoursMinutes._1, afternoonSessionEndsHoursMinutes._2)
    ))

}
