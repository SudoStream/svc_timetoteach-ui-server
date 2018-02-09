package potentialmicroservice.planning

import javax.inject.Singleton

import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.subjectdetail.SubjectName
import models.timetoteach.{ClassId, TimeToTeachUserId}
import models.timetoteach.planning.{GroupId, SubjectTermlyPlan}

import scala.concurrent.Future

@Singleton
class PlanningReaderServiceImpl extends PlanningReaderService {

  override def readSubjectTermlyPlan(tttUserId: TimeToTeachUserId,
                                     classId: ClassId,
                                     groupId: GroupId,
                                     subject: SubjectName): Future[Option[SubjectTermlyPlan]] = ???

}
