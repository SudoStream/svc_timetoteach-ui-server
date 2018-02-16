package potentialmicroservice.planning.reader.dao

import models.timetoteach.planning.PlanType
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
    it("should create a defined TermlyCurriculumSelection") {
      val maybeCurriculumSelection = planReaderTermlyCurriculumSelectionHelper.convertDocumentToTermlyCurriculumSelection(
        PlanReaderDaoHelperTermlyCurriculumSelectionTestHelper.provideSingleTermlyCurriculumSelectionDocument()
      )
      assert(maybeCurriculumSelection.isDefined)
    }
    it("should create a TermlyCurriculumSelection with a class id of classId_1b22d43d-5585-47b4-bb41-94f26d3edba5") {
      val maybeCurriculumSelection = planReaderTermlyCurriculumSelectionHelper.convertDocumentToTermlyCurriculumSelection(
        PlanReaderDaoHelperTermlyCurriculumSelectionTestHelper.provideSingleTermlyCurriculumSelectionDocument()
      )
      assert(maybeCurriculumSelection.get.classId.value === "classId_1b22d43d-5585-47b4-bb41-94f26d3edba5")
    }
    it("should create a SubjectTermlyPlan with a list of 5 selected curriculum planning areas") {
      val maybeCurriculumSelection = planReaderTermlyCurriculumSelectionHelper.convertDocumentToTermlyCurriculumSelection(
        PlanReaderDaoHelperTermlyCurriculumSelectionTestHelper.provideSingleTermlyCurriculumSelectionDocument())
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

}
