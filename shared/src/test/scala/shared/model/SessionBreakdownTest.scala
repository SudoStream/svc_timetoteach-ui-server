package shared.model

import java.time.LocalTime

import org.scalatest._
import shared.model.classtimetable.{SessionBreakdown, SubjectDetail, SubjectName}

import scala.collection.mutable

class SessionBreakdownTest extends FunSpec {

  describe("SessionBreakdown on creation") {
    it("should be empty") {
      val sessionBreakdown = SessionBreakdown(LocalTime.of(9, 0), LocalTime.of(10, 30))
      assert(sessionBreakdown.isEmpty)
    }

    it("should not be full") {
      val sessionBreakdown = SessionBreakdown(LocalTime.of(9, 0), LocalTime.of(10, 30))
      assert(!sessionBreakdown.isFull)
    }

    it("should not be partially full") {
      val sessionBreakdown = SessionBreakdown(LocalTime.of(9, 0), LocalTime.of(10, 30))
      assert(!sessionBreakdown.isPartiallyFull)
    }
  }

  describe("SessionBreakdown on invalid start and end times") {
    it("should throw an runtime exception") {
      assertThrows[RuntimeException] {
        val sessionBreakdown = SessionBreakdown(LocalTime.of(9, 0), LocalTime.of(8, 30))
      }
    }
  }

  describe("Finding empty periods in an empty session") {
    it("should return one empty period") {
      val sessionBreakdown = SessionBreakdown(LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[SubjectDetail] = scala.collection.mutable.ListBuffer()
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.size == 1)
    }
    it("should return one empty period with starta and end periods for the full session") {
      val sessionBreakdown = SessionBreakdown(LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[SubjectDetail] = scala.collection.mutable.ListBuffer()
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.head._1 == LocalTime.of(9, 0))
      assert(emptySessions.head._2 == LocalTime.of(10, 30))
    }
  }

  describe("Finding empty periods in a session with a subject for first 30 mins") {
    it("should return one empty period") {
      val sessionBreakdown = SessionBreakdown(LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[SubjectDetail] = scala.collection.mutable.ListBuffer()
      subjectsInSession += SubjectDetail(SubjectName("subject-maths"), LocalTime.of(9, 0), LocalTime.of(9, 30))
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.size == 1)
    }
    it("should return one empty period with times 9:30 to 10:30") {
      val sessionBreakdown = SessionBreakdown(LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[SubjectDetail] = scala.collection.mutable.ListBuffer()
      subjectsInSession += SubjectDetail(SubjectName("subject-maths"), LocalTime.of(9, 0), LocalTime.of(9, 30))
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.head._1 == LocalTime.of(9, 30))
      assert(emptySessions.head._2 == LocalTime.of(10, 30))
    }
  }

  describe("Finding empty periods in a session with a subject for last 15 mins") {
    it("should return one empty period") {
      val sessionBreakdown = SessionBreakdown(LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[SubjectDetail] = scala.collection.mutable.ListBuffer()
      subjectsInSession += SubjectDetail(SubjectName("subject-maths"), LocalTime.of(10, 15), LocalTime.of(10, 30))
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.size == 1)
    }
    it("should return one empty period with times 9:00 to 10:15") {
      val sessionBreakdown = SessionBreakdown(LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[SubjectDetail] = scala.collection.mutable.ListBuffer()
      subjectsInSession += SubjectDetail(SubjectName("subject-maths"), LocalTime.of(10, 15), LocalTime.of(10, 30))
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.head._1 == LocalTime.of(9, 0))
      assert(emptySessions.head._2 == LocalTime.of(10, 15))
    }
  }

  describe("Finding empty periods in a session with a subject in the middle") {
    it("should return two empty periods") {
      val sessionBreakdown = SessionBreakdown(LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[SubjectDetail] = scala.collection.mutable.ListBuffer()
      subjectsInSession += SubjectDetail(SubjectName("subject-maths"), LocalTime.of(9, 45), LocalTime.of(10, 10))
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.size == 2)
    }
    it("should return two empty periods with times [9:00-9:45] and [10:10-10:30]") {
      val sessionBreakdown = SessionBreakdown(LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[SubjectDetail] = scala.collection.mutable.ListBuffer()
      subjectsInSession += SubjectDetail(SubjectName("subject-maths"), LocalTime.of(9, 45), LocalTime.of(10, 10))
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.head._1 == LocalTime.of(10, 10))
      assert(emptySessions.head._2 == LocalTime.of(10, 30))
      assert(emptySessions.tail.head._1 == LocalTime.of(9, 0))
      assert(emptySessions.tail.head._2 == LocalTime.of(9, 45))
    }
  }

