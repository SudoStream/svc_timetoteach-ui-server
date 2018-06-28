package duplicate.model.planning

import org.scalatest.FunSpec

class FullWeeklyPlanOfLessonsTest extends FunSpec {
//  EXPRESSIVE_ARTS,
//  EXPRESSIVE_ARTS__ART
//  EXPRESSIVE_ARTS__DRAMA
//  EXPRESSIVE_ARTS__DANCE
//  EXPRESSIVE_ARTS__MUSIC
//  HEALTH_AND_WELLBEING
//  HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION
//  LITERACY__WRITING
//  LITERACY__READING
//  LITERACY__LISTENING_AND_TALKING
//  LITERACY__CLASSICAL_LANGUAGES
//  LITERACY__GAELIC_LEARNERS
//  LITERACY__LITERACY_AND_ENGLISH
//  LITERACY__LITERACY_AND_GAIDLIG
//  LITERACY__MODERN_LANGUAGES
//  MATHEMATICS
//  RME__STANDARD
//  RME__CATHOLIC
//  SCIENCE
//  SOCIAL_STUDIES
//  TECHNOLOGIES
//  TOPIC

  describe("Given a valid full weeks of plans") {
    it("should have a non empty map of lessons") {
      val fullWeeklyPlan = createFullWeeklyPlans()
      assert(fullWeeklyPlan != null)
    }
  }

  def createFullWeeklyPlans() : FullWeeklyPlanOfLessons = {
    null
  }

}
