package controllers.serviceproxies

import com.google.inject.ImplementedBy
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning.{GroupId, SubjectTermlyPlan}
import models.timetoteach.{ClassId, TimeToTeachUserId}

import scala.concurrent.Future

@ImplementedBy(classOf[PlanningReaderServiceProxyImpl])
trait PlanningReaderServiceProxy
{

  def readSubjectTermlyPlan(
                             tttUserId: TimeToTeachUserId,
                             classId: ClassId,
                             groupId: GroupId,
                             planningArea: ScottishCurriculumPlanningArea
                           ): Future[Option[SubjectTermlyPlan]]

}
