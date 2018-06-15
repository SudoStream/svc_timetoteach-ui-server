package potentialmicroservice.planning.writer

import com.google.inject.ImplementedBy
import duplicate.model.esandos.{CompletedEsAndOsByGroup, NotStartedEsAndOsByGroup}
import duplicate.model.planning.{LessonPlan, WeeklyPlanOfOneSubject}
import models.timetoteach.TimeToTeachUserId
import models.timetoteach.planning.{CurriculumAreaTermlyPlan, TermlyCurriculumSelection}
import org.mongodb.scala.Completed

import scala.concurrent.Future

@ImplementedBy(classOf[PlanningWriterServiceImpl])
trait PlanningWriterService {
  def saveSubjectTermlyPlan(planToSave: CurriculumAreaTermlyPlan): Future[Completed]

  def saveTermlyCurriculumSelection(termlyCurriculumSelection: TermlyCurriculumSelection): Future[Completed]

  def saveWeeklyPlanForSingleSubject(
                                      weeklyPlansToSave: WeeklyPlanOfOneSubject,
                                      completedEsAndOsByGroup: CompletedEsAndOsByGroup,
                                      notStartedEsOsBenchies: NotStartedEsAndOsByGroup
                                    ): Future[List[Completed]]

  def saveSingleLessonPlan(
                            lessonPlan: LessonPlan,
                            tttUserId: TimeToTeachUserId,
                            classId: String
                          ): Future[List[Completed]]

}
