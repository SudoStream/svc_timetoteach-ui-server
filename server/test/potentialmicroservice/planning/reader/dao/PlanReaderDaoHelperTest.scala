package potentialmicroservice.planning.reader.dao

import models.timetoteach.planning.PlanType
import org.scalatest.FunSpec

class PlanReaderDaoHelperTest extends FunSpec
{
  private val planReaderDao = new PlanReaderDaoSubjectTermlyPlanHelper
  {
  }

  describe("When given an empty list of mongo docs, findLatestVersionOfTermlyPlan()") {
    it("should return None") {
      val maybeSubjectTermlyPlan = planReaderDao.findLatestVersionOfTermlyPlan(Nil)
      assert(maybeSubjectTermlyPlan === None)
    }
  }

  describe("When a list of one single document is provided, findLatestVersionOfTermlyPlanDocLoop()") {
    it("should return the id 5a7e0f39e9ff0d6d4d390e3c") {
      val document = planReaderDao.findLatestVersionOfTermlyPlanDocLoop(Nil, PlanReaderDaoHelperTestHelper.provideSingleDocument())
      assert(document.getString("_id") === "5a7e0f39e9ff0d6d4d390e3c")
    }
  }

  describe("When a list of several documents is provided, findLatestVersionOfTermlyPlanDocLoop()") {
    it("should return a defined option") {
      val document = planReaderDao.findLatestVersionOfTermlyPlanDocLoop(
        PlanReaderDaoHelperTestHelper.createAListOfSeveralDocumentsMixedUp().tail,
        PlanReaderDaoHelperTestHelper.createAListOfSeveralDocumentsMixedUp().head
      )
      println(s"Document we think is latest has id = '${document.getString("_id")}'")
      assert(document.getString("_id") === "a21029985165479d9a049551")
    }
  }

  describe("Given a correctly formed termly mongo db document, convertDocumentToSubjectTermlyPlan()") {
    it("should create a defined SubjectTermlyPlan") {
      val maybeTermlyPlan = planReaderDao.convertDocumentToSubjectTermlyPlan(PlanReaderDaoHelperTestHelper.provideSingleDocument())
      assert(maybeTermlyPlan.isDefined)
    }
    it("should create a SubjectTermlyPlan with a plan type of Group") {
      val maybeTermlyPlan = planReaderDao.convertDocumentToSubjectTermlyPlan(PlanReaderDaoHelperTestHelper.provideSingleDocument())
      assert(maybeTermlyPlan.get.planType === PlanType.GROUP_LEVEL_PLAN)
    }
    it("should create a SubjectTermlyPlan with a group id of groupId_f842e787-cc90-483f-a321-49b68a252a80") {
      val maybeTermlyPlan = planReaderDao.convertDocumentToSubjectTermlyPlan(PlanReaderDaoHelperTestHelper.provideSingleDocument())
      assert(maybeTermlyPlan.get.maybeGroupId.get.value === "groupId_f842e787-cc90-483f-a321-49b68a252a80")
    }
    it("should create a SubjectTermlyPlan with a class id of classId_1b22d43d-5585-47b4-bb41-94f26d3edba5") {
      val maybeTermlyPlan = planReaderDao.convertDocumentToSubjectTermlyPlan(PlanReaderDaoHelperTestHelper.provideSingleDocument())
      assert(maybeTermlyPlan.get.classId.value === "classId_1b22d43d-5585-47b4-bb41-94f26d3edba5")
    }
    it("should create a SubjectTermlyPlan with a list of 3 esAndOsWithBenchmarks") {
      val maybeTermlyPlan = planReaderDao.convertDocumentToSubjectTermlyPlan(PlanReaderDaoHelperTestHelper.provideSingleDocument())
      assert(maybeTermlyPlan.get.eAndOsWithBenchmarks.size === 3)
    }

  }

  describe("When given a list of several mongo docs, findLatestVersionOfTermlyPlan()") {
    it("should return a defined option") {
      val maybeSubjectTermlyPlan = planReaderDao.findLatestVersionOfTermlyPlan(PlanReaderDaoHelperTestHelper.createAListOfSeveralDocumentsMixedUp())
      assert(maybeSubjectTermlyPlan.isDefined)
    }
  }

}
