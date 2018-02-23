package potentialmicroservice.planning.reader.dao

import models.timetoteach.planning.PlanType
import org.scalatest.FunSpec
import potentialmicroservice.planning.reader.dao.PlanReaderDaoSubjectTermlyPlanHelper.CLASS_LEVEL
import potentialmicroservice.planning.sharedschema.TermlyPlanningSchema

class PlanReaderDaoSubjectTermlyPlanHelperTest extends FunSpec
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
      val document = planReaderDao.findLatestVersionOfTermlyPlanDocLoop(Nil, PlanReaderDaoHelperTermlyPlanTestHelper.provideSingleTermlyPlanDocument())
      assert(document.getString("_id") === "5a7e0f39e9ff0d6d4d390e3c")
    }
  }

  describe("When a list of several documents is provided, findLatestVersionOfTermlyPlanDocLoop()") {
    it("should return a defined option") {
      val document = planReaderDao.findLatestVersionOfTermlyPlanDocLoop(
        PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsMixedUp().tail,
        PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsMixedUp().head
      )
      println(s"Document we think is latest has id = '${document.getString("_id")}'")
      assert(document.getString("_id") === "a21029985165479d9a049551")
    }
  }

  describe("Given a correctly formed termly mongo db document, convertDocumentToSubjectTermlyPlan()") {
    it("should create a defined SubjectTermlyPlan") {
      val maybeTermlyPlan = planReaderDao.convertDocumentToSubjectTermlyPlan(PlanReaderDaoHelperTermlyPlanTestHelper.provideSingleTermlyPlanDocument())
      assert(maybeTermlyPlan.isDefined)
    }
    it("should create a SubjectTermlyPlan with a plan type of Group") {
      val maybeTermlyPlan = planReaderDao.convertDocumentToSubjectTermlyPlan(PlanReaderDaoHelperTermlyPlanTestHelper.provideSingleTermlyPlanDocument())
      assert(maybeTermlyPlan.get.planType === PlanType.GROUP_LEVEL_PLAN)
    }
    it("should create a SubjectTermlyPlan with a group id of groupId_f842e787-cc90-483f-a321-49b68a252a80") {
      val maybeTermlyPlan = planReaderDao.convertDocumentToSubjectTermlyPlan(PlanReaderDaoHelperTermlyPlanTestHelper.provideSingleTermlyPlanDocument())
      assert(maybeTermlyPlan.get.maybeGroupId.get.value === "groupId_f842e787-cc90-483f-a321-49b68a252a80")
    }
    it("should create a SubjectTermlyPlan with a class id of classId_1b22d43d-5585-47b4-bb41-94f26d3edba5") {
      val maybeTermlyPlan = planReaderDao.convertDocumentToSubjectTermlyPlan(PlanReaderDaoHelperTermlyPlanTestHelper.provideSingleTermlyPlanDocument())
      assert(maybeTermlyPlan.get.classId.value === "classId_1b22d43d-5585-47b4-bb41-94f26d3edba5")
    }
    it("should create a SubjectTermlyPlan with a list of 3 esAndOsWithBenchmarks") {
      val maybeTermlyPlan = planReaderDao.convertDocumentToSubjectTermlyPlan(PlanReaderDaoHelperTermlyPlanTestHelper.provideSingleTermlyPlanDocument())
      assert(maybeTermlyPlan.get.eAndOsWithBenchmarks.size === 3)
    }

  }

  describe("When given a list of several mongo docs, findLatestVersionOfTermlyPlan()") {
    it("should return a defined option") {
      val maybeSubjectTermlyPlan = planReaderDao.findLatestVersionOfTermlyPlan(PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsMixedUp())
      assert(maybeSubjectTermlyPlan.isDefined)
    }
  }

  /////////////

  describe("When given an empty list, buildCurriculumPlanProgressForClassLoop()") {
    it("sholud returns an empty map") {
      val curriculumPlanningAreaToLatestDoc = planReaderDao.buildCurriculumPlanProgressForClassLoop(Nil, Map())
      assert(curriculumPlanningAreaToLatestDoc.isEmpty)
    }
  }

  describe("Given a mix of maths, reading and art progress buildCurriculumPlanProgressForClassLoop()") {
    it("should return a nonempty map") {
      val curriculumPlanningAreaToLatestDoc = planReaderDao.buildCurriculumPlanProgressForClassLoop(
        PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsWithGroupsMixedUpWithDifferentCurriculumPlanningAreas(), Map())
      assert(curriculumPlanningAreaToLatestDoc.nonEmpty)
    }
    it("should return a map with 3 keys") {
      val curriculumPlanningAreaToLatestDoc = planReaderDao.buildCurriculumPlanProgressForClassLoop(
        PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsWithGroupsMixedUpWithDifferentCurriculumPlanningAreas(), Map())
      assert(curriculumPlanningAreaToLatestDoc.keySet.size === 3)
    }
    it("should return a map with a 'MATHEMATICS' key") {
      val curriculumPlanningAreaToLatestDoc = planReaderDao.buildCurriculumPlanProgressForClassLoop(
        PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsWithGroupsMixedUpWithDifferentCurriculumPlanningAreas(), Map())
      assert(curriculumPlanningAreaToLatestDoc.isDefinedAt(_CurriculumAreaName("MATHEMATICS")))
    }
    it("should return a map with a 'EXPRESSIVE_ARTS__ART' key") {
      val curriculumPlanningAreaToLatestDoc = planReaderDao.buildCurriculumPlanProgressForClassLoop(
        PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsWithGroupsMixedUpWithDifferentCurriculumPlanningAreas(), Map())
      assert(curriculumPlanningAreaToLatestDoc.isDefinedAt(_CurriculumAreaName("EXPRESSIVE_ARTS__ART")))
    }
    it("should return a map with a 'LITERACY__WRITING' key") {
      val curriculumPlanningAreaToLatestDoc = planReaderDao.buildCurriculumPlanProgressForClassLoop(
        PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsWithGroupsMixedUpWithDifferentCurriculumPlanningAreas(), Map())
      assert(curriculumPlanningAreaToLatestDoc.isDefinedAt(_CurriculumAreaName("LITERACY__WRITING")))
    }
    it("should not have a 'TOODLY_DOO' key") {
      val curriculumPlanningAreaToLatestDoc = planReaderDao.buildCurriculumPlanProgressForClassLoop(
        PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsWithGroupsMixedUpWithDifferentCurriculumPlanningAreas(), Map())
      assert(!curriculumPlanningAreaToLatestDoc.isDefinedAt(_CurriculumAreaName("TOODLY_DOO")))
    }
    it("should have 1 maths groups") {
      val curriculumPlanningAreaToLatestDoc = planReaderDao.buildCurriculumPlanProgressForClassLoop(
        PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsWithGroupsMixedUpWithDifferentCurriculumPlanningAreas(), Map())
      assert(curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("MATHEMATICS")).keys.size === 1)
    }
    it("should have a latest maths group doc with a group id of groupId_f842e787-cc90-483f-a321-49b68a252a80") {
      val curriculumPlanningAreaToLatestDoc = planReaderDao.buildCurriculumPlanProgressForClassLoop(
        PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsWithGroupsMixedUpWithDifferentCurriculumPlanningAreas(), Map())
      assert(curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("MATHEMATICS")).isDefinedAt(_GroupId("groupId_f842e787-cc90-483f-a321-49b68a252a80")))
    }
    it("should have a latest maths group doc with an id of a21029985165479d9a049551") {
      val curriculumPlanningAreaToLatestDoc = planReaderDao.buildCurriculumPlanProgressForClassLoop(
        PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsWithGroupsMixedUpWithDifferentCurriculumPlanningAreas(), Map())
      val latestMathsGroupDoc = curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("MATHEMATICS"))(_GroupId("groupId_f842e787-cc90-483f-a321-49b68a252a80"))

      assert(latestMathsGroupDoc.getString(TermlyPlanningSchema._ID) === "a21029985165479d9a049551")
    }

    it("should have 3 writing groups") {
      val curriculumPlanningAreaToLatestDoc = planReaderDao.buildCurriculumPlanProgressForClassLoop(
        PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsWithGroupsMixedUpWithDifferentCurriculumPlanningAreas(), Map())
      assert(curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("LITERACY__WRITING")).keys.size === 3)
    }
    it("should have 1 writing group with group id 'groupId_writing_one'") {
      val curriculumPlanningAreaToLatestDoc = planReaderDao.buildCurriculumPlanProgressForClassLoop(
        PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsWithGroupsMixedUpWithDifferentCurriculumPlanningAreas(), Map())
      assert(curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("LITERACY__WRITING")).isDefinedAt(_GroupId("groupId_writing_one")))
    }
    it("should have 1 writing group with group id 'groupId_writing_one' which has a latest doc id of '123pdefb5a9a109ed704l321'") {
      val curriculumPlanningAreaToLatestDoc = planReaderDao.buildCurriculumPlanProgressForClassLoop(
        PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsWithGroupsMixedUpWithDifferentCurriculumPlanningAreas(), Map())
      val latestArtGroupDoc = curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("LITERACY__WRITING"))(_GroupId("groupId_writing_one"))
      assert(latestArtGroupDoc.getString(TermlyPlanningSchema._ID) === "123pdefb5a9a109ed704l321")
    }
    it("should have 1 writing group with group id 'groupId_writing_two'") {
      val curriculumPlanningAreaToLatestDoc = planReaderDao.buildCurriculumPlanProgressForClassLoop(
        PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsWithGroupsMixedUpWithDifferentCurriculumPlanningAreas(), Map())
      assert(curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("LITERACY__WRITING")).isDefinedAt(_GroupId("groupId_writing_two")))
    }
    it("should have 1 writing group with group id 'groupId_writing_two' which has a latest doc id of 'qq2pdefb5a9a109ed7042222'") {
      val curriculumPlanningAreaToLatestDoc = planReaderDao.buildCurriculumPlanProgressForClassLoop(
        PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsWithGroupsMixedUpWithDifferentCurriculumPlanningAreas(), Map())
      val latestArtGroupDoc = curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("LITERACY__WRITING"))(_GroupId("groupId_writing_two"))
      assert(latestArtGroupDoc.getString(TermlyPlanningSchema._ID) === "qq2pdefb5a9a109ed7042222")
    }
    it("should have 1 writing group with group id 'groupId_writing_three'") {
      val curriculumPlanningAreaToLatestDoc = planReaderDao.buildCurriculumPlanProgressForClassLoop(
        PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsWithGroupsMixedUpWithDifferentCurriculumPlanningAreas(), Map())
      assert(curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("LITERACY__WRITING")).isDefinedAt(_GroupId("groupId_writing_three")))
    }
    it("should have 1 writing group with group id 'groupId_writing_three' which has a latest doc id of 'ee2pdefb5a9a109ed704laee'") {
      val curriculumPlanningAreaToLatestDoc = planReaderDao.buildCurriculumPlanProgressForClassLoop(
        PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsWithGroupsMixedUpWithDifferentCurriculumPlanningAreas(), Map())
      val latestArtGroupDoc = curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("LITERACY__WRITING"))(_GroupId("groupId_writing_three"))
      assert(latestArtGroupDoc.getString(TermlyPlanningSchema._ID) === "ee2pdefb5a9a109ed704laee")
    }

    it("should have 1 EXPRESSIVE_ARTS__ART group") {
      val curriculumPlanningAreaToLatestDoc = planReaderDao.buildCurriculumPlanProgressForClassLoop(
        PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsWithGroupsMixedUpWithDifferentCurriculumPlanningAreas(), Map())
      assert(curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("EXPRESSIVE_ARTS__ART")).keys.size === 1)
    }
    it("should have 1 EXPRESSIVE_ARTS__ART group called CLASS_LEVEL ") {
      import PlanReaderDaoSubjectTermlyPlanHelper.CLASS_LEVEL
      val curriculumPlanningAreaToLatestDoc = planReaderDao.buildCurriculumPlanProgressForClassLoop(
        PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsWithGroupsMixedUpWithDifferentCurriculumPlanningAreas(), Map())
      assert(curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("EXPRESSIVE_ARTS__ART")).isDefinedAt(_GroupId(CLASS_LEVEL)))
    }
    it("should have 1 EXPRESSIVE_ARTS__ART group with id = 'z785defb5aa9a109ed704fy7z'") {
      import PlanReaderDaoSubjectTermlyPlanHelper.CLASS_LEVEL
      val curriculumPlanningAreaToLatestDoc = planReaderDao.buildCurriculumPlanProgressForClassLoop(
        PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsWithGroupsMixedUpWithDifferentCurriculumPlanningAreas(), Map())
      val latestArtGroupDoc = curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("EXPRESSIVE_ARTS__ART"))(_GroupId(CLASS_LEVEL))
      assert(latestArtGroupDoc.getString(TermlyPlanningSchema._ID) === "z785defb5aa9a109ed704fy7z")
    }

  }


}
