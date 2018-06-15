package potentialmicroservice.planning.writer

import duplicate.model.esandos.{CompletedEsAndOsByGroup, NotStartedEsAndOsByGroup}
import duplicate.model.planning.{LessonPlan, WeeklyPlanOfOneSubject}
import javax.inject.{Inject, Singleton}
import models.timetoteach.TimeToTeachUserId
import models.timetoteach.planning.{CurriculumAreaTermlyPlan, TermlyCurriculumSelection}
import org.mongodb.scala.Completed
import potentialmicroservice.planning.writer.dao.PlanWriterDao

import scala.concurrent.Future

@Singleton
class PlanningWriterServiceImpl @Inject()(planningWriterDao: PlanWriterDao) extends PlanningWriterService {

  override def saveSubjectTermlyPlan(planToSave: CurriculumAreaTermlyPlan): Future[Completed] = {
    planningWriterDao.saveSubjectTermlyPlan(planToSave)
  }

  override def saveTermlyCurriculumSelection(termlyCurriculumSelection: TermlyCurriculumSelection): Future[Completed] = {
    planningWriterDao.saveTermlyCurriculumSelection(termlyCurriculumSelection)
  }

  override def saveWeeklyPlanForSingleSubject(
                                               weeklyPlansToSave: WeeklyPlanOfOneSubject,
                                               completedEsAndOsByGroup: CompletedEsAndOsByGroup,
                                               notStartedEsOsBenchies: NotStartedEsAndOsByGroup
                                             ): Future[List[Completed]] = {
    planningWriterDao.saveWeeklyPlanForSingleSubject(
      weeklyPlansToSave,
      completedEsAndOsByGroup,
      notStartedEsOsBenchies
    )
  }

  override def saveSingleLessonPlan(
                            lessonPlan: LessonPlan,
                            tttUserId: TimeToTeachUserId,
                            classId: String
                          ): Future[List[Completed]] = {
    planningWriterDao.saveSingleLessonPlan(lessonPlan, tttUserId, classId)
  }


}
