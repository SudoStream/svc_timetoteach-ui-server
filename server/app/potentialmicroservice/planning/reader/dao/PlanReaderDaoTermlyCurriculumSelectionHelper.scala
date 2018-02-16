package potentialmicroservice.planning.reader.dao

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning._
import models.timetoteach.term.SchoolTerm
import models.timetoteach.{ClassId, TimeToTeachUserId}
import org.mongodb.scala.Document
import org.mongodb.scala.bson.BsonArray
import play.api.Logger
import potentialmicroservice.planning.sharedschema.TermlyCurriculumSelectionSchema
import utils.mongodb.MongoDbSafety.{safelyGetString, safelyGetStringNoneIfBlank}

trait PlanReaderDaoTermlyCurriculumSelectionHelper extends PlanReaderDaoCommonHelper
{
  private val logger: Logger = Logger
  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

  def findLatestVersionOfTermlyCurriculumSelection(foundTermlyCurriculumSelectionDocs: List[Document]): Option[TermlyCurriculumSelection] =
  {
    logger.debug(s"foundTermlyCurriculumSelectionDocs size = ${foundTermlyCurriculumSelectionDocs.size}")
    if (foundTermlyCurriculumSelectionDocs.isEmpty) {
      None
    } else {
      val latestVersionOfTermlyPlansDoc = findLatestVersionOfTermlyCurriculumSelectionLoop(
        foundTermlyCurriculumSelectionDocs.tail,
        foundTermlyCurriculumSelectionDocs.head)
      convertDocumentToTermlyCurriculumSelection(latestVersionOfTermlyPlansDoc)
    }
  }

  ////////////////////// Implementation ////////////////////////

  private[dao] def findLatestVersionOfTermlyCurriculumSelectionLoop(foundTermlyCurriculumSelectionDocs: List[Document], currentLatestDoc: Document): Document =
  {
    if (foundTermlyCurriculumSelectionDocs.isEmpty) currentLatestDoc
    else {
      val nextDoc = foundTermlyCurriculumSelectionDocs.head
      val maybeNextTimestampIso = safelyGetString(nextDoc, TermlyCurriculumSelectionSchema.CREATED_TIMESTAMP)
      val newLatestDoc: Document = maybeNextTimestampIso match {
        case Some(nextTimestampIso) =>
          val nextTimestamp = LocalDateTime.parse(nextTimestampIso, formatter)
          val maybeCurrentTimestampIso = safelyGetString(currentLatestDoc, TermlyCurriculumSelectionSchema.CREATED_TIMESTAMP)
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

      findLatestVersionOfTermlyCurriculumSelectionLoop(foundTermlyCurriculumSelectionDocs.tail, newLatestDoc)
    }
  }

  private[dao] def convertDocumentToTermlyCurriculumSelection(doc: Document): Option[TermlyCurriculumSelection] =
  {
    for {
      tttUserId <- safelyGetStringNoneIfBlank(doc, TermlyCurriculumSelectionSchema.TTT_USER_ID)
      classId <- safelyGetStringNoneIfBlank(doc, TermlyCurriculumSelectionSchema.CLASS_ID)
      maybeCurriculumPlanningAreasBsonArray = doc.get[BsonArray](TermlyCurriculumSelectionSchema.CURRICULUM_PLANNING_AREAS)
      if maybeCurriculumPlanningAreasBsonArray.isDefined
      curriculumPlanningAreasStringList = convertBsonArrayToListOfString(maybeCurriculumPlanningAreasBsonArray.get)
      curriculumPlanningAreas = convertListStringToScottishCurriculumPlanningAreaList(curriculumPlanningAreasStringList)
      createdTimeString <- safelyGetStringNoneIfBlank(doc, TermlyCurriculumSelectionSchema.CREATED_TIMESTAMP)

      maybeSchoolTermBsonValue = doc.get(TermlyCurriculumSelectionSchema.SCHOOL_TERM)
      maybeSchoolTerm: Option[SchoolTerm] = convertToSchoolTerm(maybeSchoolTermBsonValue)
      if maybeSchoolTerm.isDefined
    } yield TermlyCurriculumSelection(
      TimeToTeachUserId(tttUserId),
      ClassId(classId),
      curriculumPlanningAreas,
      LocalDateTime.parse(createdTimeString, formatter),
      maybeSchoolTerm.get
    )
  }


  private def convertListStringToScottishCurriculumPlanningAreaList(curriculumPlanningAreasStringList: List[String])
  : List[ScottishCurriculumPlanningArea] =
  {
    curriculumPlanningAreasStringList.map { planningAreaString =>
      TermlyCurriculumSelection.convertPlanningAreasStringToModel(planningAreaString)
    }
  }


}