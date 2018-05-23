package potentialmicroservice.planning.reader.dao

import java.time.{LocalDate, LocalTime}

import duplicate.model.FirstLevel
import duplicate.model.esandos._
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson.{BsonArray, BsonDocument}
import potentialmicroservice.planning.sharedschema.{SingleLessonPlanSchema, WeeklyPlanningSchema}
import potentialmicroservice.planning.writer.dao.PlanWriterDaoTermlyCurriculumSelectionHelper

trait RetrieveFullWeekOfLessonsDaoHelperTestCanned extends PlanWriterDaoTermlyCurriculumSelectionHelper {

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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

  private[dao] val TEST_LESSON_DATE1_OF_WEEK1 = "2018-04-30"
  private[dao] val TEST_LESSON_DATE2_OF_WEEK1 = "2018-05-01"
  private[dao] val TEST_LESSON_DATE3_OF_WEEK1 = "2018-05-02"
  private[dao] val TEST_LESSON_DATE4_OF_WEEK1 = "2018-05-03"
  private[dao] val TEST_LESSON_DATE5_OF_WEEK1 = "2018-05-04"

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

  private[dao] val TEST_ISO_TIME_0900 = "09:00"
  private[dao] val TEST_ISO_TIME_0915 = "09:15"
  private[dao] val TEST_ISO_TIME_0930 = "09:30"
  private[dao] val TEST_ISO_TIME_0945 = "09:45"
  private[dao] val TEST_ISO_TIME_1000 = "10:00"
  private[dao] val TEST_ISO_TIME_1015 = "10:15"
  private[dao] val TEST_ISO_TIME_1030 = "10:30"

  private[dao] val TEST_ACTIVITIES_PER_GROUP = BsonArray(
    BsonDocument(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "Some activity ONE",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 1,
      SingleLessonPlanSchema.GROUP_IDS -> BsonArray("group1", "group2")
    )
  )

