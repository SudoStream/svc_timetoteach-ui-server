package tttutils

import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification
import shared.model.classtimetable._

class ClassTimetableConverterFromJsonTest extends Specification {
  override def is =
    s2"""

  This is a specification for the trait 'ClassTimetableConverterFromJson'

  The 'convertSchoolDayTimeBoundaryToStartTimeTuplesToMap' method should
    Create a Map of size 3 when passed a List of 6 tuples                           $tuplesToMapCreatesSameSizedMap
    Create a Map with the Key "SchoolDayStarts()" which has value of "09:00"        $keySchoolDayStartsWithValue0900
    Create a Map with the Key "SchoolDayEnds()" which has value of "15:00"          $keySchoolDayEndsWithValue1500
    Create a Map with rest of values as expected                                    $keySchoolDayEndsWithExpectedValues
"""


  def tuplesToMapCreatesSameSizedMap: MatchResult[Any] = {
    val schoolDayBoundaryToTimeTuples = createSchoolDayBoundaryToTimeTuples()

    val schoolDayTimeBoundaryToTimeMap = {
      new ClassTimetableConverterFromJson {}
    }.convertSchoolDayTimeBoundaryToStartTimeTuplesToMap(schoolDayBoundaryToTimeTuples)

    schoolDayTimeBoundaryToTimeMap.size mustEqual 6
  }

  def keySchoolDayStartsWithValue0900: MatchResult[Any] = {
    val schoolDayBoundaryToTimeTuples = createSchoolDayBoundaryToTimeTuples()

    val schoolDayTimeBoundaryToTimeMap = {
      new ClassTimetableConverterFromJson {}
    }.convertSchoolDayTimeBoundaryToStartTimeTuplesToMap(schoolDayBoundaryToTimeTuples)

    schoolDayTimeBoundaryToTimeMap(SchoolDayStarts()) mustEqual "09:00"
  }

  def keySchoolDayEndsWithValue1500: MatchResult[Any] = {
    val schoolDayBoundaryToTimeTuples = createSchoolDayBoundaryToTimeTuples()

    val schoolDayTimeBoundaryToTimeMap = {
      new ClassTimetableConverterFromJson {}
    }.convertSchoolDayTimeBoundaryToStartTimeTuplesToMap(schoolDayBoundaryToTimeTuples)

    schoolDayTimeBoundaryToTimeMap(SchoolDayEnds()) mustEqual "15:00"
  }

  def keySchoolDayEndsWithExpectedValues: MatchResult[Any] = {
    val schoolDayBoundaryToTimeTuples = createSchoolDayBoundaryToTimeTuples()

    val schoolDayTimeBoundaryToTimeMap = {
      new ClassTimetableConverterFromJson {}
    }.convertSchoolDayTimeBoundaryToStartTimeTuplesToMap(schoolDayBoundaryToTimeTuples)

    schoolDayTimeBoundaryToTimeMap(MorningBreakStarts()) mustEqual "10:30"
    schoolDayTimeBoundaryToTimeMap(MorningBreakEnds()) mustEqual "10:45"
    schoolDayTimeBoundaryToTimeMap(LunchStarts()) mustEqual "12:00"
    schoolDayTimeBoundaryToTimeMap(LunchEnds()) mustEqual "13:00"
  }

  ////////// Test Helpers ///////////
  def createSchoolDayBoundaryToTimeTuples(): List[(SchoolDayTimeBoundary, String)] = {
    (SchoolDayStarts(), "09:00") :: (MorningBreakStarts(), "10:30") :: (MorningBreakEnds(), "10:45") ::
      (LunchStarts(), "12:00") :: (LunchEnds(), "13:00") :: (SchoolDayEnds(), "15:00") :: Nil
  }
}
