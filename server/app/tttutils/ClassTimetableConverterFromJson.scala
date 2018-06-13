package tttutils

import java.time.LocalTime

import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.time.DayOfTheWeek
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}
import shared.model.classtimetable._
import tttutils.ClassTimetableConverterToAvro.convertSchoolDayTimeBoundaryToStartTimeTuplesToMap

trait ClassTimetableConverterFromJson {

  def createSchoolTimesFromJson(classTimetableAsJson: JsValue): Map[SchoolDayTimeBoundary, String] = {
    val schoolTimesJsArray = (classTimetableAsJson \ "schoolTimes").get.as[JsArray]

    val schoolDayTimeBoundaryToStartTimeTuples = {
      for {
        boundaryJsValue <- schoolTimesJsArray.value
        boundaryJsObject = boundaryJsValue.asInstanceOf[JsObject]
        sessionBoundaryName = boundaryJsObject("sessionBoundaryName").asInstanceOf[JsString]
        boundaryStartTime = boundaryJsObject("boundaryStartTime").asInstanceOf[JsString].value
        schoolDayTimeBoundary = SchoolDayTimeBoundary.createSchoolDayTimeBoundaryFromString(sessionBoundaryName.value)
      } yield (schoolDayTimeBoundary, boundaryStartTime)
    }.toList

    val schoolDayTimeBoundaryToStartTime: Map[SchoolDayTimeBoundary, String] =
      convertSchoolDayTimeBoundaryToStartTimeTuplesToMap(schoolDayTimeBoundaryToStartTimeTuples)
    schoolDayTimeBoundaryToStartTime
  }

  private[tttutils] def convertSchoolDayTimeBoundaryToStartTimeTuplesToMap(incomingTuples: List[(SchoolDayTimeBoundary, String)]): Map[SchoolDayTimeBoundary, String] = {
    def loop(currentMap: Map[SchoolDayTimeBoundary, String],
             tupleToAdd: (SchoolDayTimeBoundary, String),
             remainingTuples: List[(SchoolDayTimeBoundary, String)]): Map[SchoolDayTimeBoundary, String] = {
      if (remainingTuples.isEmpty) {
        currentMap + (tupleToAdd._1 -> tupleToAdd._2)
      } else {
        loop(currentMap + (tupleToAdd._1 -> tupleToAdd._2), remainingTuples.head, remainingTuples.tail)
      }
    }

    loop(Map(), incomingTuples.head, incomingTuples.tail)
  }

  def convertToSessionsAsSubjectDetails(sessionsJsValues: IndexedSeq[JsValue], dayOfTheWeekString: String): List[(WwwSubjectDetail, WwwSessionOfTheWeek)] = {
    val dayOfTheWeek = WwwDayOfWeek(dayOfTheWeekString)

    {
      for {
        sessionJsValue <- sessionsJsValues
        subject = (sessionJsValue \ "subject").get.asInstanceOf[JsString].value
        startTimeIso8601 = (sessionJsValue \ "startTimeIso8601").get.asInstanceOf[JsString].value
        endTimeIso8601 = (sessionJsValue \ "endTimeIso8601").get.asInstanceOf[JsString].value
        lessonAdditionalInfo = (sessionJsValue \ "lessonAdditionalInfo").get.asInstanceOf[JsString].value
        sessionOfTheWeekString = (sessionJsValue \ "sessionOfTheWeek").get.asInstanceOf[JsString].value
        wwwSessionOfTheWeek: WwwSessionOfTheWeek = WwwSessionOfTheWeek.createSessionOfTheWeek(sessionOfTheWeekString).get
        wwwSubjectDetail = WwwSubjectDetail(
          WwwSubjectName(subject),
          WwwTimeSlot(LocalTime.parse(startTimeIso8601), LocalTime.parse(endTimeIso8601)),
          lessonAdditionalInfo
        )
      } yield (wwwSubjectDetail, wwwSessionOfTheWeek)
    }.toList
  }

  def createSubjectDetailToSessionOfTheWeekList(allSessionsOfTheWeekJsArray: JsArray): List[(WwwSubjectDetail, WwwSessionOfTheWeek)] = {
    {
      for {
        sessionOfTheWeekJsValue <- allSessionsOfTheWeekJsArray.value
        sessionOfTheWeekJsObject = sessionOfTheWeekJsValue.asInstanceOf[JsObject]
        dayOfTheWeek = (sessionOfTheWeekJsObject \ "dayOfTheWeek").get.asInstanceOf[JsString].value
        sessionsJsValues = (sessionOfTheWeekJsObject \ "sessions").get.asInstanceOf[JsArray].value
        sessionsAsSubjectDetails = convertToSessionsAsSubjectDetails(sessionsJsValues, dayOfTheWeek)
      } yield sessionsAsSubjectDetails
    }.flatten.toList
  }

  def populateClassTimetable(wWWClassTimetable: WWWClassTimetable, classTimetableAsJson: JsValue): WWWClassTimetable = {
    val allSessionsOfTheWeekJsArray = (classTimetableAsJson \ "allSessionsOfTheWeek").get.as[JsArray]

    val subjectDetailToSessionOfTheWeekList: List[(WwwSubjectDetail, WwwSessionOfTheWeek)] =
      createSubjectDetailToSessionOfTheWeekList(allSessionsOfTheWeekJsArray)

    for (subjectDetailToSessionOfTheWeekTuple <- subjectDetailToSessionOfTheWeekList) {
      val subjectDetail: WwwSubjectDetail = subjectDetailToSessionOfTheWeekTuple._1
      val sessionOfTheWeek: WwwSessionOfTheWeek = subjectDetailToSessionOfTheWeekTuple._2
      wWWClassTimetable.addSubject(subjectDetail, sessionOfTheWeek)
    }

    wWWClassTimetable
  }


}
