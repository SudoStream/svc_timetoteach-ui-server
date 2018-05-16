package potentialmicroservice.planning.reader.dao

import duplicate.model.FirstLevel
import duplicate.model.esandos._
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson.{BsonArray, BsonDocument}
import potentialmicroservice.planning.sharedschema.WeeklyPlanningSchema
import potentialmicroservice.planning.writer.dao.PlanWriterDaoTermlyCurriculumSelectionHelper

trait RetrieveFullWeekOfLessonsDaoHelperTestCanned extends PlanWriterDaoTermlyCurriculumSelectionHelper {

  def createSomeLowLevelSectionNameToSubSectionsEsAndOsBsonDocs(): List[BsonDocument] = {
    BsonDocument(
      WeeklyPlanningSchema.SELECTED_SECTION_NAME -> "Number, money and measure",
      WeeklyPlanningSchema.SELECTED_SUBSECTION_NAME -> "Estimation and rounding",
      WeeklyPlanningSchema.SELECTED_ES_AND_OS -> BsonArray("MNU 0-01a"),
      WeeklyPlanningSchema.SELECTED_BENCHMARKS -> BsonArray(
        "Recognises the number of objects in a group, without counting (subitising) and uses this information to " +
          "estimate the number of objects in other groups",
        "Checks estimates by counting.",
        "Demonstrates skills of estimation in the contexts of number and measure using relevant vocabulary, " +
          "including less than, longer than, more than and the same."
      )
    ) :: BsonDocument(
      WeeklyPlanningSchema.SELECTED_SECTION_NAME -> "Number, money and measure",
      WeeklyPlanningSchema.SELECTED_SUBSECTION_NAME -> "Number and number processes",
      WeeklyPlanningSchema.SELECTED_ES_AND_OS -> BsonArray("MNU 0-02a", "MNU 0-03a"),
      WeeklyPlanningSchema.SELECTED_BENCHMARKS -> BsonArray(
        "Explains that zero means there is none of a particular quantity and is represented by the numeral 0",
        "Recalls the number sequence forwards within the range 0 - 30, from any given number.",
        "Recalls the number sequence backwards from 20",
        "Identifies and recognises numbers from 0 to 20."
      )
    ) :: BsonDocument(
      WeeklyPlanningSchema.SELECTED_SECTION_NAME -> "Number, money and measure",
      WeeklyPlanningSchema.SELECTED_SUBSECTION_NAME -> "Fractions, decimal fractions and percentages",
      WeeklyPlanningSchema.SELECTED_ES_AND_OS -> BsonArray("MNU 0-07a"),
      WeeklyPlanningSchema.SELECTED_BENCHMARKS -> BsonArray(
        "Splits a whole into smaller parts and explains that equal parts are the same size.",
        "Uses appropriate vocabulary to describe halves"
      )
    ) :: BsonDocument(
      WeeklyPlanningSchema.SELECTED_SECTION_NAME -> "Number, money and measure",
      WeeklyPlanningSchema.SELECTED_SUBSECTION_NAME -> "Money",
      WeeklyPlanningSchema.SELECTED_ES_AND_OS -> BsonArray("MNU 0-09a"),
      WeeklyPlanningSchema.SELECTED_BENCHMARKS -> BsonArray(
        "Identifies all coins to £2",
        "Applies addition and subtraction skills and uses 1p, 2p, 5p and 10p coins to pay the exact value for items to 10p"
      )
    ) :: BsonDocument(
      WeeklyPlanningSchema.SELECTED_SECTION_NAME -> "Information handling",
      WeeklyPlanningSchema.SELECTED_SUBSECTION_NAME -> "Data and analysis",
      WeeklyPlanningSchema.SELECTED_ES_AND_OS -> BsonArray("MNU 0-20a","MNU 0-20b","MNU 0-20c"),
      WeeklyPlanningSchema.SELECTED_BENCHMARKS -> BsonArray(
        "Asks simple questions to collect data for a specific purpose",
        "Collects and organises objects for a specific purpose."
      )
    ) :: Nil

  }


