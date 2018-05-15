package potentialmicroservice.planning.reader.dao

import duplicate.model.FirstLevel
import duplicate.model.esandos._
import org.mongodb.scala.bson.BsonArray
import org.mongodb.scala.bson.collection.immutable.Document
import potentialmicroservice.planning.sharedschema.WeeklyPlanningSchema
import potentialmicroservice.planning.writer.dao.PlanWriterDaoTermlyCurriculumSelectionHelper

trait RetrieveFullWeekOfLessonsDaoHelperTestCanned extends PlanWriterDaoTermlyCurriculumSelectionHelper {


  def createSomeArtHighLevelPlans(): List[Document] = {
    List(
      createCannedHighLevelWeeklyPlan(TEST_SUBJECT_ART, TEST_WEEK2, TEST_TIMESTAMP_WEEK2_A, TEST_ART_ES_AND_OS_2),
      createCannedHighLevelWeeklyPlan(TEST_SUBJECT_ART, TEST_WEEK2, TEST_TIMESTAMP_WEEK2_B, TEST_ART_ES_AND_OS_3),
      createCannedHighLevelWeeklyPlan(TEST_SUBJECT_ART, TEST_WEEK2, TEST_TIMESTAMP_WEEK2_C, TEST_ART_ES_AND_OS_1),
      createCannedHighLevelWeeklyPlan(TEST_SUBJECT_ART, TEST_WEEK2, TEST_TIMESTAMP_WEEK2_D, TEST_ART_ES_AND_OS_2),
      createCannedHighLevelWeeklyPlan(TEST_SUBJECT_ART, TEST_WEEK2, TEST_TIMESTAMP_WEEK2_E, TEST_ART_ES_AND_OS_3)
    )
  }

