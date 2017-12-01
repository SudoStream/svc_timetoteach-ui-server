package utils

import java.time.LocalTime

import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.sessions.SessionBoundaryType
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.subjectdetail.{SubjectDetailAdditionalInfo, SubjectDetailWrapper, SubjectName}
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification
import shared.model.classtimetable._

class ClassTimetableConverterHelperToAvroTest extends Specification {
  override def is =
    s2"""

  This is a specification for the trait 'ClassTimetableConverterHelper'

  The 'createSessionBoundaries' method should
    Convert a WWW Session Boundaries To an Avro version has same size                                    $convertWwwSessionBoundariesToAvroHasSameSize
    Convert a WWW Session Boundaries To an Avro version with 1 SchoolDayStarts                           $convertWwwSessionBoundariesToAvroHasOneSchoolDayStarts
    Convert a WWW Session Boundaries To an Avro version with SchoolDayStarts is StartOfTeaching          $convertWwwSessionBoundariesToAvroHasOneSchoolDayStartsAsStartOfTeachingSession
    Convert a WWW Session Boundaries To an Avro version with SchoolDayStarts other expected values       $convertWwwSessionBoundariesToAvroHasOneSchoolDayStartsWithOtherExpectedValues
    Convert a WWW Session Boundaries To an Avro version with 1 MorningBreakStarts                        $convertWwwSessionBoundariesToAvroHasOneMorningBreakStarts
    Convert a WWW Session Boundaries To an Avro version with MorningBreakStarts is StartOfTeaching       $convertWwwSessionBoundariesToAvroHasOneMorningBreakStartsAsStartOfTeachingSession
    Convert a WWW Session Boundaries To an Avro version with MorningBreakStarts other expected values    $convertWwwSessionBoundariesToAvroHasOneMorningBreakStartsWithOtherExpectedValues
    Convert a WWW Session Boundaries To an Avro version with 1 MorningBreakEnds                          $convertWwwSessionBoundariesToAvroHasOneMorningBreakEnds
    Convert a WWW Session Boundaries To an Avro version with MorningBreakEnds is StartOfTeaching         $convertWwwSessionBoundariesToAvroHasOneMorningBreakEndsAsStartOfTeachingSession
    Convert a WWW Session Boundaries To an Avro version with MorningBreakEnds other expected values      $convertWwwSessionBoundariesToAvroHasOneMorningBreakEndsWithOtherExpectedValues
    Convert a WWW Session Boundaries To an Avro version with 1 LunchStarts                               $convertWwwSessionBoundariesToAvroHasOneLunchStarts
    Convert a WWW Session Boundaries To an Avro version with LunchStarts is StartOfTeaching              $convertWwwSessionBoundariesToAvroHasOneLunchStartsAsStartOfTeachingSession
    Convert a WWW Session Boundaries To an Avro version with LunchStarts other expected values           $convertWwwSessionBoundariesToAvroHasOneLunchStartsWithOtherExpectedValues
    Convert a WWW Session Boundaries To an Avro version with 1 LunchEnds                                 $convertWwwSessionBoundariesToAvroHasOneLunchEnds
    Convert a WWW Session Boundaries To an Avro version with LunchEnds is StartOfTeaching                $convertWwwSessionBoundariesToAvroHasOneLunchEndsAsStartOfTeachingSession
    Convert a WWW Session Boundaries To an Avro version with LunchEnds other expected values             $convertWwwSessionBoundariesToAvroHasOneLunchEndsWithOtherExpectedValues
    Convert a WWW Session Boundaries To an Avro version with 1 SchoolDayEnds                             $convertWwwSessionBoundariesToAvroHasOneSchoolDayEnds
    Convert a WWW Session Boundaries To an Avro version with SchoolDayEnds is EndOfTeaching              $convertWwwSessionBoundariesToAvroHasOneSchoolDayEndsAsEndsOfTeachingSession
    Convert a WWW Session Boundaries To an Avro version with SchoolDayEnds other expected values         $convertWwwSessionBoundariesToAvroHasOneSchoolDayEndsWithOtherExpectedValues

  The 'createSchoolTimes' method should
    Create an Avro ClassTimetableSchoolTimes object given a WWW School Day Times model                   $createSchoolTimesHappyPath

  The 'convertWwwSubjectDetailsToAvro' method should
    Create a list of SubjectDetailWrapper the same size as the input list of WwwSubjectDetail            $listOfSubjectDetailWrapperTheSameSizeAsTheInputListOfWwwSubjectDetail
    Create a list of SubjectDetailWrapper with one maths subject                                         $listOfSubjectDetailWrapperHasOneMathsSubject
    Create a list of SubjectDetailWrapper with one maths subject with expected values                    $listOfSubjectDetailWrapperHasOneMathsSubjectWithExpectedValues
    Create a list of SubjectDetailWrapper with one reading subject                                       $listOfSubjectDetailWrapperHasOneReadingSubject
    Create a list of SubjectDetailWrapper with one reading subject with expected values                  $listOfSubjectDetailWrapperHasOneReadingSubjectWithExpectedValues
    Create a list of SubjectDetailWrapper with zero writing subject                                      $listOfSubjectDetailWrapperHasZeroWritingSubject
  """

