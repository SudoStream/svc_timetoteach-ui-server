package potentialmicroservice.planning.writer

import javax.inject.{Inject, Singleton}

import models.timetoteach.planning.{SubjectTermlyPlan, TermlyCurriculumSelection}
import org.mongodb.scala.Completed
import potentialmicroservice.planning.writer.dao.PlanWriterDao

import scala.concurrent.Future

@Singleton
class PlanningWriterServiceImpl @Inject()(planningWriterDao: PlanWriterDao) extends PlanningWriterService
{

  override def saveSubjectTermlyPlan(planToSave: SubjectTermlyPlan): Future[Completed] =
  {
    planningWriterDao.saveSubjectTermlyPlan(planToSave)
  }

  override def saveTermlyCurriculumSelection(termlyCurriculumSelection: TermlyCurriculumSelection): Future[Completed] =
  {
    planningWriterDao.saveTermlyCurriculumSelection(termlyCurriculumSelection)
  }
}
