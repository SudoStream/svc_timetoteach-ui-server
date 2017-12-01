package utils

import java.time.LocalTime

import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.sessions.{SessionOfTheDay, SessionOfTheDayWrapper}
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.time.{ClassTimetableSchoolTimes, DayOfTheWeek}
import shared.model.classtimetable._

trait ClassTimetableConverterHelperFromAvro {

  def createSchoolTimes(schoolTimes: ClassTimetableSchoolTimes): Option[Map[SchoolDayTimeBoundary, String]] = {
    Some({
      for {
        sessionBoundaryWrapper <- schoolTimes.schoolSessionBoundaries
        sessionBoundary = sessionBoundaryWrapper.sessionBoundary
        sessionBoundaryName = sessionBoundary.sessionBoundaryName.value
        schoolDayTimeBoundaryAndTime: (SchoolDayTimeBoundary, String) = sessionBoundaryName match {
          case "school-day-starts" => (SchoolDayStarts(), sessionBoundary.boundaryStartTime.timeIso8601)
          case "morning-break-starts" => (MorningBreakStarts(), sessionBoundary.boundaryStartTime.timeIso8601)
          case "morning-break-ends" => (MorningBreakEnds(), sessionBoundary.boundaryStartTime.timeIso8601)
          case "lunch-starts" => (LunchStarts(), sessionBoundary.boundaryStartTime.timeIso8601)
          case "lunch-ends" => (LunchEnds(), sessionBoundary.boundaryStartTime.timeIso8601)
          case "school-day-ends" => (SchoolDayEnds(), sessionBoundary.boundaryStartTime.timeIso8601)
          case _ => (SchoolDayStarts(), sessionBoundary.boundaryStartTime.timeIso8601)
        }
      } yield schoolDayTimeBoundaryAndTime
    }.toMap)
  }

  def createAllSessionsOfTheWeek(allSessionsOfTheWeek: List[SessionOfTheDayWrapper]): Map[WwwDayOfWeek, List[WwwSessionBreakdown]] = {
    def buildSessionOfTheWeek(sessionOfTheDay: SessionOfTheDay): SessionOfTheWeek = {

    }

    def buildDayOfTheWeek(dayOfTheWeek: DayOfTheWeek): WwwDayOfWeek = {
      dayOfTheWeek match {
        case DayOfTheWeek.MONDAY => WwwDayOfWeek("Monday")
        case DayOfTheWeek.TUESDAY => WwwDayOfWeek("Tuesday")
        case DayOfTheWeek.WEDNESDAY => WwwDayOfWeek("Wednesday")
        case DayOfTheWeek.THURSDAY => WwwDayOfWeek("Thursday")
        case DayOfTheWeek.FRIDAY => WwwDayOfWeek("Friday")
      }
    }

    def loop(restOfSessions: List[SessionOfTheDayWrapper], currentMap: Map[WwwDayOfWeek, List[WwwSessionBreakdown]]): Map[WwwDayOfWeek, List[WwwSessionBreakdown]] = {
      if (restOfSessions.isEmpty) currentMap
      else {
        val sessionOfTheDay = restOfSessions.head.sessionOfTheDay
        val sessionOfTheWeek = buildSessionOfTheWeek(sessionOfTheDay)
        val wwwSessionBreakdown = WwwSessionBreakdown(
          sessionOfTheWeek,
          LocalTime.parse(sessionOfTheDay.startTime.timeIso8601),
          LocalTime.parse(sessionOfTheDay.endTime.timeIso8601)
        )
        val dayOfWeek = buildDayOfTheWeek(sessionOfTheDay.dayOfTheWeek)

        if (currentMap.get(dayOfWeek).isDefined) {
          currentMap.get(dayOfWeek)
        }
      }
    }

    loop(allSessionsOfTheWeek, Map())
  }

  def addAllSessionsToClassTimetable(theAllSessionsOfTheWeek: Map[WwwDayOfWeek, List[WwwSessionBreakdown]], wwwClassTimetable: WWWClassTimetable): WWWClassTimetable = ???
}
