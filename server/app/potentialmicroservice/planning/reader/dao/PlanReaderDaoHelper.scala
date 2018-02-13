package potentialmicroservice.planning.reader.dao

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

import duplicate.model.EandOsWithBenchmarks
import models.timetoteach.planning.{GroupId, PlanType, SubjectNameConverter, SubjectTermlyPlan}
import models.timetoteach.term.{SchoolTerm, SchoolTermName, SchoolYear}
import models.timetoteach.{ClassId, TimeToTeachUserId}
import org.bson.{BsonArray, BsonDocument}
import org.mongodb.scala.Document
import org.mongodb.scala.bson.BsonValue
import play.api.Logger
import potentialmicroservice.planning.sharedschema.TermlyPlanningSchema
import utils.mongodb.MongoDbSafety._

trait PlanReaderDaoHelper
{
  val logger: Logger = Logger
  import potentialmicroservice.planning.sharedschema.TermlyPlanningSchema.CREATED_TIMESTAMP

  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

  private[dao] def findLatestVersionOfTermlyPlanDocLoop(foundTermlyPlanDocs: List[Document], currentLatestDoc: Document): Document =
  {
    if (foundTermlyPlanDocs.isEmpty) currentLatestDoc
    else {
      val nextDoc = foundTermlyPlanDocs.head
      val maybeNextTimestampIso = safelyGetString(nextDoc, CREATED_TIMESTAMP)
      val newLatestDoc: Document = maybeNextTimestampIso match {
        case Some(nextTimestampIso) =>
          val nextTimestamp = LocalDateTime.parse(nextTimestampIso, formatter)
          val maybeCurrentTimestampIso = safelyGetString(currentLatestDoc, CREATED_TIMESTAMP)
          maybeCurrentTimestampIso match {
            case Some(currentTimestampIso) => val currentTimestamp = LocalDateTime.parse(currentTimestampIso, formatter)
              if (currentTimestamp.isBefore(nextTimestamp)) {
                nextDoc
              } else {
                currentLatestDoc
              }
            case None => currentLatestDoc
          }
        case None => currentLatestDoc
      }

      findLatestVersionOfTermlyPlanDocLoop(foundTermlyPlanDocs.tail, newLatestDoc)
    }
  }

  def findLatestVersionOfTermlyPlan(foundTermlyPlanDocs: List[Document]): Option[SubjectTermlyPlan] =
  {
    logger.debug(s"foundTermlyPlanDocs size = ${foundTermlyPlanDocs.size}")
    if (foundTermlyPlanDocs.isEmpty) {
      None
    } else {
      val latestVersionOfTermlyPlansDoc = findLatestVersionOfTermlyPlanDocLoop(foundTermlyPlanDocs.tail, foundTermlyPlanDocs.head)
      convertDocumentToSubjectTermlyPlan(latestVersionOfTermlyPlansDoc)
    }
  }

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

  private def convertBsonArrayToListOfString(array: BsonArray): List[String] =
  {
    import scala.collection.JavaConversions._
    {
      for {
        eAndoValue <- array
      } yield eAndoValue.asString().getValue
    }.toList
  }

  private def convertDocumentEandOsWithBenchmarks(document: BsonDocument): Option[EandOsWithBenchmarks] =
  {
    try {
      val selectedEsAndOsBsonArray = document.getArray(TermlyPlanningSchema.SELECTED_ES_AND_OS)
      val selectedEsAndOsList = convertBsonArrayToListOfString(selectedEsAndOsBsonArray)

      val selectedBenchmarksBsonArray = document.getArray(TermlyPlanningSchema.SELECTED_BENCHMARKS)
      val selectedBenchmarksList = convertBsonArrayToListOfString(selectedBenchmarksBsonArray)

      Some(
        EandOsWithBenchmarks(selectedEsAndOsList, selectedBenchmarksList)
      )
    } catch {
      case ex: Exception =>
        logger.warn(s"Issue creating Es And Os with Benchmarks. ${ex.getMessage}")
        None
    }
  }

  private def convertBsonArrayToEsAndOsWithBenchmarks(esAndOsWithBenchmarksBsonArray: BsonArray): List[EandOsWithBenchmarks] =
  {
    import scala.collection.JavaConversions._
    {
      for {
        esAndOsDoc <- esAndOsWithBenchmarksBsonArray.getValues
        maybeEsAndOsWithBenchmarks = convertDocumentEandOsWithBenchmarks(esAndOsDoc.asDocument())
        if maybeEsAndOsWithBenchmarks.isDefined
      } yield maybeEsAndOsWithBenchmarks.get
    }.toList
  }

  private def convertToEsAndOsWithBenchmarks(maybeEandOsWithBenchmarks: Option[BsonValue]): List[EandOsWithBenchmarks] =
  {
    val maybeOsWithBenchmarks = for {
      esAndOsWithBenchmarksBsonValue <- maybeEandOsWithBenchmarks
      esAndOsWithBenchmarksBsonArray = esAndOsWithBenchmarksBsonValue.asArray()
      maybEsAndOsWithBenchmarks = convertBsonArrayToEsAndOsWithBenchmarks(esAndOsWithBenchmarksBsonArray)
    } yield maybEsAndOsWithBenchmarks

    maybeOsWithBenchmarks match {
      case Some(esOsAndBs) => esOsAndBs
      case None => Nil
    }
  }

  private[dao] def convertDocumentToSubjectTermlyPlan(doc: Document): Option[SubjectTermlyPlan] =
  {
    for {
      tttUserId <- safelyGetStringNoneIfBlank(doc, TermlyPlanningSchema.TTT_USER_ID)
      planTypeStringValue <- safelyGetStringNoneIfBlank(doc, TermlyPlanningSchema.PLAN_TYPE)
      planType <- PlanType.createPlanTypeFromString(planTypeStringValue)
      classId <- safelyGetStringNoneIfBlank(doc, TermlyPlanningSchema.CLASS_ID)
      maybeGroupId = safelyGetStringNoneIfBlank(doc, TermlyPlanningSchema.GROUP_ID)
      groupId = maybeGroupId match {
        case Some(id) => Some(GroupId(id))
        case None => None
      }
      subjectStringValue <- safelyGetStringNoneIfBlank(doc, TermlyPlanningSchema.SUBJECT_NAME)
      subject <- SubjectNameConverter.convertSubjectNameStringToSubjectName(subjectStringValue)
      createdTimeString <- safelyGetStringNoneIfBlank(doc, TermlyPlanningSchema.CREATED_TIMESTAMP)

      maybeSchoolTermBsonValue = doc.get(TermlyPlanningSchema.SCHOOL_TERM)
      maybeSchoolTerm: Option[SchoolTerm] = convertToSchoolTerm(maybeSchoolTermBsonValue)
      if maybeSchoolTerm.isDefined

      maybeEandOsWithBenchmarks = doc.get(TermlyPlanningSchema.SELECTED_ES_AND_OS_WITH_BENCHMARKS)
      eAndOsWithBenchmarks = convertToEsAndOsWithBenchmarks(maybeEandOsWithBenchmarks)
    } yield SubjectTermlyPlan(
      TimeToTeachUserId(tttUserId),
      planType,
      maybeSchoolTerm.get,
      ClassId(classId),
      groupId,
      subject,
      LocalDateTime.parse(createdTimeString, formatter),
      eAndOsWithBenchmarks
    )
  }

}
