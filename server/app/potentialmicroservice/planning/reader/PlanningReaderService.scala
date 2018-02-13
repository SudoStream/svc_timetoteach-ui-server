package potentialmicroservice.planning.reader

import com.google.inject.ImplementedBy
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.subjectdetail.SubjectName
import models.timetoteach.planning.{GroupId, SubjectTermlyPlan}
import models.timetoteach.{ClassId, TimeToTeachUserId}

import scala.concurrent.Future

@ImplementedBy(classOf[PlanningReaderServiceImpl])
trait PlanningReaderService
{

  def readSubjectTermlyPlan(
                             tttUserId: TimeToTeachUserId,
                             classId: ClassId,
                             groupId: GroupId,
                             subject: SubjectName
                           ): Future[Option[SubjectTermlyPlan]]

}
