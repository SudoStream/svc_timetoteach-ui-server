package models.timetoteach.classtimetable

import java.time.LocalTime

import org.specs2.mutable.Specification

class SchoolDayTimesTest extends Specification {
  override def is =
    s2"""

  This is a specification for the 'SchoolDayTimes' class

  The 'SchoolDayTimes' string should
    be created successfully with valid values                        $happyPath
    throw runtime exception if morning break before school starts    $morningBreakBeforeSchoolStart
                                                      """

  def happyPath = {
    val schoolTimes = SchoolDayTimes(
      schoolDayStarts = LocalTime.of(9, 0),
      morningBreakStarts = LocalTime.of(10, 0),
      morningBreakEnds = LocalTime.of(10, 45),
      lunchStarts = LocalTime.of(12, 0),
      lunchEnds = LocalTime.of(13, 0),
      schoolDayEnds = LocalTime.of(15, 0)
    )

    schoolTimes.schoolDayStarts mustEqual LocalTime.of(9, 0)
  }

  def morningBreakBeforeSchoolStart = {
    SchoolDayTimes(
      schoolDayStarts = LocalTime.of(9, 0),
      morningBreakStarts = LocalTime.of(8, 0),
      morningBreakEnds = LocalTime.of(10, 45),
      lunchStarts = LocalTime.of(12, 0),
      lunchEnds = LocalTime.of(13, 0),
      schoolDayEnds = LocalTime.of(15, 0)
    ) must throwA[RuntimeException]
  }

}
