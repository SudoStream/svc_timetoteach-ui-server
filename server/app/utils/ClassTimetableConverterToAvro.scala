package utils

import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.ClassTimetable
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.sessions.{SessionBoundaryWrapper, SessionOfTheDayWrapper}
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.time.ClassTimetableSchoolTimes
import shared.model.classtimetable.{WwwDayOfWeek, SchoolDayTimeBoundary, WWWClassTimetable, WwwSessionBreakdown}

object ClassTimetableConverterToAvro
  extends ClassTimetableConverterHelperToAvro with ClassTimetableConverterHelperFromAvro {

  def convertAvroClassTimeTableToWww(avroTimetable: ClassTimetable): WWWClassTimetable = {
    val theSchoolTimes: Option[Map[SchoolDayTimeBoundary, String]] = createSchoolTimes(avroTimetable.schoolTimes)
    val theAllSessionsOfTheWeek: Map[WwwDayOfWeek, List[WwwSessionBreakdown]] = createAllSessionsOfTheWeek(avroTimetable.allSessionsOfTheWeek)

    val wwwClassTimetable =  WWWClassTimetable(schoolDayTimesOption = theSchoolTimes)
    addAllSessionsToClassTimetable(theAllSessionsOfTheWeek, wwwClassTimetable)
  }

  def convertWwwClassTimeTableToAvro(tttUserId : String, wwwTimetable: WWWClassTimetable): ClassTimetable = {
    val theSchoolTimes: ClassTimetableSchoolTimes = createSchoolTimes(wwwTimetable.schoolDayTimes)
    val theAllSessionsOfTheWeek: List[SessionOfTheDayWrapper] = createAllSessionsOfTheWeek(wwwTimetable.allSessionsOfTheWeekInOrderByDay)

    ClassTimetable(
      tttUserId,
      schoolTimes = theSchoolTimes,
      allSessionsOfTheWeek = theAllSessionsOfTheWeek
    )
  }

}
