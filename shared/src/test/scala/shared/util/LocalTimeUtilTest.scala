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
      assert(time.get.equals(LocalTime.of(9, 18)))
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
  describe("Given a LocalTime of 9:18 get12HourAmPmFromLocalTime") {
    it("should return a value of '9:18 AM'") {
      val timeString = LocalTimeUtil.get12HourAmPmFromLocalTime(LocalTime.of(9,18))
      assert(timeString == "9:18 AM")
    }
  }
  describe("Given a LocalTime of 15:42 get12HourAmPmFromLocalTime") {
    it("should return a value of '3:42 PM'") {
      val timeString = LocalTimeUtil.get12HourAmPmFromLocalTime(LocalTime.of(15,42))
      assert(timeString == "3:42 PM")
    }
  }
  describe("Given a LocalTime of 12:00 get12HourAmPmFromLocalTime") {
    it("should return a value of '12:00 PM'") {
      val timeString = LocalTimeUtil.get12HourAmPmFromLocalTime(LocalTime.of(12,0))
      assert(timeString == "12:00 PM")
    }
  }
  describe("Given a string time of '1:40 PM' convertStringTimeToLocalTime") {
    it("should return a localtime equal to 1:40PM") {
      val time = LocalTimeUtil.convertStringTimeToLocalTime("1:40 PM")
      assert(time.get === LocalTime.of(13,40))
    }
  }
  describe("Given a string time of '9:05 AM' convertStringTimeToLocalTime") {
    it("should return a localtime equal to 9:05 AM") {
      val time = LocalTimeUtil.convertStringTimeToLocalTime("9:05 AM")
      assert(time.get === LocalTime.of(9,5))
    }
  }

  describe("Given a string time of '12:50 PM' convertStringTimeToLocalTime") {
    it("should return a localtime equal to 12:50 PM") {
      val time = LocalTimeUtil.convertStringTimeToLocalTime("12:50 PM")
      assert(time.get === LocalTime.of(12,50))
    }
  }

  describe("Given a string time of '10:40 AM' convertStringTimeToLocalTime") {
    it("should return a localtime equal to 10:40 AM") {
      val time = LocalTimeUtil.convertStringTimeToLocalTime("10:40 AM")
      assert(time.get === LocalTime.of(10,40))
    }
  }

  describe("Given a string times of '9:05 AM' & '10:40 AM' convertStringTimeToLocalTime") {
    it("should return have localtime 9:05 AM is BEFORE 10:40 AM") {
      val time =
      assert(
        LocalTimeUtil.convertStringTimeToLocalTime("9:05 AM").get.isBefore(
        LocalTimeUtil.convertStringTimeToLocalTime("10:40 AM").get))
    }
  }

}
