package utils

import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.sessions._
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.time.{ClassTimetableSchoolTimes, StartTime}
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification
import shared.model.classtimetable._

class ClassTimetableConverterHelperFromAvroTest extends Specification {
  override def is =
    s2"""

  This is a specification for the trait 'ClassTimetableConverterHelperFromAvroTest'

  The 'createSchoolTimes' method should
    Convert an Avro ClassTimetableSchoolTimes Boundaries To a Www version which is defined             $convertAnAvroClassTimetableSchoolTimesBoundariesToAWwwVersionIsDefined
    Convert an Avro ClassTimetableSchoolTimes Boundaries To a Www version of the same size             $convertAnAvroClassTimetableSchoolTimesBoundariesToAWwwVersionOfTheSameSize
    Convert an Avro ClassTimetableSchoolTimes Boundaries To a Www version has expected values          $convertAnAvroClassTimetableSchoolTimesBoundariesToAWwwVersionWithExpectedValues

  """

  def createClassTimetableSchoolTimes(): ClassTimetableSchoolTimes = {
    val schoolSessionBoundaries: List[SessionBoundaryWrapper] =
      SessionBoundaryWrapper(SessionBoundary(
        SessionBoundaryName("school-day-starts"),
        StartTime("09:00"),
        SessionBoundaryType.START_OF_TEACHING_SESSION,
        Some(SessionName("EarlyMorningSession"))
      )) ::
        SessionBoundaryWrapper(SessionBoundary(
          SessionBoundaryName("morning-break-starts"),
          StartTime("10:30"),
          SessionBoundaryType.END_OF_TEACHING_SESSION,
          None
        )) ::
        SessionBoundaryWrapper(SessionBoundary(
          SessionBoundaryName("morning-break-ends"),
          StartTime("10:45"),
          SessionBoundaryType.START_OF_TEACHING_SESSION,
          Some(SessionName("LateMorningSession"))
        )) ::
        SessionBoundaryWrapper(SessionBoundary(
          SessionBoundaryName("lunch-starts"),
          StartTime("12:00"),
          SessionBoundaryType.END_OF_TEACHING_SESSION,
          None
        )) ::
        SessionBoundaryWrapper(SessionBoundary(
          SessionBoundaryName("lunch-ends"),
          StartTime("13:00"),
          SessionBoundaryType.START_OF_TEACHING_SESSION,
          Some(SessionName("AfternoonSession"))
        )) ::
        SessionBoundaryWrapper(SessionBoundary(
          SessionBoundaryName("school-day-ends"),
          StartTime("15:00"),
          SessionBoundaryType.END_OF_TEACHING_SESSION,
          None
        )) :: Nil

    ClassTimetableSchoolTimes(schoolSessionBoundaries)
  }

  def convertAnAvroClassTimetableSchoolTimesBoundariesToAWwwVersionIsDefined: MatchResult[Any] = {
    val sessionTimes: ClassTimetableSchoolTimes = createClassTimetableSchoolTimes()
    val maybeSchoolTimes = {
      new ClassTimetableConverterHelperFromAvro {}
    }.createSchoolTimes(sessionTimes)

    maybeSchoolTimes.isDefined mustEqual true
  }

  def convertAnAvroClassTimetableSchoolTimesBoundariesToAWwwVersionOfTheSameSize: MatchResult[Any] = {
    val sessionTimes: ClassTimetableSchoolTimes = createClassTimetableSchoolTimes()
    val maybeSchoolTimes = {
      new ClassTimetableConverterHelperFromAvro {}
    }.createSchoolTimes(sessionTimes)

    maybeSchoolTimes.get.size mustEqual 6
  }

  def convertAnAvroClassTimetableSchoolTimesBoundariesToAWwwVersionWithExpectedValues: MatchResult[Any] = {
    val sessionTimes: ClassTimetableSchoolTimes = createClassTimetableSchoolTimes()
    val maybeSchoolTimes = {
      new ClassTimetableConverterHelperFromAvro {}
    }.createSchoolTimes(sessionTimes)

    val schoolTimes = maybeSchoolTimes.get

    schoolTimes(SchoolDayStarts()) mustEqual "09:00"
    schoolTimes(MorningBreakStarts()) mustEqual "10:30"
    schoolTimes(MorningBreakEnds()) mustEqual "10:45"
    schoolTimes(LunchStarts()) mustEqual "12:00"
    schoolTimes(LunchEnds()) mustEqual "13:00"
    schoolTimes(SchoolDayEnds()) mustEqual "15:00"
  }

}