  def createSessionBoundaries(): Map[SchoolDayTimeBoundary, String] = {
    Map(
      SchoolDayStarts() -> "09:00",
      MorningBreakStarts() -> "10:30",
      MorningBreakEnds() -> "10:45",
      LunchStarts() -> "12:00",
      LunchEnds() -> "13:00",
      SchoolDayEnds() -> "15:00"
    )
  }

  def convertWwwSessionBoundariesToAvroHasSameSize: MatchResult[Any] = {
    val sessionBoundaries = createSessionBoundaries()
    val avroBoundaries = {
      new ClassTimetableConverterHelperToAvro {}
    }.createSessionBoundaries(sessionBoundaries)

    avroBoundaries.size mustEqual sessionBoundaries.size
  }

  /////

  def convertWwwSessionBoundariesToAvroHasOneSchoolDayStarts: MatchResult[Any] = {
    val sessionBoundaries = createSessionBoundaries()
    val avroBoundaries = {
      new ClassTimetableConverterHelperToAvro {}
    }.createSessionBoundaries(sessionBoundaries)

    println(s"avro: ${avroBoundaries.toString}")
    avroBoundaries.count(_.sessionBoundary.sessionBoundaryName.sessionBoundaryName == "school-day-starts") mustEqual 1
  }

  def convertWwwSessionBoundariesToAvroHasOneSchoolDayStartsAsStartOfTeachingSession: MatchResult[Any] = {
    val sessionBoundaries = createSessionBoundaries()
    val avroBoundaries = {
      new ClassTimetableConverterHelperToAvro {}
    }.createSessionBoundaries(sessionBoundaries)

    val filtered = avroBoundaries.filter(_.sessionBoundary.sessionBoundaryName.sessionBoundaryName == "school-day-starts")
    filtered.head.sessionBoundary.boundaryType mustEqual SessionBoundaryType.START_OF_TEACHING_SESSION
  }

  def convertWwwSessionBoundariesToAvroHasOneSchoolDayStartsWithOtherExpectedValues: MatchResult[Any] = {
    val sessionBoundaries = createSessionBoundaries()
    val avroBoundaries = {
      new ClassTimetableConverterHelperToAvro {}
    }.createSessionBoundaries(sessionBoundaries)

    val filtered = avroBoundaries.filter(_.sessionBoundary.sessionBoundaryName.sessionBoundaryName == "school-day-starts")
    val sessionBoundary = filtered.head.sessionBoundary

    sessionBoundary.boundaryStartTime.timeIso8601 mustEqual "09:00"
    sessionBoundary.sessionName.get.sessionName mustEqual "EarlyMorningSession"
  }

  ///

  def convertWwwSessionBoundariesToAvroHasOneSchoolDayEnds: MatchResult[Any] = {
    val sessionBoundaries = createSessionBoundaries()
    val avroBoundaries = {
      new ClassTimetableConverterHelperToAvro {}
    }.createSessionBoundaries(sessionBoundaries)

    println(s"avro: ${avroBoundaries.toString}")
    avroBoundaries.count(_.sessionBoundary.sessionBoundaryName.sessionBoundaryName == "school-day-ends") mustEqual 1
  }

