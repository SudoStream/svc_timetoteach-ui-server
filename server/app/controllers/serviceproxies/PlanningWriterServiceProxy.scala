package controllers.serviceproxies

import com.google.inject.ImplementedBy
import models.timetoteach.planning.SubjectTermlyPlan

import scala.concurrent.Future

@ImplementedBy(classOf[PlanningWriterServiceProxyImpl])
trait PlanningWriterServiceProxy {
  def saveSubjectTermlyPlan(planToSave: SubjectTermlyPlan): Future[Boolean]
}