package controllers.serviceproxies

import javax.inject.{Inject, Singleton}

import models.timetoteach.planning.{CurriculumAreaTermlyPlan, TermlyCurriculumSelection}
import org.mongodb.scala.Completed
import potentialmicroservice.planning.writer.PlanningWriterService

import scala.concurrent.Future

@Singleton
class PlanningWriterServiceProxyImpl @Inject()(planningWriterService: PlanningWriterService) extends PlanningWriterServiceProxy
{

  override def saveSubjectTermlyPlan(planToSave: CurriculumAreaTermlyPlan): Future[Completed] =
  {
    planningWriterService.saveSubjectTermlyPlan(planToSave)
  }

  override def saveTermlyCurriculumSelection(termlyCurriculumSelection: TermlyCurriculumSelection): Future[Completed] =
  {
    planningWriterService.saveTermlyCurriculumSelection(termlyCurriculumSelection)
  }

}
