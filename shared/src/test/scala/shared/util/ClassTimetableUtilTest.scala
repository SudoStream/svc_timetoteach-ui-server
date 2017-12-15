package shared.util

import org.scalatest.FunSpec
import shared.model.classtimetable.WwwSessionBreakdown

import scala.scalajs.js.JavaScriptException

class ClassTimetableUtilTest extends FunSpec with ClassTimeTableUtilTestHelper {
  describe("Given a string of 'this is nonsense' createWwwClassTimetableFromJson()") {
    it("should throw a JavaScriptException") {
      assertThrows[JavaScriptException] {
        ClassTimetableUtil.createWwwClassTimetableFromJson("this is nonsense")
      }
    }
  }

  describe("Given a valid json string of a wwwClassTimetable createWwwClassTimetableFromJson()") {
    it("should return defined") {
      val maybeClassTimetable = ClassTimetableUtil.createWwwClassTimetableFromJson(createClassTimetableAsJson())
      assert(maybeClassTimetable.isDefined)
    }
    it("should have 1 subject-science") {
      val classTimetable = ClassTimetableUtil.createWwwClassTimetableFromJson(createClassTimetableAsJson()).get
      val allWwwSubjectDetails = for {
        sessionBreakdown <- classTimetable.allSessionsOfTheWeek
        subjectDetailToFractionTuple <- sessionBreakdown.subjectsWithTimeFractionInTwelves
        subjectDetail = subjectDetailToFractionTuple._1
      } yield subjectDetail

      assert(
        allWwwSubjectDetails.count(subjectDetail => subjectDetail.subject.value == "subject-science") == 1
      )
    }

    it("should have 2 subject-art") {
      val classTimetable = ClassTimetableUtil.createWwwClassTimetableFromJson(createClassTimetableAsJson()).get
      val allWwwSubjectDetails = for {
        sessionBreakdown <- classTimetable.allSessionsOfTheWeek
        subjectDetailToFractionTuple <- sessionBreakdown.subjectsWithTimeFractionInTwelves
        subjectDetail = subjectDetailToFractionTuple._1
      } yield subjectDetail

      assert(
        allWwwSubjectDetails.count(subjectDetail => subjectDetail.subject.value == "subject-art") == 2
      )
    }

    it("should have 2 subject-writing") {
      val classTimetable = ClassTimetableUtil.createWwwClassTimetableFromJson(createClassTimetableAsJson()).get
      val allWwwSubjectDetails = for {
        sessionBreakdown <- classTimetable.allSessionsOfTheWeek
        subjectDetailToFractionTuple <- sessionBreakdown.subjectsWithTimeFractionInTwelves
        subjectDetail = subjectDetailToFractionTuple._1
      } yield subjectDetail

      assert(
        allWwwSubjectDetails.count(subjectDetail => subjectDetail.subject.value == "subject-writing") == 2
      )
    }

    it("should have 1 subject-maths") {
      val classTimetable = ClassTimetableUtil.createWwwClassTimetableFromJson(createClassTimetableAsJson()).get
      val allWwwSubjectDetails = for {
        sessionBreakdown <- classTimetable.allSessionsOfTheWeek
        subjectDetailToFractionTuple <- sessionBreakdown.subjectsWithTimeFractionInTwelves
        subjectDetail = subjectDetailToFractionTuple._1
      } yield subjectDetail

      assert(
        allWwwSubjectDetails.count(subjectDetail => subjectDetail.subject.value == "subject-maths") == 1
      )
    }

    it("should have 1 subject-geography") {
      val classTimetable = ClassTimetableUtil.createWwwClassTimetableFromJson(createClassTimetableAsJson()).get
      val allWwwSubjectDetails = for {
        sessionBreakdown <- classTimetable.allSessionsOfTheWeek
        subjectDetailToFractionTuple <- sessionBreakdown.subjectsWithTimeFractionInTwelves
        subjectDetail = subjectDetailToFractionTuple._1
      } yield subjectDetail

      assert(
        allWwwSubjectDetails.count(subjectDetail => subjectDetail.subject.value == "subject-geography") == 1
      )
    }


    it("should have 1 subject-reading") {
      val classTimetable = ClassTimetableUtil.createWwwClassTimetableFromJson(createClassTimetableAsJson()).get
      val allWwwSubjectDetails = for {
        sessionBreakdown <- classTimetable.allSessionsOfTheWeek
        subjectDetailToFractionTuple <- sessionBreakdown.subjectsWithTimeFractionInTwelves
        subjectDetail = subjectDetailToFractionTuple._1
      } yield subjectDetail

      assert(
        allWwwSubjectDetails.count(subjectDetail => subjectDetail.subject.value == "subject-reading") == 1
      )
    }

    it("should have 1 subject-spelling") {
      val classTimetable = ClassTimetableUtil.createWwwClassTimetableFromJson(createClassTimetableAsJson()).get
      val allWwwSubjectDetails = for {
        sessionBreakdown <- classTimetable.allSessionsOfTheWeek
        subjectDetailToFractionTuple <- sessionBreakdown.subjectsWithTimeFractionInTwelves
        subjectDetail = subjectDetailToFractionTuple._1
      } yield subjectDetail

      assert(
        allWwwSubjectDetails.count(subjectDetail => subjectDetail.subject.value == "subject-spelling") == 1
      )
    }

    it("should have 1 subject-topic") {
      val classTimetable = ClassTimetableUtil.createWwwClassTimetableFromJson(createClassTimetableAsJson()).get
      val allWwwSubjectDetails = for {
        sessionBreakdown <- classTimetable.allSessionsOfTheWeek
        subjectDetailToFractionTuple <- sessionBreakdown.subjectsWithTimeFractionInTwelves
        subjectDetail = subjectDetailToFractionTuple._1
      } yield subjectDetail

      assert(
        allWwwSubjectDetails.count(subjectDetail => subjectDetail.subject.value == "subject-topic") == 1
      )
    }


    it("should have 0 subject-ict") {
      val classTimetable = ClassTimetableUtil.createWwwClassTimetableFromJson(createClassTimetableAsJson()).get
      val allWwwSubjectDetails = for {
        sessionBreakdown <- classTimetable.allSessionsOfTheWeek
        subjectDetailToFractionTuple <- sessionBreakdown.subjectsWithTimeFractionInTwelves
        subjectDetail = subjectDetailToFractionTuple._1
      } yield subjectDetail

      assert(
        allWwwSubjectDetails.count(subjectDetail => subjectDetail.subject.value == "subject-ict") == 0
      )
    }


  }

}
