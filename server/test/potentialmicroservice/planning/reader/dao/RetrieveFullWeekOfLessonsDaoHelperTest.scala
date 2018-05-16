package potentialmicroservice.planning.reader.dao

import dao.MongoDbConnection
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning.ScottishCurriculumPlanningAreaWrapper
import org.scalatest.FunSpec
import utils.mongodb.MongoDbSafety

class RetrieveFullWeekOfLessonsDaoHelperTest extends FunSpec with RetrieveFullWeekOfLessonsDaoHelperTestCanned {

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
    it ("should have a 'Number, money and measure' section which has 4 subsections"){
      val setSectionSubSectionToEsOsBenchiesMap = dao.createSectionNameToSubSections(
        createSomeLowLevelSectionNameToSubSectionsEsAndOsBsonDocs()
      )
      assert(setSectionSubSectionToEsOsBenchiesMap("Number, money and measure").size === 4)
    }
    it ("should have an 'Information handling' section which has 1 subsection"){
      val setSectionSubSectionToEsOsBenchiesMap = dao.createSectionNameToSubSections(
        createSomeLowLevelSectionNameToSubSectionsEsAndOsBsonDocs()
      )
      assert(setSectionSubSectionToEsOsBenchiesMap("Information handling").size === 1)
    }
    it ("should have an 'Information handling' section with a 'Data and analysis' subsection which has 3 Es & Os"){
      val setSectionSubSectionToEsOsBenchiesMap = dao.createSectionNameToSubSections(
        createSomeLowLevelSectionNameToSubSectionsEsAndOsBsonDocs()
      )
      assert(setSectionSubSectionToEsOsBenchiesMap("Information handling")("Data and analysis").eAndOs.size === 3)
    }
    it ("should have an 'Information handling' section with a 'Data and analysis' subsection which has 2 Benchmarks"){
      val setSectionSubSectionToEsOsBenchiesMap = dao.createSectionNameToSubSections(
        createSomeLowLevelSectionNameToSubSectionsEsAndOsBsonDocs()
      )
      assert(setSectionSubSectionToEsOsBenchiesMap("Information handling")("Data and analysis").benchmarks.size === 2)
    }

  }
}
