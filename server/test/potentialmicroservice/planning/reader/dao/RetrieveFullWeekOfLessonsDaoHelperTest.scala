package potentialmicroservice.planning.reader.dao

import java.time.LocalDate

import dao.MongoDbConnection
import duplicate.model.planning.AttributeRowKey
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning.ScottishCurriculumPlanningAreaWrapper
import org.scalatest.FunSpec
import utils.mongodb.MongoDbSafety

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class RetrieveFullWeekOfLessonsDaoHelperTest extends FunSpec with RetrieveFullWeekOfLessonsDaoHelperTestCanned {
  override def getSystemDate: Future[LocalDate] = Future{
    LocalDate.of(2018,4,30)
  }
  val TEST_USER = "tttUser12345"
  val TEST_CLASS_ID = "classIdAbcde"
  val TEST_WEEK_BEGINNING = "2018-05-14"

  private val dao = new RetrieveFullWeekOfLessonsDaoHelper {
    override def getDbConnection: MongoDbConnection = null
  }

  describe("When supplied with null list, convertPlanAllSubjectsForTheWeekToModel()") {
    it("should return an empty list") {
      val res = dao.convertPlanAllSubjectsForTheWeekToModel(null)
      assert(res.isEmpty)
    }
  }

  describe("When supplied with an empty list, convertPlanAllSubjectsForTheWeekToModel()") {
    it("should return an empty list") {
      val res = dao.convertPlanAllSubjectsForTheWeekToModel(Nil)
      assert(res.isEmpty)
    }
  }

  describe("When supplied with a list of art lessons for over 3 weeks, convertPlanAllSubjectsForTheWeekToModel()") {
    it("should return a non-empty list") {
      val res = dao.convertPlanAllSubjectsForTheWeekToModel(createSomeArtHighLevelPlans())
      assert(res.nonEmpty)
    }
    it("should return a list of size 5") {
      val res = dao.convertPlanAllSubjectsForTheWeekToModel(createSomeArtHighLevelPlans())
      assert(res.size === 5)
    }
    it("should return a list which contains one of the week two timestamps") {
      val res = dao.convertPlanAllSubjectsForTheWeekToModel(createSomeArtHighLevelPlans())
      val OneOfTheWeekTwoTimestamps = MongoDbSafety.safelyParseTimestamp(TEST_TIMESTAMP_WEEK2_A)
      assert(res.count(elem => elem.timestamp.isEqual(OneOfTheWeekTwoTimestamps)) === 1)
    }
    it("should return a list which all contain 1 selected Es And Os group") {
      val res = dao.convertPlanAllSubjectsForTheWeekToModel(createSomeArtHighLevelPlans())
      assert(res.count(elem => elem.selectedEsOsBenchmarksByGroup.keys.size == 1) === 5)
    }
    it("should return a list which all contain 2 elements with 2 benchmarks") {
      val res = dao.convertPlanAllSubjectsForTheWeekToModel(createSomeArtHighLevelPlans())
      assert(res.count(elem => elem.selectedEsOsBenchmarksByGroup.head._2.setSectionNameToSubSections.head._2.head._2.benchmarks.size == 2) === 2)
    }
    it("should return a list which all contain 1 elements with 0 benchmarks") {
      val res = dao.convertPlanAllSubjectsForTheWeekToModel(createSomeArtHighLevelPlans())
      assert(res.count(elem => elem.selectedEsOsBenchmarksByGroup.head._2.setSectionNameToSubSections.head._2.head._2.benchmarks.isEmpty) === 1)
    }
    it("should return a list which all contain 2 elements with 1 benchmarks") {
      val res = dao.convertPlanAllSubjectsForTheWeekToModel(createSomeArtHighLevelPlans())
      assert(res.count(elem => elem.selectedEsOsBenchmarksByGroup.head._2.setSectionNameToSubSections.head._2.head._2.benchmarks.size == 1) === 2)
    }

  }


  describe("When supplied with a list of art lessons for over 3 weeks, latestValueForEachSubject()") {
    it("should return a non empty list") {
      val variousArtCandidates = dao.convertPlanAllSubjectsForTheWeekToModel(createSomeArtHighLevelPlans())
      val latestVersions = dao.latestValueForEachSubject(variousArtCandidates)
      assert(latestVersions.nonEmpty)
    }
    it("should return a list of 1 element") {
      val variousArtCandidates = dao.convertPlanAllSubjectsForTheWeekToModel(createSomeArtHighLevelPlans())
      val latestVersions = dao.latestValueForEachSubject(variousArtCandidates)
      assert(latestVersions.size === 1)
    }
    it(s"should return a list of 1 element with the timestamp value of $TEST_TIMESTAMP_WEEK2_E") {
      val variousArtCandidates = dao.convertPlanAllSubjectsForTheWeekToModel(createSomeArtHighLevelPlans())
      val latestVersions = dao.latestValueForEachSubject(variousArtCandidates)

      val expectedTimestamp = MongoDbSafety.safelyParseTimestamp(TEST_TIMESTAMP_WEEK2_E)

      assert(latestVersions.head.timestamp === expectedTimestamp)
    }
  }

  describe("When supplied with a list of art & mathematics lessons for over 3 weeks, latestValueForEachSubject()") {
    it("should return a non empty list") {
      val variousSubjectCandidates = dao.convertPlanAllSubjectsForTheWeekToModel(createSomeArtHighLevelPlans() ::: createSomeMathsHighLevelPlans())
      val latestVersions = dao.latestValueForEachSubject(variousSubjectCandidates)
      assert(latestVersions.nonEmpty)
    }
    it("should return a list with 2 elements in it") {
      val variousSubjectCandidates = dao.convertPlanAllSubjectsForTheWeekToModel(createSomeArtHighLevelPlans() ::: createSomeMathsHighLevelPlans())
      val latestVersions = dao.latestValueForEachSubject(variousSubjectCandidates)
      assert(latestVersions.size === 2)
    }
    it(s"should return a list with the maths element having a timestamp of $TEST_TIMESTAMP_WEEK2_C") {
      val variousSubjectCandidates = dao.convertPlanAllSubjectsForTheWeekToModel(createSomeArtHighLevelPlans() ::: createSomeMathsHighLevelPlans())
      val latestVersions = dao.latestValueForEachSubject(variousSubjectCandidates)
      val maths = latestVersions.filter(elem => elem.subject == ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.MATHEMATICS))

      val expectedTimestamp = MongoDbSafety.safelyParseTimestamp(TEST_TIMESTAMP_WEEK2_C)

      assert(maths.head.timestamp === expectedTimestamp)
    }
    it(s"should return a list with the maths element having 3 groups") {
      val variousSubjectCandidates = dao.convertPlanAllSubjectsForTheWeekToModel(createSomeArtHighLevelPlans() ::: createSomeMathsHighLevelPlans())
      val latestVersions = dao.latestValueForEachSubject(variousSubjectCandidates)
      val maths = latestVersions.filter(elem => elem.subject == ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.MATHEMATICS))

      println(s"Hello :=> ${maths.head.selectedEsOsBenchmarksByGroup.toString()}")
      assert(maths.head.selectedEsOsBenchmarksByGroup.keys.size === 3)
    }
  }

  describe("Given 5 valid BsonDocuments with set sections & subsections plus es and os, createSectionNameToSubSections()") {
    it("should return a map with 2 keys") {
      val setSectionSubSectionToEsOsBenchiesMap = dao.createSectionNameToSubSections(
        createSomeLowLevelSectionNameToSubSectionsEsAndOsBsonDocs()
      )
      assert(setSectionSubSectionToEsOsBenchiesMap.keys.size === 2)
    }
    it("should have a 'Number, money and measure' section which has 4 subsections") {
      val setSectionSubSectionToEsOsBenchiesMap = dao.createSectionNameToSubSections(
        createSomeLowLevelSectionNameToSubSectionsEsAndOsBsonDocs()
      )
      assert(setSectionSubSectionToEsOsBenchiesMap("Number, money and measure").size === 4)
    }
    it("should have an 'Information handling' section which has 1 subsection") {
      val setSectionSubSectionToEsOsBenchiesMap = dao.createSectionNameToSubSections(
        createSomeLowLevelSectionNameToSubSectionsEsAndOsBsonDocs()
      )
      assert(setSectionSubSectionToEsOsBenchiesMap("Information handling").size === 1)
    }
    it("should have an 'Information handling' section with a 'Data and analysis' subsection which has 3 Es & Os") {
      val setSectionSubSectionToEsOsBenchiesMap = dao.createSectionNameToSubSections(
        createSomeLowLevelSectionNameToSubSectionsEsAndOsBsonDocs()
      )
      assert(setSectionSubSectionToEsOsBenchiesMap("Information handling")("Data and analysis").eAndOs.size === 3)
    }
    it("should have an 'Information handling' section with a 'Data and analysis' subsection which has 2 Benchmarks") {
      val setSectionSubSectionToEsOsBenchiesMap = dao.createSectionNameToSubSections(
        createSomeLowLevelSectionNameToSubSectionsEsAndOsBsonDocs()
      )
      assert(setSectionSubSectionToEsOsBenchiesMap("Information handling")("Data and analysis").benchmarks.size === 2)
    }
  }

  describe("Given an empty list of documents, convertLessonPlansForTheWeekToModel()") {
    it("should return an empty list") {
      val lessonPlans = dao.convertLessonPlansForTheWeekToModel(Nil)
      assert(lessonPlans.isEmpty)
    }
  }

  describe("Given a single valid document, convertDocumentToLessonPlan()") {
    val lessonPlan = dao.convertDocumentToLessonPlan(createSingleMathsLessonPlanDocument())
    it("should return a lesson plan with a subject of 'MATHEMATICS'") {
      assert(lessonPlan.subject === "MATHEMATICS")
    }
    it(s"should return a lesson plan with a week beginning iso date of '$TEST_WEEK1'") {
      assert(lessonPlan.weekBeginningIsoDate === TEST_WEEK1)
    }
    it(s"should return a lesson plan with a lesson date of '$TEST_LESSON_DATE2_OF_WEEK1'") {
      assert(lessonPlan.lessonDateIso === TEST_LESSON_DATE2_OF_WEEK1)
    }
    it(s"should return a lesson plan with a lesson start time of '$TEST_ISO_TIME_0900'") {
      assert(lessonPlan.startTimeIso === TEST_ISO_TIME_0900)
    }
    it(s"should return a lesson plan with a lesson end time of '$TEST_ISO_TIME_1000'") {
      assert(lessonPlan.endTimeIso === TEST_ISO_TIME_1000)
    }

    it(s"should return a lesson plan with an activities group which contains 'Some activity ONE'") {
      assert(lessonPlan.activitiesPerGroup.isDefinedAt(AttributeRowKey("Some activity ONE",1)))
    }
    it(s"should return a lesson plan with an activities group which contains 'Some activity ONE' with 2 groups") {
      assert(lessonPlan.activitiesPerGroup(AttributeRowKey("Some activity ONE",1)).size === 2)
    }

    it(s"should return a lesson plan with resources called resourceA") {
      assert(lessonPlan.resources.contains(AttributeRowKey("resourceA",1)))
    }
    it(s"should return a lesson plan with resources called resourceB") {
      assert(lessonPlan.resources.contains(AttributeRowKey("resourceB",2)))
    }
    it(s"should return a lesson plan with resources size == 2") {
      assert(lessonPlan.resources.size === 2)
    }

    it(s"should return a lesson plan with learning intentions group which contains 'Some Learning Intention ONE'") {
      assert(lessonPlan.learningIntentionsPerGroup.isDefinedAt(AttributeRowKey("Some Learning Intention ONE",1)))
    }
    it(s"should return a lesson plan with learning intentions size 3") {
      assert(lessonPlan.learningIntentionsPerGroup.size === 3)
    }
    it(s"should return a lesson plan with success criteria which contains 'Some Success Criteria FOUR'") {
      assert(lessonPlan.successCriteriaPerGroup.isDefinedAt(AttributeRowKey("Some Success Criteria FOUR",4)))
    }
    it(s"should return a lesson plan with success criteria size 4") {
      assert(lessonPlan.successCriteriaPerGroup.size === 4)
    }
    it(s"should return a lesson plan with plenary called 'plenary FIVE'") {
      assert(lessonPlan.plenary.contains(AttributeRowKey("plenary FIVE",5)))
    }
    it(s"should return a lesson plan with plenary size == 5") {
      assert(lessonPlan.plenary.size === 5)
    }
    it(s"should return a lesson plan with formativeAssessments which contains 'Some Formative Assessment SIX'") {
      assert(lessonPlan.formativeAssessmentPerGroup.isDefinedAt(AttributeRowKey("Some Formative Assessment SIX",6)))
    }
    it(s"should return a lesson plan with formativeAssessmentPerGroup size 4") {
      assert(lessonPlan.formativeAssessmentPerGroup.size === 6)
    }
    it(s"should return a lesson plan with notes before called 'NoteB SEVEN'") {
      assert(lessonPlan.notesBefore.contains(AttributeRowKey("NoteB SEVEN",7)))
    }
    it(s"should return a lesson plan with notes before size == 7") {
      assert(lessonPlan.notesBefore.size === 7)
    }
    it(s"should return a lesson plan with notes after called 'NoteA EIGHT'") {
      assert(lessonPlan.notesAfter.contains(AttributeRowKey("NoteA EIGHT",8)))
    }
    it(s"should return a lesson plan with notes after size == 8") {
      assert(lessonPlan.notesAfter.size === 8)
    }
  }

  describe("Given a list of one document, convertLessonPlansForTheWeekToModel()") {
    it("should return a list of ONE lesson plan") {
      val lessonPlans = dao.convertLessonPlansForTheWeekToModel(createSingleMathsLessonPlanDocument() :: Nil)
      assert(lessonPlans.size === 1)
    }
  }

  describe("Given a list of one lesson plan, buildSubjectToLatestLessonsForTheWeekMap()") {
    val lessonPlans = dao.convertLessonPlansForTheWeekToModel(createSingleMathsLessonPlanDocument() :: Nil)
    val latestLessonsForTheWeek = dao.buildSubjectToLatestLessonsForTheWeekMap(lessonPlans)

    it("should return a map with one key") {
      assert(latestLessonsForTheWeek.keys.size === 1)
    }
  }

  describe("Given A Week Of Lessons For 5 Subjects, buildSubjectToLatestLessonsForTheWeekMap()") {
    val lessonPlans = dao.convertLessonPlansForTheWeekToModel(createABunchOfLessonsForVariousSubjects())
    val latestLessonsForTheWeek = dao.buildSubjectToLatestLessonsForTheWeekMap(lessonPlans)

    it("should return a non empty map") {
      assert(latestLessonsForTheWeek.nonEmpty)
    }
    it("should return a map with five keys") {
      assert(latestLessonsForTheWeek.keys.size === 5)
    }
    it("should contain 'EXPRESSIVE_ARTS__ART'"){
      assert(latestLessonsForTheWeek.isDefinedAt(ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__ART)))
    }
    it("should contain 'HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION'"){
      println(latestLessonsForTheWeek.keys.toString())
      assert(latestLessonsForTheWeek.isDefinedAt(ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION)))
    }
    it("should contain 'LITERACY__WRITING'"){
      assert(latestLessonsForTheWeek.isDefinedAt(ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.LITERACY__WRITING)))
    }
    it("should contain 'LITERACY__READING'"){
      assert(latestLessonsForTheWeek.isDefinedAt(ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.LITERACY__READING)))
    }
    it("should contain 'MATHEMATICS'"){
      assert(latestLessonsForTheWeek.isDefinedAt(ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.MATHEMATICS)))
    }

    it("should return a map which has five values for every key") {
      val numberOf5Lessons = latestLessonsForTheWeek.values.count { lessons =>
        lessons.size == 5
      }
      assert(numberOf5Lessons === 5)
    }
    it(s"should have all 25 lessons in total") {
       assert(latestLessonsForTheWeek.values.toList.flatten.size === 25)
    }
    it(s"should have all lesson values with a timestamp of $TEST_TIMESTAMP_WEEK1_E") {
      val allLessons = latestLessonsForTheWeek.values.toList.flatten
      assert(allLessons.count(elem => elem.createdTimestamp === TEST_TIMESTAMP_WEEK1_E) === 25)
    }
  }
}
