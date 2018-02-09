package potentialmicroservice.planning.reader.dao

import com.google.inject.ImplementedBy
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.subjectdetail.SubjectName
import models.timetoteach.{ClassId, TimeToTeachUserId}
import models.timetoteach.planning.{GroupId, SubjectTermlyPlan}

import scala.concurrent.Future

@ImplementedBy(classOf[PlanReaderDaoImpl])
trait PlanReaderDao
{

  def readSubjectTermlyPlan(
                             tttUserId: TimeToTeachUserId,
                             classId: ClassId,
                             groupId: GroupId,
                             subject: SubjectName
                           ): Future[Option[SubjectTermlyPlan]]

}
