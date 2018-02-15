package controllers.serviceproxies

import com.google.inject.ImplementedBy
import models.timetoteach.planning.{SubjectTermlyPlan, TermlyCurriculumSelection}
import org.mongodb.scala.Completed

import scala.concurrent.Future

@ImplementedBy(classOf[PlanningWriterServiceProxyImpl])
trait PlanningWriterServiceProxy
{
  def saveSubjectTermlyPlan(planToSave: SubjectTermlyPlan): Future[Completed]

  def saveTermlyCurriculumSelection(termlyCurriculumSelection: TermlyCurriculumSelection): Future[Completed]
}
