package utils

import java.time.LocalTime

import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.sessions.{SessionOfTheDay, SessionOfTheDayWrapper}
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.time.{ClassTimetableSchoolTimes, DayOfTheWeek}
import shared.model.classtimetable._

import scala.annotation.tailrec

trait ClassTimetableConverterHelperFromAvro {

  def createSchoolTimes(schoolTimes: ClassTimetableSchoolTimes): Option[Map[SchoolDayTimeBoundary, String]] = {
    Some({
      for {
        sessionBoundaryWrapper <- schoolTimes.schoolSessionBoundaries
        sessionBoundary = sessionBoundaryWrapper.sessionBoundary
        sessionBoundaryName = sessionBoundary.sessionBoundaryName.value
        schoolDayTimeBoundaryAndTime: (SchoolDayTimeBoundary, String) = sessionBoundaryName match {
          case "school-day-starts" => (SchoolDayStarts(), sessionBoundary.boundaryStartTime.timeIso8601)
          case "morning-break-starts" => (MorningBreakStarts(), sessionBoundary.boundaryStartTime.timeIso8601)
          case "morning-break-ends" => (MorningBreakEnds(), sessionBoundary.boundaryStartTime.timeIso8601)
          case "lunch-starts" => (LunchStarts(), sessionBoundary.boundaryStartTime.timeIso8601)
          case "lunch-ends" => (LunchEnds(), sessionBoundary.boundaryStartTime.timeIso8601)
          case "school-day-ends" => (SchoolDayEnds(), sessionBoundary.boundaryStartTime.timeIso8601)
          case _ => (SchoolDayStarts(), sessionBoundary.boundaryStartTime.timeIso8601)
        }
      } yield schoolDayTimeBoundaryAndTime
    }.toMap)
  }

  def createAllSessionsOfTheWeek(allSessionsOfTheWeek: List[SessionOfTheDayWrapper]): Map[WwwDayOfWeek, List[WwwSessionBreakdown]] = {
    def buildSessionOfTheWeek(sessionOfTheDay: SessionOfTheDay): WwwSessionOfTheWeek = {

      val dayOfTheWeek = sessionOfTheDay.dayOfTheWeek

      val wwwSession = sessionOfTheDay.sessionName.value match {
        case "early-morning-session" => WwwSession("early-morning-session")
        case "late-morning-session" => WwwSession("late-morning-session")
        case "afternoon-session" => WwwSession("afternoon-session")
        case other => throw new RuntimeException(s"Did not expect a wwwSession of '$other'")
      }
      val wwwDayOfTheWeek = dayOfTheWeek match {
        case DayOfTheWeek.MONDAY => WwwDayOfWeek("Monday")
        case DayOfTheWeek.TUESDAY => WwwDayOfWeek("Tuesday")
        case DayOfTheWeek.WEDNESDAY => WwwDayOfWeek("Wednesday")
        case DayOfTheWeek.THURSDAY => WwwDayOfWeek("Thursday")
        case DayOfTheWeek.FRIDAY => WwwDayOfWeek("Friday")
      }

      // TODO: FIX this .. no "get" please
      WwwSessionOfTheWeek.createSessionOfTheWeek(wwwDayOfTheWeek, wwwSession).get
    }

    def buildDayOfTheWeek(dayOfTheWeek: DayOfTheWeek): WwwDayOfWeek = {
      dayOfTheWeek match {
        case DayOfTheWeek.MONDAY => WwwDayOfWeek("Monday")
        case DayOfTheWeek.TUESDAY => WwwDayOfWeek("Tuesday")
        case DayOfTheWeek.WEDNESDAY => WwwDayOfWeek("Wednesday")
        case DayOfTheWeek.THURSDAY => WwwDayOfWeek("Thursday")
        case DayOfTheWeek.FRIDAY => WwwDayOfWeek("Friday")
      }
    }

    def createNewWwwSession(sessionOfTheDay: SessionOfTheDay): WwwSessionBreakdown = {
      val sessionOfTheWeek = buildSessionOfTheWeek(sessionOfTheDay)
      val wwwSubjectBreakdown = WwwSessionBreakdown(
        sessionOfTheWeek,
        LocalTime.parse(sessionOfTheDay.startTime.timeIso8601),
        LocalTime.parse(sessionOfTheDay.endTime.timeIso8601)
      )

      for {
        subjectDetailWrapper <- sessionOfTheDay.subjects
        subjectDetail = subjectDetailWrapper.subjectDetail
        wwwSubjectDetail : WwwSubjectDetail = SubjectDetailConverter.converToWwwClassDetail(subjectDetail)
      } wwwSubjectBreakdown.addSubject(wwwSubjectDetail)

      wwwSubjectBreakdown
    }

    @tailrec
    def loop(maybeSessionToAdd: Option[SessionOfTheDay], restOfSessions: List[SessionOfTheDayWrapper], currentMap: Map[WwwDayOfWeek, List[WwwSessionBreakdown]]): Map[WwwDayOfWeek, List[WwwSessionBreakdown]] = {
      if (maybeSessionToAdd.isEmpty) currentMap
      else {
        val sessionToAdd = maybeSessionToAdd.get
        val newWwwSessionBreakdown = createNewWwwSession(sessionToAdd)
        val dayOfWeek = buildDayOfTheWeek(sessionToAdd.dayOfTheWeek)

        val nextSessionsList = if (currentMap.get(dayOfWeek).isDefined) {
          val currentSessions = currentMap(dayOfWeek)
          (newWwwSessionBreakdown :: currentSessions) sortBy (_.startTime.toSecondOfDay)
        } else {
          newWwwSessionBreakdown :: Nil
        }

        val nextMap = currentMap + (dayOfWeek -> nextSessionsList)
        val nextMaybeSessionToAdd = if (restOfSessions.isEmpty) None else Some(restOfSessions.head.sessionOfTheDay)
        val nextRestOfSessions = if (restOfSessions.isEmpty) restOfSessions else restOfSessions.tail
        loop(nextMaybeSessionToAdd, nextRestOfSessions, nextMap)
      }
    }

    if (allSessionsOfTheWeek.isEmpty) Map() else {
      loop(Some(allSessionsOfTheWeek.head.sessionOfTheDay), allSessionsOfTheWeek.tail, Map())
    }
  }

  def addAllSessionsToClassTimetable(theAllSessionsOfTheWeek: Map[WwwDayOfWeek, List[WwwSessionBreakdown]], wwwClassTimetable: WWWClassTimetable): WWWClassTimetable = {
    for {
      dayToSessionBreakdownTuple <- theAllSessionsOfTheWeek
      sessionBreakdownsForTheDay = dayToSessionBreakdownTuple._2
      sessionBreakDown <- sessionBreakdownsForTheDay
      subjectsDetailsToFraction = sessionBreakDown.subjectsWithTimeFractionInTwelves
      subjectDetailToFraction <- subjectsDetailsToFraction
      wwwSubjectDetail = subjectDetailToFraction._1
      wwwSessionOfTheWeek = sessionBreakDown.sessionOfTheWeek
    } wwwClassTimetable.addSubject(wwwSubjectDetail, wwwSessionOfTheWeek)


    wwwClassTimetable
  }


}
