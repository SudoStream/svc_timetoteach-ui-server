package shared.util

import shared.model.classtimetable._

import scala.scalajs.js
import scala.scalajs.js.{Dynamic, JSON}

object ClassTimetableUtil {


  def createWwwClassTimetableFromJson(wwwClassTimetableAsJson: String): Option[WWWClassTimetable] = {
    val wwwTimetableDynamic = JSON.parse(wwwClassTimetableAsJson)

    val schoolTimesJsArray = wwwTimetableDynamic.schoolTimes.asInstanceOf[js.Array[js.Dynamic]]
    val schoolTimes = convertSchoolTimesJsArrayToMap(schoolTimesJsArray)
    val wWWClassTimetable = WWWClassTimetable(schoolTimes)

    val allSessionsOfTheWeekArray = wwwTimetableDynamic.allSessionsOfTheWeek.asInstanceOf[js.Array[js.Dynamic]]
    val allSessionsOfTheWeekSeq = allSessionsOfTheWeekArray.toList

    for (
      sessionInstance <- allSessionsOfTheWeekArray
    ) {
      val sessionsOfTheDay = sessionInstance.sessions.asInstanceOf[js.Array[js.Dynamic]]
      val wwwSessionsOfTheDay = addSujbectsToTimetable(wWWClassTimetable, sessionsOfTheDay)
    }


    None
  }

  def convertSchoolTimesJsArrayToMap(schoolTimesJsArray: js.Array[js.Dynamic]): Option[Map[SchoolDayTimeBoundary, String]] = {
    def loop(sessionBoundary: Dynamic, restSessionBoundaries: List[Dynamic], mapSoFar: Map[SchoolDayTimeBoundary, String]): Option[Map[SchoolDayTimeBoundary, String]] = {
      val boundaryNameString = sessionBoundary.sessionBoundaryName.asInstanceOf[String]
      val boundaryName = SchoolDayTimeBoundary.createSchoolDayTimeBoundaryFromString(boundaryNameString)
      val boundaryStartTime = sessionBoundary.boundaryStartTime.asInstanceOf[String]
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

  def addSujbectsToTimetable(wWWClassTimetable: WWWClassTimetable,  sessionsOfTheDay: js.Array[js.Dynamic]): scala.collection.mutable.Map[WwwSessionOfTheWeek, WwwSessionBreakdown] = {
    val sessionsToBreakdown: scala.collection.mutable.Map[WwwSessionOfTheWeek, WwwSessionBreakdown] = scala.collection.mutable.Map()

    val sessionOfWeekToBreakdownTuples = {
      for {
        sessionOfTheDay <- sessionsOfTheDay

        wwwSessionOfTheWeek = WwwSessionOfTheWeek.createSessionOfTheWeek(sessionOfTheDay.sessionOfTheWeek.asInstanceOf[String]).get

        subject = sessionOfTheDay.subject.asInstanceOf[String]
        startTimeIso8601 = sessionOfTheDay.startTimeIso8601.asInstanceOf[String]
        endTimeIso8601 = sessionOfTheDay.endTimeIso8601.asInstanceOf[String]
        lessonAdditionalInfo = sessionOfTheDay.lessonAdditionalInfo.asInstanceOf[String]

        wwwSubjectDetail: WwwSubjectDetail = WwwSubjectDetail(
          WwwSubjectName(subject),
          WwwTimeSlot(
            LocalTimeUtil.convertStringTimeToLocalTime(startTimeIso8601).get,
            LocalTimeUtil.convertStringTimeToLocalTime(endTimeIso8601).get
          ),
          lessonAdditionalInfo
        )
      } yield (wwwSessionOfTheWeek, wwwSubjectDetail)
    }.toList

    for (sessionOfWeekToBreakdownTuple <- sessionOfWeekToBreakdownTuples) {
      val wwwSessionOfTheWeek = sessionOfWeekToBreakdownTuple._1
      val wwwSubjectDetail = sessionOfWeekToBreakdownTuple._2

      sessionsToBreakdown.get(wwwSessionOfTheWeek) match {
        case Some(sessionBreakdown) =>
          sessionBreakdown.addSubject(wwwSubjectDetail)
        case None =>
          val sessionBreakdown = WwwSessionBreakdown(wwwSessionOfTheWeek,)
      }

    }
  }


}
