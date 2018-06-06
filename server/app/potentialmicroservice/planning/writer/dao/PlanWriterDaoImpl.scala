package potentialmicroservice.planning.writer.dao

import java.time.LocalDate

import controllers.time.SystemTime
import dao.MongoDbConnection
import duplicate.model.esandos.{CompletedEsAndOsByGroup, NotStartedEsAndOsByGroup}
import duplicate.model.planning.WeeklyPlanOfOneSubject
import javax.inject.{Inject, Singleton}
import models.timetoteach.planning.{CurriculumAreaTermlyPlan, TermlyCurriculumSelection}
import org.mongodb.scala.{Completed, Document, MongoCollection}
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PlanWriterDaoImpl @Inject()(
                                   mongoDbConnection: MongoDbConnection,
                                   systemTime: SystemTime
                                 )
  extends PlanWriterDao with PlanWriterDaoSubjectTermlyPlanHelper with PlanWriterDaoTermlyCurriculumSelectionHelper {
  private val termlyPlanningCollection: MongoCollection[Document] = mongoDbConnection.getTermlyPlanningCollection
  private val weeklyPlanningCollection: MongoCollection[Document] = mongoDbConnection.getWeeklyPlanningCollection
  private val lessonPlansCollection: MongoCollection[Document] = mongoDbConnection.getLessonPlanningCollection
  private val eAndOBenchmarkStatusCollection: MongoCollection[Document] = mongoDbConnection.getEAndOBenchmarkStatusCollection
  private val termlyCurriculumSelectionCollection: MongoCollection[Document] = mongoDbConnection.getTermlyCurriculumSelectionCollection

  private val logger: Logger = Logger

  override def getSystemDate: Future[LocalDate] = {
    systemTime.getToday
  }

  override def saveSubjectTermlyPlan(planToSave: CurriculumAreaTermlyPlan): Future[Completed] = {
    val termlyPlanAsDocument = convertTermlyPlanToMongoDbDocument(planToSave)
    logger.info(s"Inserting termly plan to database: ${termlyPlanAsDocument.toString}")
    val observable = termlyPlanningCollection.insertOne(termlyPlanAsDocument)
    observable.toFuture()
  }

  override def saveTermlyCurriculumSelection(
                                              termlyCurriculumSelection: TermlyCurriculumSelection
                                            ): Future[Completed] = {
    val termlyCurriculumSelectionAsDocument = convertTermlyCurriculumSelectionToMongoDbDocument(termlyCurriculumSelection)
    logger.info(s"Inserting termly curriculum selection to database: ${termlyCurriculumSelectionAsDocument.toString}")
    val observable = termlyCurriculumSelectionCollection.insertOne(termlyCurriculumSelectionAsDocument)
    observable.toFuture()
  }

  private def insertEandOBenchyStatuesDocs(docsToInsert: List[Document]): Future[List[Completed]] = {
    val futureCompletes = for {
      eAndOBenchmarkStatusAsDoc: Document <- docsToInsert
      logMsg = logger.info(s"Inserting completed/notStarted esOsBenchies to database: ${eAndOBenchmarkStatusAsDoc.toString}")
      observableLessonPlan = eAndOBenchmarkStatusCollection.insertOne(eAndOBenchmarkStatusAsDoc)
    } yield observableLessonPlan.toFuture()

    Future.sequence(futureCompletes)
  }

  private def insertWeeklyLessonsDocs(docsToInsert: List[Document]): Future[List[Completed]] = {
    val futureCompletes = for {
      lessonPlanAsDoc <- docsToInsert
      log3 = logger.info(s"Inserting weekly lessons to database: ${lessonPlanAsDoc.toString}")
      observableLessonPlan = lessonPlansCollection.insertOne(lessonPlanAsDoc)
    } yield observableLessonPlan.toFuture()

    Future.sequence(futureCompletes)
  }

  override def saveWeeklyPlanForSingleSubject(
                                               weeklyPlansToSave: WeeklyPlanOfOneSubject,
                                               completedEsAndOsByGroup: CompletedEsAndOsByGroup,
                                               notStartedEsOsBenchies: NotStartedEsAndOsByGroup
                                             ): Future[List[Completed]] = {

    val eventualInserts = for {
      today <- getSystemDate
      highLevelWeeklyPlanAsDocument = extractWeeklyPlanHighLevelAsMongoDbDocument(weeklyPlansToSave, today)
      log1 = logger.info(s"Inserting high level weekly plan to database: ${highLevelWeeklyPlanAsDocument.toString}")
      highLevelInsert = weeklyPlanningCollection.insertOne(highLevelWeeklyPlanAsDocument)

      lessonPlansAsDocuments = extractAllLessonPlansAsMongoDbDocuments(weeklyPlansToSave, today)
      log2 = logger.info(s"Inserting weekly lessons to database: ${lessonPlansAsDocuments.toString}")

      eventualWeeklyInserts = insertWeeklyLessonsDocs(lessonPlansAsDocuments)
      eventualHighLevelInsert = highLevelInsert.toFuture()
      highLevelInsert <- eventualHighLevelInsert
      weeklyInserts <- eventualWeeklyInserts
    } yield highLevelInsert :: weeklyInserts


    val eventualTupleCompletedNotStarted = for {
      today <- getSystemDate
      completedEsOsBenchiesAsDocument = extractEsOsBenchiesAsDocuments(Left(completedEsAndOsByGroup), weeklyPlansToSave, today)
      notStartedEsOsBenchiesAsDocument = extractEsOsBenchiesAsDocuments(Right(notStartedEsOsBenchies), weeklyPlansToSave, today)
      selectedEsOsBenchiesAsDocument = createEsOsBenchiesAsDocuments(weeklyPlansToSave, "STARTED", weeklyPlansToSave.groupToEsOsBenchmarks, today)
      log1 = logger.info(s"Inserting completed/notStarted esOsBenchies to database: " +
        s"${completedEsOsBenchiesAsDocument.toString()} :: " +
        s"${notStartedEsOsBenchiesAsDocument.toString()} :: " +
        s"${selectedEsOsBenchiesAsDocument.toString} ")
    } yield (completedEsOsBenchiesAsDocument, notStartedEsOsBenchiesAsDocument, selectedEsOsBenchiesAsDocument )

    val futureCompletesAndNotStartedInserts = {
      for {
        tupleCompletedNotStarted <- eventualTupleCompletedNotStarted
      } yield insertEandOBenchyStatuesDocs(tupleCompletedNotStarted._1 ::: tupleCompletedNotStarted._2 ::: tupleCompletedNotStarted._3)
    }.flatMap(res => res)

    for {
      futures1 <- eventualInserts
      futures2 <- futureCompletesAndNotStartedInserts
    } yield futures1 ::: futures2
  }

}
