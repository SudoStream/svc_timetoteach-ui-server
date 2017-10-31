package timetoteach.screens

import org.specs2.matcher.MatchResult

class ClassTimetableScreenTest extends org.specs2.mutable.Specification {

  override def is =
    s2"""

  This is a specification for the 'ClassTimetable' class

  The 'ClassTimetableTest.extractHoursAndMinutes' string should
    be created successfully with valid values         $happyPath
                                                      """

  def happyPath: MatchResult[Any] = {
    val hoursAndMins = ClassTimetableScreen.extractTotalMinutes("9:05 AM")
    hoursAndMins._1 mustEqual 9
    hoursAndMins._2 mustEqual 5
  }

}
