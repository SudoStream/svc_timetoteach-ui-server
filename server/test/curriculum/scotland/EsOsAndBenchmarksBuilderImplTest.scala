package curriculum.scotland

import duplicate.model.esandos.{ExpressiveArts, Mathematics}
import duplicate.model.{EarlyLevel, FirstLevel, SecondLevel}
import io.sudostream.timetoteach.messages.scottish.ScottishEsAndOsData
import org.scalatest.FunSpec

class EsOsAndBenchmarksBuilderImplTest extends FunSpec with CreateEsAndOs
{

  describe("Given a valid list of es and os as well as benchmarks, building them") {
    it("should be defined") {
      val scottishEsAndOsData: ScottishEsAndOsData = createScottishEsAndOsData()
      val maybeEsOsAndBenchmarks = EsOsAndBenchmarksBuilderImpl.buildTheEsOsAndBenchmarks(scottishEsAndOsData)
      assert(maybeEsOsAndBenchmarks.isDefined)
    }
    it("should have ScottishCurriculumLevel.EARLY defined") {
      val scottishEsAndOsData: ScottishEsAndOsData = createScottishEsAndOsData()
      val maybeMergedEsOsAndBenchmarks = EsOsAndBenchmarksBuilderImpl.buildTheEsOsAndBenchmarks(scottishEsAndOsData)
      assert(maybeMergedEsOsAndBenchmarks.get.get(EarlyLevel).isDefined)
    }
    it("should have [ScottishCurriculumLevel.EARLY][ScottishCurriculumAreaName.MATHEMATICS] defined") {
      val scottishEsAndOsData: ScottishEsAndOsData = createScottishEsAndOsData()
      val maybeMergedEsOsAndBenchmarks = EsOsAndBenchmarksBuilderImpl.buildTheEsOsAndBenchmarks(scottishEsAndOsData)
      assert(maybeMergedEsOsAndBenchmarks.get(EarlyLevel).get(Mathematics).isDefined)
    }
    it("should have [ScottishCurriculumLevel.EARLY][ScottishCurriculumAraName.MATHEMATICS] with sections non empty") {
      val scottishEsAndOsData: ScottishEsAndOsData = createScottishEsAndOsData()
      val maybeMergedEsOsAndBenchmarks = EsOsAndBenchmarksBuilderImpl.buildTheEsOsAndBenchmarks(scottishEsAndOsData)
      assert(maybeMergedEsOsAndBenchmarks.get(EarlyLevel)(Mathematics).setSectionNameToSubSections.nonEmpty)
    }
    it("should have [ScottishCurriculumLevel.EARLY][ScottishCurriculumAraName.MATHEMATICS] with" +
      " section 'Number, money and measure' non empty") {
      val scottishEsAndOsData: ScottishEsAndOsData = createScottishEsAndOsData()
      val maybeMergedEsOsAndBenchmarks = EsOsAndBenchmarksBuilderImpl.buildTheEsOsAndBenchmarks(scottishEsAndOsData)
      assert(maybeMergedEsOsAndBenchmarks.get(EarlyLevel)(Mathematics).setSectionNameToSubSections.get("Number, money and measure").isDefined)
    }
    it("should have [ScottishCurriculumLevel.EARLY][ScottishCurriculumAraName.MATHEMATICS] with section" +
      " 'Number, money and measure' & sub-section 'Estimation and rounding' non empty") {
      val scottishEsAndOsData: ScottishEsAndOsData = createScottishEsAndOsData()
      val maybeMergedEsOsAndBenchmarks = EsOsAndBenchmarksBuilderImpl.buildTheEsOsAndBenchmarks(scottishEsAndOsData)
      assert(maybeMergedEsOsAndBenchmarks.get(EarlyLevel)(Mathematics).setSectionNameToSubSections("Number, money and measure").get("Estimation and rounding").isDefined)
    }
    it("should have [ScottishCurriculumLevel.EARLY][ScottishCurriculumAraName.MATHEMATICS] with section" +
      " 'Number, money and measure' & sub-section 'Estimation and rounding' has 3 benchmarks") {
      val scottishEsAndOsData: ScottishEsAndOsData = createScottishEsAndOsData()
      val maybeMergedEsOsAndBenchmarks = EsOsAndBenchmarksBuilderImpl.buildTheEsOsAndBenchmarks(scottishEsAndOsData)
      assert(maybeMergedEsOsAndBenchmarks.get(EarlyLevel)(Mathematics).setSectionNameToSubSections("Number, money and measure")("Estimation and rounding").benchmarks.size === 3)
    }
    it("should have [ScottishCurriculumLevel.EARLY][ScottishCurriculumAraName.MATHEMATICS] with section" +
      " 'Number, money and measure' & sub-section 'Estimation and rounding' has 1 E and O") {
      val scottishEsAndOsData: ScottishEsAndOsData = createScottishEsAndOsData()
      val maybeMergedEsOsAndBenchmarks = EsOsAndBenchmarksBuilderImpl.buildTheEsOsAndBenchmarks(scottishEsAndOsData)
      assert(maybeMergedEsOsAndBenchmarks.get(EarlyLevel)(Mathematics).setSectionNameToSubSections("Number, money and measure")("Estimation and rounding").eAndOs.size === 1)
    }
    it("should have [ScottishCurriculumLevel.SECOND][ScottishCurriculumAreaName.EXPRESSIVE_ARTS] with section" +
      " 'Music' be defined") {
      val scottishEsAndOsData: ScottishEsAndOsData = createScottishEsAndOsData()
      val maybeMergedEsOsAndBenchmarks = EsOsAndBenchmarksBuilderImpl.buildTheEsOsAndBenchmarks(scottishEsAndOsData)
      assert(maybeMergedEsOsAndBenchmarks.get(SecondLevel)(ExpressiveArts).setSectionNameToSubSections.isDefinedAt("Music"))
    }
    it("should have [ScottishCurriculumLevel.SECOND][ScottishCurriculumAreaName.EXPRESSIVE_ARTS] with section" +
      " 'Music' sholud have 2 es and os") {
      val scottishEsAndOsData: ScottishEsAndOsData = createScottishEsAndOsData()
      val maybeMergedEsOsAndBenchmarks = EsOsAndBenchmarksBuilderImpl.buildTheEsOsAndBenchmarks(scottishEsAndOsData)
      assert(maybeMergedEsOsAndBenchmarks.get(SecondLevel)(ExpressiveArts).setSectionNameToSubSections("Music")(EsOsAndBenchmarksBuilderImpl.NO_SUBSECTION_NAME).eAndOs.size == 2)
    }

    //////

    it("should have ScottishCurriculumLevel.FIRST defined") {
      val scottishEsAndOsData: ScottishEsAndOsData = createScottishEsAndOsData()
      val maybeMergedEsOsAndBenchmarks = EsOsAndBenchmarksBuilderImpl.buildTheEsOsAndBenchmarks(scottishEsAndOsData)
      assert(maybeMergedEsOsAndBenchmarks.get.get(FirstLevel).isDefined)
    }

    /////////


    it("should have [ScottishCurriculumLevel.EARLY][ScottishCurriculumAraName.MATHEMATICS] with section" +
      " 'Number, money and measure' & sub-section 'Number and number processes' non empty") {
      val scottishEsAndOsData: ScottishEsAndOsData = createScottishEsAndOsData()
      val maybeMergedEsOsAndBenchmarks = EsOsAndBenchmarksBuilderImpl.buildTheEsOsAndBenchmarks(scottishEsAndOsData)
      assert(maybeMergedEsOsAndBenchmarks.get(EarlyLevel)(Mathematics).setSectionNameToSubSections("Number, money and measure").get("Number and number processes").isDefined)
    }
    it("should have [ScottishCurriculumLevel.EARLY][ScottishCurriculumAraName.MATHEMATICS] with section" +
      " 'Number, money and measure' & sub-section 'Number and number processes' has 14 benchmarks") {
      val scottishEsAndOsData: ScottishEsAndOsData = createScottishEsAndOsData()
      val maybeMergedEsOsAndBenchmarks = EsOsAndBenchmarksBuilderImpl.buildTheEsOsAndBenchmarks(scottishEsAndOsData)
      assert(maybeMergedEsOsAndBenchmarks.get(EarlyLevel)(Mathematics).setSectionNameToSubSections("Number, money and measure")("Number and number processes").benchmarks.size === 14)
    }


  }

}
