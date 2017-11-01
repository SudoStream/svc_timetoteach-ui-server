package shared.model

import java.time.LocalTime

import scala.annotation.tailrec
import scala.collection.mutable

case class SessionBreakdown(startTime: LocalTime, endTime: LocalTime) {
  require(startTime.isBefore(endTime))

  private val subjectsInSession: mutable.ListBuffer[SubjectDetail] = scala.collection.mutable.ListBuffer()
  private val SUBJECT_EMPTY = "subject-empty"

  def getEmptyTimePeriodsInSession(session: mutable.ListBuffer[SubjectDetail]):
  List[(LocalTime, LocalTime)] = {
    @tailrec
    def emptyPeriodAccumluator(session: mutable.ListBuffer[SubjectDetail],
                               earliestStartNotFilled: Option[LocalTime],
                               accEmptyPeriods: List[(LocalTime, LocalTime)]): List[(LocalTime, LocalTime)] = {
      def extractEmptySpaceBefore = {
        val firstNonEmptySubjectDetail = session.head
        val maybeEmptyPeriodBeforeFirstSubject: Option[(LocalTime, LocalTime)] = earliestStartNotFilled match {
          case Some(start) =>
            if (startTime.isBefore(firstNonEmptySubjectDetail.startTime)) {
              Some((startTime, firstNonEmptySubjectDetail.startTime))
            } else None
          case None => None
        }

        val nextAccValue = maybeEmptyPeriodBeforeFirstSubject match {
          case Some(value) => value :: accEmptyPeriods
          case None => accEmptyPeriods
        }
        (firstNonEmptySubjectDetail, nextAccValue)
      }

      if (session.isEmpty) {
        if (accEmptyPeriods.isEmpty) (this.startTime, this.endTime) :: Nil
        else accEmptyPeriods
      } else if (session.size == 1) {
        val (lastNonEmptySubjectDetail: SubjectDetail, nextAccValue: List[(LocalTime, LocalTime)]) = extractEmptySpaceBefore

        val maybePeriodAfter = if (lastNonEmptySubjectDetail.endTime.isBefore(this.endTime)) {
          Some(lastNonEmptySubjectDetail.endTime, this.endTime)
        } else None

        maybePeriodAfter match {
          case Some(value) => value :: nextAccValue
          case None => nextAccValue
        }
      } else {
        val (firstNonEmptySubjectDetail: SubjectDetail, nextAccValue: List[(LocalTime, LocalTime)]) = extractEmptySpaceBefore

        emptyPeriodAccumluator(session.tail, Some(firstNonEmptySubjectDetail.endTime), nextAccValue)
      }
    }

    emptyPeriodAccumluator(session, Some(this.startTime), Nil)
  }

  private def reevaluateEmptySpace(): Unit = {
    if (!isEmpty) {
      val subjectsWithoutEmpty = subjectsInSession.filterNot(_.subject.value == SUBJECT_EMPTY)
      val emptyTimePeriodsInSession = getEmptyTimePeriodsInSession(subjectsWithoutEmpty)
      for (emptyPeriod <- emptyTimePeriodsInSession) {
        subjectsWithoutEmpty += SubjectDetail(SubjectName(SUBJECT_EMPTY), emptyPeriod._1, emptyPeriod._2)
      }
    }
  }

  private def addSubjectToSession(subjectDetail: SubjectDetail): Unit = {
    subjectsInSession += subjectDetail
    reevaluateEmptySpace()
  }

  addSubjectToSession(SubjectDetail(SubjectName(SUBJECT_EMPTY), this.startTime, this.endTime))

  def isEmpty: Boolean = {
    subjectsInSession.count(_.subject.value == SUBJECT_EMPTY) == 1 && subjectsInSession.size == 1
  }
  def isFull: Boolean = {
    subjectsInSession.count(_.subject.value == SUBJECT_EMPTY) == 0 && subjectsInSession.nonEmpty
  }
  def isPartiallyFull: Boolean = !isEmpty && !isFull

  def addSubjectWithSpecificTimes(subjectDetail: SubjectDetail): Boolean = {
    //    if (canAddSubjectWithinRequestedTimes(startTime, endTime)) {
    //      addSubjectToSession(subjectDetail)
    //    } else {
    false
    //    }
  }


}