  describe("Finding empty periods in a session with two subjects beside each other in the middle") {
    it("should return two empty periods") {
      val sessionBreakdown = SessionBreakdown(LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[SubjectDetail] = scala.collection.mutable.ListBuffer()
      subjectsInSession += SubjectDetail(SubjectName("subject-maths"), LocalTime.of(9, 30), LocalTime.of(9, 50))
      subjectsInSession += SubjectDetail(SubjectName("subject-reading"), LocalTime.of(9, 50), LocalTime.of(10, 10))
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.size == 2)
    }
    it("should return two empty periods with times [9:00-9:30] and [10:10-10:30]") {
      val sessionBreakdown = SessionBreakdown(LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[SubjectDetail] = scala.collection.mutable.ListBuffer()
      subjectsInSession += SubjectDetail(SubjectName("subject-maths"), LocalTime.of(9, 30), LocalTime.of(9, 50))
      subjectsInSession += SubjectDetail(SubjectName("subject-reading"), LocalTime.of(9, 50), LocalTime.of(10, 10))
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.head._1 == LocalTime.of(10, 10))
      assert(emptySessions.head._2 == LocalTime.of(10, 30))
      assert(emptySessions.tail.head._1 == LocalTime.of(9, 0))
      assert(emptySessions.tail.head._2 == LocalTime.of(9, 30))
    }
  }

  describe("Finding empty periods in a session with two subjects beside with space between each other in the middle") {
    it("should return three empty periods") {
      val sessionBreakdown = SessionBreakdown(LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[SubjectDetail] = scala.collection.mutable.ListBuffer()
      subjectsInSession += SubjectDetail(SubjectName("subject-maths"), LocalTime.of(9, 30), LocalTime.of(9, 50))
      subjectsInSession += SubjectDetail(SubjectName("subject-reading"), LocalTime.of(10, 10), LocalTime.of(10, 20))
      val emptySessions = sessionBreakdown.getEmptyTimePeriodsInGivenSession(subjectsInSession)
      assert(emptySessions.size == 3)
    }
    it("should return three empty periods with times [10:20-10:30], [9:50-10:10] and [9:00-9:30]") {
      val sessionBreakdown = SessionBreakdown(LocalTime.of(9, 0), LocalTime.of(10, 30))
      val subjectsInSession: mutable.ListBuffer[SubjectDetail] = scala.collection.mutable.ListBuffer()
      subjectsInSession += SubjectDetail(SubjectName("subject-maths"), LocalTime.of(9, 30), LocalTime.of(9, 50))
      subjectsInSession += SubjectDetail(SubjectName("subject-reading"), LocalTime.of(10, 10), LocalTime.of(10, 20))
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
      val sessionBreakdown = SessionBreakdown(LocalTime.of(9, 0), LocalTime.of(10, 30))
      sessionBreakdown.addSubject(
        SubjectDetail(SubjectName("subject-maths"),
          startTime = LocalTime.of(9, 0),
          endTime = LocalTime.of(10, 0)))
      assert(sessionBreakdown.isPartiallyFull)
    }
    it("should not be empty") {
      val sessionBreakdown = SessionBreakdown(LocalTime.of(9, 0), LocalTime.of(10, 30))
      sessionBreakdown.addSubject(
        SubjectDetail(SubjectName("subject-maths"),
          startTime = LocalTime.of(9, 0),
          endTime = LocalTime.of(10, 0)))
      assert(!sessionBreakdown.isEmpty)
    }
    it("should not be full") {
      val sessionBreakdown = SessionBreakdown(LocalTime.of(9, 0), LocalTime.of(10, 30))
      sessionBreakdown.addSubject(
        SubjectDetail(SubjectName("subject-maths"),
          startTime = LocalTime.of(9, 0),
          endTime = LocalTime.of(10, 0)))
      assert(!sessionBreakdown.isFull)
    }
    it("should return one empty period") {
      val sessionBreakdown = SessionBreakdown(LocalTime.of(9, 0), LocalTime.of(10, 30))
      sessionBreakdown.addSubject(
        SubjectDetail(SubjectName("subject-maths"),
          startTime = LocalTime.of(9, 0),
          endTime = LocalTime.of(10, 0)))

      assert(sessionBreakdown.getEmptyTimePeriodsAvailable.size == 1)
    }
    it("should return one empty period with times 9:00 to 10:15") {
      val sessionBreakdown = SessionBreakdown(LocalTime.of(9, 0), LocalTime.of(10, 30))
      sessionBreakdown.addSubject(
        SubjectDetail(SubjectName("subject-maths"),
          startTime = LocalTime.of(9, 0),
          endTime = LocalTime.of(10, 0)))
      assert(sessionBreakdown.getEmptyTimePeriodsAvailable.head._1 == LocalTime.of(10, 0))
      assert(sessionBreakdown.getEmptyTimePeriodsAvailable.head._2 == LocalTime.of(10, 30))
    }

  }

}