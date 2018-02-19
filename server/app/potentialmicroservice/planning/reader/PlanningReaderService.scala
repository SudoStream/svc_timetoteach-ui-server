package potentialmicroservice.planning.reader

import com.google.inject.ImplementedBy
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning.{GroupId, CurriculumAreaTermlyPlan, TermlyCurriculumSelection}
import models.timetoteach.{ClassId, TimeToTeachUserId}

import scala.concurrent.Future

@ImplementedBy(classOf[PlanningReaderServiceImpl])
trait PlanningReaderService
{

  def currentTermlyCurriculumSelection(
                                        tttUserId: TimeToTeachUserId,
                                        classId: ClassId
                                      ): Future[Option[TermlyCurriculumSelection]]

  def readCurriculumAreaTermlyPlanForGroup(
                             tttUserId: TimeToTeachUserId,
                             classId: ClassId,
                             groupId: GroupId,
                             planningArea: ScottishCurriculumPlanningArea
                           ): Future[Option[CurriculumAreaTermlyPlan]]

}
