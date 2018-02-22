package potentialmicroservice.planning.reader.dao

import javax.inject.{Inject, Singleton}
import dao.MongoDbConnection
import duplicate.model.ClassDetails
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning._
import models.timetoteach.term.SchoolTerm
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

  override def currentTermlyCurriculumSelection(tttUserId: TimeToTeachUserId,
                                                classId: ClassId,
                                                term: SchoolTerm): Future[Option[TermlyCurriculumSelection]] =
  {
    logger.info(s"Looking for latest termly curriculum selection from Database: $tttUserId|$classId|$term")

    val findMatcher = BsonDocument(
      TermlyCurriculumSelectionSchema.TTT_USER_ID -> tttUserId.value,
      TermlyCurriculumSelectionSchema.CLASS_ID -> classId.value,
      TermlyCurriculumSelectionSchema.SCHOOL_TERM -> BsonDocument(
        TermlyCurriculumSelectionSchema.SCHOOL_YEAR -> term.schoolYear.niceValue,
        TermlyCurriculumSelectionSchema.SCHOOL_TERM_NAME -> term.schoolTermName.toString,
        TermlyCurriculumSelectionSchema.SCHOOL_TERM_FIRST_DAY -> term.termFirstDay.toString,
        TermlyCurriculumSelectionSchema.SCHOOL_TERM_LAST_DAY -> term.termLastDay.toString
      )
    )

    logger.debug(s"Looking for latest termly curriculum selection from Database with matcher ${findMatcher.toString}")

    val futureFoundCurriculumSelectionDocuments = termlyCurriculumSelectionCollection.find(findMatcher).toFuture()
    futureFoundCurriculumSelectionDocuments.map {
      foundTermlyCurriculumSelectionDocs: Seq[Document] => findLatestVersionOfTermlyCurriculumSelection(foundTermlyCurriculumSelectionDocs.toList)
    }
  }

  override def curriculumPlanProgress(tttUserId: TimeToTeachUserId,
                                      classDetails: ClassDetails,
                                      planningAreas: List[ScottishCurriculumPlanningArea],
                                      term: SchoolTerm): Future[Option[CurriculumPlanProgressForClass]] = {
    Future{
      None
    }
  }

  override def readCurriculumAreaTermlyPlanForGroup(tttUserId: TimeToTeachUserId,
                                                    classId: ClassId,
                                                    groupId: GroupId,
                                                    planningArea: ScottishCurriculumPlanningArea): Future[Option[CurriculumAreaTermlyPlan]] =
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

  override def readCurriculumAreaTermlyPlanForClassLevel(tttUserId: TimeToTeachUserId, classId: ClassId, planningArea: ScottishCurriculumPlanningArea): Future[Option[CurriculumAreaTermlyPlan]] =
  {
    logger.info(s"Reading subject termly plan for class level from Database: $tttUserId|$classId|$planningArea")

    val findMatcher = BsonDocument(
      TermlyPlanningSchema.TTT_USER_ID -> tttUserId.value,
      TermlyPlanningSchema.CLASS_ID -> classId.value,
      TermlyPlanningSchema.CURRICULUM_PLANNING_AREA -> planningArea.toString,
      TermlyPlanningSchema.PLAN_TYPE -> PlanType.CLASS_LEVEL_PLAN.toString
    )

    val futureFoundTermlyPlanDocuments = termlyPlanningCollection.find(findMatcher).toFuture()
    futureFoundTermlyPlanDocuments.map {
      foundTermlyPlanDocs: Seq[Document] => findLatestVersionOfTermlyPlan(foundTermlyPlanDocs.toList)
    }
  }
}