  def createSomeArtHighLevelPlans(): List[Document] = {
    List(
      createCannedHighLevelWeeklyPlan(TEST_SUBJECT_ART, TEST_WEEK2, TEST_TIMESTAMP_WEEK2_D, TEST_ART_ES_AND_OS_2),
      createCannedHighLevelWeeklyPlan(TEST_SUBJECT_ART, TEST_WEEK2, TEST_TIMESTAMP_WEEK2_A, TEST_ART_ES_AND_OS_2),
      createCannedHighLevelWeeklyPlan(TEST_SUBJECT_ART, TEST_WEEK2, TEST_TIMESTAMP_WEEK2_E, TEST_ART_ES_AND_OS_3),
      createCannedHighLevelWeeklyPlan(TEST_SUBJECT_ART, TEST_WEEK2, TEST_TIMESTAMP_WEEK2_C, TEST_ART_ES_AND_OS_1),
      createCannedHighLevelWeeklyPlan(TEST_SUBJECT_ART, TEST_WEEK2, TEST_TIMESTAMP_WEEK2_B, TEST_ART_ES_AND_OS_3)
    )
  }

  def createSomeMathsHighLevelPlans(): List[Document] = {
    List(
      createCannedHighLevelWeeklyPlan(TEST_SUBJECT_MATHS, TEST_WEEK2, TEST_TIMESTAMP_WEEK2_A, TEST_MATHS_ES_AND_OS_1),
      createCannedHighLevelWeeklyPlan(TEST_SUBJECT_MATHS, TEST_WEEK2, TEST_TIMESTAMP_WEEK2_C, TEST_MATHS_ES_AND_OS_2),
      createCannedHighLevelWeeklyPlan(TEST_SUBJECT_MATHS, TEST_WEEK2, TEST_TIMESTAMP_WEEK2_B, TEST_MATHS_ES_AND_OS_1)
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

  private[dao] val TEST_MATHS_SECTION_NAME = "Mathematics"
  private[dao] val TEST_MATHS_SUBSECTION_NAME = "Shapes"

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


  private[dao] val TEST_MATHS_ES_AND_OS_1: Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel] = Map(
    "group1" -> EsAndOsPlusBenchmarksForCurriculumAreaAndLevel(
      FirstLevel,
      Mathematics,
      Map(
        TEST_MATHS_SECTION_NAME -> Map(
          TEST_MATHS_SUBSECTION_NAME -> EandOSetSubSection(
            "",
            List(
              EandO("EXA 1-02a", Nil)
            ),
            Nil
          )
        )
      )
    ),
    "group2" -> EsAndOsPlusBenchmarksForCurriculumAreaAndLevel(
      FirstLevel,
      Mathematics,
      Map(
        TEST_MATHS_SECTION_NAME -> Map(
          TEST_MATHS_SUBSECTION_NAME -> EandOSetSubSection(
            "",
            List(
              EandO("EXA 1-02a", Nil)
            ),
            Nil
          )
        )
      )
    ),
    "group3" -> EsAndOsPlusBenchmarksForCurriculumAreaAndLevel(
      FirstLevel,
      Mathematics,
      Map(
        TEST_MATHS_SECTION_NAME -> Map(
          TEST_MATHS_SUBSECTION_NAME -> EandOSetSubSection(
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


  private[dao] val TEST_MATHS_ES_AND_OS_2: Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel] = Map(
    "group1" -> EsAndOsPlusBenchmarksForCurriculumAreaAndLevel(
      FirstLevel,
      Mathematics,
      Map(
        TEST_MATHS_SECTION_NAME -> Map(
          TEST_MATHS_SUBSECTION_NAME -> EandOSetSubSection(
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
    ),
    "group2" -> EsAndOsPlusBenchmarksForCurriculumAreaAndLevel(
      FirstLevel,
      Mathematics,
      Map(
        TEST_MATHS_SECTION_NAME -> Map(
          TEST_MATHS_SUBSECTION_NAME -> EandOSetSubSection(
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
    ),
    "group3" -> EsAndOsPlusBenchmarksForCurriculumAreaAndLevel(
      FirstLevel,
      Mathematics,
      Map(
        TEST_MATHS_SECTION_NAME -> Map(
          TEST_MATHS_SUBSECTION_NAME -> EandOSetSubSection(
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
