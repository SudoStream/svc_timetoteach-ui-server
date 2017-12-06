package utils

import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.{ClassName, ClassTimetable, TimeToTeachId}
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.sessions.{SessionBoundaryWrapper, SessionOfTheDayWrapper}
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.time.ClassTimetableSchoolTimes
import shared.model.classtimetable._

object ClassTimetableConverterToAvro
  extends ClassTimetableConverterHelperToAvro with ClassTimetableConverterHelperFromAvro {

  def convertAvroClassTimeTableToWww(avroTimetable: ClassTimetable): WWWClassTimetable = {
    val theSchoolTimes: Option[Map[SchoolDayTimeBoundary, String]] = createSchoolTimes(avroTimetable.schoolTimes)
    val theAllSessionsOfTheWeek: Map[WwwDayOfWeek, List[WwwSessionBreakdown]] = createAllSessionsOfTheWeek(avroTimetable.allSessionsOfTheWeek)

    val wwwClassTimetable =  WWWClassTimetable(schoolDayTimesOption = theSchoolTimes)
    addAllSessionsToClassTimetable(theAllSessionsOfTheWeek, wwwClassTimetable)
  }

  def convertWwwClassTimeTableToAvro(tttUserId : String, wwwClassName: WwwClassName, wwwTimetable: WWWClassTimetable): ClassTimetable = {
    val theSchoolTimes: ClassTimetableSchoolTimes = createSchoolTimes(wwwTimetable.schoolDayTimes)
    val theAllSessionsOfTheWeek: List[SessionOfTheDayWrapper] = createAllSessionsOfTheWeek(wwwTimetable.allSessionsOfTheWeekInOrderByDay)

    ClassTimetable(
      TimeToTeachId(tttUserId),
      ClassName(wwwClassName.value),
      schoolTimes = theSchoolTimes,
      allSessionsOfTheWeek = theAllSessionsOfTheWeek
    )
  }

}
