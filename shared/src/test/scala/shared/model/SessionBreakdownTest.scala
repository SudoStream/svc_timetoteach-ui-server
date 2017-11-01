package shared.model

import java.time.LocalTime

import org.scalatest._

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

  describe("A new SessionBreakdown after subject added") {
    it("should be partially full") {
      val sessionBreakdown = SessionBreakdown(LocalTime.of(9, 0), LocalTime.of(10, 30))
      sessionBreakdown.addSubjectFullSession(SubjectDetail(SubjectName("subject-maths")))
      assert(sessionBreakdown.isPartiallyFull)
    }
  }

}