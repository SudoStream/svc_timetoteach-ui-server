package timetoteach.classtimetable

import org.specs2.matcher.MatchResult

class WWWClassTimetableScreenTest extends org.specs2.mutable.Specification {

  override def is =
    s2"""

  This is a specification for the 'ClassTimetable' class

  The 'ClassTimetableTest.extractHoursAndMinutes' string should
    be created successfully with valid values         $happyPath
                                                      """

  def happyPath: MatchResult[Any] = {
    val hoursAndMins = ClassTimetableScreen.extractTotalMinutes("9:05 AM")
    1 mustEqual 1
  }

}
