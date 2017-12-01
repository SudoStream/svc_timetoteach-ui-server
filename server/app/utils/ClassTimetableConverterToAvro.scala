package utils

import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.ClassTimetable
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.sessions.{SessionBoundaryWrapper, SessionOfTheDayWrapper}
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.time.ClassTimetableSchoolTimes
import shared.model.classtimetable.WWWClassTimetable

object ClassTimetableConverterToAvro
  extends ClassTimetableConverterHelperToAvro with ClassTimetableConverterHelperFromAvro {

  def convertAvroClassTimeTableToWww(avroTimetable: ClassTimetable): WWWClassTimetable = {
    val theSchoolTimes: ClassTimetableSchoolTimes = createSchoolTimes(avroTimetable.schoolTimes)
    val theAllSessionsOfTheWeek: List[SessionOfTheDayWrapper] = createAllSessionsOfTheWeek(avroTimetable.allSessionsOfTheWeek)

    val wwwClassTimetable =  WWWClassTimetable(schoolDayTimesOption = None)
    addAllSessionsToClassTimetable(theAllSessionsOfTheWeek, wwwClassTimetable)
  }

  def convertWwwClassTimeTableToAvro(wwwTimetable: WWWClassTimetable): ClassTimetable = {
    val theSchoolTimes: ClassTimetableSchoolTimes = createSchoolTimes(wwwTimetable.schoolDayTimes)
    val theAllSessionsOfTheWeek: List[SessionOfTheDayWrapper] = createAllSessionsOfTheWeek(wwwTimetable.allSessionsOfTheWeekInOrderByDay)

    ClassTimetable(
      schoolTimes = theSchoolTimes,
      allSessionsOfTheWeek = theAllSessionsOfTheWeek
    )
  }

}
