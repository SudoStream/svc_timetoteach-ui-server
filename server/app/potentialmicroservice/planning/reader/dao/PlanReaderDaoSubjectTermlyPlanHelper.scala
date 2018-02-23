package potentialmicroservice.planning.reader.dao

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import duplicate.model._
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning._
import models.timetoteach.term.SchoolTerm
import models.timetoteach.{ClassId, TimeToTeachUserId}
import org.bson.{BsonArray, BsonDocument}
import org.mongodb.scala.Document
import org.mongodb.scala.bson.BsonValue
import play.api.Logger
import potentialmicroservice.planning.sharedschema.TermlyPlanningSchema
import utils.mongodb.MongoDbSafety._

import scala.annotation.tailrec

case class _CurriculumAreaName(curriculumAreaName: String)

case class _GroupId(groupId: String)


object PlanReaderDaoSubjectTermlyPlanHelper
{
  val NO_GROUP_ID_FOUND = "NO_GROUP_ID_FOUND"
  val CLASS_LEVEL = "CLASS_LEVEL"
}

trait PlanReaderDaoSubjectTermlyPlanHelper extends PlanReaderDaoCommonHelper
{
  import PlanReaderDaoSubjectTermlyPlanHelper.{CLASS_LEVEL, NO_GROUP_ID_FOUND}

  private val logger: Logger = Logger
  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")


  def findLatestVersionOfTermlyPlan(foundTermlyPlanDocs: List[Document]): Option[CurriculumAreaTermlyPlan] =
  {
    logger.debug(s"foundTermlyPlanDocs size = ${foundTermlyPlanDocs.size}")
    if (foundTermlyPlanDocs.isEmpty) {
      None
    } else {
      val latestVersionOfTermlyPlansDoc = findLatestVersionOfTermlyPlanDocLoop(foundTermlyPlanDocs.tail, foundTermlyPlanDocs.head)
      convertDocumentToSubjectTermlyPlan(latestVersionOfTermlyPlansDoc)
    }
  }


