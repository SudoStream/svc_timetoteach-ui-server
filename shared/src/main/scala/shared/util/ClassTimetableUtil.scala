package shared.util

import shared.model.classtimetable._

import scala.scalajs.js
import scala.scalajs.js.{Dynamic, JSON}

object ClassTimetableUtil {


  def createWwwClassTimetableFromJson(wwwClassTimetableAsJson: String): Option[WWWClassTimetable] = {
    println(s"createWwwClassTimetableFromJson: ${wwwClassTimetableAsJson.replace("&quot;","\"")}")
    val wwwTimetableDynamic = JSON.parse(wwwClassTimetableAsJson.replace("&quot;","\""))

    val schoolTimesJsArray = wwwTimetableDynamic.schoolTimes.asInstanceOf[js.Array[js.Dynamic]]
    val schoolTimes = convertSchoolTimesJsArrayToMap(schoolTimesJsArray)
    val wWWClassTimetable = WWWClassTimetable(schoolTimes)

    val allSessionsOfTheWeekArray = wwwTimetableDynamic.allSessionsOfTheWeek.asInstanceOf[js.Array[js.Dynamic]]
    val allSessionsOfTheWeekSeq = allSessionsOfTheWeekArray.toList

    for (
      sessionInstance <- allSessionsOfTheWeekArray
    ) {
      val sessionsOfTheDay = sessionInstance.sessions.asInstanceOf[js.Array[js.Dynamic]]
      val wwwSessionsOfTheDay = addSubjectsToTimetable(wWWClassTimetable, sessionsOfTheDay)
    }


    Some(wWWClassTimetable)
  }

  private def convertSchoolTimesJsArrayToMap(schoolTimesJsArray: js.Array[js.Dynamic]): Option[Map[SchoolDayTimeBoundary, String]] = {
    def loop(sessionBoundary: Dynamic, restSessionBoundaries: List[Dynamic], mapSoFar: Map[SchoolDayTimeBoundary, String]): Option[Map[SchoolDayTimeBoundary, String]] = {
      val boundaryNameString = sessionBoundary.sessionBoundaryName.asInstanceOf[String]
      val boundaryName = SchoolDayTimeBoundary.createSchoolDayTimeBoundaryFromString(boundaryNameString)
      val boundaryStartTime = sessionBoundary.boundaryStartTime.asInstanceOf[String]
      println(s"adding to schools times map : (${boundaryName.value} -> $boundaryStartTime)")
      val newMap = mapSoFar + (boundaryName -> boundaryStartTime)
      if (restSessionBoundaries.isEmpty) {
        Some(newMap)
      } else {
        loop(restSessionBoundaries.head, restSessionBoundaries.tail, newMap)
      }
    }

    loop(schoolTimesJsArray.toList.head, schoolTimesJsArray.toList.tail, Map())
  }

  // private val SessionsOfTheWeek: scala.collection.mutable.Map[WwwSessionOfTheWeek, WwwSessionBreakdown]

  private  def addSubjectsToTimetable(wWWClassTimetable: WWWClassTimetable, sessionsOfTheDay: js.Array[js.Dynamic]): Unit = {
    val sessionsToBreakdown: scala.collection.mutable.Map[WwwSessionOfTheWeek, WwwSessionBreakdown] = scala.collection.mutable.Map()

    for (
      sessionOfTheDay <- sessionsOfTheDay
    ) {
      println(s"addSubjectsToTimetable: ${sessionOfTheDay.sessionOfTheWeek.asInstanceOf[String]}")
      val wwwSessionOfTheWeek = WwwSessionOfTheWeek.createSessionOfTheWeek(sessionOfTheDay.sessionOfTheWeek.asInstanceOf[String]).get

      val subject = sessionOfTheDay.subject.asInstanceOf[String]
      val startTimeIso8601 = sessionOfTheDay.startTimeIso8601.asInstanceOf[String]
      val endTimeIso8601 = sessionOfTheDay.endTimeIso8601.asInstanceOf[String]
      val lessonAdditionalInfo = sessionOfTheDay.lessonAdditionalInfo.asInstanceOf[String]

      val wwwSubjectDetail: WwwSubjectDetail = WwwSubjectDetail(
        WwwSubjectName(subject),
        WwwTimeSlot(
          LocalTimeUtil.convertStringTimeToLocalTime(startTimeIso8601).get,
          LocalTimeUtil.convertStringTimeToLocalTime(endTimeIso8601).get
        ),
        lessonAdditionalInfo
      )

      wWWClassTimetable.addSubject(wwwSubjectDetail, wwwSessionOfTheWeek)
    }
  }


}
