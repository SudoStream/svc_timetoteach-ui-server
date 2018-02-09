package potentialmicroservice.planning

import javax.inject.{Inject, Singleton}

import models.timetoteach.planning.SubjectTermlyPlan
import org.mongodb.scala.Completed
import potentialmicroservice.planning.dao.PlanWriterDao

import scala.concurrent.Future

@Singleton
class PlanningWriterServiceImpl @Inject()(planningWriterDao: PlanWriterDao) extends PlanningWriterService {

  override def saveSubjectTermlyPlan(planToSave: SubjectTermlyPlan): Future[Completed] = {
    planningWriterDao.saveSubjectTermlyPlan(planToSave)
  }

}
