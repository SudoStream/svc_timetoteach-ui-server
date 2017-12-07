package shared.model.classtimetable

case class WWWClassTimetable(private val schoolDayTimesOption: Option[Map[SchoolDayTimeBoundary, String]])
  extends WwwAllSessionsOfTheWeek {

  override def toString: String = {

    s"""
       |{
       |  "schoolTimes" : [
       |     ${getSchoolDayTimesAsJson()}
       |  ],
       |  "allSessionsOfTheWeek" : [
       |     ${allSessionsOfTheWeekAsJson()}
       |  ]
       |}
    """.stripMargin
  }

  lazy val schoolDayTimes: Map[SchoolDayTimeBoundary, String] = createSchoolDayTimes(schoolDayTimesOption)
  override def getSchoolDayTimes: Map[SchoolDayTimeBoundary, String] = schoolDayTimes

  def getSchoolDayTimesAsJson(): String = {
    val boundariesAsJs = for {
      schoolDayTimeEntryTuple <- schoolDayTimes
      boundaryName = schoolDayTimeEntryTuple._1.value
      boundaryStartTime = schoolDayTimeEntryTuple._2
    } yield
      s"""
         |{
         |  "sessionBoundaryName" : "$boundaryName",
         |  "boundaryStartTime"   : "$boundaryStartTime"
         |}
       """

    boundariesAsJs.mkString(",\n")
  }

  def getCurrentState: ClassTimetableState = {
    if (sessionsOfTheWeek.values.count(_.isFull) == sessionsOfTheWeek.values.size) CompletelyFull()
    else if (sessionsOfTheWeek.values.count(_.isEmpty) == sessionsOfTheWeek.values.size) EntirelyEmpty()
    else PartiallyComplete()
  }

  def getTimeSlotForSession(session: WwwSessionOfTheWeek): Option[WwwTimeSlot] = {
    val maybeSessionBreakDown = sessionsOfTheWeek.get(session)
    maybeSessionBreakDown match {
      case Some(sessionBreakdown) =>
        Some(WwwTimeSlot(sessionBreakdown.startTime, sessionBreakdown.endTime))
      case None => None
    }
  }

  private val beenEdits = false
  def hasBeenEdited: Boolean = beenEdits

  def getFirstAvailableTimeSlot(maybeSessionOfTheWeek: Option[WwwSessionOfTheWeek]): Option[WwwTimeSlot] = {
    (for {
      sessionOfTheWeek <- maybeSessionOfTheWeek
      sessionBreakdown <- sessionsOfTheWeek.get(sessionOfTheWeek)
    } yield sessionBreakdown.getFirstEmptyTimePeriodAvailable).flatten
  }

  def getFirstAvailableTimeSlot(sessionOfTheWeek: WwwSessionOfTheWeek, fractionOfSession: Fraction): Option[WwwTimeSlot] = {
    sessionsOfTheWeek.get(sessionOfTheWeek) match {
      case Some(sessionBreakdown) =>
        sessionBreakdown.getFirstAvailableTimeSlot(fractionOfSession)
      case None => None
    }
  }

  def addSubject(subjectDetail: WwwSubjectDetail, sessionOfTheWeek: WwwSessionOfTheWeek): Boolean = {
    println(s"addSubject: ${subjectDetail.toString} to ${sessionOfTheWeek.toString}")
    sessionsOfTheWeek.get(sessionOfTheWeek) match {
      case Some(sessionBreakdown) =>
        sessionBreakdown.addSubject(subjectDetail)
      case None => false
    }
  }

  def editSubject(subjectDetail: WwwSubjectDetail, sessionOfTheWeek: WwwSessionOfTheWeek): Boolean = {
    sessionsOfTheWeek.get(sessionOfTheWeek) match {
      case Some(sessionBreakdown) =>
        sessionBreakdown.editSubject(subjectDetail)
      case None => false
    }
  }

  def removeSubject(subjectDetail: WwwSubjectDetail, sessionOfTheWeek: WwwSessionOfTheWeek): Boolean = {
    sessionsOfTheWeek.get(sessionOfTheWeek) match {
      case Some(sessionBreakdown) =>
        sessionBreakdown.removeSubject(subjectDetail)
      case None => false
    }
  }

  def getAdditionalInfoForSubject(subjectDetail: WwwSubjectDetail, sessionOfTheWeek: WwwSessionOfTheWeek): Option[String] = {
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

  def allSessionsOfTheWeekAsJson(): String = {
    {
      for {
        dayOfWeekToSessionsTuple <- allSessionsOfTheWeekInOrderByDay
        dayOfWeek = dayOfWeekToSessionsTuple._1
        sessions = dayOfWeekToSessionsTuple._2
      } yield
        s"""
           |{
           |  "dayOfTheWeek" : "${dayOfWeek.value}",
           |  "sessions" : [
           |  ${sessions.map(hmm => hmm.toString()).mkString(",")}
           |     ]
           |}
       """.stripMargin
    }.mkString("\n,")
  }

  def allSessionsOfTheWeek: List[WwwSessionBreakdown] = {
    {
      {
        for {
          daySessionsTuple <- allSessionsOfTheWeekInOrderByDay
        } yield daySessionsTuple._2
      }.flatten
    }.toList
  }

  def allSessionsOfTheWeekInOrderByDay: Map[WwwDayOfWeek, List[WwwSessionBreakdown]] = {
    def buildDayToSessionsMapAcc(currentMap: Map[WwwDayOfWeek, List[WwwSessionBreakdown]],
                                 remainingSessionsToAdd: List[WwwSessionBreakdown]): Map[WwwDayOfWeek, List[WwwSessionBreakdown]] = {
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
