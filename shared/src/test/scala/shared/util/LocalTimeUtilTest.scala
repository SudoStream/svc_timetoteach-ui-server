package shared.util

import java.time.LocalTime

import org.scalatest._

class LocalTimeUtilTest extends FunSpec {
  describe("Given a string of '09:18' LocalTimeUtil") {
    it("should return a tuple (9,18)") {
      val maybeTuple = LocalTimeUtil.extractHoursAndMinutesFromString("09:18")
      assert(maybeTuple.isDefined)
      assert(maybeTuple.get._1 == 9)
      assert(maybeTuple.get._2 == 18)
    }
    it("should return a LocalTime of 9:18)") {
      val time = LocalTimeUtil.convertStringTimeToLocalTime("09:18")
      assert(time.isDefined)
      assert(time.get.equals(LocalTime.of(9,18)))
    }
  }
  describe("Given a string of 'hello' LocalTimeUtil") {
    it("should return an undefined option") {
      val maybeTuple = LocalTimeUtil.extractHoursAndMinutesFromString("hello")
      assert(maybeTuple.isEmpty)
    }
    it("should return a LocalTime not defined)") {
      val time = LocalTimeUtil.convertStringTimeToLocalTime("hello")
      assert(time.isEmpty)
    }

  }

}
