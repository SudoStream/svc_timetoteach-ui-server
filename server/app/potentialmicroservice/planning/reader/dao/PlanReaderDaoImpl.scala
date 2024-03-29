package potentialmicroservice.planning.reader.dao

import dao.MongoDbConnection
import duplicate.model
import duplicate.model.ClassDetails
import duplicate.model.esandos.{CompletedEsAndOsByGroup, CompletedEsAndOsByGroupBySubject, StartedEsAndOsByGroupBySubject}
import duplicate.model.planning.FullWeeklyPlanOfLessons
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import javax.inject.{Inject, Singleton}
import models.timetoteach.planning._
import models.timetoteach.term.SchoolTerm
import models.timetoteach.{ClassId, TimeToTeachUserId}
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonString}
import org.mongodb.scala.{Document, MongoCollection}
import play.api.Logger
import potentialmicroservice.planning.sharedschema.{TermlyCurriculumSelectionSchema, TermlyPlanningSchema}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

@Singleton
class PlanReaderDaoImpl @Inject()(mongoDbConnection: MongoDbConnection) extends PlanReaderDao
  with PlanReaderDaoSubjectTermlyPlanHelper with PlanReaderDaoTermlyCurriculumSelectionHelper
  with RetrieveFullWeekOfLessonsDaoHelper {

  private val logger: Logger = Logger
  private val termlyPlanningCollection: MongoCollection[Document] = mongoDbConnection.getTermlyPlanningCollection
  private val termlyCurriculumSelectionCollection: MongoCollection[Document] = mongoDbConnection.getTermlyCurriculumSelectionCollection

  override def getDbConnection: MongoDbConnection = mongoDbConnection

  override def currentTermlyCurriculumSelection(tttUserId: TimeToTeachUserId,
                                                classId: ClassId,
                                                term: SchoolTerm): Future[Option[TermlyCurriculumSelection]] = {
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

  override def currentTermlyCurriculumSelection(tttUserId: TimeToTeachUserId, classes: List[ClassId], term: SchoolTerm): Future[Map[ClassId, Option[TermlyCurriculumSelection]]] = {
    logger.info(s"Looking for latest termly curriculum selection from Database for following class ids: $tttUserId|$term|${classes.toString}")

    val findMatcher = BsonDocument(
      TermlyCurriculumSelectionSchema.TTT_USER_ID -> tttUserId.value,

      TermlyCurriculumSelectionSchema.CLASS_ID -> BsonDocument(
        "$in" -> createClassIdArray(classes)
      ),

      TermlyCurriculumSelectionSchema.SCHOOL_TERM -> BsonDocument(
        TermlyCurriculumSelectionSchema.SCHOOL_YEAR -> term.schoolYear.niceValue,
        TermlyCurriculumSelectionSchema.SCHOOL_TERM_NAME -> term.schoolTermName.toString,
        TermlyCurriculumSelectionSchema.SCHOOL_TERM_FIRST_DAY -> term.termFirstDay.toString,
        TermlyCurriculumSelectionSchema.SCHOOL_TERM_LAST_DAY -> term.termLastDay.toString
      )
    )

    logger.debug(s"Looking for latest termly curriculum selection from Database with matcher ${findMatcher.toString}")

    val futureFoundCurriculumSelectionDocuments = termlyCurriculumSelectionCollection.find(findMatcher).toFuture()

    futureFoundCurriculumSelectionDocuments.onComplete {
      case Success(docsFound) =>
        logger.info(s"currentTermlyCurriculumSelection found ${docsFound.size}")
        logger.debug(s"currentTermlyCurriculumSelection found ${docsFound.toString}")
      case Failure(ex) => logger.error("Problem finding termly selections : " + ex.getMessage)
    }

    futureFoundCurriculumSelectionDocuments.map {
      foundTermlyCurriculumSelectionDocs: Seq[Document] => findLatestVersionOfTermlyCurriculumSelectionForEachClassId(foundTermlyCurriculumSelectionDocs.toList)
    }

  }

  override def curriculumPlanProgress(tttUserId: TimeToTeachUserId,
                                      classDetails: ClassDetails,
                                      planningAreas: List[ScottishCurriculumPlanningArea],
                                      term: SchoolTerm): Future[Option[CurriculumPlanProgressForClass]] = {
    logger.info(s"Looking for latest plan progress from Database: $tttUserId|$classDetails|$planningAreas|$term")

    val findMatcher = BsonDocument(
      TermlyPlanningSchema.TTT_USER_ID -> tttUserId.value,
      TermlyPlanningSchema.CLASS_ID -> classDetails.id.id,
      TermlyPlanningSchema.SCHOOL_TERM -> BsonDocument(
        TermlyPlanningSchema.SCHOOL_YEAR -> term.schoolYear.niceValue,
        TermlyPlanningSchema.SCHOOL_TERM_NAME -> term.schoolTermName.toString,
        TermlyPlanningSchema.SCHOOL_TERM_FIRST_DAY -> term.termFirstDay.toString,
        TermlyPlanningSchema.SCHOOL_TERM_LAST_DAY -> term.termLastDay.toString
      )
    )

    logger.debug(s"Looking for latest termly curriculum selection from Database with matcher ${findMatcher.toString}")

    val futureFoundCurriculumPlanningDocs = termlyPlanningCollection.find(findMatcher).toFuture()
    futureFoundCurriculumPlanningDocs.map {
      foundCurriculumPlanningDocs: Seq[Document] =>
        buildCurriculumPlanProgressForClass(
          foundCurriculumPlanningDocs.toList,
          classDetails,
          planningAreas
        )
    }
  }

  override def curriculumPlanProgressForClasses(tttUserId: TimeToTeachUserId,
                                                classes: List[ClassDetails],
                                                classIdToPlanningSelection: Map[ClassId, List[ScottishCurriculumPlanningArea]],
                                                term: SchoolTerm): Future[Map[model.ClassId, Int]] = {
    logger.debug(s"curriculumPlanProgressForClasses() : ${classIdToPlanningSelection.toString()}")

    val listOfClassId_FutureMaybePlanProgress = for {
      classDetails <- classes
      if classIdToPlanningSelection.isDefinedAt(models.timetoteach.ClassId(classDetails.id.id))
      planningAreasForClass = classIdToPlanningSelection(models.timetoteach.ClassId(classDetails.id.id))
      futureMaybePlanProgress = curriculumPlanProgress(tttUserId, classDetails, planningAreasForClass, term)
    } yield (classDetails.id, futureMaybePlanProgress)

    val listOfFutureMaybePlanProgressWithHandleErrors = listOfClassId_FutureMaybePlanProgress.map { elem =>
      (elem._1, elem._2.recover {
        case NonFatal(e) =>
          logger.warn(s"Had an issue processing plan progress: ${e.getMessage}")
          None
      }
      )
    }

    val classIdToFutureInt = for {
      classIdToEventualMaybeCurriculumPlanProgressTuple <- listOfFutureMaybePlanProgressWithHandleErrors
      classId = classIdToEventualMaybeCurriculumPlanProgressTuple._1
      eventualMaybeCurriculumPlanProgress = classIdToEventualMaybeCurriculumPlanProgressTuple._2
      overallClassProgressPercent = eventualMaybeCurriculumPlanProgress map {
        case Some(curriculumProgress) =>
          val progressForCurriculumNumerator = curriculumProgress.curriculumProgressMap.values.map { elem => elem._1.percentValue }.sum
          val progressForCurriculumDenominator = curriculumProgress.curriculumProgressMap.values.size
          (classId, progressForCurriculumNumerator / progressForCurriculumDenominator)
        case None => (classId, 0)
      }
    } yield overallClassProgressPercent

    Future.sequence(classIdToFutureInt).map {
      listOfClassIdToOverallProgressPercent => listOfClassIdToOverallProgressPercent.toMap
    }
  }


  override def readCurriculumAreaTermlyPlanForGroup(tttUserId: TimeToTeachUserId,
                                                    classId: ClassId,
                                                    groupId: GroupId,
                                                    planningArea: ScottishCurriculumPlanningArea): Future[Option[CurriculumAreaTermlyPlan]] = {
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

  override def readCurriculumAreaTermlyPlanForClassLevel(tttUserId: TimeToTeachUserId, classId: ClassId, planningArea: ScottishCurriculumPlanningArea): Future[Option[CurriculumAreaTermlyPlan]] = {
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


  override def retrieveFullWeekOfLessons(tttUserId: TimeToTeachUserId,
                                         classId: ClassId,
                                         mondayDateOfWeekIso: String): Future[FullWeeklyPlanOfLessons] = {
    retrieveFullWeekOfLessonsImpl(tttUserId, classId, mondayDateOfWeekIso)
  }

  override def completedEsOsBenchmarks(
                                        tttUserId: TimeToTeachUserId,
                                        classId: ClassId,
                                        mondayDateOfWeekIso: String
                                      ): Future[CompletedEsAndOsByGroupBySubject] = {
    completedEsOsBenchmarksImpl(tttUserId, classId, mondayDateOfWeekIso)
  }

  def completedAndStartedEsOsBenchmarks(
                                         tttUserId: TimeToTeachUserId,
                                         classId: ClassId,
                                         mondayDateOfWeekIso: String
                                       ): Future[(CompletedEsAndOsByGroupBySubject, StartedEsAndOsByGroupBySubject)] = {
    completedAndStartedEsOsBenchmarksImpl(tttUserId, classId, mondayDateOfWeekIso)
  }


  ////


  private def createClassIdArray(classes: List[ClassId]): BsonArray = {
    BsonArray({
      for {
        classId <- classes
      } yield BsonString(classId.value)
    })
  }

}