package potentialmicroservice.planning.reader.dao

import org.scalatest.FunSpec

class PlanReaderTermlyCurriculumSelectionHelperTest extends FunSpec
{
  private val planReaderTermlyCurriculumSelectionHelper = new PlanReaderTermlyCurriculumSelectionHelper
  {}

  describe("When given an empty list, findLatestVersionOfTermlyCurriculumSelection()") {
    it("should return None") {
      val maybeTermlyCurriculumSelection = planReaderTermlyCurriculumSelectionHelper.findLatestVersionOfTermlyCurriculumSelection(Nil)
      assert(maybeTermlyCurriculumSelection.isEmpty)
    }
  }


}
