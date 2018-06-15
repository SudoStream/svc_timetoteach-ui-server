package controllers.serviceproxies

import duplicate.model.esandos.{CompletedEsAndOsByGroup, NotStartedEsAndOsByGroup}
import duplicate.model.planning.{LessonPlan, WeeklyPlanOfOneSubject}
import javax.inject.{Inject, Singleton}
import models.timetoteach.TimeToTeachUserId
import models.timetoteach.planning.{CurriculumAreaTermlyPlan, TermlyCurriculumSelection}
import org.mongodb.scala.Completed
import potentialmicroservice.planning.writer.PlanningWriterService

import scala.concurrent.Future

@Singleton
class PlanningWriterServiceProxyImpl @Inject()(planningWriterService: PlanningWriterService) extends PlanningWriterServiceProxy {

  override def saveSubjectTermlyPlan(planToSave: CurriculumAreaTermlyPlan): Future[Completed] = {
    planningWriterService.saveSubjectTermlyPlan(planToSave)
  }

  override def saveTermlyCurriculumSelection(termlyCurriculumSelection: TermlyCurriculumSelection): Future[Completed] = {
    planningWriterService.saveTermlyCurriculumSelection(termlyCurriculumSelection)
  }

  override def saveWeeklyPlanForSingleSubject(
                                               weeklyPlansToSave: WeeklyPlanOfOneSubject,
                                               completedEsAndOsByGroup: CompletedEsAndOsByGroup,
                                               notStartedEsOsBenchies: NotStartedEsAndOsByGroup
                                             ): Future[List[Completed]] = {
    planningWriterService.saveWeeklyPlanForSingleSubject(
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
    planningWriterService.saveSingleLessonPlan(lessonPlan, tttUserId, classId)
  }


}
