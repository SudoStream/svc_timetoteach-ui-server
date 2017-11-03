package shared.model

import java.time.LocalTime

import org.scalatest._
import shared.model.classtimetable._

class ClassTimetableTest extends FunSpec with ClassTimetableTestHelper {

  describe("Class Timetable created with no school times passed in") {

    it("should have School Day Times default dictionary created") {
      val classTimetable: ClassTimetable = ClassTimetable(None)

      assert(classTimetable.schoolDayTimes.get(SchoolDayStarts()).isDefined)
      assert(classTimetable.schoolDayTimes.get(MorningBreakStarts()).isDefined)
      assert(classTimetable.schoolDayTimes.get(MorningBreakEnds()).isDefined)
      assert(classTimetable.schoolDayTimes.get(LunchStarts()).isDefined)
      assert(classTimetable.schoolDayTimes.get(LunchEnds()).isDefined)
      assert(classTimetable.schoolDayTimes.get(SchoolDayEnds()).isDefined)
    }

    it("should have 6 entries") {
      val classTimetable: ClassTimetable = ClassTimetable(None)

      assert(classTimetable.schoolDayTimes.size == 6)
    }

    it("should have expected default times for each school day break point") {
      val classTimetable: ClassTimetable = ClassTimetable(None)

      assert(classTimetable.schoolDayTimes(SchoolDayStarts()) == "09:00")
      assert(classTimetable.schoolDayTimes(MorningBreakStarts()) == "10:30")
      assert(classTimetable.schoolDayTimes(MorningBreakEnds()) == "10:45")
      assert(classTimetable.schoolDayTimes(LunchStarts()) == "12:00")
      assert(classTimetable.schoolDayTimes(LunchEnds()) == "13:00")
      assert(classTimetable.schoolDayTimes(SchoolDayEnds()) == "15:00")
    }
  }

  describe("Class Timetable created with school times passed in that have 1 less entry") {
    it("should throw a runtime exception") {
      val schoolDayTimesWithOneLessEntry: Map[SchoolDayTimeBoundary, String] = Map(
        SchoolDayStarts() -> "09:00",
        MorningBreakStarts() -> "10:30",
        LunchStarts() -> "12:00",
        LunchEnds() -> "13:00",
        SchoolDayEnds() -> "15:00"
      )

      assertThrows[RuntimeException] {
        val classTimetable: ClassTimetable = ClassTimetable(Some(schoolDayTimesWithOneLessEntry))
      }
    }
  }

  describe("A new ClassTimetable") {
    it("should have a current state of 'ENTIRELY_EMPTY'") {
      val classTimetable: ClassTimetable = ClassTimetable(None)
      assert(classTimetable.getCurrentState == EntirelyEmpty())
    }

    it("should not have any edits been recorded") {
      val classTimetable: ClassTimetable = ClassTimetable(None)
      assert(!classTimetable.hasBeenEdited)
    }
  }

  describe("A ClassTimetable with maths added to middle of Monday session") {
    it("should be partially full") {
      val classTimetable: ClassTimetable = ClassTimetable(None)
      val mathsOnMonday = SubjectDetail(SubjectName("subject-maths"), TimeSlot(LocalTime.of(9, 30), LocalTime.of(9, 50)))
      classTimetable.addSubject(mathsOnMonday, MondayEarlyMorningSession())
      assert(classTimetable.getCurrentState == PartiallyComplete())
    }
  }


  describe("A ClassTimetable with all sessions filled") {
    it("should be full") {
      val classTimetable: ClassTimetable  = createFullClassTimetable
      assert(classTimetable.getCurrentState == CompletelyFull())
    }
  }

  describe("A ClassTimetable with all sessions filled with one subject then removed") {
    it("should be partially full") {
      val classTimetable: ClassTimetable  = createFullClassTimetable
      classTimetable.removeSubject(SubjectDetail(
        SubjectName("subject-reading"),
        TimeSlot(LocalTime.of(13,0), LocalTime.of(15,0))
      ),
        WednesdayAfternoonSession())
      assert(classTimetable.getCurrentState == PartiallyComplete())
    }
  }

  describe("A ClassTimetable with all sessions filled then cleared") {
    it("should be empty") {
      val classTimetable: ClassTimetable  = createFullClassTimetable
      classTimetable.clearWholeTimetable()
      assert(classTimetable.getCurrentState == EntirelyEmpty())
    }
  }

}
