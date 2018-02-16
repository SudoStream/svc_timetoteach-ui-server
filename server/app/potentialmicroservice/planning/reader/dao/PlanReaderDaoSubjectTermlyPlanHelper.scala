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

trait PlanReaderDaoSubjectTermlyPlanHelper extends PlanReaderDaoCommonHelper
{
  private val logger: Logger = Logger
  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

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

  ////////////////////// Implementation ////////////////////////

  private[dao] def findLatestVersionOfTermlyPlanDocLoop(foundTermlyPlanDocs: List[Document], currentLatestDoc: Document): Document =
  {
    if (foundTermlyPlanDocs.isEmpty) currentLatestDoc
    else {
      val nextDoc = foundTermlyPlanDocs.head
      val maybeNextTimestampIso = safelyGetString(nextDoc, TermlyPlanningSchema.CREATED_TIMESTAMP)
      val newLatestDoc: Document = maybeNextTimestampIso match {
        case Some(nextTimestampIso) =>
          val nextTimestamp = LocalDateTime.parse(nextTimestampIso, formatter)
          val maybeCurrentTimestampIso = safelyGetString(currentLatestDoc, TermlyPlanningSchema.CREATED_TIMESTAMP)
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
      curriculumPlanningAreaValue <- safelyGetStringNoneIfBlank(doc, TermlyPlanningSchema.CURRICULUM_PLANNING_AREA)
      curriculumPlanningArea <- SubjectNameConverter.convertSubjectNameStringToSubjectName(curriculumPlanningAreaValue)
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
      curriculumPlanningArea,
      LocalDateTime.parse(createdTimeString, formatter),
      eAndOsWithBenchmarks
    )
  }

}
