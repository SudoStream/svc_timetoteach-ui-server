package controllers.serviceproxies

import com.google.inject.ImplementedBy

@ImplementedBy(classOf[PlanningWriterServiceProxyImpl])
trait PlanningWriterServiceProxy {
  def saveSubjectTermlyPlan()
}
