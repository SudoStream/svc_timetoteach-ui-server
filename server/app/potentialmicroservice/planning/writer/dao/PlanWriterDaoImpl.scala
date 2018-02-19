package potentialmicroservice.planning.writer.dao

import javax.inject.{Inject, Singleton}

import dao.MongoDbConnection
import models.timetoteach.planning.{CurriculumAreaTermlyPlan, TermlyCurriculumSelection}
import org.mongodb.scala.{Completed, Document, MongoCollection}
import play.api.Logger

import scala.concurrent.Future

@Singleton
class PlanWriterDaoImpl @Inject()(mongoDbConnection: MongoDbConnection)
  extends PlanWriterDao with PlanWriterDaoSubjectTermlyPlanHelper with PlanWriterDaoTermlyCurriculumSelectionHelper
{
  private val termlyPlanningCollection: MongoCollection[Document] = mongoDbConnection.getTermlyPlanningCollection
  private val termlyCurriculumSelectionCollection: MongoCollection[Document] = mongoDbConnection.getTermlyCurriculumSelectionCollection
  private val logger: Logger = Logger

  override def saveSubjectTermlyPlan(planToSave: CurriculumAreaTermlyPlan): Future[Completed] =
  {
    val termlyPlanAsDocument = convertTermlyPlanToMongoDbDocument(planToSave)
    logger.info(s"Inserting termly plan to database: ${termlyPlanAsDocument.toString}")
    val observable = termlyPlanningCollection.insertOne(termlyPlanAsDocument)
    observable.toFuture()
  }

  override def saveTermlyCurriculumSelection(termlyCurriculumSelection: TermlyCurriculumSelection): Future[Completed] =
  {
    val termlyCurriculumSelectionAsDocument = convertTermlyCurriculumSelectionToMongoDbDocument(termlyCurriculumSelection)
    logger.info(s"Inserting termly curriculum selection to database: ${termlyCurriculumSelectionAsDocument.toString}")
    val observable = termlyCurriculumSelectionCollection.insertOne(termlyCurriculumSelectionAsDocument)
    observable.toFuture()
  }
}
