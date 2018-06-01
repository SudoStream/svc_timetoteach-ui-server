package potentialmicroservice.planning.reader.dao

import dao.MongoDbConnection
import models.timetoteach.{ClassId, TimeToTeachUserId}
import org.scalatest.FunSpec
import potentialmicroservice.planning.sharedschema.EsAndOsStatusSchema

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

  describe("When supplied with list of 5 versions of an art E and O, latestVersionOfEachEandOBenchmark()") {
    val allEAndOBenchmarksStatuses = dao.latestVersionOfEachEandOBenchmark(create1ArtEAndOsWith5StatusesLatestComplete())

    it("should return a list of 1 item") {
      assert(allEAndOBenchmarksStatuses.size === 1)
    }
    it("should have its 1 item marked COMPLETE") {
      assert(allEAndOBenchmarksStatuses.head.getString(EsAndOsStatusSchema.STATUS) === "COMPLETE")
    }
    it(s"should have its 1 item with a timestamp of $TEST_TIMESTAMP_WEEK1_E") {
      assert(allEAndOBenchmarksStatuses.head.getString(EsAndOsStatusSchema.CREATED_TIMESTAMP) === TEST_TIMESTAMP_WEEK1_E)
    }
  }

  describe("When supplied with list of various versions of 3 art E and O, latestVersionOfEachEandOBenchmark()") {
    val allEAndOBenchmarksStatuses = dao.latestVersionOfEachEandOBenchmark(create3ArtEAndOsStatusesWithVariousVersions())

    it("should return a list of 3 item") {
      assert(allEAndOBenchmarksStatuses.size === 3)
    }
    it("should have 2 COMPLETE's") {
      assert(allEAndOBenchmarksStatuses.count(elem => elem.getString(EsAndOsStatusSchema.STATUS) == "COMPLETE") === 2)
    }
    it("should have 1 NOT_STARTED") {
      assert(allEAndOBenchmarksStatuses.count(elem => elem.getString(EsAndOsStatusSchema.STATUS) == "NOT_STARTED") === 1)
    }
    it(s"should have 2 item with a timestamp of $TEST_TIMESTAMP_WEEK1_E") {
      assert(allEAndOBenchmarksStatuses.count(elem => elem.getString(EsAndOsStatusSchema.CREATED_TIMESTAMP) == TEST_TIMESTAMP_WEEK1_E) === 2)
    }
    it(s"should have 1 item with a timestamp of $TEST_TIMESTAMP_WEEK1_B") {
      assert(allEAndOBenchmarksStatuses.count(elem => elem.getString(EsAndOsStatusSchema.CREATED_TIMESTAMP) == TEST_TIMESTAMP_WEEK1_B) === 1)
    }

  }

  describe("Given a list of 3 items, with 2 COMPLETES in it, filterForCompleted()"){
    val allEAndOBenchmarksStatuses = dao.latestVersionOfEachEandOBenchmark(create3ArtEAndOsStatusesWithVariousVersions())
    val completes = dao.filterForCompleted(allEAndOBenchmarksStatuses)

    it("should have a list of 2 items"){
      assert(completes.size === 2)
    }
    it("should have a no items that are not COMPLETE"){
      assert(completes.count(elem => elem.getString(EsAndOsStatusSchema.STATUS) != "COMPLETE") === 0)
    }
  }

}
