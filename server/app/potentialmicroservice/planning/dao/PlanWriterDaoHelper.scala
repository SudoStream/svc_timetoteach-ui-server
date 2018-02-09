package potentialmicroservice.planning.dao

import duplicate.model.EandOsWithBenchmarks
import models.timetoteach.planning.SubjectTermlyPlan
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonString}

trait PlanWriterDaoHelper {
  import potentialmicroservice.planning.dao.schema.TermlyPlanningSchema._

  def convertTermlyPlanToMongoDbDocument(planToSave: SubjectTermlyPlan): Document = {

    val groupIdValue = if (planToSave.maybeGroupId.isDefined) {
      planToSave.maybeGroupId.get.value
    } else {
      ""
    }

    val endYear = if (planToSave.schoolTerm.schoolYear.maybeCalendarYearEnd.isDefined) {
      "-" + planToSave.schoolTerm.schoolYear.maybeCalendarYearEnd.get
    } else ""
    val schoolYearValue: String = planToSave.schoolTerm.schoolYear.calendarYearStart.toString + endYear

    Document(
      TTT_USER_ID -> planToSave.tttUserId.value,
      CLASS_ID -> planToSave.classId.value,
      PLAN_TYPE -> planToSave.planType.toString,
      GROUP_ID -> groupIdValue,
      SUBJECT_NAME -> planToSave.subject.toString,
      CREATED_TIMESTAMP -> planToSave.createdTime.toString.replace("T", " "),
      SCHOOL_TERM -> BsonDocument(
        SCHOOL_YEAR -> schoolYearValue,
        SCHOOL_TERM_NAME -> planToSave.schoolTerm.schoolTermName.toString,
        SCHOOL_TERM_FIRST_DAY -> planToSave.schoolTerm.termFirstDay.toString,
        SCHOOL_TERM_LAST_DAY -> planToSave.schoolTerm.termLastDay.toString
      ),
      SELECTED_ES_AND_OS_WITH_BENCHMARKS -> convertListOfEsAndOsToBsonArray(planToSave.eandOsWithBenchmarks)
    )
  }

  private def convertListOfEsAndOsToBsonArray(eandOsWithBenchmarksList: List[EandOsWithBenchmarks]): BsonArray = {
    BsonArray({
      for {
        osWithBenchmarks <- eandOsWithBenchmarksList
      } yield BsonDocument(
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
