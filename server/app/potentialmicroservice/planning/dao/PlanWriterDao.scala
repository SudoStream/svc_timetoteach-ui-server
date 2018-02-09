package potentialmicroservice.planning.dao

import models.timetoteach.planning.SubjectTermlyPlan

import scala.concurrent.Future

trait PlanWriterDao {

  def saveSubjectTermlyPlan(planToSave: SubjectTermlyPlan): Future[Boolean]

}
