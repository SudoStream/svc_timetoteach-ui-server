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

import scala.annotation.tailrec

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

  def findLatestVersionOfTermlyCurriculumSelectionForEachClassId(foundTermlyCurriculumSelectionDocs: List[Document]): Map[ClassId, Option[TermlyCurriculumSelection]] =
  {
    logger.debug(s"foundTermlyCurriculumSelectionDocs size = ${foundTermlyCurriculumSelectionDocs.size}")
    if (foundTermlyCurriculumSelectionDocs.isEmpty) {
      Map()
    } else {
      val latestVersionOfTermlyPlansDocMap = findLatestVersionOfTermlyCurriculumSelectionForEachClassIdLoop(
        foundTermlyCurriculumSelectionDocs.tail, Map())
      convertMapDocumentToTermlyCurriculumSelection(latestVersionOfTermlyPlansDocMap)
    }
  }

  /////////////////////////////////////////// Impl /////////////////////////////

  @tailrec
  private[dao] final def findLatestVersionOfTermlyCurriculumSelectionForEachClassIdLoop(
                                                                                         remainingTermlySelectionDocs: List[Document],
                                                                                         currentMap: Map[ClassId, Document]
                                                                                       ): Map[ClassId, Document] =
  {
    if (remainingTermlySelectionDocs.isEmpty) currentMap
    else {
      val nextDocToMaybeAdd = remainingTermlySelectionDocs.head
      val maybeNextClassIdOfDoc = safelyGetString(nextDocToMaybeAdd, TermlyCurriculumSelectionSchema.CLASS_ID)
      val nextMapVersion = maybeNextClassIdOfDoc match {
        case Some(nextClassId) =>
          currentMap.get(ClassId(nextClassId)) match {
            case Some(nextDoc) =>
              val currentLatestDoc = currentMap.getOrElse(ClassId(nextClassId), Document())
              val newLatestDoc = mostRecentDoc(currentLatestDoc, nextDocToMaybeAdd)
              currentMap + (ClassId(nextClassId) -> newLatestDoc)
            case None =>
              currentMap + (ClassId(nextClassId) -> nextDocToMaybeAdd)
          }
        case None => currentMap
      }
      findLatestVersionOfTermlyCurriculumSelectionForEachClassIdLoop(remainingTermlySelectionDocs.tail, nextMapVersion)
    }
  }

  @tailrec
  private[dao] final def findLatestVersionOfTermlyCurriculumSelectionLoop(foundTermlyCurriculumSelectionDocs: List[Document], currentLatestDoc: Document): Document =
  {
    if (foundTermlyCurriculumSelectionDocs.isEmpty) currentLatestDoc
    else {
      val nextDoc = foundTermlyCurriculumSelectionDocs.head
      val newLatestDoc: Document = mostRecentDoc(currentLatestDoc, nextDoc)
      findLatestVersionOfTermlyCurriculumSelectionLoop(foundTermlyCurriculumSelectionDocs.tail, newLatestDoc)
    }
  }

  private def mostRecentDoc(currentLatestDoc: Document, nextDoc: Document) =
  {
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
    newLatestDoc
  }

  private[dao] def convertMapDocumentToTermlyCurriculumSelection(classIdToLatestDoc: Map[ClassId, Document]): Map[ClassId, Option[TermlyCurriculumSelection]] =
  {
    {
      for {
        classId <- classIdToLatestDoc.keys
        docToConvert = classIdToLatestDoc(classId)
        maybeTermlyCurriculumSelection = convertDocumentToTermlyCurriculumSelection(docToConvert)
      } yield (classId, maybeTermlyCurriculumSelection)
    }.toMap
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