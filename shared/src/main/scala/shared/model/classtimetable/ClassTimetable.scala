package shared.model.classtimetable

import java.time.LocalTime

case class ClassTimetable(private val schoolDayTimesOption: Option[Map[SchoolDayTimeBoundary, String]])
  extends SchoolDayTimes {

  val schoolDayTimes: Map[SchoolDayTimeBoundary, String] = createSchoolDayTimes(schoolDayTimesOption)

  private def getSchoolDayHoursMinutes(key: SchoolDayTimeBoundary): (Int, Int) = {
    val schoolDayTime = schoolDayTimes.get(key)
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

  private val sessionsOfTheWeek: scala.collection.mutable.Map[SessionOfTheWeek, SessionBreakdown] = scala.collection.mutable.Map()
  private lazy val earlyMorningSessionStartsHoursMinutes = getSchoolDayHoursMinutes(SchoolDayStarts())
  private lazy val earlyMorningSessionEndsHoursMinutes = getSchoolDayHoursMinutes(MorningBreakStarts())
  private lazy val lateMorningSessionStartsHoursMinutes = getSchoolDayHoursMinutes(MorningBreakEnds())
  private lazy val lateMorningSessionEndsHoursMinutes = getSchoolDayHoursMinutes(LunchStarts())
  private lazy val afternoonSessionStartsHoursMinutes = getSchoolDayHoursMinutes(LunchEnds())
  private lazy val afternoonSessionEndsHoursMinutes = getSchoolDayHoursMinutes(SchoolDayEnds())

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

  private val allowedStateValues = Set(
    "ENTIRELY_EMPTY", "PARTIALLY_COMPLETE", "COMPLETE")
  def getCurrentState: String = {
    if (sessionsOfTheWeek.values.count(_.isFull) == sessionsOfTheWeek.values.size) "COMPLETE"
    else if (sessionsOfTheWeek.values.count(_.isEmpty) == sessionsOfTheWeek.values.size) "ENTIRELY_EMPTY"
    else "PARTIALLY_COMPLETE"
  }

  /**
    *
    */
  private val beenEdits = false
  def hasBeenEdited: Boolean = beenEdits

  def addSubject(subjectDetail: SubjectDetail, sessionOfTheWeek: SessionOfTheWeek): Boolean = {
    sessionsOfTheWeek.get(sessionOfTheWeek) match {
      case Some(sessionBreakdown) =>
        sessionBreakdown.addSubject(subjectDetail)
      case None => false
    }
  }

}