  private[dao] val TEST_RESOURCES = BsonArray(
    Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "resourceA",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 1
    ),
    Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "resourceB",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 2
    )
  )

  private[dao] val TEST_LEARNING_INTENTIONS_PER_GROUP = BsonArray(
    BsonDocument(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "Some Learning Intention ONE",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 1,
      SingleLessonPlanSchema.GROUP_IDS -> BsonArray("group1", "group2")
    ),
    BsonDocument(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "Some Learning Intention TWO",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 2,
      SingleLessonPlanSchema.GROUP_IDS -> BsonArray("group1", "group2")
    ),
    BsonDocument(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "Some Learning Intention THREE",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 3,
      SingleLessonPlanSchema.GROUP_IDS -> BsonArray("group1", "group2")
    )
  )

  private[dao] val TEST_SUCCESS_CRITERIA_PER_GROUP = BsonArray(
    BsonDocument(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "Some Success Criteria ONE",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 1,
      SingleLessonPlanSchema.GROUP_IDS -> BsonArray("group1", "group2")
    ),
    BsonDocument(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "Some Success Criteria TWO",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 2,
      SingleLessonPlanSchema.GROUP_IDS -> BsonArray("group1", "group2")
    ),
    BsonDocument(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "Some Success Criteria THREE",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 3,
      SingleLessonPlanSchema.GROUP_IDS -> BsonArray("group1", "group2")
    ),
    BsonDocument(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "Some Success Criteria FOUR",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 4,
      SingleLessonPlanSchema.GROUP_IDS -> BsonArray("group1", "group2")
    )
  )

  private[dao] val TEST_PLENARIES = BsonArray(
    Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "plenary ONE",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 1
    ),
    Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "plenary TWO",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 2
    ),
    Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "plenary THREE",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 3
    ),
    Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "plenary FOUR",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 4
    ),
    Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "plenary FIVE",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 5
    )
  )

  private[dao] val TEST_FORMATIVE_ASSESSMENT_PER_GROUP = BsonArray(
    BsonDocument(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "Some Formative Assessment ONE",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 1,
      SingleLessonPlanSchema.GROUP_IDS -> BsonArray("group1", "group2")
    ), BsonDocument(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "Some Formative Assessment TWO",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 2,
      SingleLessonPlanSchema.GROUP_IDS -> BsonArray("group1", "group2")
    ), BsonDocument(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "Some Formative Assessment THREE",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 3,
      SingleLessonPlanSchema.GROUP_IDS -> BsonArray("group1", "group2")
    ), BsonDocument(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "Some Formative Assessment FOUR",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 4,
      SingleLessonPlanSchema.GROUP_IDS -> BsonArray("group1", "group2")
    ), BsonDocument(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "Some Formative Assessment FIVE",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 5,
      SingleLessonPlanSchema.GROUP_IDS -> BsonArray("group1", "group2")
    ), BsonDocument(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "Some Formative Assessment SIX",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 6,
      SingleLessonPlanSchema.GROUP_IDS -> BsonArray("group1", "group2")
    )
  )

  private[dao] val TEST_NOTES_BEFORE = BsonArray(
    Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "NoteB ONE",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 1
    ),
    Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "NoteB TWO",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 2
    ), Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "NoteB THREE",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 3
    ), Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "NoteB FOUR",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 4
    ), Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "NoteB FIVE",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 5
    ), Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "NoteB SIX",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 6
    ), Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "NoteB SEVEN",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 7
    ))

  private[dao] val TEST_NOTES_AFTER = BsonArray(
    Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "NoteA ONE",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 1
    ),
    Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "NoteA TWO",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 2
    ),
    Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "NoteA THREE",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 3
    ),
    Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "NoteA FOUR",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 4
    ),
    Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "NoteA FIVE",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 5
    ),
    Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "NoteA SIX",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 6
    ),
    Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "NoteA SEVEN",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 7
    ),
    Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> "NoteA EIGHT",
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> 8
    ))

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


  def oneWeeksWorthOfLessonsForThisSubjectAndStartTime(subject: String, startTime: String, weekBeginning: String): List[Document] = {
    val wkBeginning = LocalDate.parse(weekBeginning)
    val startLocalTime = LocalTime.parse(startTime)
    val endLocalTime = startLocalTime.plusHours(1)

    {
      for {
        dayNumber <- List.range(0, 5)
        lessonDate = wkBeginning.plusDays(dayNumber).toString
        lesson1 = createLessonPlanDocument(subject, weekBeginning, lessonDate, TEST_TIMESTAMP_WEEK1_A, startTime,
          endLocalTime.toString.replace("T", " "), TEST_ACTIVITIES_PER_GROUP, TEST_RESOURCES,
          TEST_LEARNING_INTENTIONS_PER_GROUP, TEST_SUCCESS_CRITERIA_PER_GROUP, TEST_PLENARIES, TEST_FORMATIVE_ASSESSMENT_PER_GROUP,
          TEST_NOTES_BEFORE, TEST_NOTES_AFTER
        )
        lesson2 = createLessonPlanDocument(subject, weekBeginning, lessonDate, TEST_TIMESTAMP_WEEK1_C, startTime,
          endLocalTime.toString.replace("T", " "), TEST_ACTIVITIES_PER_GROUP, TEST_RESOURCES,
          TEST_LEARNING_INTENTIONS_PER_GROUP, TEST_SUCCESS_CRITERIA_PER_GROUP, TEST_PLENARIES, TEST_FORMATIVE_ASSESSMENT_PER_GROUP,
          TEST_NOTES_BEFORE, TEST_NOTES_AFTER
        )
        lesson3 = createLessonPlanDocument(subject, weekBeginning, lessonDate, TEST_TIMESTAMP_WEEK1_E, startTime,
          endLocalTime.toString.replace("T", " "), TEST_ACTIVITIES_PER_GROUP, TEST_RESOURCES,
          TEST_LEARNING_INTENTIONS_PER_GROUP, TEST_SUCCESS_CRITERIA_PER_GROUP, TEST_PLENARIES, TEST_FORMATIVE_ASSESSMENT_PER_GROUP,
          TEST_NOTES_BEFORE, TEST_NOTES_AFTER
        )
        lesson4 = createLessonPlanDocument(subject, weekBeginning, lessonDate, TEST_TIMESTAMP_WEEK1_D, startTime,
          endLocalTime.toString.replace("T", " "), TEST_ACTIVITIES_PER_GROUP, TEST_RESOURCES,
          TEST_LEARNING_INTENTIONS_PER_GROUP, TEST_SUCCESS_CRITERIA_PER_GROUP, TEST_PLENARIES, TEST_FORMATIVE_ASSESSMENT_PER_GROUP,
          TEST_NOTES_BEFORE, TEST_NOTES_AFTER
        )
        lesson5 = createLessonPlanDocument(subject, weekBeginning, lessonDate, TEST_TIMESTAMP_WEEK1_B, startTime,
          endLocalTime.toString.replace("T", " "), TEST_ACTIVITIES_PER_GROUP, TEST_RESOURCES,
          TEST_LEARNING_INTENTIONS_PER_GROUP, TEST_SUCCESS_CRITERIA_PER_GROUP, TEST_PLENARIES, TEST_FORMATIVE_ASSESSMENT_PER_GROUP,
          TEST_NOTES_BEFORE, TEST_NOTES_AFTER
        )
      } yield List(lesson1, lesson2, lesson3, lesson4, lesson5)
    }.flatten
  }

  def oneWeeksWorthOfLessonsForTheseSubjectsAndStartTime(subjects: List[(String, String)], weekBeginning: String): List[Document] = {
    {
      for {
        subject <- subjects
      } yield oneWeeksWorthOfLessonsForThisSubjectAndStartTime(subject._1, subject._2, weekBeginning)
    }.flatten
  }

  def createABunchOfLessonsForVariousSubjects(): List[Document] = {
    val subjectsAndStartTimes = List(
      ("EXPRESSIVE_ARTS__ART", "09:00"),
      ("HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION", "10:00"),
      ("LITERACY__WRITING", "11:00"),
      ("LITERACY__READING", "13:00"),
      ("MATHEMATICS", "14:00")
    )

    oneWeeksWorthOfLessonsForTheseSubjectsAndStartTime(subjectsAndStartTimes, TEST_WEEK1)
  }


  def createSingleMathsLessonPlanDocument(): Document = {
    createLessonPlanDocument(
      TEST_SUBJECT_MATHS,
      TEST_WEEK1,
      TEST_LESSON_DATE2_OF_WEEK1,
      TEST_TIMESTAMP_WEEK1_A,
      TEST_ISO_TIME_0900,
      TEST_ISO_TIME_1000,
      TEST_ACTIVITIES_PER_GROUP,
      TEST_RESOURCES,
      TEST_LEARNING_INTENTIONS_PER_GROUP,
      TEST_SUCCESS_CRITERIA_PER_GROUP,
      TEST_PLENARIES,
      TEST_FORMATIVE_ASSESSMENT_PER_GROUP,
      TEST_NOTES_BEFORE,
      TEST_NOTES_AFTER
    )
  }

  def createLessonPlanDocument(subject: String,
                               weekBeginning: String,
                               lessonDate: String,
                               createdTimestamp: String,
                               lessonStartTime: String,
                               lessonEndTime: String,
                               activitiesPerGroup: BsonArray,
                               resources: BsonArray,
                               learningIntentions: BsonArray,
                               successCriteria: BsonArray,
                               plenaries: BsonArray,
                               formativeAssessments: BsonArray,
                               notesBefore: BsonArray,
                               notesAfter: BsonArray
                              ): Document = {
    Document(
      SingleLessonPlanSchema.TTT_USER_ID -> TEST_USER1,
      SingleLessonPlanSchema.CLASS_ID -> TEST_CLASS1,
      SingleLessonPlanSchema.SUBJECT -> subject,
      SingleLessonPlanSchema.SUBJECT_ADDITIONAL_INFO -> "",
      SingleLessonPlanSchema.WEEK_BEGINNING_ISO_DATE -> weekBeginning,
      SingleLessonPlanSchema.LESSON_DATE -> lessonDate,
      SingleLessonPlanSchema.CREATED_TIMESTAMP -> createdTimestamp,
      SingleLessonPlanSchema.LESSON_START_TIME -> lessonStartTime,
      SingleLessonPlanSchema.LESSON_END_TIME -> lessonEndTime,

      SingleLessonPlanSchema.ACTIVITIES_PER_GROUP -> activitiesPerGroup,
      SingleLessonPlanSchema.RESOURCES -> resources,
      SingleLessonPlanSchema.LEARNING_INTENTIONS_PER_GROUP -> learningIntentions,
      SingleLessonPlanSchema.SUCCESS_CRITERIA_PER_GROUP -> successCriteria,
      SingleLessonPlanSchema.PLENARIES -> plenaries,
      SingleLessonPlanSchema.FORMATIVE_ASSESSMENT_PER_GROUP -> formativeAssessments,
      SingleLessonPlanSchema.NOTES_BEFORE -> notesBefore,
      SingleLessonPlanSchema.NOTES_AFTER -> notesAfter
    )
  }

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
          "including less than, longeroneWeeksWorthOfLessonsForTheseSubjectsAndStartTime than, more than and the same."
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
      WeeklyPlanningSchema.SELECTED_ES_AND_OS -> BsonArray("MNU 0-20a", "MNU 0-20b", "MNU 0-20c"),
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
