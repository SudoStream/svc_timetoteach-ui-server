package potentialmicroservice.planning.writer.dao

import duplicate.model.EandOsWithBenchmarks
import models.timetoteach.planning.CurriculumAreaTermlyPlan
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonString}

trait PlanWriterDaoSubjectTermlyPlanHelper {
  import potentialmicroservice.planning.sharedschema.TermlyPlanningSchema._

  def convertTermlyPlanToMongoDbDocument(planToSave: CurriculumAreaTermlyPlan): Document = {

    val groupIdValue = if (planToSave.maybeGroupId.isDefined) {
      planToSave.maybeGroupId.get.value
    } else {
      ""
    }

    Document(
      TTT_USER_ID -> planToSave.tttUserId.value,
      CLASS_ID -> planToSave.classId.value,
      PLAN_TYPE -> planToSave.planType.toString,
      GROUP_ID -> groupIdValue,
      CURRICULUM_PLANNING_AREA -> planToSave.planningArea.toString,
      CREATED_TIMESTAMP -> planToSave.createdTime.toString.replace("T", " "),
      SCHOOL_TERM -> BsonDocument(
        SCHOOL_YEAR -> planToSave.schoolTerm.schoolYear.niceValue,
        SCHOOL_TERM_NAME -> planToSave.schoolTerm.schoolTermName.toString,
        SCHOOL_TERM_FIRST_DAY -> planToSave.schoolTerm.termFirstDay.toString,
        SCHOOL_TERM_LAST_DAY -> planToSave.schoolTerm.termLastDay.toString
      ),
      SELECTED_ES_AND_OS_WITH_BENCHMARKS -> convertListOfEsAndOsToBsonArray(planToSave.eAndOsWithBenchmarks)
    )
  }

  private def convertListOfEsAndOsToBsonArray(eandOsWithBenchmarksList: List[EandOsWithBenchmarks]): BsonArray = {
    BsonArray({
      for {
        osWithBenchmarks <- eandOsWithBenchmarksList
      } yield BsonDocument(
        SELECTED_SECTION_NAME -> osWithBenchmarks.sectionName,
        SELECTED_SUBSECTION_NAME -> osWithBenchmarks.subsectionName,
        SELECTED_ES_AND_OS -> convertListOfStringToBsonArray(osWithBenchmarks.eAndOCodes),
        SELECTED_BENCHMARKS -> convertListOfStringToBsonArray(osWithBenchmarks.benchmarks)
      )
    })
  }

    private def convertListOfStringToBsonArray(listOfStrings: List[String]): BsonArray = {
    BsonArray({
      for {
        element <- listOfStrings
      } yield BsonString(element)
    })
  }

}
