package shared.model

import java.time.LocalTime

case class SessionBreakdown(startTime: LocalTime, endTime: LocalTime) {
  require(startTime.isBefore(endTime))

  def isEmpty: Boolean = true
  def isFull: Boolean = false
  def isPartiallyFull: Boolean = !isEmpty && !isFull


  def addSubjectFullSession(subjectDetail: SubjectDetail): Boolean = {
    false
  }

  def addSubjectToEarliestOneThirdSession(subjectDetail: SubjectDetail): Boolean = {
    false
  }

  def addSubjectToEarliestTwoThirdsSession(subjectDetail: SubjectDetail): Boolean = {
    false
  }

  def addSubjectToEarliestHalfSession(subjectDetail: SubjectDetail): Boolean = {
    false
  }

  def addSubjectWithSpecificTimes(subjectDetail: SubjectDetail, startTime: LocalTime, endTime: LocalTime): Boolean = {
    false
  }


}
