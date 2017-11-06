package timetoteach.screens

import java.time.LocalTime

import org.specs2.matcher.MatchResult
import shared.model.classtimetable._

import scala.annotation.tailrec

class ClassTimetableScreenHtmlGeneratorTest extends org.specs2.mutable.Specification {

  override def is =
    s2"""

  This is a specification for the 'ClassTimetableScreenHtmlGenerator' class

  An empty classtimetable should
        |have an HTML generation with 15 empty sessions     $emptyClassTimetable
        |and 2 sessions 50/50 should look as expected       $sessionBreakdownWithMathsAndReadingHalfEach
        |class timetable with 50/50                         $classTimetableWithMathsAndReadingHalfEach
                                                      """

  def emptyClassTimetable: MatchResult[Any] = {
    val emptyClasstimeTable = ClassTimetable(None)
    val html = ClassTimetableScreenHtmlGenerator.generateHtmlForClassTimetable(emptyClasstimeTable)
    println(s"Generated html:=\n$html")
    countSubstring(html, "dayoftheweek-row") mustEqual 5
  }

  def countSubstring(fullString: String, substring: String): Int = {
    @tailrec def count(pos: Int, c: Int): Int = {
      val idx = fullString indexOf(substring, pos)
      if (idx == -1) c else count(idx + substring.length, c + 1)
    }

    count(0, 0)
  }

  def sessionBreakdownWithMathsAndReadingHalfEach: MatchResult[Any] = {
    val breakdown: SessionBreakdown = SessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
    val maths = SubjectDetail(
      SubjectName("subject-maths"),
      TimeSlot(LocalTime.of(9, 0), LocalTime.of(9, 45))
    )
    val reading = SubjectDetail(
      SubjectName("subject-reading"),
      TimeSlot(LocalTime.of(9, 45), LocalTime.of(10, 30))
    )
    breakdown.addSubject(maths)
    breakdown.addSubject(reading)

    val html = ClassTimetableScreenHtmlGenerator.generateSubjectButtons(breakdown)
    html.toString mustEqual
      "<button class=\"col-6 rounded subject subject-maths\">Maths</button>"+
      "<button class=\"col-6 rounded subject subject-reading\">Reading</button>"
  }


  def classTimetableWithMathsAndReadingHalfEach: MatchResult[Any] = {
    val classTimetable = ClassTimetable(None)

    val breakdown: SessionBreakdown = SessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
    val maths = SubjectDetail(
      SubjectName("subject-maths"),
      TimeSlot(LocalTime.of(9, 0), LocalTime.of(9, 45))
    )
    val reading = SubjectDetail(
      SubjectName("subject-reading"),
      TimeSlot(LocalTime.of(9, 45), LocalTime.of(10, 30))
    )

    classTimetable.addSubject(maths, MondayEarlyMorningSession())
    classTimetable.addSubject(reading, MondayEarlyMorningSession())

    val html = ClassTimetableScreenHtmlGenerator.generateHtmlForClassTimetable(classTimetable)
    html.toString mustEqual
      "<button class=\"col-6 rounded subject subject-maths\">Maths</button>"+
        "<button class=\"col-6 rounded subject subject-reading\">Reading</button>"
  }

}
