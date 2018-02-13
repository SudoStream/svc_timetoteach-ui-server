package potentialmicroservice.planning.writer.dao

import javax.inject.{Inject, Singleton}

import dao.MongoDbConnection
import models.timetoteach.planning.SubjectTermlyPlan
import org.mongodb.scala.{Completed, Document, MongoCollection}
import play.api.Logger

import scala.concurrent.Future

@Singleton
class PlanWriterDaoImpl @Inject()(mongoDbConnection: MongoDbConnection)
  extends PlanWriterDao with PlanWriterDaoHelper
{
  private val termlyPlanningCollection: MongoCollection[Document] = mongoDbConnection.getTermlyPlanningCollection
  private val logger: Logger = Logger

  override def saveSubjectTermlyPlan(planToSave: SubjectTermlyPlan): Future[Completed] =
  {
    logger.info(s"Upserting termly plan to Database: ${planToSave.toString}")
    val termlyPlanAsDocument = convertTermlyPlanToMongoDbDocument(planToSave)
    val observable = termlyPlanningCollection.insertOne(termlyPlanAsDocument)
    observable.toFuture()
  }

}
