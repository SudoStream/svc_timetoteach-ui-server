package models.timetoteach.planning

import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import org.scalatest.FunSuite

class ScottishCurriculumPlanningAreaWrapperTest
  extends FunSuite
{
  test("EXPRESSIVE_ARTS nice value is 'Expressive Arts'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS)
    assert(wrapper.niceValue() === "Expressive Arts")
  }

  test("EXPRESSIVE_ARTS__ART nice value is 'Expressive Arts : Art'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__ART)
    assert(wrapper.niceValue() === "Expressive Arts : Art")
  }

  test("RME__STANDARD nice value is 'RME : Standard'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.RME__STANDARD)
    assert(wrapper.niceValue() === "RME : Religious & Moral Education")
  }

  test("HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION nice value is 'Health And Wellbeing : Physical Education'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION)
    assert(wrapper.niceValue() === "Health And Wellbeing : Physical Education")
  }

  test("HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION is composite") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION)
    assert(wrapper.isCompositeValue)
  }

  test("HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION is composite2") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION)
    assert(!wrapper.isNotCompositeValue)
  }

  test("EXPRESSIVE_ARTS nice value is NOT composite value") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS)
    assert(wrapper.isNotCompositeValue)
  }

  test("EXPRESSIVE_ARTS nice value is NOT composite value2") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS)
    assert(!wrapper.isCompositeValue)
  }

  test("EXPRESSIVE_ARTS nice header value is None") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS)
    assert(wrapper.niceHeaderValueIfPresent().isEmpty)
  }

  test("EXPRESSIVE_ARTS nice specific value is None") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS)
    assert(wrapper.niceSpecificValueIfPresent().isEmpty)
  }

  test("HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION nice header value is 'Health And Wellbeing'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION)
    assert(wrapper.niceHeaderValueIfPresent().get === "Health And Wellbeing")
  }

  test("HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION nice specific value is 'Physical Education'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION)
    assert(wrapper.niceSpecificValueIfPresent().get === "Physical Education")
  }

  test("RME__CATHOLIC nice header value is 'RME'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.RME__CATHOLIC)
    assert(wrapper.niceHeaderValueIfPresent().get === "RME")
  }

  test("RME__CATHOLIC nice specific value is 'Catholic'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.RME__CATHOLIC)
    assert(wrapper.niceSpecificValueIfPresent().get === "Religious & Moral Education (Catholic)")
  }

  test("EXPRESSIVE_ARTS__ART planAtGroupLevel is 'false'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__ART)
    assert(!wrapper.planAtGroupLevel)
  }

  test("EXPRESSIVE_ARTS__DRAMA planAtGroupLevel is 'false'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__DRAMA)
    assert(!wrapper.planAtGroupLevel)
  }

  test("EXPRESSIVE_ARTS__MUSIC planAtGroupLevel is 'false'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__MUSIC)
    assert(!wrapper.planAtGroupLevel)
  }

  test("HEALTH_AND_WELLBEING planAtGroupLevel is 'false'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING)
    assert(!wrapper.planAtGroupLevel)
  }
  test("HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION planAtGroupLevel is 'false'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION)
    assert(!wrapper.planAtGroupLevel)
  }
  test("LITERACY__WRITING planAtGroupLevel is 'true'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.LITERACY__WRITING)
    assert(wrapper.planAtGroupLevel)
  }
  test("LITERACY__READING planAtGroupLevel is 'true'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.LITERACY__READING)
    assert(wrapper.planAtGroupLevel)
  }
  test("LITERACY__CLASSICAL_LANGUAGES planAtGroupLevel is 'true'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.LITERACY__CLASSICAL_LANGUAGES)
    assert(wrapper.planAtGroupLevel)
  }
  test("LITERACY__GAELIC_LEARNERS planAtGroupLevel is 'false'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.LITERACY__GAELIC_LEARNERS)
    assert(wrapper.planAtGroupLevel)
  }

  test("LITERACY__LITERACY_AND_ENGLISH planAtGroupLevel is 'true'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.LITERACY__LITERACY_AND_ENGLISH)
    assert(wrapper.planAtGroupLevel)
  }
  test("LITERACY__LITERACY_AND_GAIDLIG planAtGroupLevel is 'true'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.LITERACY__LITERACY_AND_GAIDLIG)
    assert(wrapper.planAtGroupLevel)
  }
  test("LITERACY__MODERN_LANGUAGES planAtGroupLevel is 'true'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.LITERACY__MODERN_LANGUAGES)
    assert(wrapper.planAtGroupLevel)
  }
  test("MATHEMATICS planAtGroupLevel is 'true'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.MATHEMATICS)
    assert(wrapper.planAtGroupLevel)
  }
  test("RME__STANDARD planAtGroupLevel is 'false'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.RME__STANDARD)
    assert(!wrapper.planAtGroupLevel)
  }
  test("RME__CATHOLIC planAtGroupLevel is 'false'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.RME__CATHOLIC)
    assert(!wrapper.planAtGroupLevel)
  }
  test("SCIENCE planAtGroupLevel is 'false'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.SCIENCE)
    assert(!wrapper.planAtGroupLevel)
  }
  test("SOCIAL_STUDIES planAtGroupLevel is 'false'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.SOCIAL_STUDIES)
    assert(!wrapper.planAtGroupLevel)
  }
  test("TECHNOLOGIES planAtGroupLevel is 'false'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.TECHNOLOGIES)
    assert(!wrapper.planAtGroupLevel)
  }
  test("TOPIC planAtGroupLevel is 'false'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.TOPIC)
    assert(!wrapper.planAtGroupLevel)
  }
  test("NONE planAtGroupLevel is 'false'") {
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.NONE)
    assert(!wrapper.planAtGroupLevel)
  }


}
