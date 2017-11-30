package utils

import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.sessions._
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.time.{ClassTimetableSchoolTimes, StartTime}
import shared.model.classtimetable._

trait ClassTimetableConverterHelper {

  def createSessionBoundaries(schoolDayTimes: Map[SchoolDayTimeBoundary, String]): List[SessionBoundaryWrapper] = {
    for {
      boundaryTimesTuple <- schoolDayTimes.toList
      boundaryType: SessionBoundaryType = boundaryTimesTuple._1.value match {
        case "school-day-starts" | "morning-break-ends" | "lunch-ends" => SessionBoundaryType.START_OF_TEACHING_SESSION
        case _ => SessionBoundaryType.END_OF_TEACHING_SESSION
      }
      sessionName: Option[SessionName] = boundaryTimesTuple._1.value match {
        case "school-day-starts" => Some(SessionName("EarlyMorningSession"))
        case "morning-break-ends" => Some(SessionName("LateMorningSession"))
        case "lunch-ends" => Some(SessionName("AfternoonSession"))
        case _ => None
      }
    } yield SessionBoundaryWrapper(SessionBoundary(
      SessionBoundaryName(boundaryTimesTuple._1.value),
      StartTime(boundaryTimesTuple._2),
      boundaryType,
      sessionName
    ))
  }

  def createSchoolTimes(schoolDayTimes: Map[SchoolDayTimeBoundary, String]): ClassTimetableSchoolTimes = {
    ClassTimetableSchoolTimes(createSessionBoundaries(schoolDayTimes))
  }

  def createAllSessionsOfTheWeek(allSessionsOfTheWeek: Map[DayOfWeek, List[SessionBreakdown]]): scala.List[SessionOfTheDayWrapper] = {

  }


}
