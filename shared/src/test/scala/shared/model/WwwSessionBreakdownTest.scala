package shared.model

import java.time.LocalTime

import org.scalatest._
import shared.model.classtimetable._

import scala.collection.mutable

class WwwSessionBreakdownTest extends FunSpec {

  describe("SessionBreakdown on creation") {
    it("should be empty") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      assert(sessionBreakdown.isEmpty)
    }

    it("should not be full") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      assert(!sessionBreakdown.isFull)
    }

    it("should not be partially full") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      assert(!sessionBreakdown.isPartiallyFull)
    }

    it("should have a list of subjects and fraction time values of twelves: one subject-empty = 12/12") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsAndTwelves = sessionBreakdown.subjectsWithTimeFractionInTwelves
      assert(subjectsAndTwelves.head._1.subject == WwwSubjectName("subject-empty"))
      assert(subjectsAndTwelves.head._2 == 12)
    }
    it("should have a list of subjects and fraction time values of twelves : should add up to 12/12") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsAndTwelves = sessionBreakdown.subjectsWithTimeFractionInTwelves
      assert(subjectsAndTwelves.map(entry => entry._2).sum == 12)
    }

    it("should have a first available timeslot for full session as 9:00 - 10:30") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      val availableTimeSlot = sessionBreakdown.getFirstAvailableTimeSlot(Whole())
      assert(availableTimeSlot.get == WwwTimeSlot(LocalTime.of(9, 0), LocalTime.of(10, 30)))
    }

    it("should have a first available timeslot for half session as 9:00 - 9:45") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      val availableTimeSlot = sessionBreakdown.getFirstAvailableTimeSlot(OneHalf())
      assert(availableTimeSlot.get == WwwTimeSlot(LocalTime.of(9, 0), LocalTime.of(9, 45)))
    }

  }

  describe("SessionBreakdown on invalid start and end times") {
    it("should throw an runtime exception") {
      assertThrows[RuntimeException] {
        val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(8, 30))
      }
    }
  }

  describe("Finding empty periods in an empty session") {
    it("should return one empty period") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[WwwSubjectDetail] = scala.collection.mutable.ListBuffer()
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.size == 1)
    }
    it("should return one empty period with starta and end periods for the full session") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[WwwSubjectDetail] = scala.collection.mutable.ListBuffer()
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.head._1 == LocalTime.of(9, 0))
      assert(emptySessions.head._2 == LocalTime.of(10, 30))
    }
  }

  describe("Finding empty periods in a session with a subject for first 30 mins") {
    it("should return one empty period") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[WwwSubjectDetail] = scala.collection.mutable.ListBuffer()
      subjectsInSession += WwwSubjectDetail(WwwSubjectName("subject-maths"), WwwTimeSlot(LocalTime.of(9, 0), LocalTime.of(9, 30)))
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.size == 1)
    }
    it("should return one empty period with times 9:30 to 10:30") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[WwwSubjectDetail] = scala.collection.mutable.ListBuffer()
      subjectsInSession += WwwSubjectDetail(WwwSubjectName("subject-maths"), WwwTimeSlot(LocalTime.of(9, 0), LocalTime.of(9, 30)))
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.head._1 == LocalTime.of(9, 30))
      assert(emptySessions.head._2 == LocalTime.of(10, 30))
    }
  }

  describe("Finding empty periods in a session with a subject for last 15 mins") {
    it("should return one empty period") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[WwwSubjectDetail] = scala.collection.mutable.ListBuffer()
      subjectsInSession += WwwSubjectDetail(WwwSubjectName("subject-maths"), WwwTimeSlot(LocalTime.of(10, 15), LocalTime.of(10, 30)))
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.size == 1)
    }
    it("should return one empty period with times 9:00 to 10:15") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[WwwSubjectDetail] = scala.collection.mutable.ListBuffer()
      subjectsInSession += WwwSubjectDetail(WwwSubjectName("subject-maths"), WwwTimeSlot(LocalTime.of(10, 15), LocalTime.of(10, 30)))
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.head._1 == LocalTime.of(9, 0))
      assert(emptySessions.head._2 == LocalTime.of(10, 15))
    }
  }

  describe("Finding empty periods in a session with a subject in the middle") {
    it("should return two empty periods") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[WwwSubjectDetail] = scala.collection.mutable.ListBuffer()
      subjectsInSession += WwwSubjectDetail(WwwSubjectName("subject-maths"), WwwTimeSlot(LocalTime.of(9, 45), LocalTime.of(10, 10)))
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.size == 2)
    }
    it("should return two empty periods with times [9:00-9:45] and [10:10-10:30]") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[WwwSubjectDetail] = scala.collection.mutable.ListBuffer()
      subjectsInSession += WwwSubjectDetail(WwwSubjectName("subject-maths"), WwwTimeSlot(LocalTime.of(9, 45), LocalTime.of(10, 10)))
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.head._1 == LocalTime.of(10, 10))
      assert(emptySessions.head._2 == LocalTime.of(10, 30))
      assert(emptySessions.tail.head._1 == LocalTime.of(9, 0))
      assert(emptySessions.tail.head._2 == LocalTime.of(9, 45))
    }
  }

  describe("Finding empty periods in a session with two subjects beside each other in the middle") {
    it("should return two empty periods") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[WwwSubjectDetail] = scala.collection.mutable.ListBuffer()
      subjectsInSession += WwwSubjectDetail(WwwSubjectName("subject-maths"), WwwTimeSlot(LocalTime.of(9, 30), LocalTime.of(9, 50)))
      subjectsInSession += WwwSubjectDetail(WwwSubjectName("subject-reading"), WwwTimeSlot(LocalTime.of(9, 50), LocalTime.of(10, 10)))
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.size == 2)
    }
    it("should return two empty periods with times [9:00-9:30] and [10:10-10:30]") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[WwwSubjectDetail] = scala.collection.mutable.ListBuffer()
      subjectsInSession += WwwSubjectDetail(WwwSubjectName("subject-maths"), WwwTimeSlot(LocalTime.of(9, 30), LocalTime.of(9, 50)))
      subjectsInSession += WwwSubjectDetail(WwwSubjectName("subject-reading"), WwwTimeSlot(LocalTime.of(9, 50), LocalTime.of(10, 10)))
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.head._1 == LocalTime.of(10, 10))
      assert(emptySessions.head._2 == LocalTime.of(10, 30))
      assert(emptySessions.tail.head._1 == LocalTime.of(9, 0))
      assert(emptySessions.tail.head._2 == LocalTime.of(9, 30))
    }
  }

  describe("Finding empty periods in a session with two subjects beside with space between each other in the middle") {
    it("should return three empty periods") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[WwwSubjectDetail] = scala.collection.mutable.ListBuffer()
      subjectsInSession += WwwSubjectDetail(WwwSubjectName("subject-maths"), WwwTimeSlot(LocalTime.of(9, 30), LocalTime.of(9, 50)))
      subjectsInSession += WwwSubjectDetail(WwwSubjectName("subject-reading"), WwwTimeSlot(LocalTime.of(10, 10), LocalTime.of(10, 20)))
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.size == 3)
    }
    it("should return three empty periods with times [10:20-10:30], [9:50-10:10] and [9:00-9:30]") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[WwwSubjectDetail] = scala.collection.mutable.ListBuffer()
      subjectsInSession += WwwSubjectDetail(WwwSubjectName("subject-maths"), WwwTimeSlot(LocalTime.of(9, 30), LocalTime.of(9, 50)))
      subjectsInSession += WwwSubjectDetail(WwwSubjectName("subject-reading"), WwwTimeSlot(LocalTime.of(10, 10), LocalTime.of(10, 20)))
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.head._1 == LocalTime.of(10, 20))
      assert(emptySessions.head._2 == LocalTime.of(10, 30))
      assert(emptySessions.tail.head._1 == LocalTime.of(9, 50))
      assert(emptySessions.tail.head._2 == LocalTime.of(10, 10))
      assert(emptySessions.tail.tail.head._1 == LocalTime.of(9, 0))
      assert(emptySessions.tail.tail.head._2 == LocalTime.of(9, 30))
    }

  }

  describe("A new SessionBreakdown after subject added that does not take up full session") {
    it("should be partially full") {

      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))

      assert(sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-maths"),
          WwwTimeSlot(startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0))))
      )

      assert(sessionBreakdown.isPartiallyFull)
    }
    it("should not be empty") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-maths"),
          WwwTimeSlot(startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0))))
      assert(!sessionBreakdown.isEmpty)
    }
    it("should not be full") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-maths"),
          WwwTimeSlot(startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0))))
      assert(!sessionBreakdown.isFull)
    }
    it("should return one empty period") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-maths"),
          WwwTimeSlot(startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0))))

      assert(sessionBreakdown.getEmptyTimePeriodsAvailable.size == 1)
    }
    it("should return one empty period with times 9:00 to 10:15") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-maths"),
          WwwTimeSlot(startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0))))
      assert(sessionBreakdown.getEmptyTimePeriodsAvailable.head._1 == LocalTime.of(10, 0))
      assert(sessionBreakdown.getEmptyTimePeriodsAvailable.head._2 == LocalTime.of(10, 30))
    }

    it("should have NO timeslot for full session") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-maths"),
          WwwTimeSlot(startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0))))

      val availableTimeSlot = sessionBreakdown.getFirstAvailableTimeSlot(Whole())
      assert(availableTimeSlot.isEmpty)
    }

    it("should have NO timeslot for half session") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-maths"),
          WwwTimeSlot(startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0))))

      val availableTimeSlot = sessionBreakdown.getFirstAvailableTimeSlot(OneHalf())
      assert(availableTimeSlot.isEmpty)
    }

    it("should have NO timeslot for two thirds session") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-maths"),
          WwwTimeSlot(startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0))))

      val availableTimeSlot = sessionBreakdown.getFirstAvailableTimeSlot(TwoThirds())
      assert(availableTimeSlot.isEmpty)
    }

    it("should have one timeslot for one thirds session: 10:00-10:30") {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-maths"),
          WwwTimeSlot(startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0))))

      val availableTimeSlot = sessionBreakdown.getFirstAvailableTimeSlot(OneThird())
      assert(availableTimeSlot.isDefined)
      assert(availableTimeSlot.get == WwwTimeSlot(LocalTime.of(10, 0), LocalTime.of(10, 30)))
    }

  }

  describe("A new SessionBreakdown after 2 subjects added") {
    def addTwoSubjects() = {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))

      sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-maths"),
          WwwTimeSlot(startTime = LocalTime.of(9, 30),
            endTime = LocalTime.of(10, 0)))
      )

      sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-reading"),
          WwwTimeSlot(startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(10, 20)))
      )
      sessionBreakdown
    }

    it("should have 2 subjects") {
      val sessionBreakdown: WwwSessionBreakdown = addTwoSubjects()
      assert(sessionBreakdown.numberOfSubjectsInSession == 2)
    }

    it("should have a nice pretty print version") {
      val sessionBreakdown: WwwSessionBreakdown = addTwoSubjects()
      val prettyPrintSubjects = sessionBreakdown.prettyStringOfSession
      println(s"Nice subjects print:-\n $prettyPrintSubjects")
      assert(prettyPrintSubjects.nonEmpty)
    }

    it("should have a list of subjects and fraction time values of twelves: 1st subject-empty = 4/12") {
      val sessionBreakdown: WwwSessionBreakdown = addTwoSubjects()
      val subjectsAndTwelves = sessionBreakdown.subjectsWithTimeFractionInTwelves
      assert(subjectsAndTwelves.head._1.subject == WwwSubjectName("subject-empty"))
      assert(subjectsAndTwelves.head._2 == 4)
    }
    it("should have a list of subjects and fraction time values of twelves: 1st subject-maths = 4/12") {
      val sessionBreakdown: WwwSessionBreakdown = addTwoSubjects()
      val subjectsAndTwelves = sessionBreakdown.subjectsWithTimeFractionInTwelves
      assert(subjectsAndTwelves(1)._1.subject == WwwSubjectName("subject-maths"))
      assert(subjectsAndTwelves(1)._2 == 4)
    }
    it("should have a list of subjects and fraction time values of twelves: 1st subject-reading = 2/12") {
      val sessionBreakdown: WwwSessionBreakdown = addTwoSubjects()
      val subjectsAndTwelves = sessionBreakdown.subjectsWithTimeFractionInTwelves
      assert(subjectsAndTwelves(2)._1.subject == WwwSubjectName("subject-reading"))
      assert(subjectsAndTwelves(2)._2 == 2)
    }
    it("should have a list of subjects and fraction time values of twelves: 2nd subject-empty = 2/12") {
      val sessionBreakdown: WwwSessionBreakdown = addTwoSubjects()
      val subjectsAndTwelves = sessionBreakdown.subjectsWithTimeFractionInTwelves
      assert(subjectsAndTwelves(3)._1.subject == WwwSubjectName("subject-empty"))
      assert(subjectsAndTwelves(3)._2 == 2)
    }
    it("should have a list of subjects and fraction time values of twelves : should add up to 12/12") {
      val sessionBreakdown: WwwSessionBreakdown = addTwoSubjects()
      val subjectsAndTwelves = sessionBreakdown.subjectsWithTimeFractionInTwelves
      assert(subjectsAndTwelves.map(entry => entry._2).sum == 12)
    }
    it("should have one timeslot for one thirds session: 9:00-9:30") {
      val sessionBreakdown: WwwSessionBreakdown = addTwoSubjects()

      val availableTimeSlot = sessionBreakdown.getFirstAvailableTimeSlot(OneThird())
      assert(availableTimeSlot.isDefined)
      assert(availableTimeSlot.get == WwwTimeSlot(LocalTime.of(9, 0), LocalTime.of(9, 30)))
    }

  }

  describe("A new SessionBreakdown after 2 subjects added and 1 then removed leaving one session in the middle") {

    def addTwoAndRemoveOneSubject(): WwwSessionBreakdown = {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-maths"),
          WwwTimeSlot(startTime = LocalTime.of(9, 10),
            endTime = LocalTime.of(9, 30)))
      )
      sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-reading"),
          WwwTimeSlot(startTime = LocalTime.of(9, 30),
            endTime = LocalTime.of(10, 0)))
      )
      sessionBreakdown.removeSubject(
        WwwSubjectDetail(WwwSubjectName("subject-reading"),
          WwwTimeSlot(startTime = LocalTime.of(9, 30),
            endTime = LocalTime.of(10, 0)))
      )
      sessionBreakdown
    }

    it("should be partially full") {
      val sessionBreakdown: WwwSessionBreakdown = addTwoAndRemoveOneSubject()
      assert(sessionBreakdown.isPartiallyFull)
    }
    it("should have one subject") {
      val sessionBreakdown: WwwSessionBreakdown = addTwoAndRemoveOneSubject()
      assert(sessionBreakdown.numberOfSubjectsInSession == 1)
    }
  }

  describe("A new SessionBreakdown after 2 subjects added and then cleared") {
    def addTwoSubjectsThenClear() = {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))
      sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-maths"),
          WwwTimeSlot(startTime = LocalTime.of(9, 10),
            endTime = LocalTime.of(9, 30)))
      )
      sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-reading"),
          WwwTimeSlot(startTime = LocalTime.of(9, 30),
            endTime = LocalTime.of(10, 0)))
      )
      sessionBreakdown.clear()
      sessionBreakdown
    }

    it("should be empty") {
      val sessionBreakdown: WwwSessionBreakdown = addTwoSubjectsThenClear()
      assert(sessionBreakdown.isEmpty)
    }
    it("should have one large empty session") {
      val sessionBreakdown: WwwSessionBreakdown = addTwoSubjectsThenClear()
      assert(sessionBreakdown.getEmptyTimePeriodsAvailable.size == 1)
    }

  }

  describe("A new SessionBreakdown after 2 subjects added taking 50/50 space") {
    def addTwoSubjectsFiftyFifty() = {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(9, 0), LocalTime.of(10, 30))

      sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-maths"),
          WwwTimeSlot(startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(9, 45)))
      )

      sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-reading"),
          WwwTimeSlot(startTime = LocalTime.of(9, 45),
            endTime = LocalTime.of(10, 30)))
      )
      sessionBreakdown
    }

    it("should have a list of subjects and fraction time values of twelves: subject-maths = 6/12") {
      val sessionBreakdown: WwwSessionBreakdown = addTwoSubjectsFiftyFifty()
      val subjectsAndTwelves = sessionBreakdown.subjectsWithTimeFractionInTwelves
      assert(subjectsAndTwelves.head._1.subject == WwwSubjectName("subject-maths"))
      assert(subjectsAndTwelves.head._2 == 6)
    }
    it("should have a list of subjects and fraction time values of twelves: subject-reading = 6/12") {
      val sessionBreakdown: WwwSessionBreakdown = addTwoSubjectsFiftyFifty()
      val subjectsAndTwelves = sessionBreakdown.subjectsWithTimeFractionInTwelves
      assert(subjectsAndTwelves(1)._1.subject == WwwSubjectName("subject-reading"))
      assert(subjectsAndTwelves(1)._2 == 6)
    }
    it("should have a list of subjects and fraction time values of twelves : should add up to 12/12") {
      val sessionBreakdown: WwwSessionBreakdown = addTwoSubjectsFiftyFifty()
      val subjectsAndTwelves = sessionBreakdown.subjectsWithTimeFractionInTwelves
      assert(subjectsAndTwelves.map(entry => entry._2).sum == 12)
    }

  }

  describe("A new SessionBreakdown after 1 subjects added to first half") {
    def addOneSubjectToFirstHalf() = {
      val sessionBreakdown = WwwSessionBreakdown(MondayEarlyMorningSession(), LocalTime.of(10, 45), LocalTime.of(12, 0))
      val availableSlot = sessionBreakdown.getFirstAvailableTimeSlot(OneHalf()).get

      sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-maths"), availableSlot)
      )
      sessionBreakdown
    }

    it("should have 1 subject") {
      val sessionBreakdown: WwwSessionBreakdown = addOneSubjectToFirstHalf()
      assert(sessionBreakdown.numberOfSubjectsInSession == 1)
    }

    it("should be possible to add another subject for second half") {
      println("==============================")
      val sessionBreakdown: WwwSessionBreakdown = addOneSubjectToFirstHalf()
      println("- - - - - - - - - - - - - - - - ")
      val secondAvailableSlot = sessionBreakdown.getFirstAvailableTimeSlot(OneHalf())
      println("==============================")
      assert(secondAvailableSlot.isDefined)

      sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-reading"), secondAvailableSlot.get)
      )
    }
  }

  describe("A subject added with timeslot *before* session") {
    it("should not be allowed to add") {
      val sessionBreakdown = WwwSessionBreakdown(MondayLateMorningWwwSession(), LocalTime.of(10, 45), LocalTime.of(12, 0))
      val timeSlot = WwwTimeSlot(LocalTime.of(10,40), LocalTime.of(11,30))

      val addedOkay = sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-maths"), timeSlot)
      )
      assert(!addedOkay)
    }
  }

  describe("A subject added with timeslot *after* session") {
    it("should not be allowed to add") {
      val sessionBreakdown = WwwSessionBreakdown(MondayLateMorningWwwSession(), LocalTime.of(10, 45), LocalTime.of(12, 0))
      val timeSlot = WwwTimeSlot(LocalTime.of(11,40), LocalTime.of(12,5))

      val addedOkay = sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-maths"), timeSlot)
      )
      assert(!addedOkay)
    }
  }

  describe("Maths is added to a middle slot then p.e. attempted to add before this") {
    it("should allow the addition with no empty space between") {
      val sessionBreakdown = WwwSessionBreakdown(MondayLateMorningWwwSession(), LocalTime.of(10, 45), LocalTime.of(12, 0))
      val mathsTimeSlot = WwwTimeSlot(LocalTime.of(11,0), LocalTime.of(11,30))
      val mathsAddedOkay = sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-maths"), mathsTimeSlot)
      )
      assert(mathsAddedOkay)

      val peTimeSlot = WwwTimeSlot(LocalTime.of(10,45), LocalTime.of(11,0))
      val peAddedOkay = sessionBreakdown.addSubject(
        WwwSubjectDetail(WwwSubjectName("subject-physical-education"), peTimeSlot)
      )
      assert(peAddedOkay)

      println(s"subjects in 12s = ${sessionBreakdown.subjectsWithTimeFractionInTwelves.toString()}")

      val emptyPerdiods = sessionBreakdown.getEmptyTimePeriodsAvailable
      println(s"Empty Periods = ${emptyPerdiods.toString}")
      assert(emptyPerdiods.size == 1)
    }
  }

}