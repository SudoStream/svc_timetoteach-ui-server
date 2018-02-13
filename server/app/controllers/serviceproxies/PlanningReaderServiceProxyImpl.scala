package controllers.serviceproxies

import javax.inject.{Inject, Singleton}

import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.subjectdetail.SubjectName
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
                                     subject: SubjectName): Future[Option[SubjectTermlyPlan]] =
  {
    planningReaderService.readSubjectTermlyPlan(tttUserId, classId, groupId, subject)
  }
}