  def convertWwwSessionBoundariesToAvroHasOneSchoolDayEndsAsEndsOfTeachingSession: MatchResult[Any] = {
    val sessionBoundaries = createSessionBoundaries()
    val avroBoundaries = {
      new ClassTimetableConverterHelperToAvro {}
    }.createSessionBoundaries(sessionBoundaries)

    val filtered = avroBoundaries.filter(_.sessionBoundary.sessionBoundaryName.sessionBoundaryName == "school-day-ends")
    filtered.head.sessionBoundary.boundaryType mustEqual SessionBoundaryType.END_OF_TEACHING_SESSION
  }

  def convertWwwSessionBoundariesToAvroHasOneSchoolDayEndsWithOtherExpectedValues: MatchResult[Any] = {
    val sessionBoundaries = createSessionBoundaries()
    val avroBoundaries = {
      new ClassTimetableConverterHelperToAvro {}
    }.createSessionBoundaries(sessionBoundaries)

    val filtered = avroBoundaries.filter(_.sessionBoundary.sessionBoundaryName.sessionBoundaryName == "school-day-ends")
    val sessionBoundary = filtered.head.sessionBoundary

    sessionBoundary.boundaryStartTime.timeIso8601 mustEqual "15:00"
    sessionBoundary.sessionName.isDefined mustEqual false
  }

  ///

  def convertWwwSessionBoundariesToAvroHasOneMorningBreakStarts: MatchResult[Any] = {
    val sessionBoundaries = createSessionBoundaries()
    val avroBoundaries = {
      new ClassTimetableConverterHelperToAvro {}
    }.createSessionBoundaries(sessionBoundaries)

    println(s"avro: ${avroBoundaries.toString}")
    avroBoundaries.count(_.sessionBoundary.sessionBoundaryName.sessionBoundaryName == "morning-break-starts") mustEqual 1
  }

  def convertWwwSessionBoundariesToAvroHasOneMorningBreakStartsAsStartOfTeachingSession: MatchResult[Any] = {
    val sessionBoundaries = createSessionBoundaries()
    val avroBoundaries = {
      new ClassTimetableConverterHelperToAvro {}
    }.createSessionBoundaries(sessionBoundaries)

    val filtered = avroBoundaries.filter(_.sessionBoundary.sessionBoundaryName.sessionBoundaryName == "morning-break-starts")
    filtered.head.sessionBoundary.boundaryType mustEqual SessionBoundaryType.END_OF_TEACHING_SESSION
  }

  def convertWwwSessionBoundariesToAvroHasOneMorningBreakStartsWithOtherExpectedValues: MatchResult[Any] = {
    val sessionBoundaries = createSessionBoundaries()
    val avroBoundaries = {
      new ClassTimetableConverterHelperToAvro {}
    }.createSessionBoundaries(sessionBoundaries)

    val filtered = avroBoundaries.filter(_.sessionBoundary.sessionBoundaryName.sessionBoundaryName == "morning-break-starts")
    val sessionBoundary = filtered.head.sessionBoundary

    sessionBoundary.boundaryStartTime.timeIso8601 mustEqual "10:30"
    sessionBoundary.sessionName.isDefined mustEqual false
  }

  ///

  def convertWwwSessionBoundariesToAvroHasOneMorningBreakEnds: MatchResult[Any] = {
    val sessionBoundaries = createSessionBoundaries()
    val avroBoundaries = {
      new ClassTimetableConverterHelperToAvro {}
    }.createSessionBoundaries(sessionBoundaries)

    println(s"avro: ${avroBoundaries.toString}")
    avroBoundaries.count(_.sessionBoundary.sessionBoundaryName.sessionBoundaryName == "morning-break-ends") mustEqual 1
  }

  def convertWwwSessionBoundariesToAvroHasOneMorningBreakEndsAsStartOfTeachingSession: MatchResult[Any] = {
    val sessionBoundaries = createSessionBoundaries()
    val avroBoundaries = {
      new ClassTimetableConverterHelperToAvro {}
    }.createSessionBoundaries(sessionBoundaries)

    val filtered = avroBoundaries.filter(_.sessionBoundary.sessionBoundaryName.sessionBoundaryName == "morning-break-ends")
    filtered.head.sessionBoundary.boundaryType mustEqual SessionBoundaryType.START_OF_TEACHING_SESSION
  }

