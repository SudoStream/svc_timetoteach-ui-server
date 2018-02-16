package potentialmicroservice.planning.reader.dao

import java.time.LocalDate

import models.timetoteach.term.{SchoolTerm, SchoolTermName, SchoolYear}
import org.bson.BsonArray
import org.mongodb.scala.bson.BsonValue
import potentialmicroservice.planning.sharedschema.TermlyPlanningSchema
import utils.mongodb.MongoDbSafety.safelyGetStringNoneIfBlank

trait PlanReaderDaoCommonHelper
{

  def convertToSchoolTerm(maybeSchoolTermValue: Option[BsonValue]): Option[SchoolTerm] =
  {
    maybeSchoolTermValue match {
      case Some(schoolTermValue) =>
        val schoolTermDoc = schoolTermValue.asDocument()
        for {
          calendarYearValue <- safelyGetStringNoneIfBlank(schoolTermDoc, TermlyPlanningSchema.SCHOOL_YEAR)
          yearsInCalendar = calendarYearValue.split("-")
          yearStart = yearsInCalendar(0).toInt
          maybeYearEnd = if (yearsInCalendar.size == 2) {
            Some(yearsInCalendar(1).toInt)
          } else None

          schoolTermNameString <- safelyGetStringNoneIfBlank(schoolTermDoc, TermlyPlanningSchema.SCHOOL_TERM_NAME)
          maybeSchoolTermName = SchoolTermName.convertToSchoolTermName(schoolTermNameString)
          if maybeSchoolTermName.isDefined
          schoolTermFirstDay <- safelyGetStringNoneIfBlank(schoolTermDoc, TermlyPlanningSchema.SCHOOL_TERM_FIRST_DAY)
          schoolTermLastDay <- safelyGetStringNoneIfBlank(schoolTermDoc, TermlyPlanningSchema.SCHOOL_TERM_LAST_DAY)
        } yield SchoolTerm(
          SchoolYear(yearStart, maybeYearEnd),
          maybeSchoolTermName.get,
          LocalDate.parse(schoolTermFirstDay),
          LocalDate.parse(schoolTermLastDay)
        )
      case None => None
    }
  }

  def convertBsonArrayToListOfString(array: BsonArray): List[String] =
  {
    import scala.collection.JavaConversions._
    {
      for {
        eAndoValue <- array
      } yield eAndoValue.asString().getValue
    }.toList
  }

}
