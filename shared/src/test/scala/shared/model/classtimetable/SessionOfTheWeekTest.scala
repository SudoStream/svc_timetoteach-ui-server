package shared.model.classtimetable

import org.scalatest._

class SessionOfTheWeekTest extends FunSpec {
  describe("Early Monday Morning SessionOfTheWeek") {
    it("should have a value of 'monday-early-morning-session'") {
      val sessionOfTheWeek : SessionOfTheWeek = MondayEarlyMorningSession()
      assert(sessionOfTheWeek.value == "monday-early-morning-session")
    }
  }
  describe("Late Morning Wednesday SessionOfTheWeek") {
    it("should have a value of 'wednesday-late-morning-session'") {
      val sessionOfTheWeek : SessionOfTheWeek = WednesdayLateMorningSession()
      assert(sessionOfTheWeek.value == "wednesday-late-morning-session")
    }
  }
  describe("Friday Afternoon SessionOfTheWeek") {
    it("should have a value of 'friday-afternoon-session'") {
      val sessionOfTheWeek : SessionOfTheWeek = FridayAfternoonSession()
      assert(sessionOfTheWeek.value == "friday-afternoon-session")
    }
  }

}
