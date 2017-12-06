package shared.model.classtimetable

import java.time.LocalTime
import java.time.temporal.ChronoUnit.MINUTES

import scala.annotation.tailrec
import scala.collection.mutable

case class WwwSessionBreakdown(sessionOfTheWeek: WwwSessionOfTheWeek, startTime: LocalTime, endTime: LocalTime) {
  require(startTime.isBefore(endTime))

  private var subjectsInSession: mutable.ListBuffer[WwwSubjectDetail] = scala.collection.mutable.ListBuffer()
  private val SUBJECT_EMPTY = "subject-empty"

  override def toString(): String = {
    {
      for {
        subjectDetail <- subjectsInSession
      } yield
        s"""
          |{
          |  "subject" : "${subjectDetail.subject.value}",
          |  "startTimeIso8601" : "${subjectDetail.timeSlot.startTime.toString}",
          |  "endTimeIso8601" : "${subjectDetail.timeSlot.endTime.toString}",
          |  "lessonAdditionalInfo" : "${subjectDetail.lessonAdditionalInfo}"
          |}
       """.stripMargin
    }.mkString(",\n")
  }

  def getEmptyTimePeriodsAvailable: List[(LocalTime, LocalTime)] = {
    val subjectsWithoutEmpty = subjectsInSession.filterNot(_.subject.value == SUBJECT_EMPTY)
    getEmptyTimePeriodsInGivenSession(subjectsWithoutEmpty)
  }

  def getFirstEmptyTimePeriodAvailable: Option[WwwTimeSlot] = {
    val allEmptyPeriods = getEmptyTimePeriodsAvailable.sortBy(_._1)
    if (allEmptyPeriods.nonEmpty) {
      val timeSlot = WwwTimeSlot(allEmptyPeriods.head._1, allEmptyPeriods.head._2)
      Some(timeSlot)
    } else {
      None
    }
  }

  def getEmptyTimePeriodsInGivenSession(session: mutable.ListBuffer[WwwSubjectDetail]):
  List[(LocalTime, LocalTime)] = {
    @tailrec
    def emptyPeriodAccumluator(session: mutable.ListBuffer[WwwSubjectDetail],
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
        val (lastNonEmptySubjectDetail: WwwSubjectDetail, nextAccValue: List[(LocalTime, LocalTime)]) = extractAnyEmptySpaceBefore

        val maybePeriodAfter = if (lastNonEmptySubjectDetail.timeSlot.endTime.isBefore(this.endTime)) {
          Some(lastNonEmptySubjectDetail.timeSlot.endTime, this.endTime)
        } else None

        maybePeriodAfter match {
          case Some(value) => value :: nextAccValue
          case None => nextAccValue
        }
      } else {
        val (firstNonEmptySubjectDetail: WwwSubjectDetail, nextAccValue: List[(LocalTime, LocalTime)]) = extractAnyEmptySpaceBefore

        emptyPeriodAccumluator(session.tail, firstNonEmptySubjectDetail.timeSlot.endTime, nextAccValue)
      }
    }

    emptyPeriodAccumluator(session, this.startTime, Nil)
  }

  private def reevaluateEmptySpace(): Unit = {
    if (!isEmpty) {
      val subjectsWithoutEmpty: mutable.ListBuffer[WwwSubjectDetail] = subjectsInSession.filterNot(_.subject.value == SUBJECT_EMPTY)
      println(s"reevaluateEmptySpace subjectsWithoutEmpty: ${subjectsWithoutEmpty.toString}")
      val emptyTimePeriodsInSession: List[(LocalTime, LocalTime)] = getEmptyTimePeriodsInGivenSession(subjectsWithoutEmpty)
      println(s"reevaluateEmptySpace emptyTimePeriodsInSession: ${emptyTimePeriodsInSession.toString}")
      for (emptyPeriod <- emptyTimePeriodsInSession) {
        subjectsWithoutEmpty += WwwSubjectDetail(WwwSubjectName(SUBJECT_EMPTY), WwwTimeSlot(emptyPeriod._1, emptyPeriod._2))
      }
      subjectsInSession = subjectsWithoutEmpty
    }
  }

  private def addSubjectToSession(subjectDetail: WwwSubjectDetail): Unit = {
    subjectsInSession += subjectDetail
    subjectsInSession = subjectsInSession.sortBy(_.timeSlot.startTime)
    reevaluateEmptySpace()
  }

  private def editSubjectToSession(subjectDetailToEdit: WwwSubjectDetail): Boolean = {
    val subjectExistsOnce = subjectsInSession.count(_ == subjectDetailToEdit)
    if (subjectExistsOnce == 1) {
      subjectsInSession = subjectsInSession.filterNot(_ == subjectDetailToEdit)
      subjectsInSession += subjectDetailToEdit
      subjectsInSession = subjectsInSession.sortBy(_.timeSlot.startTime)
      reevaluateEmptySpace()
      true
    } else {
      false
    }
  }

  addSubjectToSession(WwwSubjectDetail(WwwSubjectName(SUBJECT_EMPTY), WwwTimeSlot(this.startTime, this.endTime)))

  def isEmpty: Boolean = {
    subjectsInSession.count(_.subject.value == SUBJECT_EMPTY) == 1 && subjectsInSession.size == 1
  }
  def isFull: Boolean = {
    subjectsInSession.count(_.subject.value == SUBJECT_EMPTY) == 0 && subjectsInSession.nonEmpty
  }
  def isPartiallyFull: Boolean = !isEmpty && !isFull

  private def canAddSubjectWithinRequestedTimes(proposedTimeSlot: WwwTimeSlot): Boolean = {
    println(s"proposedTimeSlot:- ${proposedTimeSlot.toString}")
    val subjectsWithoutEmpty = subjectsInSession.filterNot(_.subject.value == SUBJECT_EMPTY)
    val emptyTimePeriodsInSession = getEmptyTimePeriodsInGivenSession(subjectsWithoutEmpty)
    println(s"emptyTimePeriodsInSession : ${emptyTimePeriodsInSession.toString()}")
    val poptentialEmptyPeriodsThatMatch: Seq[Option[(LocalTime, LocalTime)]] =
      for {
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

  def getFirstAvailableTimeSlot(fractionOfSession: Fraction): Option[WwwTimeSlot] = {
    val emptyPeriods = getEmptyTimePeriodsAvailable

    val numberOfMinutesForProposedSession =
      Math.floor(fractionOfSession.multiplier * MINUTES.between(this.startTime, this.endTime))

    val possibleSlots: List[WwwTimeSlot] = for {
      emptyPeriod <- emptyPeriods
      maybePeriodThatFits = if (MINUTES.between(emptyPeriod._1, emptyPeriod._2) >= (numberOfMinutesForProposedSession - 5)) {
        Some(WwwTimeSlot(emptyPeriod._1, emptyPeriod._2))
      } else None
      periodThatFits <- maybePeriodThatFits
    } yield periodThatFits

    val firstAvailableTimeSlot = possibleSlots.headOption
    firstAvailableTimeSlot match {
      case Some(availableTimeSlot) =>

        val roundedNumberOfMinutesNearestFive = fractionOfSession match {
          case OneHalf() =>
            5 * Math.round((numberOfMinutesForProposedSession + 1) / 5)
          case _ =>
            5 * Math.round(numberOfMinutesForProposedSession / 5)
        }

        val proposedEndTime = availableTimeSlot.startTime.plusMinutes(roundedNumberOfMinutesNearestFive)
        println(s"The proposed end time = ${proposedEndTime}")
        println(s"The empty session end time = ${availableTimeSlot.endTime}")
        val endTime = if (Math.abs(MINUTES.between(availableTimeSlot.endTime, proposedEndTime)) <= 5) {
          availableTimeSlot.endTime
        } else {
          proposedEndTime
        }
        Some(
          WwwTimeSlot(availableTimeSlot.startTime, endTime)
        )
      case None => None
    }
  }

  def addSubject(subjectDetail: WwwSubjectDetail): Boolean = {
    if (canAddSubjectWithinRequestedTimes(subjectDetail.timeSlot)) {
      addSubjectToSession(subjectDetail)
      true
    } else {
      false
    }
  }

  def editSubject(subjectDetail: WwwSubjectDetail): Boolean = {
    editSubjectToSession(subjectDetail)
  }

  def getSubject(subjectDetail: WwwSubjectDetail): Option[WwwSubjectDetail] = {
    val subjectsFound = subjectsInSession.filter(_ == subjectDetail)
    if (subjectsFound.size == 1) {
      Some(subjectsFound.head)
    } else {
      None
    }
  }

  def removeSubject(subjectDetail: WwwSubjectDetail): Boolean = {
    if (subjectsInSession.contains(subjectDetail)) {
      subjectsInSession = subjectsInSession.filterNot(_.equals(subjectDetail))
      reevaluateEmptySpace()
      !subjectsInSession.contains(subjectDetail)
    } else {
      false
    }
  }

  def clear(): Unit = {
    subjectsInSession = subjectsInSession.filter(_.subject.value == SUBJECT_EMPTY)
    reevaluateEmptySpace()
  }

  def subjectsWithTimeFractionInTwelves: List[(WwwSubjectDetail, Long)] = {
    val subjectsSorted = subjectsInSession.toList.sortBy {
      subjectDetail => subjectDetail.timeSlot.startTime
    }

    val subjectsToSessionMinutes = subjectsSorted.map {
      subjectDetail =>
        (subjectDetail,
          MINUTES.between(subjectDetail.timeSlot.startTime, subjectDetail.timeSlot.endTime))
    }

    val subjectsToSessionFractionOfTwelfth = subjectsToSessionMinutes.map {
      entry =>
        val fullSessionMinutes = MINUTES.between(this.startTime, this.endTime).toDouble
        val entrySubjectMinutes = entry._2.toDouble
        val entrySubjectTwelfthFractionNumerator = ((entrySubjectMinutes / fullSessionMinutes) * 12).toLong
        (entry._1, entrySubjectTwelfthFractionNumerator)
    }

    val correctToAddUpToTwelve = -1 * (subjectsToSessionFractionOfTwelfth.map { entry => entry._2 }.sum - 12)
    val currentLast = subjectsToSessionFractionOfTwelfth.last
    subjectsToSessionFractionOfTwelfth.filterNot(_ == currentLast) ::: (currentLast._1, currentLast._2 + correctToAddUpToTwelve) :: Nil
  }

  def prettyStringOfSession: String = {
    val startTimeString = s"${sessionOfTheWeek.value}, starts at ${this.startTime.toString}\n"
    val subjectsString = subjectsInSession.toList.sortBy {
      subjectDetail => subjectDetail.timeSlot.startTime
    }.map {
      subject =>
        s"\t${subject.timeSlot.startTime.toString}-${subject.timeSlot.endTime.toString}: " +
          s"${subject.subject.value.replace("subject-", "").capitalize} " +
          (if (subject.lessonAdditionalInfo.nonEmpty) subject.lessonAdditionalInfo else "") + "\n"
    }.mkString
    startTimeString + subjectsString + "------\n"
  }
}
