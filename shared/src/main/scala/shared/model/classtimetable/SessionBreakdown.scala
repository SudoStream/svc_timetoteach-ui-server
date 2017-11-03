package shared.model.classtimetable

import java.time.LocalTime

import scala.annotation.tailrec
import scala.collection.mutable

case class SessionBreakdown(startTime: LocalTime, endTime: LocalTime) {
  require(startTime.isBefore(endTime))

  private var subjectsInSession: mutable.ListBuffer[SubjectDetail] = scala.collection.mutable.ListBuffer()
  private val SUBJECT_EMPTY = "subject-empty"

  def getEmptyTimePeriodsAvailable: List[(LocalTime, LocalTime)] = {
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
          if (earliestStartNotFilled.isBefore(firstNonEmptySubjectDetail.timeSlot.startTime)) {
            Some((earliestStartNotFilled, firstNonEmptySubjectDetail.timeSlot.startTime))
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

        val maybePeriodAfter = if (lastNonEmptySubjectDetail.timeSlot.endTime.isBefore(this.endTime)) {
          Some(lastNonEmptySubjectDetail.timeSlot.endTime, this.endTime)
        } else None

        maybePeriodAfter match {
          case Some(value) => value :: nextAccValue
          case None => nextAccValue
        }
      } else {
        val (firstNonEmptySubjectDetail: SubjectDetail, nextAccValue: List[(LocalTime, LocalTime)]) = extractAnyEmptySpaceBefore

        emptyPeriodAccumluator(session.tail, firstNonEmptySubjectDetail.timeSlot.endTime, nextAccValue)
      }
    }

    emptyPeriodAccumluator(session, this.startTime, Nil)
  }

  private def reevaluateEmptySpace(): Unit = {
    if (!isEmpty) {
      val subjectsWithoutEmpty: mutable.ListBuffer[SubjectDetail] = subjectsInSession.filterNot(_.subject.value == SUBJECT_EMPTY)
      val emptyTimePeriodsInSession: List[(LocalTime, LocalTime)] = getEmptyTimePeriodsInGivenSession(subjectsWithoutEmpty)
      for (emptyPeriod <- emptyTimePeriodsInSession) {
        subjectsWithoutEmpty += SubjectDetail(SubjectName(SUBJECT_EMPTY), TimeSlot(emptyPeriod._1, emptyPeriod._2))
      }
      subjectsInSession = subjectsWithoutEmpty
    }
  }

  private def addSubjectToSession(subjectDetail: SubjectDetail): Unit = {
    subjectsInSession += subjectDetail
    reevaluateEmptySpace()
  }

  addSubjectToSession(SubjectDetail(SubjectName(SUBJECT_EMPTY), TimeSlot(this.startTime, this.endTime)))

  def isEmpty: Boolean = {
    subjectsInSession.count(_.subject.value == SUBJECT_EMPTY) == 1 && subjectsInSession.size == 1
  }
  def isFull: Boolean = {
    subjectsInSession.count(_.subject.value == SUBJECT_EMPTY) == 0 && subjectsInSession.nonEmpty
  }
  def isPartiallyFull: Boolean = !isEmpty && !isFull

  private def canAddSubjectWithinRequestedTimes(proposedTimeSlot: TimeSlot): Boolean = {
    val subjectsWithoutEmpty = subjectsInSession.filterNot(_.subject.value == SUBJECT_EMPTY)
    val emptyTimePeriodsInSession = getEmptyTimePeriodsInGivenSession(subjectsWithoutEmpty)
    val poptentialEmptyPeriodsThatMatch: Seq[Option[(LocalTime, LocalTime)]] = for {
      emptyPeriod <- emptyTimePeriodsInSession

      validEmptyPeriod = if ((emptyPeriod._1.isBefore(proposedTimeSlot.startTime) || emptyPeriod._1.equals(proposedTimeSlot.startTime))
        &&
        (emptyPeriod._2.isAfter(proposedTimeSlot.endTime) || emptyPeriod._2.equals(proposedTimeSlot.endTime))
      ) {
        Some(emptyPeriod)
      } else {

        None
      }
    } yield validEmptyPeriod

    poptentialEmptyPeriodsThatMatch.count(_.isDefined) == 1
  }

  def numberOfSubjectsInSession: Int = {
    subjectsInSession.filterNot(_.subject.value == SUBJECT_EMPTY).size
  }

  def addSubject(subjectDetail: SubjectDetail): Boolean = {
    if (canAddSubjectWithinRequestedTimes(subjectDetail.timeSlot)) {
      addSubjectToSession(subjectDetail)
      true
    } else {
      false
    }
  }

  def removeSubject(subjectDetail: SubjectDetail): Boolean = {
      false
  }


}
