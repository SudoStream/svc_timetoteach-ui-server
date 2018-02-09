package potentialmicroservice.planning.reader.dao

import javax.inject.{Inject, Singleton}

import dao.MongoDbConnection
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.subjectdetail.SubjectName
import models.timetoteach.{ClassId, TimeToTeachUserId}
import models.timetoteach.planning.{GroupId, SubjectTermlyPlan}

import scala.concurrent.Future

@Singleton
class PlanReaderDaoImpl @Inject()(mongoDbConnection: MongoDbConnection) extends PlanReaderDao
{

  override def readSubjectTermlyPlan(tttUserId: TimeToTeachUserId,
                                     classId: ClassId,
                                     groupId: GroupId,
                                     subject: SubjectName): Future[Option[SubjectTermlyPlan]] = ???

}
