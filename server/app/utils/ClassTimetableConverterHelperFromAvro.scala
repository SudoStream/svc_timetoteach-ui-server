package utils

import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.sessions.SessionOfTheDayWrapper
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.time.ClassTimetableSchoolTimes
import shared.model.classtimetable._

trait ClassTimetableConverterHelperFromAvro {

  def createSchoolTimes(schoolTimes: ClassTimetableSchoolTimes): Option[Map[SchoolDayTimeBoundary, String]] = {
    Some({for {
      sessionBoundaryWrapper <- schoolTimes.schoolSessionBoundaries
      sessionBoundary = sessionBoundaryWrapper.sessionBoundary
      sessionBoundaryName = sessionBoundary.sessionBoundaryName.value
      schoolDayTimeBoundaryAndTime : (SchoolDayTimeBoundary, String) = sessionBoundaryName match {
        case "school-day-starts" => (SchoolDayStarts(), sessionBoundary.boundaryStartTime.timeIso8601)
        case "morning-break-starts" => (MorningBreakStarts(), sessionBoundary.boundaryStartTime.timeIso8601)
        case "morning-break-ends" => (MorningBreakEnds(), sessionBoundary.boundaryStartTime.timeIso8601)
        case "lunch-starts" => (LunchStarts(), sessionBoundary.boundaryStartTime.timeIso8601)
        case "lunch-ends" => (LunchEnds(), sessionBoundary.boundaryStartTime.timeIso8601)
        case "school-day-ends" => (SchoolDayEnds(), sessionBoundary.boundaryStartTime.timeIso8601)
        case _ => (SchoolDayStarts(), sessionBoundary.boundaryStartTime.timeIso8601)
      }
    } yield schoolDayTimeBoundaryAndTime}.toMap)
  }

  def createAllSessionsOfTheWeek(allSessionsOfTheWeek: List[SessionOfTheDayWrapper]): Map[DayOfWeek, List[WwwSessionBreakdown]] = {
    Map()
  }

  def addAllSessionsToClassTimetable(theAllSessionsOfTheWeek: Map[DayOfWeek, List[WwwSessionBreakdown]], wwwClassTimetable: WWWClassTimetable): WWWClassTimetable = ???
}
