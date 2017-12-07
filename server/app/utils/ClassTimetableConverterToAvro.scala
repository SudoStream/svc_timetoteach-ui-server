package utils

import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.sessions.SessionOfTheDayWrapper
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.time.ClassTimetableSchoolTimes
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.{ClassName, ClassTimetable, TimeToTeachId}
import play.api.Logger
import play.api.libs.json._
import shared.model.classtimetable._

object ClassTimetableConverterToAvro
  extends ClassTimetableConverterHelperToAvro with ClassTimetableConverterHelperFromAvro with ClassTimetableConverterFromJson {

  val logger: Logger = Logger

  def convertJsonClassTimetableToWwwClassTimetable(classTimetableAsJsonString: String): WWWClassTimetable = {
    val classTimetableAsJson: JsValue = Json.parse(classTimetableAsJsonString)
    logger.debug(s"Class Timetable as Json: ${classTimetableAsJson.toString()}")
    val schoolDayTimeBoundaryToStartTime = createSchoolTimesFromJson(classTimetableAsJson)
    val wWWClassTimetable : WWWClassTimetable = WWWClassTimetable(Some(schoolDayTimeBoundaryToStartTime))
    populateClassTimetable(wWWClassTimetable, classTimetableAsJson)
  }

  def convertAvroClassTimeTableToWww(avroTimetable: ClassTimetable): WWWClassTimetable = {
    val theSchoolTimes: Option[Map[SchoolDayTimeBoundary, String]] = createSchoolTimes(avroTimetable.schoolTimes)
    val theAllSessionsOfTheWeek: Map[WwwDayOfWeek, List[WwwSessionBreakdown]] = createAllSessionsOfTheWeek(avroTimetable.allSessionsOfTheWeek)

    val wwwClassTimetable = WWWClassTimetable(schoolDayTimesOption = theSchoolTimes)
    addAllSessionsToClassTimetable(theAllSessionsOfTheWeek, wwwClassTimetable)
  }

  def convertWwwClassTimeTableToAvro(tttUserId: String, wwwClassName: WwwClassName, wwwTimetable: WWWClassTimetable): ClassTimetable = {
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
