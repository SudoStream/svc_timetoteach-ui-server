package controllers.serviceproxies

import duplicate.model.ClassDetails
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import javax.inject.{Inject, Singleton}
import models.timetoteach.planning.{CurriculumAreaTermlyPlan, CurriculumPlanProgressForClass, GroupId, TermlyCurriculumSelection}
import models.timetoteach.term.SchoolTerm
import models.timetoteach.{ClassId, TimeToTeachUserId}
import potentialmicroservice.planning.reader.PlanningReaderService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PlanningReaderServiceProxyImpl @Inject()(planningReaderService: PlanningReaderService) extends PlanningReaderServiceProxy
{

  override def currentTermlyCurriculumSelection(tttUserId: TimeToTeachUserId, classId: ClassId, term: SchoolTerm): Future[Option[TermlyCurriculumSelection]] =
  {
    planningReaderService.currentTermlyCurriculumSelection(tttUserId, classId, term)
  }

  override def curriculumPlanProgress(tttUserId: TimeToTeachUserId,
                                      classDetails: ClassDetails,
                                      maybeCurrentTermlyCurriculumSelection: Option[TermlyCurriculumSelection]
                                     ): Future[Option[CurriculumPlanProgressForClass]] =
  {
    maybeCurrentTermlyCurriculumSelection match {
      case Some(currentTermlyCurriculumSelection) =>
        planningReaderService.curriculumPlanProgress(
          tttUserId,
          classDetails, currentTermlyCurriculumSelection.planningAreas,
          currentTermlyCurriculumSelection.schoolTerm)
      case None => Future {
        None
      }
    }
  }

  override def readCurriculumAreaTermlyPlanForGroup(tttUserId: TimeToTeachUserId,
                                                    classId: ClassId,
                                                    groupId: GroupId,
                                                    planningArea: ScottishCurriculumPlanningArea): Future[Option[CurriculumAreaTermlyPlan]] =
  {
    planningReaderService.readCurriculumAreaTermlyPlanForGroup(tttUserId, classId, groupId, planningArea)
  }


  override def readCurriculumAreaTermlyPlanForClassLevel(tttUserId: TimeToTeachUserId, classId: ClassId, planningArea: ScottishCurriculumPlanningArea): Future[Option[CurriculumAreaTermlyPlan]] =
  {
    planningReaderService.readCurriculumAreaTermlyPlanForClassLevel(tttUserId, classId, planningArea)
  }

}
