package utils

import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.sessions.SessionOfTheDayWrapper
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.time.ClassTimetableSchoolTimes
import shared.model.classtimetable.WWWClassTimetable

trait ClassTimetableConverterHelperFromAvro {

  def createSchoolTimes(schoolTimes: ClassTimetableSchoolTimes): ClassTimetableSchoolTimes = ???

  def createAllSessionsOfTheWeek(allSessionsOfTheWeek: List[SessionOfTheDayWrapper]): List[SessionOfTheDayWrapper] = ???

  def addAllSessionsToClassTimetable(theAllSessionsOfTheWeek: List[SessionOfTheDayWrapper], wwwClassTimetable: WWWClassTimetable): WWWClassTimetable = ???
}
