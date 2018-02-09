package potentialmicroservice.planning.dao

import models.timetoteach.planning.SubjectTermlyPlan
import org.mongodb.scala.bson.BsonDocument

trait PlanWriterDaoHelper {

  def convertTermlyPlanToMongoDbDocument(planToSave: SubjectTermlyPlan) : BsonDocument = ???

}
