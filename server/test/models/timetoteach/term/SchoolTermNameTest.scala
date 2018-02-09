package models.timetoteach.term

import org.scalatest.FunSpec

class SchoolTermNameTest extends FunSpec {

  describe("SchoolTermName.niceValue() of AUTUMN_FIRST_TERM") {
    it("should equal 'Autumn First Term'") {
      assert(SchoolTermName.niceValue(SchoolTermName.AUTUMN_FIRST_TERM) === "Autumn First Term")
    }
  }

}