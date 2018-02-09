package potentialmicroservice.planning

import models.timetoteach.planning.SubjectTermlyPlan

import scala.concurrent.Future

trait PlanningWriterService {

  def saveSubjectTermlyPlan(planToSave: SubjectTermlyPlan): Future[Boolean]

}
