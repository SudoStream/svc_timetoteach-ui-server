package shared.model.classtimetable

import org.scalatest._

class WwwSessionOfTheWeekTest extends FunSpec {
  describe("Early Monday Morning SessionOfTheWeek") {
    it("should have a value of 'monday-early-morning-session'") {
      val sessionOfTheWeek : WwwSessionOfTheWeek = MondayEarlyMorningSession()
      assert(sessionOfTheWeek.value == "monday-early-morning-session")
    }
    it("should have a session minus day value of early-morning-session") {
      val sessionOfTheWeek : WwwSessionOfTheWeek = MondayEarlyMorningSession()
      assert(sessionOfTheWeek.valueWithoutDay == "early-morning-session")
    }
  }
  describe("Late Morning Wednesday SessionOfTheWeek") {
    it("should have a value of 'wednesday-late-morning-session'") {
      val sessionOfTheWeek : WwwSessionOfTheWeek = WednesdayLateMorningWwwSession()
      assert(sessionOfTheWeek.value == "wednesday-late-morning-session")
    }
    it("should have a session minus day value of late-morning-session") {
      val sessionOfTheWeek : WwwSessionOfTheWeek = WednesdayLateMorningWwwSession()
      assert(sessionOfTheWeek.valueWithoutDay == "late-morning-session")
    }
  }
  describe("Friday Afternoon SessionOfTheWeek") {
    it("should have a value of 'friday-afternoon-session'") {
      val sessionOfTheWeek : WwwSessionOfTheWeek = FridayAfternoonWwwSession()
      assert(sessionOfTheWeek.value == "friday-afternoon-session")
    }
    it("should have a session minus day value of afternoon-session") {
      val sessionOfTheWeek : WwwSessionOfTheWeek = FridayAfternoonWwwSession()
      assert(sessionOfTheWeek.valueWithoutDay == "afternoon-session")
    }
  }

}
