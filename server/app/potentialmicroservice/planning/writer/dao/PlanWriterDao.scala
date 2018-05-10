package potentialmicroservice.planning.writer.dao

import com.google.inject.ImplementedBy
import duplicate.model.planning.WeeklyPlanOfOneSubject
import models.timetoteach.planning.{CurriculumAreaTermlyPlan, TermlyCurriculumSelection}
import org.mongodb.scala.Completed

import scala.concurrent.Future

@ImplementedBy(classOf[PlanWriterDaoImpl])
trait PlanWriterDao
{

  def saveSubjectTermlyPlan(planToSave: CurriculumAreaTermlyPlan): Future[Completed]

  def saveTermlyCurriculumSelection(termlyCurriculumSelection: TermlyCurriculumSelection): Future[Completed]

  def saveWeeklyPlanForSingleSubject(weeklyPlansToSave: WeeklyPlanOfOneSubject): Future[List[Completed]]

}
