package shared.model.classtimetable

import java.time.LocalTime

import scala.scalajs.js.Dictionary

case class ClassTimetable(private val schoolDayTimesOption: Option[Dictionary[String]]) {

  /**
    *
    */
  private val allowedDictionaryEntries = Set(
    "schoolDayStarts", "morningBreakStarts", "morningBreakEnds", "lunchStarts", "lunchEnds", "schoolDayEnds")

  val schoolDayTimes: Dictionary[String] = schoolDayTimesOption match {
    case Some(dictionary) =>
      if (dictionary.size != allowedDictionaryEntries.size) {
        val errorMsg = s"Require ${allowedDictionaryEntries.size} entries for dictionary, specifically '" +
          s"${allowedDictionaryEntries.mkString(" ")}' but there were ${dictionary.size} passed in, specifically '" +
          s"${dictionary.keys.mkString(" ")}'"
        throw new RuntimeException(errorMsg)
      }
      dictionary.keys.foreach(
        keyname => if (!allowedDictionaryEntries.contains(keyname)) {
          val errorMsg = "Require ${allowedDictionaryEntries.size} entries for dictionary, specifically '" +
            s"${allowedDictionaryEntries.mkString(" ")}', but there was an erroneous entry = '$keyname'"
          throw new RuntimeException(errorMsg)
        }
      )
      dictionary

    case _ =>
      val defaultSchoolDayTimes = Dictionary[String]()
      defaultSchoolDayTimes.update("schoolDayStarts", "09:00")
      defaultSchoolDayTimes.update("morningBreakStarts", "10:30")
      defaultSchoolDayTimes.update("morningBreakEnds", "10:45")
      defaultSchoolDayTimes.update("lunchStarts", "12:00")
      defaultSchoolDayTimes.update("lunchEnds", "13:00")
      defaultSchoolDayTimes.update("schoolDayEnds", "15:00")
      defaultSchoolDayTimes
  }

  private def getSchoolDayHoursMinutes(key: String): (Int, Int) = {
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

  /**
    *
    */
  private val sessionsOfTheWeek: scala.collection.mutable.Map[SessionOfTheWeek, SessionBreakdown] = scala.collection.mutable.Map()
  private lazy val earlyMorningSessionStartsHoursMinutes = getSchoolDayHoursMinutes("schoolDayStarts")
  private lazy val earlyMorningSessionEndsHoursMinutes = getSchoolDayHoursMinutes("morningBreakStarts")
  private lazy val lateMorningSessionStartsHoursMinutes = getSchoolDayHoursMinutes("morningBreakEnds")
  private lazy val lateMorningSessionEndsHoursMinutes = getSchoolDayHoursMinutes("lunchStarts")
  private lazy val afternoonSessionStartsHoursMinutes = getSchoolDayHoursMinutes("lunchEnds")
  private lazy val afternoonSessionEndsHoursMinutes = getSchoolDayHoursMinutes("schoolDayEnds")

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
    if (sessionsOfTheWeek.values.count(_.isFull) == sessionsOfTheWeek.values.size ) "COMPLETE"
    else if (sessionsOfTheWeek.values.count(_.isEmpty) == sessionsOfTheWeek.values.size) "ENTIRELY_EMPTY"
    else "PARTIALLY_COMPLETE"
  }

  /**
    *
    */
  private val beenEdits = false
  def hasBeenEdited: Boolean = beenEdits

  def addSubject(subjectDetail: SubjectDetail, sessionOfTheWeek: SessionOfTheWeek) : Boolean = {
    sessionsOfTheWeek.get(sessionOfTheWeek) match {
      case Some(sessionBreakdown) =>
        sessionBreakdown.addSubject(subjectDetail)
      case None => false
    }
  }

}