  def buildCurriculumPlanProgressForClass(foundCurriculumPlanningDocs: List[Document],
                                          classDetails: ClassDetails,
                                          planningAreas: List[ScottishCurriculumPlanningArea]): Option[CurriculumPlanProgressForClass] =
  {
    logger.debug(s"buildCurriculumPlanProgressForClass() size = ${foundCurriculumPlanningDocs.size}")
    if (foundCurriculumPlanningDocs.isEmpty) {
      None
    } else {
      val curriculumPlanningAreaToLatestDoc = buildCurriculumPlanProgressForClassLoop(foundCurriculumPlanningDocs, Map())
      convertDocumentToCurriculumPlanProgressForClass(curriculumPlanningAreaToLatestDoc, classDetails.groups, planningAreas)
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////// Implementation buildCurriculumPlanProgressForClass ////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////

  private def extractGroupId(maybeGroupId: Option[String]): String =
  {
    val groupId = maybeGroupId.getOrElse(NO_GROUP_ID_FOUND)
    if (groupId.isEmpty) {
      CLASS_LEVEL
    } else {
      groupId
    }
  }

  private[dao] def buildCurriculumPlanProgressForClassLoop(remainingDocs: List[Document],
                                                           currentCurriculumProgressMap: Map[_CurriculumAreaName, Map[_GroupId, Document]])
  : Map[_CurriculumAreaName, Map[_GroupId, Document]] =
  {
    if (remainingDocs.isEmpty) currentCurriculumProgressMap
    else {
      val nextDoc = remainingDocs.head
      val maybeCurriculumAreaForNextDoc = safelyGetString(nextDoc, TermlyPlanningSchema.CURRICULUM_PLANNING_AREA)
      val newCurriculumProgressMap: Map[_CurriculumAreaName, Map[_GroupId, Document]] = maybeCurriculumAreaForNextDoc match {
        case Some(curriculumAreaForNextDoc) =>
          val maybeGroupIdForNextDoc = safelyGetString(nextDoc, TermlyPlanningSchema.GROUP_ID)

          if (currentCurriculumProgressMap.isDefinedAt(_CurriculumAreaName(curriculumAreaForNextDoc))) {
            val currentGroupIdToLatestDocument = currentCurriculumProgressMap(_CurriculumAreaName(curriculumAreaForNextDoc))
            val groupId = extractGroupId(maybeGroupIdForNextDoc)

            val latestGroupIdToLatestDoc: Map[_GroupId, Document] = if (currentGroupIdToLatestDocument.isDefinedAt(_GroupId(groupId))) {
              val currentLatestDoc = currentGroupIdToLatestDocument(_GroupId(groupId))
              val latestDoc = findLatestVersionOfTermlyPlanDocLoop(nextDoc :: Nil, currentLatestDoc)
              currentGroupIdToLatestDocument + (_GroupId(groupId) -> latestDoc)
            } else {
              currentGroupIdToLatestDocument + (_GroupId(groupId) -> nextDoc)
            }

            currentCurriculumProgressMap + (_CurriculumAreaName(curriculumAreaForNextDoc) -> latestGroupIdToLatestDoc)

          } else {
            currentCurriculumProgressMap + (_CurriculumAreaName(curriculumAreaForNextDoc) ->
              Map(_GroupId(extractGroupId(maybeGroupIdForNextDoc)) -> nextDoc))
          }
        case None => currentCurriculumProgressMap
      }
      buildCurriculumPlanProgressForClassLoop(remainingDocs.tail, newCurriculumProgressMap)
    }
  }

  private[dao] def createZeroedProgressMap(planningAreas: List[ScottishCurriculumPlanningArea],
                                           classGroups: List[Group]):
  Map[ScottishCurriculumPlanningAreaWrapper,
    (OverallClassLevelProgressPercent, Map[Group, GroupLevelProgressPercent])] =
  {
    @tailrec
    def buildZeroedMapLoop(planningAreasToAdd: List[ScottishCurriculumPlanningArea],
                           currentZeroedMap: Map[ScottishCurriculumPlanningAreaWrapper,
                             (OverallClassLevelProgressPercent, Map[Group, GroupLevelProgressPercent])]
                          ): Map[ScottishCurriculumPlanningAreaWrapper,
      (OverallClassLevelProgressPercent, Map[Group, GroupLevelProgressPercent])] =
    {
      if (planningAreasToAdd.isEmpty) {
        currentZeroedMap
      } else {
        val nextPlanningAreaToAdd = ScottishCurriculumPlanningAreaWrapper(planningAreasToAdd.head)
        val newVersionOfZeroedMap = if (currentZeroedMap.isDefinedAt(nextPlanningAreaToAdd)) {
          logger.warn("To be honest I would be surprised if this line is exceuted. Will add all groups in a oner. Just returning current version")
          currentZeroedMap
        } else {
          val newGroupsToAdd = classGroups.filter(group => group.groupType == nextPlanningAreaToAdd.groupType)
          val newGroupToProgressMap = {
            for {
              group <- newGroupsToAdd
            } yield (group, GroupLevelProgressPercent(0))
          }.toMap

          currentZeroedMap + (nextPlanningAreaToAdd -> (OverallClassLevelProgressPercent(0), newGroupToProgressMap))
        }
        buildZeroedMapLoop(planningAreasToAdd.tail, newVersionOfZeroedMap)
      }
    }

    buildZeroedMapLoop(planningAreas, Map())
  }

  def lookupGroup(groupId: _GroupId, classGroups: List[Group]): Option[Group] =
  {
    val foundGroups = classGroups.filter(group => group.groupId.id == groupId.groupId)
    foundGroups.headOption
  }

  private[dao] def addGroupAndClassLevelProgressMap(initialZeroedProgress:
                                            Map[ScottishCurriculumPlanningAreaWrapper,
                                              (OverallClassLevelProgressPercent, Map[Group, GroupLevelProgressPercent])],
                                                    curriculumPlanningAreaToLatestDoc: Map[_CurriculumAreaName, Map[_GroupId, Document]],
                                                    classGroups: List[Group]):
  Map[ScottishCurriculumPlanningAreaWrapper,
    (OverallClassLevelProgressPercent, Map[Group, GroupLevelProgressPercent])] =
  {
    @tailrec
    def addGroupLevelProgressMapLoop(remainingGroupsToUpdate: List[(_CurriculumAreaName, _GroupId, Document)],
                                     currentProgress: Map[ScottishCurriculumPlanningAreaWrapper,
                                       (OverallClassLevelProgressPercent, Map[Group, GroupLevelProgressPercent])]):
    Map[ScottishCurriculumPlanningAreaWrapper, (OverallClassLevelProgressPercent, Map[Group, GroupLevelProgressPercent])] =
    {
      if (remainingGroupsToUpdate.isEmpty) currentProgress
      else {
        val nextGroupProgressToAddTuple3 = remainingGroupsToUpdate.head
        val maybeNextPlanningAreaToAdd = CurriculumAreaConverter.convertCurriculumAreaStringToModel(nextGroupProgressToAddTuple3._1.curriculumAreaName)
        val nextProgress: Map[ScottishCurriculumPlanningAreaWrapper, (OverallClassLevelProgressPercent, Map[Group, GroupLevelProgressPercent])] = if (maybeNextPlanningAreaToAdd.isDefined) {
          val planningAreaKey = ScottishCurriculumPlanningAreaWrapper(maybeNextPlanningAreaToAdd.get)
          if (currentProgress.isDefinedAt(planningAreaKey)) {
            val currentClassGroupProgressTuple = currentProgress(planningAreaKey)
            val maybeNextGroupToAdd = lookupGroup(nextGroupProgressToAddTuple3._2, classGroups)
            maybeNextGroupToAdd match {
              case Some(group) => if (currentClassGroupProgressTuple._2.isDefinedAt(group)) {
                logger.warn(s"ANDY ADD HERE YEAH!!!!!!!!!!!!!!!!")
                // TODO: We are assuming at this point that if we have a document saved, then this is 100% at this group level
                // Obviously there could be a list of 0 es and os, which would mean, or perhaps mean, no progress.
                val newGroupProgressMap = currentClassGroupProgressTuple._2 + (group -> GroupLevelProgressPercent(100))
                val classLevelPercent = newGroupProgressMap.values.map(wrapped => wrapped.percentValue).sum / newGroupProgressMap.values.size
                val newClassGroupProgressTuple = (OverallClassLevelProgressPercent(classLevelPercent), newGroupProgressMap)
                currentProgress + (planningAreaKey -> newClassGroupProgressTuple)
              } else {
                logger.warn(s"The group with id ${nextGroupProgressToAddTuple3._2} is NOT defined in the map. I would have expected it to be this point. Should already have been created.")
                currentProgress
              }
              case None =>
                if (nextGroupProgressToAddTuple3._2.groupId == CLASS_LEVEL) {
                  logger.info(s"Okay, cool, we have a class level one to add here to ${planningAreaKey.niceValue()}")
                  currentProgress + (planningAreaKey -> (OverallClassLevelProgressPercent(100), Map()))
                } else {
                  logger.warn(s"Could not find group with id ${nextGroupProgressToAddTuple3._2}")
                  currentProgress
                }
            }
          } else {
            logger.warn(s"$planningAreaKey is not defined in the map. This is most odd")
            currentProgress
          }
        } else {
          logger.warn(s"The following planning area seems invalid : ${nextGroupProgressToAddTuple3._1.curriculumAreaName}")
          currentProgress
        }

        addGroupLevelProgressMapLoop(remainingGroupsToUpdate.tail, nextProgress)
      }
    }

    val curriculumPlanningAreaToLatestDocFlattened = {
      for {
        planningArea <- curriculumPlanningAreaToLatestDoc.keys
        overallProgressAndGroupProgressTuple <- curriculumPlanningAreaToLatestDoc(planningArea)
      } yield (planningArea, overallProgressAndGroupProgressTuple._1, curriculumPlanningAreaToLatestDoc(planningArea)(overallProgressAndGroupProgressTuple._1))
    }.toList

    addGroupLevelProgressMapLoop(curriculumPlanningAreaToLatestDocFlattened, initialZeroedProgress)
  }

  private[dao] def convertDocumentToCurriculumPlanProgressForClass(curriculumPlanningAreaToLatestDoc:
                                                                   Map[_CurriculumAreaName, Map[_GroupId, Document]],
                                                                   classGroups: List[Group],
                                                                   planningAreas: List[ScottishCurriculumPlanningArea]
                                                                  )
  : Option[CurriculumPlanProgressForClass] =
  {
    val initialZeroedProgress = createZeroedProgressMap(planningAreas, classGroups)
    val groupAndClassLevelProgress = addGroupAndClassLevelProgressMap(initialZeroedProgress, curriculumPlanningAreaToLatestDoc, classGroups)
    Some(CurriculumPlanProgressForClass(groupAndClassLevelProgress))
  }


  //////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////// Implementation findLatestVersionOfTermlyPlan //////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////

  private[dao] def findLatestVersionOfTermlyPlanDocLoop(foundTermlyPlanDocs: List[Document],
                                                        currentLatestDoc: Document): Document =
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

  private def convertToEsAndOsWithBenchmarks(maybeEandOsWithBenchmarks: Option[BsonValue])
  : List[EandOsWithBenchmarks] =
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

  private[dao] def convertDocumentToSubjectTermlyPlan(doc: Document): Option[CurriculumAreaTermlyPlan] =
  {
    for {
      tttUserId <- safelyGetStringNoneIfBlank(doc, TermlyPlanningSchema.TTT_USER_ID)
      planTypeStringValue <- safelyGetStringNoneIfBlank(doc, TermlyPlanningSchema.PLAN_TYPE)
      planType <- PlanType.createPlanTypeFromString(planTypeStringValue)
      classId <- safelyGetStringNoneIfBlank(doc, TermlyPlanningSchema.CLASS_ID)
      maybeGroupId = safelyGetStringNoneIfBlank(doc, TermlyPlanningSchema.GROUP_ID)
      groupId = maybeGroupId match {
        case Some(id) => Some(models.timetoteach.planning.GroupId(id))
        case None => None
      }
      curriculumPlanningAreaValue <- safelyGetStringNoneIfBlank(doc, TermlyPlanningSchema.CURRICULUM_PLANNING_AREA)
      curriculumPlanningArea <- CurriculumAreaConverter.convertCurriculumAreaStringToModel(curriculumPlanningAreaValue)
      createdTimeString <- safelyGetStringNoneIfBlank(doc, TermlyPlanningSchema.CREATED_TIMESTAMP)

      maybeSchoolTermBsonValue = doc.get(TermlyPlanningSchema.SCHOOL_TERM)
      maybeSchoolTerm: Option[SchoolTerm] = convertToSchoolTerm(maybeSchoolTermBsonValue)
      if maybeSchoolTerm.isDefined

      maybeEandOsWithBenchmarks = doc.get(TermlyPlanningSchema.SELECTED_ES_AND_OS_WITH_BENCHMARKS)
      eAndOsWithBenchmarks = convertToEsAndOsWithBenchmarks(maybeEandOsWithBenchmarks)
    } yield CurriculumAreaTermlyPlan(
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
