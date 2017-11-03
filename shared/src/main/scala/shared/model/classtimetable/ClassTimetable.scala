package shared.model.classtimetable

case class ClassTimetable(private val schoolDayTimesOption: Option[Map[SchoolDayTimeBoundary, String]])
  extends AllSessionsOfTheWeek {

  lazy val schoolDayTimes: Map[SchoolDayTimeBoundary, String] = createSchoolDayTimes(schoolDayTimesOption)
  override def getSchoolDayTimes: Map[SchoolDayTimeBoundary, String] = schoolDayTimes

  def getCurrentState: ClassTimetableState = {
    if (sessionsOfTheWeek.values.count(_.isFull) == sessionsOfTheWeek.values.size) CompletelyFull()
    else if (sessionsOfTheWeek.values.count(_.isEmpty) == sessionsOfTheWeek.values.size) EntirelyEmpty()
    else PartiallyComplete()
  }

  private val beenEdits = false
  def hasBeenEdited: Boolean = beenEdits

  def addSubject(subjectDetail: SubjectDetail, sessionOfTheWeek: SessionOfTheWeek): Boolean = {
    sessionsOfTheWeek.get(sessionOfTheWeek) match {
      case Some(sessionBreakdown) =>
        sessionBreakdown.addSubject(subjectDetail)
      case None => false
    }
  }

  def removeSubject(subjectDetail: SubjectDetail, sessionOfTheWeek: SessionOfTheWeek): Boolean = {
    sessionsOfTheWeek.get(sessionOfTheWeek) match {
      case Some(sessionBreakdown) =>
        sessionBreakdown.removeSubject(subjectDetail)
      case None => false
    }
  }

  def clearWholeTimetable(): Unit = {
    sessionsOfTheWeek.values.foreach{
      sessionBreakdown => sessionBreakdown.clear()
    }
  }

}
