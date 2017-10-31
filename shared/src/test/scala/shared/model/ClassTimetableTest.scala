package shared.model

import org.scalatest._

import scala.scalajs.js.Dictionary

class ClassTimetableTest extends FunSpec {

  describe("Class Timetable created with no school times passed in") {

    it("should have School Day Times default dictionary created") {
      val classTimetable: ClassTimetable = ClassTimetable(None)

      assert(classTimetable.schoolDayTimes.get("schoolDayStarts").isDefined)
      assert(classTimetable.schoolDayTimes.get("morningBreakStarts").isDefined)
      assert(classTimetable.schoolDayTimes.get("morningBreakEnds").isDefined)
      assert(classTimetable.schoolDayTimes.get("lunchStarts").isDefined)
      assert(classTimetable.schoolDayTimes.get("lunchEnds").isDefined)
      assert(classTimetable.schoolDayTimes.get("schoolDayEnds").isDefined)
    }

    it("should have 6 entries") {
      val classTimetable: ClassTimetable = ClassTimetable(None)

      assert(classTimetable.schoolDayTimes.size == 6)
    }

    it("should have expected default times for each school day break point") {
      val classTimetable: ClassTimetable = ClassTimetable(None)

      assert(classTimetable.schoolDayTimes.get("schoolDayStarts").get == "09:00")
      assert(classTimetable.schoolDayTimes.get("morningBreakStarts").get == "10:30")
      assert(classTimetable.schoolDayTimes.get("morningBreakEnds").get == "10:45")
      assert(classTimetable.schoolDayTimes.get("lunchStarts").get == "12:00")
      assert(classTimetable.schoolDayTimes.get("lunchEnds").get == "13:00")
      assert(classTimetable.schoolDayTimes.get("schoolDayEnds").get == "15:00")
    }
  }

  describe("Class Timetable created with school times passed in that have 2 extra entries") {
    it("should throw a runtime exception") {
      val schoolDayTimesWithTwoExtraEntries = Dictionary[String]()
      schoolDayTimesWithTwoExtraEntries.update("schoolDayStarts", "09:00")
      schoolDayTimesWithTwoExtraEntries.update("morningBreakStarts", "10:30")
      schoolDayTimesWithTwoExtraEntries.update("morningBreakEnds", "10:45")
      schoolDayTimesWithTwoExtraEntries.update("lunchStarts", "12:00")
      schoolDayTimesWithTwoExtraEntries.update("lunchEnds", "13:00")
      schoolDayTimesWithTwoExtraEntries.update("afternoonBreakStarts", "14:00")
      schoolDayTimesWithTwoExtraEntries.update("afternoonBreakEnds", "14:10")
      schoolDayTimesWithTwoExtraEntries.update("schoolDayEnds", "15:00")

      assertThrows[RuntimeException] {
        val classTimetable: ClassTimetable = ClassTimetable(Some(schoolDayTimesWithTwoExtraEntries))
      }
    }
  }

  describe("Class Timetable created with school times passed in that have 1 less entry") {
    it("should throw a runtime exception") {
      val schoolDayTimesWithOneLessEntry = Dictionary[String]()
      schoolDayTimesWithOneLessEntry.update("schoolDayStarts", "09:00")
      schoolDayTimesWithOneLessEntry.update("morningBreakStarts", "10:30")
      schoolDayTimesWithOneLessEntry.update("lunchStarts", "12:00")
      schoolDayTimesWithOneLessEntry.update("lunchEnds", "13:00")
      schoolDayTimesWithOneLessEntry.update("schoolDayEnds", "15:00")

      assertThrows[RuntimeException] {
        val classTimetable: ClassTimetable = ClassTimetable(Some(schoolDayTimesWithOneLessEntry))
      }
    }
  }

  describe("Class Timetable created with school times passed in that has erroneous keyname") {
    it("should throw a runtime exception") {
      val schoolDayTimesWithErrorKey = Dictionary[String]()
      schoolDayTimesWithErrorKey.update("schoolDayStarts", "09:00")
      schoolDayTimesWithErrorKey.update("morningBreakStarts", "10:30")
      schoolDayTimesWithErrorKey.update("morningBreakEnds", "10:45")
      schoolDayTimesWithErrorKey.update("thisIsWrong", "12:00")
      schoolDayTimesWithErrorKey.update("lunchEnds", "13:00")
      schoolDayTimesWithErrorKey.update("schoolDayEnds", "15:00")

      assertThrows[RuntimeException] {
        val classTimetable: ClassTimetable = ClassTimetable(Some(schoolDayTimesWithErrorKey))
      }
    }
  }

  describe("A new ClassTimetable") {
    it("should have a current state of 'ENTIRELY_EMPTY'") {
      val classTimetable: ClassTimetable = ClassTimetable(None)
      assert(classTimetable.getCurrentState == "ENTIRELY_EMPTY")
    }

    it("should not have any edits been recorded") {
      val classTimetable: ClassTimetable = ClassTimetable(None)
      assert(!classTimetable.hasBeenEdited)
    }

    it("should have an state of all EMPTY sessions") {
      val classTimetable: ClassTimetable = ClassTimetable(None)
      assert(false)
    }

  }

}
