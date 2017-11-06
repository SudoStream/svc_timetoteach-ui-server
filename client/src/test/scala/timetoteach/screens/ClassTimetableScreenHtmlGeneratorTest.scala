package timetoteach.screens

import org.specs2.matcher.MatchResult
import shared.model.classtimetable.ClassTimetable

import scala.annotation.tailrec

class ClassTimetableScreenHtmlGeneratorTest extends org.specs2.mutable.Specification {

  override def is =
    s2"""

  This is a specification for the 'ClassTimetableScreenHtmlGenerator' class

  An empty classtimetabl should
        |have an HTML generation with 15 empty sessions     $emptyClassTimetable
                                                      """

  def emptyClassTimetable: MatchResult[Any] = {
    val emptyClasstimeTable = ClassTimetable(None)
    val html = ClassTimetableScreenHtmlGenerator.generateHtmlForClassTimetable(emptyClasstimeTable)
    println(s"Generated html:=\n$html")
    countSubstring(html,"dayoftheweek-row") mustEqual 15
  }

  def countSubstring(fullString: String, substring: String): Int = {
    @tailrec def count(pos: Int, c: Int): Int = {
      val idx = fullString indexOf(substring, pos)
      if (idx == -1) c else count(idx + substring.length, c + 1)
    }

    count(0, 0)
  }

}
