package controllers.serviceproxies

import javax.inject.{Inject, Singleton}

import models.timetoteach.planning.SubjectTermlyPlan
import potentialmicroservice.planning.PlanningWriterService

import scala.concurrent.Future

@Singleton
class PlanningWriterServiceProxyImpl @Inject()(planningWriterService : PlanningWriterService ) extends PlanningWriterServiceProxy {

  override def saveSubjectTermlyPlan(planToSave: SubjectTermlyPlan): Future[Boolean] = {
    planningWriterService.saveSubjectTermlyPlan(planToSave)
  }

}
