package potentialmicroservice.planning.reader.dao

import dao.MongoDbConnection
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


}