  def createCannedHighLevelWeeklyPlan(
                                       subject: String,
                                       weekBeginning: String,
                                       timestamp: String,
                                       selectedEsOsBenchmarksByGroup: Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]
                                     ): Document = {
    Document(
      WeeklyPlanningSchema.TTT_USER_ID -> TEST_USER1,
      WeeklyPlanningSchema.CLASS_ID -> TEST_USER1,
      WeeklyPlanningSchema.SUBJECT -> subject,
      WeeklyPlanningSchema.WEEK_BEGINNING_ISO_DATE -> weekBeginning,
      WeeklyPlanningSchema.CREATED_TIMESTAMP -> timestamp,
      WeeklyPlanningSchema.SELECTED_ES_OS_AND_BENCHMARKS_BY_GROUP -> convertGroupToEsAndOsBenchmarksToBsonArray(selectedEsOsBenchmarksByGroup),
      WeeklyPlanningSchema.COMPLETED_ES_OS_AND_BENCHMARKS_BY_GROUP -> BsonArray()
    )
  }


  /////////////////////////////


  private[dao] val TEST_USER1 = "user1111"
  private[dao] val TEST_CLASS1 = "class1111"
  private[dao] val TEST_SUBJECT_MATHS = "MATHEMATICS"
  private[dao] val TEST_SUBJECT_ART = "EXPRESSIVE_ARTS__ART"
  private[dao] val TEST_SUBJECT_DRAMA = "EXPRESSIVE_ARTS__DRAMA"
  private[dao] val TEST_SUBJECT_MUSIC = "EXPRESSIVE_ARTS__MUSIC"
  private[dao] val TEST_SUBJECT_PE = "HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION"
  private[dao] val TEST_SUBJECT_WRITING = "LITERACY__WRITING"
  private[dao] val TEST_SUBJECT_READING = "LITERACY__READING"
  private[dao] val TEST_SUBJECT_SCIENCE = "SCIENCE"
  private[dao] val TEST_SUBJECT_TECHNOLOGY = "TECHNOLOGIES"

  private[dao] val TEST_WEEK1 = "2018-04-30"
  private[dao] val TEST_WEEK2 = "2018-05-07"
  private[dao] val TEST_WEEK3 = "2018-05-14"

  private[dao] val TEST_TIMESTAMP_WEEK1_A = "2018-04-27 14:24:36.185"
  private[dao] val TEST_TIMESTAMP_WEEK1_B = "2018-04-27 14:33:13.222"
  private[dao] val TEST_TIMESTAMP_WEEK1_C = "2018-04-27 15:54:56.886"
  private[dao] val TEST_TIMESTAMP_WEEK1_D = "2018-04-28 09:11:39.982"
  private[dao] val TEST_TIMESTAMP_WEEK1_E = "2018-04-29 11:02:08.495"

  private[dao] val TEST_TIMESTAMP_WEEK2_A = "2018-05-04 10:22:14.599"
  private[dao] val TEST_TIMESTAMP_WEEK2_B = "2018-05-04 14:33:13.222"
  private[dao] val TEST_TIMESTAMP_WEEK2_C = "2018-05-05 09:54:56.886"
  private[dao] val TEST_TIMESTAMP_WEEK2_D = "2018-05-05 10:11:39.982"
  private[dao] val TEST_TIMESTAMP_WEEK2_E = "2018-05-05 17:02:08.495"

  private[dao] val TEST_TIMESTAMP_WEEK3_A = "2018-05-11 10:22:14.599"
  private[dao] val TEST_TIMESTAMP_WEEK3_B = "2018-05-11 14:33:13.222"
  private[dao] val TEST_TIMESTAMP_WEEK3_C = "2018-05-12 09:54:56.886"
  private[dao] val TEST_TIMESTAMP_WEEK3_D = "2018-05-12 10:11:39.982"
  private[dao] val TEST_TIMESTAMP_WEEK3_E = "2018-05-12 17:02:08.495"

  private[dao] val TEST_ART_SECTION_NAME = "Art and design"
  private[dao] val TEST_ART_SUBSECTION_NAME = "NO_SUBSECTION_NAME"

  private[dao] val TEST_ART_ES_AND_OS_1: Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel] = Map(
    "CLASS_LEVEL" -> EsAndOsPlusBenchmarksForCurriculumAreaAndLevel(
      FirstLevel,
      ExpressiveArts__Art,
      Map(
        TEST_ART_SECTION_NAME -> Map(
          TEST_ART_SUBSECTION_NAME -> EandOSetSubSection(
            "",
            List(
              EandO("EXA 1-02a", Nil)
            ),
            Nil
          )
        )
      )
    )
  )

  private[dao] val TEST_ART_ES_AND_OS_2: Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel] = Map(
    "CLASS_LEVEL" -> EsAndOsPlusBenchmarksForCurriculumAreaAndLevel(
      FirstLevel,
      ExpressiveArts__Art,
      Map(
        TEST_ART_SECTION_NAME -> Map(
          TEST_ART_SUBSECTION_NAME -> EandOSetSubSection(
            "",
            Nil,
            List(
              Benchmark("Shares thoughts and feelings by expressing personal views in response to the work " +
                "of at least one artist and one designer.")
            )
          )
        )
      )
    )
  )

  private[dao] val TEST_ART_ES_AND_OS_3: Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel] = Map(
    "CLASS_LEVEL" -> EsAndOsPlusBenchmarksForCurriculumAreaAndLevel(
      FirstLevel,
      ExpressiveArts__Art,
      Map(
        TEST_ART_SECTION_NAME -> Map(
          TEST_ART_SUBSECTION_NAME -> EandOSetSubSection(
            "",
            List(
              EandO("EXA 1-02a", Nil)
            ),
            List(
              Benchmark("Shares thoughts and feelings by expressing personal views in response to the work " +
                "of at least one artist and one designer."),
              Benchmark("Records directly from experiences across the curriculum, for example, observes and " +
                "sketches a view from a window, features of the built environment, pets, self or others.")
            )
          )
        )
      )
    )
  )


}
