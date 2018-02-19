package potentialmicroservice.planning.reader

import javax.inject.{Inject, Singleton}

import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning.{GroupId, CurriculumAreaTermlyPlan, TermlyCurriculumSelection}
import models.timetoteach.{ClassId, TimeToTeachUserId}
import potentialmicroservice.planning.reader.dao.PlanReaderDao

import scala.concurrent.Future

@Singleton
class PlanningReaderServiceImpl @Inject()(planningReaderDao: PlanReaderDao) extends PlanningReaderService
{

  override def readCurriculumAreaTermlyPlanForGroup(tttUserId: TimeToTeachUserId,
                                                    classId: ClassId,
                                                    groupId: GroupId,
                                                    planningArea: ScottishCurriculumPlanningArea): Future[Option[CurriculumAreaTermlyPlan]] =
  {
    planningReaderDao.readCurriculumAreaTermlyPlanForGroup(tttUserId, classId, groupId, planningArea)
  }

  override def currentTermlyCurriculumSelection(tttUserId: TimeToTeachUserId,
                                                classId: ClassId): Future[Option[TermlyCurriculumSelection]] =
  {
    planningReaderDao.currentTermlyCurriculumSelection(tttUserId,classId)
  }
}
