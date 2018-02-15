package controllers.serviceproxies

import javax.inject.{Inject, Singleton}

import models.timetoteach.planning.{SubjectTermlyPlan, TermlyCurriculumSelection}
import org.mongodb.scala.Completed
import potentialmicroservice.planning.writer.PlanningWriterService

import scala.concurrent.Future

@Singleton
class PlanningWriterServiceProxyImpl @Inject()(planningWriterService: PlanningWriterService) extends PlanningWriterServiceProxy
{

  override def saveSubjectTermlyPlan(planToSave: SubjectTermlyPlan): Future[Completed] =
  {
    planningWriterService.saveSubjectTermlyPlan(planToSave)
  }

  override def saveTermlyCurriculumSelection(termlyCurriculumSelection: TermlyCurriculumSelection): Future[Completed] =
  {
    planningWriterService.saveTermlyCurriculumSelection(termlyCurriculumSelection)
  }

}
