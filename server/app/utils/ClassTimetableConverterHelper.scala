package utils

import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.sessions._
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.subjectdetail.{SubjectDetail, SubjectDetailAdditionalInfo, SubjectDetailWrapper, SubjectName}
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.time.{ClassTimetableSchoolTimes, DayOfTheWeek, EndTime, StartTime}
import shared.model.classtimetable._

trait ClassTimetableConverterHelper {

  def createSchoolTimes(schoolDayTimes: Map[SchoolDayTimeBoundary, String]): ClassTimetableSchoolTimes = {
    ClassTimetableSchoolTimes(createSessionBoundaries(schoolDayTimes))
  }

  def createAllSessionsOfTheWeek(allSessionsOfTheWeek: Map[DayOfWeek, List[WwwSessionBreakdown]]): scala.List[SessionOfTheDayWrapper] = {
    {
      for {
        dayToSessionBreakdownsTuple <- allSessionsOfTheWeek
        sessionBreakdown <- dayToSessionBreakdownsTuple._2

        theSessionName = sessionBreakdown.sessionOfTheWeek.valueWithoutDay
        theDay = dayToSessionBreakdownsTuple._1.value match {
          case "Monday" => DayOfTheWeek.MONDAY
          case "Tuesday" => DayOfTheWeek.TUESDAY
          case "Wednesday" => DayOfTheWeek.WEDNESDAY
          case "Thursday" => DayOfTheWeek.THURSDAY
          case "Friday" => DayOfTheWeek.FRIDAY
          case _ => DayOfTheWeek.MONDAY
        }
        theStartTime = sessionBreakdown.startTime.toString.take(5)
        theEndTime = sessionBreakdown.endTime.toString.take(5)
        wwwSubjectDetails = sessionBreakdown.subjectsWithTimeFractionInTwelves.map(tuple => tuple._1)
        theSubjectDetails = convertWwwSubjectDetailsToAvro(wwwSubjectDetails)
      } yield SessionOfTheDayWrapper(SessionOfTheDay(
        sessionName = SessionName(theSessionName),
        dayOfTheWeek = theDay,
        startTime = StartTime(theStartTime),
        endTime = EndTime(theEndTime),
        subjects = theSubjectDetails
      ))
    }.toList
  }

  ////////
  private[utils] def convertWwwSubjectDetailsToAvro(wwwSubjectDetails: List[WwwSubjectDetail]): List[SubjectDetailWrapper] = {
    for {
      wwwSubjectDetail <- wwwSubjectDetails
      subjectName = wwwSubjectDetail.subject.value match {
        case "subject-empty" => SubjectName.EMPTY
        case "subject-golden-time" => SubjectName.GOLDEN_TIME
        case "subject-other" => SubjectName.OTHER
        case "subject-ict" => SubjectName.ICT
        case "subject-music" => SubjectName.MUSIC
        case "subject-drama" => SubjectName.DRAMA
        case "subject-health" => SubjectName.HEALTH
        case "subject-teacher-covertime" => SubjectName.TEACHER_COVERTIME
        case "subject-assembly" => SubjectName.ASSEMBLY
        case "subject-reading" => SubjectName.READING
        case "subject-spelling" => SubjectName.SPELLING
        case "subject-writing" => SubjectName.WRITING
        case "subject-maths" => SubjectName.MATHS
        case "subject-topic" => SubjectName.TOPIC
        case "subject-physical-education" => SubjectName.PHYSICAL_EDUCATION
        case "subject-soft-start" => SubjectName.SOFT_START
        case "subject-numeracy" => SubjectName.NUMERACY
        case "subject-art" => SubjectName.ART
        case "subject-rme" => SubjectName.RME
      }
    } yield SubjectDetailWrapper(SubjectDetail(
      subjectName,
      StartTime(wwwSubjectDetail.timeSlot.startTime.toString.take(5)),
      EndTime(wwwSubjectDetail.timeSlot.endTime.toString.take(5)),
      SubjectDetailAdditionalInfo(wwwSubjectDetail.lessonAdditionalInfo)
    ))
  }

  private[utils] def createSessionBoundaries(schoolDayTimes: Map[SchoolDayTimeBoundary, String]): List[SessionBoundaryWrapper] = {
    for {
      boundaryTimesTuple <- schoolDayTimes.toList
      boundaryType: SessionBoundaryType = boundaryTimesTuple._1.value match {
        case "school-day-starts" | "morning-break-ends" | "lunch-ends" => SessionBoundaryType.START_OF_TEACHING_SESSION
        case _ => SessionBoundaryType.END_OF_TEACHING_SESSION
      }
      sessionName: Option[SessionName] = boundaryTimesTuple._1.value match {
        case "school-day-starts" => Some(SessionName("EarlyMorningSession"))
        case "morning-break-ends" => Some(SessionName("LateMorningSession"))
        case "lunch-ends" => Some(SessionName("AfternoonSession"))
        case _ => None
      }
    } yield SessionBoundaryWrapper(SessionBoundary(
      SessionBoundaryName(boundaryTimesTuple._1.value),
      StartTime(boundaryTimesTuple._2),
      boundaryType,
      sessionName
    ))
  }


}
