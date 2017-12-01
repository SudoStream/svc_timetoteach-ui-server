package shared.model.classtimetable

import org.scalatest.FunSpec

class WwwDayOfWeekTest extends FunSpec {
  describe("Monday") {
    it("should have a short value of 'Mon'") {
      val dayOfWeek = WwwDayOfWeek("Monday")
      assert(dayOfWeek.shortValue == "Mon")
    }
  }
}
