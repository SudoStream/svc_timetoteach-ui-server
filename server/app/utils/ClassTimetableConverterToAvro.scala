package utils

import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.ClassTimetable
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.sessions.{SessionBoundaryWrapper, SessionOfTheDayWrapper}
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.time.ClassTimetableSchoolTimes
import shared.model.classtimetable.WWWClassTimetable

object ClassTimetableConverterToAvro extends ClassTimetableConverterHelperToAvro {

  def convertAvroClassTimeTableToWww(avroTimetable: ClassTimetable) : WWWClassTimetable = ???

  def convertWwwClassTimeTableToAvro(wwwTimetable: WWWClassTimetable) : ClassTimetable = {
    val theSchoolTimes: ClassTimetableSchoolTimes = createSchoolTimes(wwwTimetable.schoolDayTimes)
    val theAllSessionsOfTheWeek: List[SessionOfTheDayWrapper] = createAllSessionsOfTheWeek(wwwTimetable.allSessionsOfTheWeekInOrderByDay)

    ClassTimetable(
      schoolTimes = theSchoolTimes,
      allSessionsOfTheWeek = theAllSessionsOfTheWeek
    )
  }

}
