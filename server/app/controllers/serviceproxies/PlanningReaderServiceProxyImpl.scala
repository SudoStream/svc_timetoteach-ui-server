package controllers.serviceproxies

import javax.inject.{Inject, Singleton}

import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning.{CurriculumAreaTermlyPlan, GroupId, TermlyCurriculumSelection}
import models.timetoteach.{ClassId, TimeToTeachUserId}
import potentialmicroservice.planning.reader.PlanningReaderService

import scala.concurrent.Future

@Singleton
class PlanningReaderServiceProxyImpl @Inject()(planningReaderService: PlanningReaderService) extends PlanningReaderServiceProxy
{

  override def currentTermlyCurriculumSelection(tttUserId: TimeToTeachUserId, classId: ClassId): Future[Option[TermlyCurriculumSelection]] =
  {
    planningReaderService.currentTermlyCurriculumSelection(tttUserId, classId)
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
