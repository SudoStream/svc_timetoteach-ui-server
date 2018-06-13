package tttutils

import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.sessions._
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.subjectdetail.{SubjectDetail, SubjectDetailAdditionalInfo, SubjectDetailWrapper, SubjectName}
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.time.{ClassTimetableSchoolTimes, DayOfTheWeek, EndTime, StartTime}
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification
import shared.model.classtimetable.{ClassTimetableState, _}

class ClassTimetableConverterHelperFromAvroTest extends Specification {
  override def is =
    s2"""

  This is a specification for the trait 'ClassTimetableConverterHelperFromAvroTest'

  The 'createSchoolTimes' method should
    Convert an Avro ClassTimetableSchoolTimes Boundaries To a Www version which is defined             $convertAnAvroClassTimetableSchoolTimesBoundariesToAWwwVersionIsDefined
    Convert an Avro ClassTimetableSchoolTimes Boundaries To a Www version of the same size             $convertAnAvroClassTimetableSchoolTimesBoundariesToAWwwVersionOfTheSameSize
    Convert an Avro ClassTimetableSchoolTimes Boundaries To a Www version has expected values          $convertAnAvroClassTimetableSchoolTimesBoundariesToAWwwVersionWithExpectedValues

  The 'createAllSessionsOfTheWeek' method should
    Convert an Avro list of SessionOfTheDayWrapper to WWW map version of same size                     $convertAnAvroListOfSessionOfTheDayWrapperToWwwMapVersionOfSameSize
    As above With First Session Made Up Of Maths And Reading                                           $asAboveWithFirstSessionWithExpectedValues
    As Above With First Session Having Maths And Reading                                               $asAboveWithFirstSessionHavingMathsAndReading
    Second Session Is As Expected                                                                      $secondSessionIsAsExpected

  The 'addAllSessionsToClassTimetable' method should
    Add all the sessions to the Www Class timetable                                                    $addAllSessionsToClassTimetableHappyPath
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

  ////////////////////

  def createSessionOfTheDayWrapperList(): List[SessionOfTheDayWrapper] = {
    SessionOfTheDayWrapper(SessionOfTheDay(
      SessionName("early-morning-session"),
      DayOfTheWeek.MONDAY,
      StartTime("09:00"),
      EndTime("10:30"),
      List(SubjectDetailWrapper(SubjectDetail(
        SubjectName.MATHS,
        StartTime("09:00"),
        EndTime("09:45"),
        SubjectDetailAdditionalInfo("")
      )),
        SubjectDetailWrapper(SubjectDetail(
          SubjectName.READING,
          StartTime("09:45"),
          EndTime("10:30"),
          SubjectDetailAdditionalInfo("Check Homework")
        ))
      )
    )) :: SessionOfTheDayWrapper(SessionOfTheDay(
      SessionName("late-morning-session"),
      DayOfTheWeek.MONDAY,
      StartTime("10:45"),
      EndTime("12:00"),
      List(SubjectDetailWrapper(SubjectDetail(
        SubjectName.EMPTY,
        StartTime("10:45"),
        EndTime("12:00"),
        SubjectDetailAdditionalInfo("")
      )))
    )) :: SessionOfTheDayWrapper(SessionOfTheDay(
      SessionName("afternoon-session"),
      DayOfTheWeek.MONDAY,
      StartTime("13:00"),
      EndTime("15:00"),
      List(SubjectDetailWrapper(SubjectDetail(
        SubjectName.EMPTY,
        StartTime("13:00"),
        EndTime("15:00"),
        SubjectDetailAdditionalInfo("")
      )))
    )) :: SessionOfTheDayWrapper(SessionOfTheDay(
      SessionName("early-morning-session"),
      DayOfTheWeek.TUESDAY,
      StartTime("09:00"),
      EndTime("10:45"),
      List(SubjectDetailWrapper(SubjectDetail(
        SubjectName.EMPTY,
        StartTime("09:00"),
        EndTime("10:45"),
        SubjectDetailAdditionalInfo("")
      )))
    )) :: SessionOfTheDayWrapper(SessionOfTheDay(
      SessionName("late-morning-session"),
      DayOfTheWeek.TUESDAY,
      StartTime("10:45"),
      EndTime("12:00"),
      List(SubjectDetailWrapper(SubjectDetail(
        SubjectName.EMPTY,
        StartTime("10:45"),
        EndTime("12:00"),
        SubjectDetailAdditionalInfo("")
      )))
    )) :: SessionOfTheDayWrapper(SessionOfTheDay(
      SessionName("afternoon-session"),
      DayOfTheWeek.TUESDAY,
      StartTime("13:00"),
      EndTime("15:00"),
      List(SubjectDetailWrapper(SubjectDetail(
        SubjectName.EMPTY,
        StartTime("13:00"),
        EndTime("15:00"),
        SubjectDetailAdditionalInfo("")
      )))
    )) :: SessionOfTheDayWrapper(SessionOfTheDay(
      SessionName("early-morning-session"),
      DayOfTheWeek.WEDNESDAY,
      StartTime("09:00"),
      EndTime("10:45"),
      List(SubjectDetailWrapper(SubjectDetail(
        SubjectName.EMPTY,
        StartTime("09:00"),
        EndTime("10:45"),
        SubjectDetailAdditionalInfo("")
      )))
    )) :: SessionOfTheDayWrapper(SessionOfTheDay(
      SessionName("late-morning-session"),
      DayOfTheWeek.WEDNESDAY,
      StartTime("10:45"),
      EndTime("12:00"),
      List(SubjectDetailWrapper(SubjectDetail(
        SubjectName.EMPTY,
        StartTime("10:45"),
        EndTime("12:00"),
        SubjectDetailAdditionalInfo("")
      )))
    )) :: SessionOfTheDayWrapper(SessionOfTheDay(
      SessionName("afternoon-session"),
      DayOfTheWeek.WEDNESDAY,
      StartTime("13:00"),
      EndTime("15:00"),
      List(SubjectDetailWrapper(SubjectDetail(
        SubjectName.EMPTY,
        StartTime("13:00"),
        EndTime("15:00"),
        SubjectDetailAdditionalInfo("")
      )))
    )) :: SessionOfTheDayWrapper(SessionOfTheDay(
      SessionName("early-morning-session"),
      DayOfTheWeek.THURSDAY,
      StartTime("09:00"),
      EndTime("10:45"),
      List(SubjectDetailWrapper(SubjectDetail(
        SubjectName.EMPTY,
        StartTime("09:00"),
        EndTime("10:45"),
        SubjectDetailAdditionalInfo("")
      )))
    )) :: SessionOfTheDayWrapper(SessionOfTheDay(
      SessionName("late-morning-session"),
      DayOfTheWeek.THURSDAY,
      StartTime("10:45"),
      EndTime("12:00"),
      List(SubjectDetailWrapper(SubjectDetail(
        SubjectName.EMPTY,
        StartTime("10:45"),
        EndTime("12:00"),
        SubjectDetailAdditionalInfo("")
      )))
    )) :: SessionOfTheDayWrapper(SessionOfTheDay(
      SessionName("afternoon-session"),
      DayOfTheWeek.THURSDAY,
      StartTime("13:00"),
      EndTime("15:00"),
      List(SubjectDetailWrapper(SubjectDetail(
        SubjectName.EMPTY,
        StartTime("13:00"),
        EndTime("15:00"),
        SubjectDetailAdditionalInfo("")
      )))
    )) :: SessionOfTheDayWrapper(SessionOfTheDay(
      SessionName("early-morning-session"),
      DayOfTheWeek.FRIDAY,
      StartTime("09:00"),
      EndTime("10:45"),
      List(SubjectDetailWrapper(SubjectDetail(
        SubjectName.EMPTY,
        StartTime("09:00"),
        EndTime("10:45"),
        SubjectDetailAdditionalInfo("")
      )))
    )) :: SessionOfTheDayWrapper(SessionOfTheDay(
      SessionName("late-morning-session"),
      DayOfTheWeek.FRIDAY,
      StartTime("10:45"),
      EndTime("12:00"),
      List(SubjectDetailWrapper(SubjectDetail(
        SubjectName.EMPTY,
        StartTime("10:45"),
        EndTime("12:00"),
        SubjectDetailAdditionalInfo("")
      )))
    )) :: SessionOfTheDayWrapper(SessionOfTheDay(
      SessionName("afternoon-session"),
      DayOfTheWeek.FRIDAY,
      StartTime("13:00"),
      EndTime("15:00"),
      List(SubjectDetailWrapper(SubjectDetail(
        SubjectName.EMPTY,
        StartTime("13:00"),
        EndTime("15:00"),
        SubjectDetailAdditionalInfo("")
      )))
    )) :: Nil

  }

  def convertAnAvroListOfSessionOfTheDayWrapperToWwwMapVersionOfSameSize: MatchResult[Any] = {
    val allSessionsOfTheWeek = createSessionOfTheDayWrapperList()
    val dayToSessionsOfTheDayMap = {
      new ClassTimetableConverterHelperFromAvro {}
    }.createAllSessionsOfTheWeek(allSessionsOfTheWeek)

    println(s"dayToSessionsOfTheDayMap : ${dayToSessionsOfTheDayMap.toString}")

    dayToSessionsOfTheDayMap.values.flatten.size mustEqual allSessionsOfTheWeek.size
  }

  def asAboveWithFirstSessionWithExpectedValues: MatchResult[Any] = {
    val allSessionsOfTheWeek = createSessionOfTheDayWrapperList()
    val dayToSessionsOfTheDayMap = {
      new ClassTimetableConverterHelperFromAvro {}
    }.createAllSessionsOfTheWeek(allSessionsOfTheWeek)

    val sessionsOnMonday = dayToSessionsOfTheDayMap(WwwDayOfWeek("Monday"))
    sessionsOnMonday.head.startTime.toString mustEqual "09:00"
    sessionsOnMonday.head.endTime.toString mustEqual "10:30"
    sessionsOnMonday.head.sessionOfTheWeek.isInstanceOf[MondayEarlyMorningSession] mustEqual true
  }

  def asAboveWithFirstSessionHavingMathsAndReading: MatchResult[Any] = {
    val allSessionsOfTheWeek = createSessionOfTheDayWrapperList()
    val dayToSessionsOfTheDayMap = {
      new ClassTimetableConverterHelperFromAvro {}
    }.createAllSessionsOfTheWeek(allSessionsOfTheWeek)

    val sessionsOnMonday = dayToSessionsOfTheDayMap(WwwDayOfWeek("Monday"))
    sessionsOnMonday.head.subjectsWithTimeFractionInTwelves.size mustEqual 2
  }

  def secondSessionIsAsExpected: MatchResult[Any] = {
    val allSessionsOfTheWeek = createSessionOfTheDayWrapperList()
    val dayToSessionsOfTheDayMap = {
      new ClassTimetableConverterHelperFromAvro {}
    }.createAllSessionsOfTheWeek(allSessionsOfTheWeek)

    println(s"All sessions of the week: ${dayToSessionsOfTheDayMap.toString}")

    val sessionsOnMonday = dayToSessionsOfTheDayMap(WwwDayOfWeek("Monday"))
    sessionsOnMonday.tail.head.startTime.toString mustEqual "10:45"
    sessionsOnMonday.tail.head.endTime.toString mustEqual "12:00"
    sessionsOnMonday.tail.head.sessionOfTheWeek.isInstanceOf[MondayLateMorningSession] mustEqual true
  }


  ///////////////////////

  def addAllSessionsToClassTimetableHappyPath(): MatchResult[Any] = {
    val allSessionsOfTheWeek = createSessionOfTheDayWrapperList()
    val dayToSessionsOfTheDayMap = {
      new ClassTimetableConverterHelperFromAvro {}
    }.createAllSessionsOfTheWeek(allSessionsOfTheWeek)

    val wwwClassTimetable: WWWClassTimetable = WWWClassTimetable(None)

    val updatedWwwClassTimetable = {
      new ClassTimetableConverterHelperFromAvro {}
    }.addAllSessionsToClassTimetable(dayToSessionsOfTheDayMap, wwwClassTimetable)

    updatedWwwClassTimetable.getCurrentState mustEqual PartiallyComplete()
  }

}
