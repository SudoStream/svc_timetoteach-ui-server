package potentialmicroservice.planning.reader.dao

import models.timetoteach.ClassId
import org.scalatest.FunSpec

class PlanReaderDaoTermlyCurriculumSelectionHelperTest extends FunSpec
{
  private val planReaderTermlyCurriculumSelectionHelper = new PlanReaderDaoTermlyCurriculumSelectionHelper
  {}

  describe("When given an empty list, findLatestVersionOfTermlyCurriculumSelection()") {
    it("should return None") {
      val maybeTermlyCurriculumSelection = planReaderTermlyCurriculumSelectionHelper.findLatestVersionOfTermlyCurriculumSelection(Nil)
      assert(maybeTermlyCurriculumSelection.isEmpty)
    }
  }

  describe("When a list of one single document is provided, findLatestVersionOfTermlyCurriculumSelection()") {
    it("should return the id 5a7e0f39e9ff0d6d4d390e3c") {
      val document = planReaderTermlyCurriculumSelectionHelper.findLatestVersionOfTermlyCurriculumSelectionLoop(Nil,
        PlanReaderDaoHelperTermlyCurriculumSelectionTestHelper.provideSingleTermlyCurriculumSelectionDocument())
      assert(document.getString("_id") === "5a7e0f39e9ff0d6d4d390e3c")
    }
  }

  describe("When a list of several documents is provided, findLatestVersionOfTermlyCurriculumSelection()") {
    it("should return a defined option") {
      val document = planReaderTermlyCurriculumSelectionHelper.findLatestVersionOfTermlyCurriculumSelectionLoop(
        PlanReaderDaoHelperTermlyCurriculumSelectionTestHelper.createAListOfSeveralTermlyCurriculumSelectionDocumentsMixedUp().tail,
        PlanReaderDaoHelperTermlyCurriculumSelectionTestHelper.createAListOfSeveralTermlyCurriculumSelectionDocumentsMixedUp().head
      )
      println(s"Document we think is latest has id = '${document.getString("_id")}'")
      assert(document.getString("_id") === "a21029985165479d9a049551")
    }
  }

  describe("Given a correctly formed termly mongo db document, convertDocumentToTermlyCurriculumSelection()") {
    val maybeCurriculumSelection = planReaderTermlyCurriculumSelectionHelper.convertDocumentToTermlyCurriculumSelection(
      PlanReaderDaoHelperTermlyCurriculumSelectionTestHelper.provideSingleTermlyCurriculumSelectionDocument()
    )

    it("should create a defined TermlyCurriculumSelection") {
      assert(maybeCurriculumSelection.isDefined)
    }
    it("should create a TermlyCurriculumSelection with a class id of classId_1b22d43d-5585-47b4-bb41-94f26d3edba5") {
      assert(maybeCurriculumSelection.get.classId.value === "classId_1b22d43d-5585-47b4-bb41-94f26d3edba5")
    }
    it("should create a SubjectTermlyPlan with a list of 5 selected curriculum planning areas") {
      assert(maybeCurriculumSelection.get.planningAreas.size === 5)
    }

  }

  describe("When given a list of several mongo docs, findLatestVersionOfTermlyCurriculumSelection()") {
    it("should return a defined option") {
      val maybeCurriculumSelection = planReaderTermlyCurriculumSelectionHelper.findLatestVersionOfTermlyCurriculumSelection(
        PlanReaderDaoHelperTermlyCurriculumSelectionTestHelper.createAListOfSeveralTermlyCurriculumSelectionDocumentsMixedUp()
      )
      assert(maybeCurriculumSelection.isDefined)
    }
  }

  describe("When give an empty list of class ids, findLatestVersionOfTermlyCurriculumSelectionForEachClassId") {
    it("should return an empty map") {
      val classIdToMaybeSelection =
        planReaderTermlyCurriculumSelectionHelper.findLatestVersionOfTermlyCurriculumSelectionForEachClassId(Nil)
      assert(classIdToMaybeSelection.isEmpty)
    }
  }

  describe("When give a list of mixed class ids, findLatestVersionOfTermlyCurriculumSelectionForEachClassIdLoop()") {
    val classIdToMaybeSelection =
      planReaderTermlyCurriculumSelectionHelper.findLatestVersionOfTermlyCurriculumSelectionForEachClassIdLoop(
        PlanReaderDaoHelperTermlyCurriculumSelectionTestHelper.createAListOfSeveralTermlyCurriculumSelectionDocumentsMixedUpWithDifferentClassIds(),
        Map()
      )

    it("should return a non empty map") {
      assert(classIdToMaybeSelection.nonEmpty)
    }
    it("should have a map with 3 keys") {
      assert(classIdToMaybeSelection.keys.size === 3)
    }
    it("should have a map with the classid1 which has an id of 204534664378917127976998") {
      assert(classIdToMaybeSelection(ClassId("classId1")).get("_id").get.asString().getValue === "204534664378917127976998")
    }
    it("should have a map with the classid2 which has an id of 448bcf3f4450361f4271b444") {
      assert(classIdToMaybeSelection(ClassId("classId2")).get("_id").get.asString().getValue === "448bcf3f4450361f4271b444")
    }
    it("should have a map with the classid3 which has an id of 1010defb5aa9a109ed701010") {
      assert(classIdToMaybeSelection(ClassId("classId3")).get("_id").get.asString().getValue === "1010defb5aa9a109ed701010")
    }
  }


  describe("When give a list of mixed class ids, findLatestVersionOfTermlyCurriculumSelectionForEachClassId()") {
    val classIdToMaybeSelection =
      planReaderTermlyCurriculumSelectionHelper.findLatestVersionOfTermlyCurriculumSelectionForEachClassId(
        PlanReaderDaoHelperTermlyCurriculumSelectionTestHelper.
          createAListOfSeveralTermlyCurriculumSelectionDocumentsMixedUpWithDifferentClassIds()
      )

    it("should return a non empty map") {
      assert(classIdToMaybeSelection.nonEmpty)
    }
    it("should return a map with 3 keys") {
      assert(classIdToMaybeSelection.keySet.size === 3)
    }
  }

}
