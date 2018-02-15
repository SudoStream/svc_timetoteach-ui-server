package potentialmicroservice.planning.writer.dao

import com.google.inject.ImplementedBy
import models.timetoteach.planning.{SubjectTermlyPlan, TermlyCurriculumSelection}
import org.mongodb.scala.Completed

import scala.concurrent.Future

@ImplementedBy(classOf[PlanWriterDaoImpl])
trait PlanWriterDao
{

  def saveSubjectTermlyPlan(planToSave: SubjectTermlyPlan): Future[Completed]

  def saveTermlyCurriculumSelection(termlyCurriculumSelection: TermlyCurriculumSelection): Future[Completed]

}
