package shared.util

import org.scalatest._

class PlanningHelperTest extends FunSpec
{
  describe("Given a string with NO jawn nonfriendly characters, i.e. normal, encodeAnyJawnNonFriendlyCharacters()") {
    it("should return the same string") {
      val encoded = PlanningHelper.encodeAnyJawnNonFriendlyCharacters("hello")
      assert(encoded === "hello")
    }
  }

  describe("Given a string with jawn nonfriendly characters, '1 = 2/2', encodeAnyJawnNonFriendlyCharacters()") {
    it("should return '1 [[EQUALS]] 2/2' ") {
      val encoded = PlanningHelper.encodeAnyJawnNonFriendlyCharacters("1 = 2/2")
      assert(encoded === "1 [[EQUALS]] 2/2")
    }
  }

  describe("Given a string with jawn nonfriendly character repeated, '1 = 2/2 = 3/3 = 4/4', encodeAnyJawnNonFriendlyCharacters()") {
    it("should return '1 [[EQUALS]] 2/2 [[EQUALS]] 3/3 [[EQUALS]] 4/4' ") {
      val encoded = PlanningHelper.encodeAnyJawnNonFriendlyCharacters("1 = 2/2 = 3/3 = 4/4")
      assert(encoded === "1 [[EQUALS]] 2/2 [[EQUALS]] 3/3 [[EQUALS]] 4/4")
    }
  }

  describe("Given a string with jawn nonfriendly character repeated, '2/2 = 100%', encodeAnyJawnNonFriendlyCharacters()") {
    it("should return '2/2 [[EQUALS]] 100[[PERCENT]]' ") {
      val encoded = PlanningHelper.encodeAnyJawnNonFriendlyCharacters("2/2 = 100%")
      assert(encoded === "2/2 [[EQUALS]] 100[[PERCENT]]")
    }
  }


  describe("Given a simple string, 'hello', decoding") {
    it("should returns the same string") {
      val decoded = PlanningHelper.decodeAnyNonFriendlyCharacters("hello")
      assert(decoded === "hello")
    }
  }

  describe("Given a string, '1 [[EQUALS]] 2/2', decoding") {
    it("should return the string '1 = 2/2'") {
      val decoded = PlanningHelper.decodeAnyNonFriendlyCharacters("1 [[EQUALS]] 2/2")
      assert(decoded === "1 = 2/2")
    }
  }

  describe("Given a string, '1 [[EQUALS]] 2/2 [[EQUALS]] 3/3 [[EQUALS]] 4/4', decoding") {
    it("should return the string '1 = 2/2 = 3/3 = 4/4'") {
      val decoded = PlanningHelper.decodeAnyNonFriendlyCharacters("1 [[EQUALS]] 2/2 [[EQUALS]] 3/3 [[EQUALS]] 4/4")
      assert(decoded === "1 = 2/2 = 3/3 = 4/4")
    }
  }


  describe("Given a string, '2/2 [[EQUALS]] 100[[PERCENT]]', decoding") {
    it("should return the string '2/2 = 100%'") {
      val decoded = PlanningHelper.decodeAnyNonFriendlyCharacters("2/2 [[EQUALS]] 100[[PERCENT]]")
      assert(decoded === "2/2 = 100%")
    }
  }

  describe("Given a string, '&', encoding") {
    it("[[AMPERSAND]]'") {
      val decoded = PlanningHelper.encodeAnyJawnNonFriendlyCharacters("&")
      assert(decoded === "&")
    }
  }

}


