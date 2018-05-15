package potentialmicroservice.planning.reader.dao

import java.time.LocalDateTime

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
  }


}
