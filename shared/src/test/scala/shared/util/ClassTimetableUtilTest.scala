package shared.util

import org.scalatest.FunSpec

import scala.scalajs.js.JavaScriptException

class ClassTimetableUtilTest extends FunSpec with ClassTimeTableUtilTestHelper {
  describe("Given a string of 'this is nonsense' createWwwClassTimetableFromJson()") {
    it("should throw a JavaScriptException") {
      assertThrows[JavaScriptException]{
        ClassTimetableUtil.createWwwClassTimetableFromJson("this is nonsense")
      }
    }
  }

  describe("Given a valid json string of a wwwClassTimetable createWwwClassTimetableFromJson()") {
    it("should return defined") {
      val maybeClassTimetable = ClassTimetableUtil.createWwwClassTimetableFromJson(createClassTimetableAsJson())
      assert(maybeClassTimetable.isDefined)
    }
  }

}
