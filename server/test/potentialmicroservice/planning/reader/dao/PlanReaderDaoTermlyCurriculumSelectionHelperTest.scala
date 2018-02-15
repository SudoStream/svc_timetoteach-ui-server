package potentialmicroservice.planning.reader.dao

import org.scalatest.FunSpec

class PlanReaderDaoTermlyCurriculumSelectionHelperTest extends FunSpec
{
  private val planReaderTermlyCurriculumSelectionHelper = new PlanReaderDaoTermlyCurriculumSelectionHelper
  {}

  describe("When given an empty list, findLatestVersionOfTermlyCurriculumSelection()") {
    it("should return None") {
      val maybeTermlyCurriculumSelection = planReaderTermlyCurriculumSelectionHelper.findLatestVersionOfTermlyCurriculumSelection(Nil)
      assert(maybeTermlyCurriculumSelection.isEmpty)
    }
  }


}
