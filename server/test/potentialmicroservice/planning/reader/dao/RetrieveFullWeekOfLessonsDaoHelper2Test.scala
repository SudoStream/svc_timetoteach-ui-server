package potentialmicroservice.planning.reader.dao

import dao.MongoDbConnection
import models.timetoteach.{ClassId, TimeToTeachUserId}
import org.scalatest.FunSpec

import scala.concurrent.Await
import scala.concurrent.duration._

class RetrieveFullWeekOfLessonsDaoHelper2Test extends FunSpec with RetrieveFullWeekOfLessonsDaoHelper2TestCanned {

  private val dao = new RetrieveFullWeekOfLessonsDaoHelper {
    override def getDbConnection: MongoDbConnection = null
  }

  describe("When supplied with null list, readAllEAndOsBenchmarksStatuses()") {
    it("should return an empty list") {
      val futureAllEAndOBenchmarksStatuses = dao.readAllEAndOsBenchmarksStatuses(null, ClassId(TEST_CLASSA))
      val allEAndOBenchmarksStatuses = Await.result(futureAllEAndOBenchmarksStatuses, 1.second)
      assert(allEAndOBenchmarksStatuses.isEmpty)
    }
  }

  describe("When supplied with list of 5 versions of an art E and O, readAllEAndOsBenchmarksStatuses()") {
    val futureAllEAndOBenchmarksStatuses = dao.readAllEAndOsBenchmarksStatuses(TimeToTeachUserId(TEST_USER1), ClassId(TEST_CLASSA))
    val allEAndOBenchmarksStatuses = Await.result(futureAllEAndOBenchmarksStatuses, 1.second)

    it("should return an empty list") {

      assert(allEAndOBenchmarksStatuses.isEmpty)
    }
  }


}
