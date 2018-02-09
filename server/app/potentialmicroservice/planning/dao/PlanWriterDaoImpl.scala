package potentialmicroservice.planning.dao

import javax.inject.Singleton

import models.timetoteach.planning.SubjectTermlyPlan

import scala.concurrent.Future

@Singleton
class PlanWriterDaoImpl extends PlanWriterDao {

  override def saveSubjectTermlyPlan(planToSave: SubjectTermlyPlan): Future[Boolean] = ???

}