  def convertWwwSessionBoundariesToAvroHasOneMorningBreakEndsWithOtherExpectedValues: MatchResult[Any] = {
    val sessionBoundaries = createSessionBoundaries()
    val avroBoundaries = {
      new ClassTimetableConverterHelperToAvro {}
    }.createSessionBoundaries(sessionBoundaries)

    val filtered = avroBoundaries.filter(_.sessionBoundary.sessionBoundaryName.sessionBoundaryName == "morning-break-ends")
    val sessionBoundary = filtered.head.sessionBoundary

    sessionBoundary.boundaryStartTime.timeIso8601 mustEqual "10:45"
    sessionBoundary.sessionName.isDefined mustEqual true
    sessionBoundary.sessionName.get.sessionName mustEqual "LateMorningSession"
  }

  ///

  def convertWwwSessionBoundariesToAvroHasOneLunchStarts: MatchResult[Any] = {
    val sessionBoundaries = createSessionBoundaries()
    val avroBoundaries = {
      new ClassTimetableConverterHelperToAvro {}
    }.createSessionBoundaries(sessionBoundaries)

    println(s"avro: ${avroBoundaries.toString}")
    avroBoundaries.count(_.sessionBoundary.sessionBoundaryName.sessionBoundaryName == "lunch-starts") mustEqual 1
  }

  def convertWwwSessionBoundariesToAvroHasOneLunchStartsAsStartOfTeachingSession: MatchResult[Any] = {
    val sessionBoundaries = createSessionBoundaries()
    val avroBoundaries = {
      new ClassTimetableConverterHelperToAvro {}
    }.createSessionBoundaries(sessionBoundaries)

    val filtered = avroBoundaries.filter(_.sessionBoundary.sessionBoundaryName.sessionBoundaryName == "lunch-starts")
    filtered.head.sessionBoundary.boundaryType mustEqual SessionBoundaryType.END_OF_TEACHING_SESSION
  }

  def convertWwwSessionBoundariesToAvroHasOneLunchStartsWithOtherExpectedValues: MatchResult[Any] = {
    val sessionBoundaries = createSessionBoundaries()
    val avroBoundaries = {
      new ClassTimetableConverterHelperToAvro {}
    }.createSessionBoundaries(sessionBoundaries)

    val filtered = avroBoundaries.filter(_.sessionBoundary.sessionBoundaryName.sessionBoundaryName == "lunch-starts")
    val sessionBoundary = filtered.head.sessionBoundary

    sessionBoundary.boundaryStartTime.timeIso8601 mustEqual "12:00"
    sessionBoundary.sessionName.isDefined mustEqual false
  }

  ///

  def convertWwwSessionBoundariesToAvroHasOneLunchEnds: MatchResult[Any] = {
    val sessionBoundaries = createSessionBoundaries()
    val avroBoundaries = {
      new ClassTimetableConverterHelperToAvro {}
    }.createSessionBoundaries(sessionBoundaries)

    println(s"avro: ${avroBoundaries.toString}")
    avroBoundaries.count(_.sessionBoundary.sessionBoundaryName.sessionBoundaryName == "lunch-ends") mustEqual 1
  }

  def convertWwwSessionBoundariesToAvroHasOneLunchEndsAsStartOfTeachingSession: MatchResult[Any] = {
    val sessionBoundaries = createSessionBoundaries()
    val avroBoundaries = {
      new ClassTimetableConverterHelperToAvro {}
    }.createSessionBoundaries(sessionBoundaries)

    val filtered = avroBoundaries.filter(_.sessionBoundary.sessionBoundaryName.sessionBoundaryName == "lunch-ends")
    filtered.head.sessionBoundary.boundaryType mustEqual SessionBoundaryType.START_OF_TEACHING_SESSION
  }

  def convertWwwSessionBoundariesToAvroHasOneLunchEndsWithOtherExpectedValues: MatchResult[Any] = {
    val sessionBoundaries = createSessionBoundaries()
    val avroBoundaries = {
      new ClassTimetableConverterHelperToAvro {}
    }.createSessionBoundaries(sessionBoundaries)

    val filtered = avroBoundaries.filter(_.sessionBoundary.sessionBoundaryName.sessionBoundaryName == "lunch-ends")
    val sessionBoundary = filtered.head.sessionBoundary

    sessionBoundary.boundaryStartTime.timeIso8601 mustEqual "13:00"
    sessionBoundary.sessionName.isDefined mustEqual true
    sessionBoundary.sessionName.get.sessionName mustEqual "AfternoonSession"
  }

  //////////////

