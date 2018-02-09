package potentialmicroservice.planning.dao

import javax.inject.{Inject, Singleton}

import dao.MongoDbConnection
import models.timetoteach.planning.SubjectTermlyPlan
import org.mongodb.scala.Completed
import play.api.Logger

import scala.concurrent.Future

@Singleton
class PlanWriterDaoImpl @Inject()(mongoDbConnection: MongoDbConnection)
  extends PlanWriterDao with PlanWriterDaoHelper {

  val logger: Logger = Logger

  override def saveSubjectTermlyPlan(planToSave: SubjectTermlyPlan): Future[Completed] = {
    logger.info(s"Upserting termly plan to Database: ${planToSave.toString}")
    val termlyPlanningCollection = mongoDbConnection.getTermlyPlanningCollection
    val termlyPlanAsDocument = convertTermlyPlanToMongoDbDocument(planToSave)
    val observable = termlyPlanningCollection.insertOne(termlyPlanAsDocument)
    observable.toFuture()
  }

}
