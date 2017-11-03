package shared.model.classtimetable

import java.time.LocalTime

case class ClassTimetable(private val schoolDayTimesOption: Option[Map[SchoolDayTimeBoundary, String]])
  extends AllSessionsOfTheWeek {

  lazy val schoolDayTimes: Map[SchoolDayTimeBoundary, String] = createSchoolDayTimes(schoolDayTimesOption)
  override def getSchoolDayTimes: Map[SchoolDayTimeBoundary, String] = schoolDayTimes

  private val allowedStateValues = Set(
    "ENTIRELY_EMPTY", "PARTIALLY_COMPLETE", "COMPLETE")
  def getCurrentState: String = {
    if (sessionsOfTheWeek.values.count(_.isFull) == sessionsOfTheWeek.values.size) "COMPLETE"
    else if (sessionsOfTheWeek.values.count(_.isEmpty) == sessionsOfTheWeek.values.size) "ENTIRELY_EMPTY"
    else "PARTIALLY_COMPLETE"
  }

  /**
    *
    */
  private val beenEdits = false
  def hasBeenEdited: Boolean = beenEdits

  def addSubject(subjectDetail: SubjectDetail, sessionOfTheWeek: SessionOfTheWeek): Boolean = {
    sessionsOfTheWeek.get(sessionOfTheWeek) match {
      case Some(sessionBreakdown) =>
        sessionBreakdown.addSubject(subjectDetail)
      case None => false
    }
  }

}
