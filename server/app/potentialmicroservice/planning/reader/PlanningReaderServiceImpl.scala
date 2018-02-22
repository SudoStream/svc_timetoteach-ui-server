package potentialmicroservice.planning.reader

import javax.inject.{Inject, Singleton}
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning.{CurriculumAreaTermlyPlan, GroupId, TermlyCurriculumSelection}
import models.timetoteach.term.SchoolTerm
import models.timetoteach.{ClassId, TimeToTeachUserId}
import potentialmicroservice.planning.reader.dao.PlanReaderDao

import scala.concurrent.Future

@Singleton
class PlanningReaderServiceImpl @Inject()(planningReaderDao: PlanReaderDao) extends PlanningReaderService
{

  override def currentTermlyCurriculumSelection(tttUserId: TimeToTeachUserId,
                                                classId: ClassId,
                                                term: SchoolTerm): Future[Option[TermlyCurriculumSelection]] =
  {
    planningReaderDao.currentTermlyCurriculumSelection(tttUserId, classId, term)
  }


  override def readCurriculumAreaTermlyPlanForGroup(tttUserId: TimeToTeachUserId,
                                                    classId: ClassId,
                                                    groupId: GroupId,
                                                    planningArea: ScottishCurriculumPlanningArea): Future[Option[CurriculumAreaTermlyPlan]] =
  {
    planningReaderDao.readCurriculumAreaTermlyPlanForGroup(tttUserId, classId, groupId, planningArea)
  }


  override def readCurriculumAreaTermlyPlanForClassLevel(tttUserId: TimeToTeachUserId, classId: ClassId, planningArea: ScottishCurriculumPlanningArea): Future[Option[CurriculumAreaTermlyPlan]] =
  {
    planningReaderDao.readCurriculumAreaTermlyPlanForClassLevel(tttUserId, classId, planningArea)
  }

}
