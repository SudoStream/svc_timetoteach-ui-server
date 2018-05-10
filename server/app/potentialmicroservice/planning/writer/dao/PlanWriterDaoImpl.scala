package potentialmicroservice.planning.writer.dao

import dao.MongoDbConnection
import duplicate.model.planning.WeeklyPlanOfOneSubject
import javax.inject.{Inject, Singleton}
import models.timetoteach.planning.{CurriculumAreaTermlyPlan, TermlyCurriculumSelection}
import org.mongodb.scala.{Completed, Document, MongoCollection}
import play.api.Logger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class PlanWriterDaoImpl @Inject()(mongoDbConnection: MongoDbConnection)
  extends PlanWriterDao with PlanWriterDaoSubjectTermlyPlanHelper with PlanWriterDaoTermlyCurriculumSelectionHelper {
  private val termlyPlanningCollection: MongoCollection[Document] = mongoDbConnection.getTermlyPlanningCollection
  private val weeklyPlanningCollection: MongoCollection[Document] = mongoDbConnection.getWeeklyPlanningCollection
  private val lessonPlansCollection: MongoCollection[Document] = mongoDbConnection.getLessonPlanningCollection
  private val termlyCurriculumSelectionCollection: MongoCollection[Document] = mongoDbConnection.getTermlyCurriculumSelectionCollection

  private val logger: Logger = Logger

  override def saveSubjectTermlyPlan(planToSave: CurriculumAreaTermlyPlan): Future[Completed] = {
    val termlyPlanAsDocument = convertTermlyPlanToMongoDbDocument(planToSave)
    logger.info(s"Inserting termly plan to database: ${termlyPlanAsDocument.toString}")
    val observable = termlyPlanningCollection.insertOne(termlyPlanAsDocument)
    observable.toFuture()
  }

  override def saveTermlyCurriculumSelection(termlyCurriculumSelection: TermlyCurriculumSelection): Future[Completed] = {
    val termlyCurriculumSelectionAsDocument = convertTermlyCurriculumSelectionToMongoDbDocument(termlyCurriculumSelection)
    logger.info(s"Inserting termly curriculum selection to database: ${termlyCurriculumSelectionAsDocument.toString}")
    val observable = termlyCurriculumSelectionCollection.insertOne(termlyCurriculumSelectionAsDocument)
    observable.toFuture()
  }

  override def saveWeeklyPlanForSingleSubject(weeklyPlansToSave: WeeklyPlanOfOneSubject): Future[List[Completed]] = {
    val highLevelWeeklyPlanAsDocument = extractWeeklyPlanHighLevelAsMongoDbDocument(weeklyPlansToSave)
    logger.info(s"Inserting high level weekly plan to database: ${highLevelWeeklyPlanAsDocument.toString}")
    val observableWeeklyPlan = weeklyPlanningCollection.insertOne(highLevelWeeklyPlanAsDocument)
    val highLevelPlanInsertFutureComplete = observableWeeklyPlan.toFuture()

    val lessonPlansAsDocuments = extractAllLessonPlansAsMongoDbDocuments(weeklyPlansToSave)
    logger.info(s"Inserting weekly lessons to database: ${lessonPlansAsDocuments .toString}")
    val futureLessonCompletes = for {
      lessonPlanAsDoc <- lessonPlansAsDocuments
      logMsg = logger.info(s"Inserting weekly lessons to database: ${lessonPlanAsDoc .toString}")
      observableLessonPlan = lessonPlansCollection.insertOne(lessonPlanAsDoc)
    } yield observableLessonPlan.toFuture()

    val allFutureInserts = highLevelPlanInsertFutureComplete :: futureLessonCompletes
    Future.sequence(allFutureInserts)
  }

}
