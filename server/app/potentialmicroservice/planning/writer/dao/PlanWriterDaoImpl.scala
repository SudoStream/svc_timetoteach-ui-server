package potentialmicroservice.planning.writer.dao

import javax.inject.{Inject, Singleton}

import dao.MongoDbConnection
import models.timetoteach.planning.SubjectTermlyPlan
import org.mongodb.scala.{Completed, Document, MongoCollection}
import play.api.Logger

import scala.concurrent.Future

@Singleton
class PlanWriterDaoImpl @Inject()(mongoDbConnection: MongoDbConnection)
  extends PlanWriterDao with PlanWriterDaoSubjectTermlyPlanHelper
{
  private val termlyPlanningCollection: MongoCollection[Document] = mongoDbConnection.getTermlyPlanningCollection
  private val logger: Logger = Logger

  override def saveSubjectTermlyPlan(planToSave: SubjectTermlyPlan): Future[Completed] =
  {
    val termlyPlanAsDocument = convertTermlyPlanToMongoDbDocument(planToSave)
    logger.info(s"Inserting termly plan to database: ${termlyPlanAsDocument.toString}")
    val observable = termlyPlanningCollection.insertOne(termlyPlanAsDocument)
    observable.toFuture()
  }

}
