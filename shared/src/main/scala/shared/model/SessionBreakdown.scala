package shared.model

import java.time.LocalTime

import scala.annotation.tailrec
import scala.collection.mutable

case class SessionBreakdown(startTime: LocalTime, endTime: LocalTime) {
  require(startTime.isBefore(endTime))

  private val subjectsInSession: mutable.ListBuffer[SubjectDetail] = scala.collection.mutable.ListBuffer()
  private val SUBJECT_EMPTY = "subject-empty"

  def getEmptyTimePeriodsAvailable : List[(LocalTime, LocalTime)] = {
    val subjectsWithoutEmpty = subjectsInSession.filterNot(_.subject.value == SUBJECT_EMPTY)
    getEmptyTimePeriodsInGivenSession(subjectsWithoutEmpty)
  }

  def getEmptyTimePeriodsInGivenSession(session: mutable.ListBuffer[SubjectDetail]):
  List[(LocalTime, LocalTime)] = {
    @tailrec
    def emptyPeriodAccumluator(session: mutable.ListBuffer[SubjectDetail],
                               earliestStartNotFilled: LocalTime,
                               accEmptyPeriods: List[(LocalTime, LocalTime)]): List[(LocalTime, LocalTime)] = {
      def extractAnyEmptySpaceBefore = {
        val firstNonEmptySubjectDetail = session.head
        val maybeEmptyPeriodBeforeFirstSubject: Option[(LocalTime, LocalTime)] =
          if (earliestStartNotFilled.isBefore(firstNonEmptySubjectDetail.startTime)) {
            Some((earliestStartNotFilled, firstNonEmptySubjectDetail.startTime))
          } else None

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
        val (lastNonEmptySubjectDetail: SubjectDetail, nextAccValue: List[(LocalTime, LocalTime)]) = extractAnyEmptySpaceBefore

        val maybePeriodAfter = if (lastNonEmptySubjectDetail.endTime.isBefore(this.endTime)) {
          Some(lastNonEmptySubjectDetail.endTime, this.endTime)
        } else None

        maybePeriodAfter match {
          case Some(value) => value :: nextAccValue
          case None => nextAccValue
        }
      } else {
        val (firstNonEmptySubjectDetail: SubjectDetail, nextAccValue: List[(LocalTime, LocalTime)]) = extractAnyEmptySpaceBefore

        emptyPeriodAccumluator(session.tail, firstNonEmptySubjectDetail.endTime, nextAccValue)
      }
    }

    emptyPeriodAccumluator(session, this.startTime, Nil)
  }

  private def reevaluateEmptySpace(): Unit = {
    if (!isEmpty) {
      val subjectsWithoutEmpty = subjectsInSession.filterNot(_.subject.value == SUBJECT_EMPTY)
      val emptyTimePeriodsInSession = getEmptyTimePeriodsInGivenSession(subjectsWithoutEmpty)
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

  def canAddSubjectWithinRequestedTimes(proposedStartTime: LocalTime, proposedEndTime: LocalTime): Boolean = {
    val subjectsWithoutEmpty = subjectsInSession.filterNot(_.subject.value == SUBJECT_EMPTY)
    val emptyTimePeriodsInSession = getEmptyTimePeriodsInGivenSession(subjectsWithoutEmpty)

    val poptentialEmptyPeriodsThatMatch: Seq[Option[(LocalTime, LocalTime)]] = for {
      emptyPeriod <- emptyTimePeriodsInSession
      validEmptyPeriod = if ((emptyPeriod._1.isBefore(proposedStartTime) || emptyPeriod._1.eq(proposedStartTime))
        &&
        (emptyPeriod._2.isAfter(proposedEndTime) || emptyPeriod._2.eq(proposedEndTime))
      ) {
        Some(emptyPeriod)
      } else None
    } yield validEmptyPeriod


    poptentialEmptyPeriodsThatMatch.count( _.isDefined) == 1
  }

  def addSubject(subjectDetail: SubjectDetail): Boolean = {
    if (canAddSubjectWithinRequestedTimes(startTime, endTime)) {
      addSubjectToSession(subjectDetail)
      true
    } else {
      false
    }
  }


}
