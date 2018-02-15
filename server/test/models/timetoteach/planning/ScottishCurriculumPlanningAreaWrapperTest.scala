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
    assert(wrapper.niceValue() === "RME : Standard")
  }

  test("HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION nice value is 'Health And Wellbeing : Physical Education'"){
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION)
    assert(wrapper.niceValue() === "Health And Wellbeing : Physical Education")
  }

  test("HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION is composite"){
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION)
    assert(wrapper.isCompositeValue)
  }

  test("HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION is composite2"){
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

  test("EXPRESSIVE_ARTS nice header value is None"){
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS)
    assert(wrapper.niceHeaderValueIfPresent().isEmpty)
  }

  test("EXPRESSIVE_ARTS nice specific value is None"){
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS)
    assert(wrapper.niceSpecificValueIfPresent().isEmpty)
  }

  test("HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION nice header value is 'Health And Wellbeing'"){
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION)
    assert(wrapper.niceHeaderValueIfPresent().get === "Health And Wellbeing")
  }

  test("HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION nice specific value is 'Physical Education'"){
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION)
    assert(wrapper.niceSpecificValueIfPresent().get === "Physical Education")
  }

  test("RME__CATHOLIC nice header value is 'RME'"){
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.RME__CATHOLIC)
    assert(wrapper.niceHeaderValueIfPresent().get === "RME")
  }

  test("RME__CATHOLIC nice specific value is 'Catholic'"){
    val wrapper = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.RME__CATHOLIC)
    assert(wrapper.niceSpecificValueIfPresent().get === "Catholic")
  }

}
