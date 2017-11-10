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

  def getTimeSlotForSession(session: SessionOfTheWeek): Option[TimeSlot] = {
    val maybeSessionBreakDown = sessionsOfTheWeek.get(session)
    maybeSessionBreakDown match {
      case Some(sessionBreakdown) =>
        Some(TimeSlot(sessionBreakdown.startTime, sessionBreakdown.endTime))
      case None => None
    }
  }

  private val beenEdits = false
  def hasBeenEdited: Boolean = beenEdits

  def getFirstAvailableTimeSlot(maybeSessionOfTheWeek: Option[SessionOfTheWeek]): Option[TimeSlot] = {
    (for {
      sessionOfTheWeek <- maybeSessionOfTheWeek
      sessionBreakdown <- sessionsOfTheWeek.get(sessionOfTheWeek)
    } yield sessionBreakdown.getFirstEmptyTimePeriodAvailable).flatten
  }

  def getFirstAvailableTimeSlot(sessionOfTheWeek: SessionOfTheWeek, fractionOfSession: Fraction): Option[TimeSlot] = {
    sessionsOfTheWeek.get(sessionOfTheWeek) match {
      case Some(sessionBreakdown) =>
        sessionBreakdown.getFirstAvailableTimeSlot(fractionOfSession)
      case None => None
    }
  }

  def addSubject(subjectDetail: SubjectDetail, sessionOfTheWeek: SessionOfTheWeek): Boolean = {
    sessionsOfTheWeek.get(sessionOfTheWeek) match {
      case Some(sessionBreakdown) =>
        sessionBreakdown.addSubject(subjectDetail)
      case None => false
    }
  }

  def editSubject(subjectDetail: SubjectDetail, sessionOfTheWeek: SessionOfTheWeek): Boolean = {
    sessionsOfTheWeek.get(sessionOfTheWeek) match {
      case Some(sessionBreakdown) =>
        sessionBreakdown.editSubject(subjectDetail)
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

  def getAdditionalInfoForSubject(subjectDetail: SubjectDetail, sessionOfTheWeek: SessionOfTheWeek): Option[String] = {
    sessionsOfTheWeek.get(sessionOfTheWeek) match {
      case Some(sessionBreakdown) =>
        val maybeSubjectDetail = sessionBreakdown.getSubject(subjectDetail)
        maybeSubjectDetail match {
          case Some(subject) => Some(subject.lessonAdditionalInfo)
          case None => None
        }
      case None => None
    }
  }

  def clearWholeTimetable(): Unit = {
    sessionsOfTheWeek.values.foreach {
      sessionBreakdown => sessionBreakdown.clear()
    }
  }

  def allSessionsOfTheWeekInOrderByDay: Map[DayOfWeek, List[SessionBreakdown]] = {
    def buildDayToSessionsMapAcc(currentMap: Map[DayOfWeek, List[SessionBreakdown]],
                                 remainingSessionsToAdd: List[SessionBreakdown]): Map[DayOfWeek, List[SessionBreakdown]] = {
      if (remainingSessionsToAdd.isEmpty) currentMap
      else {
        val sessionToAdd = remainingSessionsToAdd.head
        val currentDayOfWeekList = currentMap.getOrElse(sessionToAdd.sessionOfTheWeek.dayOfTheWeek, Nil)
        val newDayOfWeekList = (sessionToAdd :: currentDayOfWeekList).
          sortBy(sessionBreakdown => sessionBreakdown.sessionOfTheWeek.ordinalNumber)

        buildDayToSessionsMapAcc(
          currentMap + (sessionToAdd.sessionOfTheWeek.dayOfTheWeek -> newDayOfWeekList),
          remainingSessionsToAdd.tail)
      }
    }

    buildDayToSessionsMapAcc(Map(), sessionsOfTheWeek.values.toList)
  }

  def currentTimetablePrettyString: String = {
    sessionsOfTheWeek.values.map {
      sessionBreakdown =>
        sessionBreakdown.prettyStringOfSession
    }.mkString
  }


}
