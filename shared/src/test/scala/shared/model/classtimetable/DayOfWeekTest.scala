package shared.model.classtimetable

import org.scalatest.FunSpec

class DayOfWeekTest extends FunSpec {
  describe("Monday") {
    it("should have a short value of 'Mon'") {
      val dayOfWeek = DayOfWeek("Monday")
      assert(dayOfWeek.shortValue == "Mon")
    }
  }
}
