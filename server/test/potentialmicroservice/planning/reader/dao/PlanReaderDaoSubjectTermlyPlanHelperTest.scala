package potentialmicroservice.planning.reader.dao

import duplicate.model.Group
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning.{GroupLevelProgressPercent, OverallClassLevelProgressPercent, PlanType, ScottishCurriculumPlanningAreaWrapper}
import org.scalatest.FunSpec
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
      assert(document.getString("_id") === "a21029985165479d9a049551")
    }
  }

  describe("Given a correctly formed termly mongo db document, convertDocumentToSubjectTermlyPlan()") {
    val maybeTermlyPlan = planReaderDao.convertDocumentToSubjectTermlyPlan(PlanReaderDaoHelperTermlyPlanTestHelper.provideSingleTermlyPlanDocument())
    it("should create a defined SubjectTermlyPlan") {
      assert(maybeTermlyPlan.isDefined)
    }
    it("should create a SubjectTermlyPlan with a plan type of Group") {
      assert(maybeTermlyPlan.get.planType === PlanType.GROUP_LEVEL_PLAN)
    }
    it("should create a SubjectTermlyPlan with a group id of groupId_f842e787-cc90-483f-a321-49b68a252a80") {
      assert(maybeTermlyPlan.get.maybeGroupId.get.value === "groupId_f842e787-cc90-483f-a321-49b68a252a80")
    }
    it("should create a SubjectTermlyPlan with a class id of classId_1b22d43d-5585-47b4-bb41-94f26d3edba5") {
      assert(maybeTermlyPlan.get.classId.value === "classId_1b22d43d-5585-47b4-bb41-94f26d3edba5")
    }
    it("should create a SubjectTermlyPlan with a list of 3 esAndOsWithBenchmarks") {
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

    val curriculumPlanningAreaToLatestDoc = planReaderDao.buildCurriculumPlanProgressForClassLoop(
      PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsWithGroupsMixedUpWithDifferentCurriculumPlanningAreas(), Map())

    it("should return a nonempty map") {
      assert(curriculumPlanningAreaToLatestDoc.nonEmpty)
    }
    it("should return a map with 3 keys") {
      assert(curriculumPlanningAreaToLatestDoc.keySet.size === 3)
    }
    it("should return a map with a 'MATHEMATICS' key") {
      assert(curriculumPlanningAreaToLatestDoc.isDefinedAt(_CurriculumAreaName("MATHEMATICS")))
    }
    it("should return a map with a 'EXPRESSIVE_ARTS__ART' key") {
      assert(curriculumPlanningAreaToLatestDoc.isDefinedAt(_CurriculumAreaName("EXPRESSIVE_ARTS__ART")))
    }
    it("should return a map with a 'LITERACY__WRITING' key") {
      assert(curriculumPlanningAreaToLatestDoc.isDefinedAt(_CurriculumAreaName("LITERACY__WRITING")))
    }
    it("should not have a 'TOODLY_DOO' key") {
      assert(!curriculumPlanningAreaToLatestDoc.isDefinedAt(_CurriculumAreaName("TOODLY_DOO")))
    }
    it("should have 1 maths groups") {
      assert(curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("MATHEMATICS")).keys.size === 1)
    }
    it("should have a latest maths group doc with a group id of groupId_f842e787-cc90-483f-a321-49b68a252a80") {
      assert(curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("MATHEMATICS")).isDefinedAt(_GroupId("groupId_f842e787-cc90-483f-a321-49b68a252a80")))
    }
    it("should have a latest maths group doc with an id of a21029985165479d9a049551") {
      val latestMathsGroupDoc = curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("MATHEMATICS"))(_GroupId("groupId_f842e787-cc90-483f-a321-49b68a252a80"))
      assert(latestMathsGroupDoc.getString(TermlyPlanningSchema._ID) === "a21029985165479d9a049551")
    }
    it("should have 3 writing groups") {
      assert(curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("LITERACY__WRITING")).keys.size === 3)
    }
    it("should have 1 writing group with group id 'groupId_writing_one'") {
      assert(curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("LITERACY__WRITING")).isDefinedAt(_GroupId("groupId_writing_one")))
    }
    it("should have 1 writing group with group id 'groupId_writing_one' which has a latest doc id of '123pdefb5a9a109ed704l321'") {
      val latestArtGroupDoc = curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("LITERACY__WRITING"))(_GroupId("groupId_writing_one"))
      assert(latestArtGroupDoc.getString(TermlyPlanningSchema._ID) === "123pdefb5a9a109ed704l321")
    }
    it("should have 1 writing group with group id 'groupId_writing_two'") {
      assert(curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("LITERACY__WRITING")).isDefinedAt(_GroupId("groupId_writing_two")))
    }
    it("should have 1 writing group with group id 'groupId_writing_two' which has a latest doc id of 'qq2pdefb5a9a109ed7042222'") {
      val latestArtGroupDoc = curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("LITERACY__WRITING"))(_GroupId("groupId_writing_two"))
      assert(latestArtGroupDoc.getString(TermlyPlanningSchema._ID) === "qq2pdefb5a9a109ed7042222")
    }
    it("should have 1 writing group with group id 'groupId_writing_three'") {
      assert(curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("LITERACY__WRITING")).isDefinedAt(_GroupId("groupId_writing_three")))
    }
    it("should have 1 writing group with group id 'groupId_writing_three' which has a latest doc id of 'ee2pdefb5a9a109ed704laee'") {
      val latestArtGroupDoc = curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("LITERACY__WRITING"))(_GroupId("groupId_writing_three"))
      assert(latestArtGroupDoc.getString(TermlyPlanningSchema._ID) === "ee2pdefb5a9a109ed704laee")
    }
    it("should have 1 EXPRESSIVE_ARTS__ART group") {
      assert(curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("EXPRESSIVE_ARTS__ART")).keys.size === 1)
    }
    it("should have 1 EXPRESSIVE_ARTS__ART group called CLASS_LEVEL ") {
      import PlanReaderDaoSubjectTermlyPlanHelper.CLASS_LEVEL
      assert(curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("EXPRESSIVE_ARTS__ART")).isDefinedAt(_GroupId(CLASS_LEVEL)))
    }
    it("should have 1 EXPRESSIVE_ARTS__ART group with id = 'z785defb5aa9a109ed704fy7z'") {
      import PlanReaderDaoSubjectTermlyPlanHelper.CLASS_LEVEL
      val latestArtGroupDoc = curriculumPlanningAreaToLatestDoc(_CurriculumAreaName("EXPRESSIVE_ARTS__ART"))(_GroupId(CLASS_LEVEL))
      assert(latestArtGroupDoc.getString(TermlyPlanningSchema._ID) === "z785defb5aa9a109ed704fy7z")
    }
  }

  describe("Given 2 empty lists, createZeroedProgressMap()") {
    it("should return an empty map") {
      val zeroedProgressMap = planReaderDao.createZeroedProgressMap(Nil, Nil)
      assert(zeroedProgressMap.isEmpty)
    }
  }

  describe("Given a list of maths, writing and art, createZeroedProgressMap()") {

    val zeroedProgressMap = planReaderDao.createZeroedProgressMap(
      PlanReaderDaoHelperTermlyPlanTestHelper.createScottishCurriculumPlanningAreaList(),
      PlanReaderDaoHelperTermlyPlanTestHelper.createClassGroupsList()
    )

    it("should return a map with 4 keys") {
      assert(zeroedProgressMap.keySet.size === 4)
    }
    it("should return a map with maths class level progress = zero") {
      val maths = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.MATHEMATICS)
      assert(zeroedProgressMap(maths)._1.percentValue === 0)
    }
    it("should return a map with drama class level progress = zero") {
      val drama = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__DRAMA)
      assert(zeroedProgressMap(drama)._1.percentValue === 0)
    }
    it("should return a map with maths 3 maths groups") {
      val maths = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.MATHEMATICS)
      assert(zeroedProgressMap(maths)._2.size === 3)
    }
    it("should return a map with maths one maths group id 'groupId_squares'") {
      val maths = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.MATHEMATICS)
      assert(zeroedProgressMap(maths)._2.keySet.count(group => group.groupId.id == "groupId_squares") === 1)
    }
    it("should return a map with maths one maths group id 'groupId_circles'") {
      val maths = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.MATHEMATICS)
      assert(zeroedProgressMap(maths)._2.keySet.count(group => group.groupId.id == "groupId_circles") === 1)
    }
    it("should return a map with maths one maths group id 'groupId_f842e787-cc90-483f-a321-49b68a252a80'") {
      val maths = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.MATHEMATICS)
      assert(zeroedProgressMap(maths)._2.keySet.count(group => group.groupId.id == "groupId_f842e787-cc90-483f-a321-49b68a252a80") === 1)
    }
    it("should return a map with maths ZERO maths group id 'toodlydoo'") {
      val maths = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.MATHEMATICS)
      assert(zeroedProgressMap(maths)._2.keySet.count(group => group.groupId.id == "toodlydoo") === 0)
    }
    it("should return a map with 3 writing groups") {
      val writing = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.LITERACY__WRITING)
      assert(zeroedProgressMap(writing)._2.size === 3)
    }
    it("should return a map with one writing group id 'groupId_writing_one'") {
      val writing = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.LITERACY__WRITING)
      assert(zeroedProgressMap(writing)._2.keySet.count(group => group.groupId.id == "groupId_writing_one") === 1)
    }
    it("should return a map with one writing group id 'groupId_writing_two'") {
      val writing = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.LITERACY__WRITING)
      assert(zeroedProgressMap(writing)._2.keySet.count(group => group.groupId.id == "groupId_writing_two") === 1)
    }
    it("should return a map with one writing group id 'groupId_writing_three'") {
      val writing = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.LITERACY__WRITING)
      assert(zeroedProgressMap(writing)._2.keySet.count(group => group.groupId.id == "groupId_writing_three") === 1)
    }
    it("should return a map with reading not defined") {
      val reading = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.LITERACY__READING)
      assert(!zeroedProgressMap.isDefinedAt(reading))
    }
    it("should return a map with 0 art group") {
      val art = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__ART)
      assert(zeroedProgressMap(art)._2.size === 0)
    }
    it("should return a map with art class level progress at 0") {
      val art = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__ART)
      assert(zeroedProgressMap(art)._1.percentValue === 0)
    }
  }

  describe("Given populated maps of initialZeroedProgress & curriculumPlanningAreaToLatestDoc, addGroupLevelProgressMap()") {

    val curriculumPlanningAreaToLatestDoc = planReaderDao.buildCurriculumPlanProgressForClassLoop(
      PlanReaderDaoHelperTermlyPlanTestHelper.createAListOfSeveralTermlyPlanDocumentsWithGroupsMixedUpWithDifferentCurriculumPlanningAreas(), Map())

    val zeroedProgressMap = planReaderDao.createZeroedProgressMap(
      PlanReaderDaoHelperTermlyPlanTestHelper.createScottishCurriculumPlanningAreaList(),
      PlanReaderDaoHelperTermlyPlanTestHelper.createClassGroupsList()
    )

    val groupLevelProgressMap = planReaderDao.addGroupAndClassLevelProgressMap(
      zeroedProgressMap,
      curriculumPlanningAreaToLatestDoc,
      PlanReaderDaoHelperTermlyPlanTestHelper.createClassGroupsList()
    )

    it("should have a drama progress of zero") {
      val drama = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__DRAMA)
      assert(groupLevelProgressMap(drama)._1.percentValue == 0)
    }
    it("should have an art progress of 100") {
      val drama = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__ART)
      assert(groupLevelProgressMap(drama)._1.percentValue == 100)
    }
    it("should have an writing progress of 100") {
      val writing = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.LITERACY__WRITING)
      assert(groupLevelProgressMap(writing)._1.percentValue == 100)
    }
    it("should have a maths progress of 33") {
      val maths = ScottishCurriculumPlanningAreaWrapper(ScottishCurriculumPlanningArea.MATHEMATICS)
      assert(groupLevelProgressMap(maths)._1.percentValue == 33)
    }

  }
}
