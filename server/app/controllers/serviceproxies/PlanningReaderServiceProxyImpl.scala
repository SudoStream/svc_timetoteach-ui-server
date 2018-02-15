package controllers.serviceproxies

import javax.inject.{Inject, Singleton}

import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning.{GroupId, SubjectTermlyPlan}
import models.timetoteach.{ClassId, TimeToTeachUserId}
import potentialmicroservice.planning.reader.PlanningReaderService

import scala.concurrent.Future

@Singleton
class PlanningReaderServiceProxyImpl @Inject()(planningReaderService: PlanningReaderService) extends PlanningReaderServiceProxy
{
  override def readSubjectTermlyPlan(tttUserId: TimeToTeachUserId,
                                     classId: ClassId,
                                     groupId: GroupId,
                                     planningArea: ScottishCurriculumPlanningArea): Future[Option[SubjectTermlyPlan]] =
  {
    planningReaderService.readSubjectTermlyPlan(tttUserId, classId, groupId, planningArea)
  }
}
