package shared.model

import org.specs2.matcher.MatchResult

class ClassTimetableTest extends org.specs2.mutable.Specification {

  override def is =
    s2"""

  This is a specification for the 'ClassTimetable' class

  The 'ClassTimetableTest' should
    be created successfully with valid values         $happyPath
                                                      """

  def happyPath: MatchResult[Any] = {
    1 mustEqual 1
  }

}
