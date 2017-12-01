package shared.model.classtimetable

case class WWWClassTimetable(private val schoolDayTimesOption: Option[Map[SchoolDayTimeBoundary, String]])
  extends WwwAllSessionsOfTheWeek {

  lazy val schoolDayTimes: Map[SchoolDayTimeBoundary, String] = createSchoolDayTimes(schoolDayTimesOption)
  override def getSchoolDayTimes: Map[SchoolDayTimeBoundary, String] = schoolDayTimes

  def getCurrentState: ClassTimetableState = {
    if (sessionsOfTheWeek.values.count(_.isFull) == sessionsOfTheWeek.values.size) CompletelyFull()
    else if (sessionsOfTheWeek.values.count(_.isEmpty) == sessionsOfTheWeek.values.size) EntirelyEmpty()
    else PartiallyComplete()
  }

  def getTimeSlotForSession(session: SessionOfTheWeek): Option[WwwTimeSlot] = {
    val maybeSessionBreakDown = sessionsOfTheWeek.get(session)
    maybeSessionBreakDown match {
      case Some(sessionBreakdown) =>
        Some(WwwTimeSlot(sessionBreakdown.startTime, sessionBreakdown.endTime))
      case None => None
    }
  }

  private val beenEdits = false
  def hasBeenEdited: Boolean = beenEdits

  def getFirstAvailableTimeSlot(maybeSessionOfTheWeek: Option[SessionOfTheWeek]): Option[WwwTimeSlot] = {
    (for {
      sessionOfTheWeek <- maybeSessionOfTheWeek
      sessionBreakdown <- sessionsOfTheWeek.get(sessionOfTheWeek)
    } yield sessionBreakdown.getFirstEmptyTimePeriodAvailable).flatten
  }

  def getFirstAvailableTimeSlot(sessionOfTheWeek: SessionOfTheWeek, fractionOfSession: Fraction): Option[WwwTimeSlot] = {
    sessionsOfTheWeek.get(sessionOfTheWeek) match {
      case Some(sessionBreakdown) =>
        sessionBreakdown.getFirstAvailableTimeSlot(fractionOfSession)
      case None => None
    }
  }

  def addSubject(subjectDetail: WwwSubjectDetail, sessionOfTheWeek: SessionOfTheWeek): Boolean = {
    sessionsOfTheWeek.get(sessionOfTheWeek) match {
      case Some(sessionBreakdown) =>
        sessionBreakdown.addSubject(subjectDetail)
      case None => false
    }
  }

  def editSubject(subjectDetail: WwwSubjectDetail, sessionOfTheWeek: SessionOfTheWeek): Boolean = {
    sessionsOfTheWeek.get(sessionOfTheWeek) match {
      case Some(sessionBreakdown) =>
        sessionBreakdown.editSubject(subjectDetail)
      case None => false
    }
  }

  def removeSubject(subjectDetail: WwwSubjectDetail, sessionOfTheWeek: SessionOfTheWeek): Boolean = {
    sessionsOfTheWeek.get(sessionOfTheWeek) match {
      case Some(sessionBreakdown) =>
        sessionBreakdown.removeSubject(subjectDetail)
      case None => false
    }
    }

  def getAdditionalInfoForSubject(subjectDetail: WwwSubjectDetail, sessionOfTheWeek: SessionOfTheWeek): Option[String] = {
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

  def allSessionsOfTheWeek: List[WwwSessionBreakdown] = {
    {{for {
      daySessionsTuple <- allSessionsOfTheWeekInOrderByDay
    } yield daySessionsTuple._2}.flatten}.toList
  }

  def allSessionsOfTheWeekInOrderByDay: Map[DayOfWeek, List[WwwSessionBreakdown]] = {
    def buildDayToSessionsMapAcc(currentMap: Map[DayOfWeek, List[WwwSessionBreakdown]],
                                 remainingSessionsToAdd: List[WwwSessionBreakdown]): Map[DayOfWeek, List[WwwSessionBreakdown]] = {
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
