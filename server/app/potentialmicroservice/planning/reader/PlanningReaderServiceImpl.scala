package potentialmicroservice.planning.reader

import javax.inject.{Inject, Singleton}

import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.subjectdetail.SubjectName
import models.timetoteach.planning.{GroupId, SubjectTermlyPlan}
import models.timetoteach.{ClassId, TimeToTeachUserId}
import potentialmicroservice.planning.reader.dao.PlanReaderDao

import scala.concurrent.Future

@Singleton
class PlanningReaderServiceImpl @Inject()(planningReaderDao: PlanReaderDao) extends PlanningReaderService
{

  override def readSubjectTermlyPlan(tttUserId: TimeToTeachUserId,
                                     classId: ClassId,
                                     groupId: GroupId,
                                     subject: SubjectName): Future[Option[SubjectTermlyPlan]] =
  {
    planningReaderDao.readSubjectTermlyPlan(tttUserId, classId, groupId, subject)
  }

}
