package controllers.serviceproxies

import com.google.inject.ImplementedBy
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning.{GroupId, SubjectTermlyPlan, TermlyCurriculumSelection}
import models.timetoteach.{ClassId, TimeToTeachUserId}

import scala.concurrent.Future

@ImplementedBy(classOf[PlanningReaderServiceProxyImpl])
trait PlanningReaderServiceProxy
{

  def currentTermlyCurriculumSelection(
                                        tttUserId: TimeToTeachUserId,
                                        classId: ClassId
                                      ): Future[Option[TermlyCurriculumSelection]]

  def readSubjectTermlyPlan(
                             tttUserId: TimeToTeachUserId,
                             classId: ClassId,
                             groupId: GroupId,
                             planningArea: ScottishCurriculumPlanningArea
                           ): Future[Option[SubjectTermlyPlan]]

}
