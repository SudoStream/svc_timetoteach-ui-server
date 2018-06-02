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

  describe("Given canned & filtered list of 1 COMPLETE, buildMapOfEAndOsBenchmarks()") {
    val completesMap = dao.buildMapOfEAndOsBenchmarks(createOneCompleteDoc())
    it("should create a map with 1 values in it"){
      assert(completesMap.values.size === 1)
    }
  }

  describe("Given canned & filtered list of 2 COMPLETES, buildMapOfEAndOsBenchmarks()") {
    val completesMap = dao.buildMapOfEAndOsBenchmarks(createTwoCompleteDoc())
    it(s"should create a map with 2 es and os for $TEST_SUBJECT_ART $GROUP_ID_CLASS $TEST_ART_SECTION_NAME $TEST_ART_SUBSECTION_NAME"){
      assert(completesMap(TEST_SUBJECT_ART)(GROUP_ID_CLASS)(TEST_ART_SECTION_NAME)(TEST_ART_SUBSECTION_NAME).eAndOs.size === 2)
    }
  }

  describe("Given a selection of es and os /  benchmarks for Art and Maths, buildMapOfEAndOsBenchmarks()") {
    val completesMap = dao.buildMapOfEAndOsBenchmarks(createMixOfEsOsAndBenchmarksDifferentSubjectsComplete())
    it(s"should create a map with 2 es and os for '$TEST_SUBJECT_MATHS' '$GROUP_ID_MATHS1' '$TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE' '$TEST_MATHS_SUBSECTION_SHAPES'"){
      assert(completesMap(TEST_SUBJECT_MATHS)(GROUP_ID_MATHS1)(TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE)(TEST_MATHS_SUBSECTION_SHAPES).eAndOs.size === 2)
    }
    it(s"should create a map with 3 benchmarks for '$TEST_SUBJECT_MATHS' '$GROUP_ID_MATHS1' '$TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE' '$TEST_MATHS_SUBSECTION_SHAPES'"){
      assert(completesMap(TEST_SUBJECT_MATHS)(GROUP_ID_MATHS1)(TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE)(TEST_MATHS_SUBSECTION_SHAPES).benchmarks.size === 3)
    }
    it(s"should create a map with 5 benchmarks for '$TEST_SUBJECT_MATHS' '$GROUP_ID_MATHS1' '$TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE' '$TEST_MATHS_SUBSECTION_MONEY'"){
      assert(completesMap(TEST_SUBJECT_MATHS)(GROUP_ID_MATHS1)(TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE)(TEST_MATHS_SUBSECTION_MONEY).benchmarks.size === 5)
    }
    it(s"should create a map with 2 es and os for '$TEST_SUBJECT_MATHS' '$GROUP_ID_MATHS1' '$TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE' '$TEST_MATHS_SUBSECTION_MONEY'"){
      assert(completesMap(TEST_SUBJECT_MATHS)(GROUP_ID_MATHS1)(TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE)(TEST_MATHS_SUBSECTION_MONEY).eAndOs.size === 2)
    }
    it(s"should create a map with 1 es and os for '$TEST_SUBJECT_MATHS' '$GROUP_ID_MATHS2' '$TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE' '$TEST_MATHS_SUBSECTION_MONEY'"){
      assert(completesMap(TEST_SUBJECT_MATHS)(GROUP_ID_MATHS2)(TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE)(TEST_MATHS_SUBSECTION_MONEY).eAndOs.size === 1)
    }
    it(s"should create a map with 0 benchmarks for '$TEST_SUBJECT_MATHS' '$GROUP_ID_MATHS2' '$TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE' '$TEST_MATHS_SUBSECTION_MONEY'"){
      assert(completesMap(TEST_SUBJECT_MATHS)(GROUP_ID_MATHS2)(TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE)(TEST_MATHS_SUBSECTION_MONEY).benchmarks.isEmpty)
    }
    it(s"should create a map with 2 es and os for '$TEST_SUBJECT_MATHS' '$GROUP_ID_MATHS2' '$TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE' '$TEST_MATHS_SUBSECTION_SHAPES'"){
      assert(completesMap(TEST_SUBJECT_MATHS)(GROUP_ID_MATHS2)(TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE)(TEST_MATHS_SUBSECTION_SHAPES).eAndOs.size === 2)
    }
    it(s"should create a map with 0 benchmarks for '$TEST_SUBJECT_MATHS' '$GROUP_ID_MATHS2' '$TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE' '$TEST_MATHS_SUBSECTION_SHAPES'"){
      assert(completesMap(TEST_SUBJECT_MATHS)(GROUP_ID_MATHS2)(TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE)(TEST_MATHS_SUBSECTION_SHAPES).benchmarks.isEmpty)
    }
    it(s"should create a map with 0 es and os for '$TEST_SUBJECT_MATHS' '$GROUP_ID_MATHS3' '$TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE' '$TEST_MATHS_SUBSECTION_SHAPES'"){
      assert(completesMap(TEST_SUBJECT_MATHS)(GROUP_ID_MATHS3)(TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE)(TEST_MATHS_SUBSECTION_SHAPES).eAndOs.isEmpty)
    }
    it(s"should create a map with 2 benchmarks for '$TEST_SUBJECT_MATHS' '$GROUP_ID_MATHS3' '$TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE' '$TEST_MATHS_SUBSECTION_SHAPES'"){
      assert(completesMap(TEST_SUBJECT_MATHS)(GROUP_ID_MATHS3)(TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE)(TEST_MATHS_SUBSECTION_SHAPES).benchmarks.size === 2)
    }
    it(s"should create a map with 0 es and os for '$TEST_SUBJECT_MATHS' '$GROUP_ID_MATHS3' '$TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE' '$TEST_MATHS_SUBSECTION_MONEY'"){
      assert(completesMap(TEST_SUBJECT_MATHS)(GROUP_ID_MATHS3)(TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE)(TEST_MATHS_SUBSECTION_MONEY).eAndOs.isEmpty)
    }
    it(s"should create a map with 1 benchmark for '$TEST_SUBJECT_MATHS' '$GROUP_ID_MATHS3' '$TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE' '$TEST_MATHS_SUBSECTION_MONEY'"){
      assert(completesMap(TEST_SUBJECT_MATHS)(GROUP_ID_MATHS3)(TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE)(TEST_MATHS_SUBSECTION_MONEY).benchmarks.size === 1)
    }
    it(s"should create a map with 3 es and os for $TEST_SUBJECT_ART $GROUP_ID_CLASS $TEST_ART_SECTION_NAME $TEST_ART_SUBSECTION_NAME"){
      assert(completesMap(TEST_SUBJECT_ART)(GROUP_ID_CLASS)(TEST_ART_SECTION_NAME)(TEST_ART_SUBSECTION_NAME).eAndOs.size === 3)
    }
    it(s"should create a map with 4 benchmarks for $TEST_SUBJECT_ART $GROUP_ID_CLASS $TEST_ART_SECTION_NAME $TEST_ART_SUBSECTION_NAME"){
      assert(completesMap(TEST_SUBJECT_ART)(GROUP_ID_CLASS)(TEST_ART_SECTION_NAME)(TEST_ART_SUBSECTION_NAME).benchmarks.size === 4)
    }
  }
}
