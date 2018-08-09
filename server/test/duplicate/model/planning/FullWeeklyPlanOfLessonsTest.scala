package duplicate.model.planning

import org.scalatest.FunSpec

class FullWeeklyPlanOfLessonsTest extends FunSpec {

  val TTT_USER_ID = "tttUserId12345"
  val CLASS_ID = "classIdABCDE"
  val WEEK_BEGINNING_ISO_DATE = "2018-05-07"
975
  describe("The test createFullWeeklyPlans()") {
    val fullWeeklyPlan = createFullWeeklyPlans()
    it("should have a non empty map of lessons") {
      assert(fullWeeklyPlan != null)
    }
  }

  describe("Given a valid full weeks of plans, calling dayToLessons()") {
    val fullWeeklyPlan = createFullWeeklyPlans()
    it("should have a non null map") {
      assert(fullWeeklyPlan.dayToLessons() != null)
    }
    it("should have a map with five keys") {
      assert(fullWeeklyPlan.dayToLessons().keys.size === 5)
    }
  }

  def createSubjectLessonPlans(subject: String): List[LessonPlan] = {
    subject match {
      case "LITERACY__WRITING" =>
        LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-08", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-09", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-10", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-11", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          Nil
      case "LITERACY__READING" =>
        LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          Nil
      case "MATHEMATICS" =>
        LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          Nil
      case "HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION" =>
        LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          Nil
      case "SCIENCE" =>
        LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          Nil
      case "TOPIC" =>
        LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          Nil
      case "EXPRESSIVE_ARTS__ART" =>
        LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          Nil
      case "RME__STANDARD" =>
        LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          LessonPlan(subject, "", WEEK_BEGINNING_ISO_DATE, "2018-05-07", "09:00", "10:00", "2018-05-04 21:14:33.347", Map(), Nil, Map(), Map(), List(), Map(), Nil, Nil) ::
          Nil
    }
  }

  def createSubjectToWeeklyPlanOfSubject(subject: String): WeeklyPlanOfOneSubject = {
    WeeklyPlanOfOneSubject(TTT_USER_ID, CLASS_ID, subject, WEEK_BEGINNING_ISO_DATE, Map(), createSubjectLessonPlans(subject))
  }

  def createSubjectToWeeklyPlanOfSubjectMap(): Map[String, WeeklyPlanOfOneSubject] = {
    Map(
      "LITERACY__WRITING" -> createSubjectToWeeklyPlanOfSubject("LITERACY__WRITING"),
      "LITERACY__READING" -> createSubjectToWeeklyPlanOfSubject("LITERACY__READING"),
      "MATHEMATICS" -> createSubjectToWeeklyPlanOfSubject("MATHEMATICS"),
      "HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION" -> createSubjectToWeeklyPlanOfSubject("HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION"),
      "SCIENCE" -> createSubjectToWeeklyPlanOfSubject("SCIENCE"),
      "TOPIC" -> createSubjectToWeeklyPlanOfSubject("TOPIC"),
      "EXPRESSIVE_ARTS__ART" -> createSubjectToWeeklyPlanOfSubject("EXPRESSIVE_ARTS__ART"),
      "RME__STANDARD" -> createSubjectToWeeklyPlanOfSubject("RME__STANDARD")
    )
  }

  def createFullWeeklyPlans(): FullWeeklyPlanOfLessons = {
    FullWeeklyPlanOfLessons(
      TTT_USER_ID,
      CLASS_ID,
      WEEK_BEGINNING_ISO_DATE,
      subjectToWeeklyPlanOfSubject = createSubjectToWeeklyPlanOfSubjectMap()
    )
  }

}
