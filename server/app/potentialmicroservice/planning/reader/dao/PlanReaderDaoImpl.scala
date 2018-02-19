package potentialmicroservice.planning.reader.dao

import javax.inject.{Inject, Singleton}

import dao.MongoDbConnection
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning.{GroupId, SubjectTermlyPlan, TermlyCurriculumSelection}
import models.timetoteach.{ClassId, TimeToTeachUserId}
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.{Document, MongoCollection}
import play.api.Logger
import potentialmicroservice.planning.sharedschema.{TermlyCurriculumSelectionSchema, TermlyPlanningSchema}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PlanReaderDaoImpl @Inject()(mongoDbConnection: MongoDbConnection) extends PlanReaderDao
  with PlanReaderDaoSubjectTermlyPlanHelper with PlanReaderDaoTermlyCurriculumSelectionHelper
{
  private val logger: Logger = Logger
  private val termlyPlanningCollection: MongoCollection[Document] = mongoDbConnection.getTermlyPlanningCollection
  private val termlyCurriculumSelectionCollection: MongoCollection[Document] = mongoDbConnection.getTermlyCurriculumSelectionCollection

  override def readCurriculumAreaTermlyPlanForGroup(tttUserId: TimeToTeachUserId,
                                                    classId: ClassId,
                                                    groupId: GroupId,
                                                    planningArea: ScottishCurriculumPlanningArea): Future[Option[SubjectTermlyPlan]] =
  {
    logger.info(s"Reading subject termly plan for group from Database: $tttUserId|$classId|$groupId|$planningArea")

    val findMatcher = BsonDocument(
      TermlyPlanningSchema.TTT_USER_ID -> tttUserId.value,
      TermlyPlanningSchema.CLASS_ID -> classId.value,
      TermlyPlanningSchema.GROUP_ID -> groupId.value,
      TermlyPlanningSchema.CURRICULUM_PLANNING_AREA -> planningArea.toString
    )

    val futureFoundTermlyPlanDocuments = termlyPlanningCollection.find(findMatcher).toFuture()
    futureFoundTermlyPlanDocuments.map {
      foundTermlyPlanDocs: Seq[Document] => findLatestVersionOfTermlyPlan(foundTermlyPlanDocs.toList)
    }
  }

  override def currentTermlyCurriculumSelection(tttUserId: TimeToTeachUserId,
                                                classId: ClassId): Future[Option[TermlyCurriculumSelection]] =
  {
    logger.info(s"Looking for latest termly curriculum selection from Database: $tttUserId|$classId")

    val findMatcher = BsonDocument(
      TermlyCurriculumSelectionSchema.TTT_USER_ID -> tttUserId.value,
      TermlyCurriculumSelectionSchema.CLASS_ID -> classId.value
    )

    val futureFoundCurriculumSelectionDocuments = termlyCurriculumSelectionCollection.find(findMatcher).toFuture()
    futureFoundCurriculumSelectionDocuments.map {
      foundTermlyCurriculumSelectionDocs: Seq[Document] => findLatestVersionOfTermlyCurriculumSelection(foundTermlyCurriculumSelectionDocs.toList)
    }
  }
}
