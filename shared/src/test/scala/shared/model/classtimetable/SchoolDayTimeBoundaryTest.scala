package shared.model.classtimetable

import org.scalatest.FunSpec

class SchoolDayTimeBoundaryTest extends FunSpec {
  describe("'school-day-starts' passed to createSchoolDayTimeBoundaryFromString() should ") {
    it("create an instance of SchoolDayStarts()") {
      val boundary = SchoolDayTimeBoundary.createSchoolDayTimeBoundaryFromString("school-day-starts")
      assert(boundary.isInstanceOf[SchoolDayStarts])
    }
  }

  describe("'school-day-ends' passed to createSchoolDayTimeBoundaryFromString() should ") {
    it("create an instance of SchoolDayEnds()") {
      val boundary = SchoolDayTimeBoundary.createSchoolDayTimeBoundaryFromString("school-day-ends")
      assert(boundary.isInstanceOf[SchoolDayEnds])
    }
  }

  describe("'morning-break-starts' passed to createSchoolDayTimeBoundaryFromString() should ") {
    it("create an instance of MorningBreakStarts()") {
      val boundary = SchoolDayTimeBoundary.createSchoolDayTimeBoundaryFromString("morning-break-starts")
      assert(boundary.isInstanceOf[MorningBreakStarts])
    }
  }

  describe("'morning-break-ends' passed to createSchoolDayTimeBoundaryFromString() should ") {
    it("create an instance of MorningBreakEnds()") {
      val boundary = SchoolDayTimeBoundary.createSchoolDayTimeBoundaryFromString("morning-break-ends")
      assert(boundary.isInstanceOf[MorningBreakEnds])
    }
  }

  describe("'lunch-starts' passed to createSchoolDayTimeBoundaryFromString() should ") {
    it("create an instance of LunchStarts()") {
      val boundary = SchoolDayTimeBoundary.createSchoolDayTimeBoundaryFromString("lunch-starts")
      assert(boundary.isInstanceOf[LunchStarts])
    }
  }

  describe("'lunch-ends' passed to createSchoolDayTimeBoundaryFromString() should ") {
    it("create an instance of LunchEnds()") {
      val boundary = SchoolDayTimeBoundary.createSchoolDayTimeBoundaryFromString("lunch-ends")
      assert(boundary.isInstanceOf[LunchEnds])
    }
  }

  describe("'total-nonsense' passed to createSchoolDayTimeBoundaryFromString() should ") {
    it("throw and exception") {
      assertThrows[RuntimeException] {
        SchoolDayTimeBoundary.createSchoolDayTimeBoundaryFromString("total-nonsense")
      }
    }
  }



}