  def createSchoolTimesHappyPath: MatchResult[Any] = {
    val sessionBoundaries = createSessionBoundaries()
    val avroSchoolTimes = {
      new ClassTimetableConverterHelperToAvro {}
    }.createSchoolTimes(sessionBoundaries)

    avroSchoolTimes.schoolSessionBoundaries.size mustEqual 6
  }

  /////////////

  def createAListOfWwwSubjectDetail(): List[WwwSubjectDetail] = {
    WwwSubjectDetail(WwwSubjectName("subject-maths"), WwwTimeSlot(LocalTime.of(9, 0), LocalTime.of(10, 0))) ::
      WwwSubjectDetail(WwwSubjectName("subject-reading"), WwwTimeSlot(LocalTime.of(14, 5), LocalTime.of(15, 30)), "Plus Homework") :: Nil
  }

  def listOfSubjectDetailWrapperTheSameSizeAsTheInputListOfWwwSubjectDetail: MatchResult[Any] = {
    val wwwSubjectDetails = createAListOfWwwSubjectDetail()
    val avroSubjectDetails = {
      new ClassTimetableConverterHelperToAvro {}
    }.convertWwwSubjectDetailsToAvro(wwwSubjectDetails)

    avroSubjectDetails.size mustEqual wwwSubjectDetails.size
  }

  def listOfSubjectDetailWrapperHasOneMathsSubject: MatchResult[Any] = {
    val wwwSubjectDetails = createAListOfWwwSubjectDetail()
    val avroSubjectDetails = {
      new ClassTimetableConverterHelperToAvro {}
    }.convertWwwSubjectDetailsToAvro(wwwSubjectDetails)

    avroSubjectDetails.count(_.subjectDetail.subjectName == SubjectName.MATHS) mustEqual 1
  }

  def listOfSubjectDetailWrapperHasOneMathsSubjectWithExpectedValues: MatchResult[Any] = {
    val wwwSubjectDetails = createAListOfWwwSubjectDetail()
    val avroSubjectDetails = {
      new ClassTimetableConverterHelperToAvro {}
    }.convertWwwSubjectDetailsToAvro(wwwSubjectDetails)

    val subjects = avroSubjectDetails.filter(_.subjectDetail.subjectName == SubjectName.MATHS)
    val mathsSubjectDetail = subjects.head.subjectDetail
    mathsSubjectDetail.additionalInfo.additionalInfo mustEqual ""
    mathsSubjectDetail.startTime.timeIso8601 mustEqual "09:00"
    mathsSubjectDetail.endTime.timeIso8601 mustEqual "10:00"
  }


  def listOfSubjectDetailWrapperHasOneReadingSubject: MatchResult[Any] = {
    val wwwSubjectDetails = createAListOfWwwSubjectDetail()
    val avroSubjectDetails = {
      new ClassTimetableConverterHelperToAvro {}
    }.convertWwwSubjectDetailsToAvro(wwwSubjectDetails)

    avroSubjectDetails.count(_.subjectDetail.subjectName == SubjectName.READING) mustEqual 1
  }



  def listOfSubjectDetailWrapperHasOneReadingSubjectWithExpectedValues: MatchResult[Any] = {
    val wwwSubjectDetails = createAListOfWwwSubjectDetail()
    val avroSubjectDetails = {
      new ClassTimetableConverterHelperToAvro {}
    }.convertWwwSubjectDetailsToAvro(wwwSubjectDetails)

    val subjects = avroSubjectDetails.filter(_.subjectDetail.subjectName == SubjectName.READING)
    val mathsSubjectDetail = subjects.head.subjectDetail
    mathsSubjectDetail.additionalInfo.additionalInfo mustEqual "Plus Homework"
    mathsSubjectDetail.startTime.timeIso8601 mustEqual "14:05"
    mathsSubjectDetail.endTime.timeIso8601 mustEqual "15:30"
  }

  def listOfSubjectDetailWrapperHasZeroWritingSubject: MatchResult[Any] = {
    val wwwSubjectDetails = createAListOfWwwSubjectDetail()
    val avroSubjectDetails = {
      new ClassTimetableConverterHelperToAvro {}
    }.convertWwwSubjectDetailsToAvro(wwwSubjectDetails)

    avroSubjectDetails.count(_.subjectDetail.subjectName == SubjectName.WRITING) mustEqual 0
  }


}
