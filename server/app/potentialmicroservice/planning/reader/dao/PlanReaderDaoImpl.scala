package potentialmicroservice.planning.reader.dao

import javax.inject.{Inject, Singleton}

import dao.MongoDbConnection
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.subjectdetail.SubjectName
import models.timetoteach.planning.{GroupId, SubjectTermlyPlan}
import models.timetoteach.{ClassId, TimeToTeachUserId}
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.{Document, MongoCollection}
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PlanReaderDaoImpl @Inject()(mongoDbConnection: MongoDbConnection) extends PlanReaderDao with PlanReaderDaoHelper
{
  import potentialmicroservice.planning.sharedschema.TermlyPlanningSchema._

  private val termlyPlanningCollection: MongoCollection[Document] = mongoDbConnection.getTermlyPlanningCollection

  override def readSubjectTermlyPlan(tttUserId: TimeToTeachUserId,
                                     classId: ClassId,
                                     groupId: GroupId,
                                     subject: SubjectName): Future[Option[SubjectTermlyPlan]] =
  {
    logger.info(s"Reading subject termly plan from Database: $tttUserId|$classId|$groupId|$subject")

    val findMatcher = BsonDocument(
      TTT_USER_ID -> tttUserId.value,
      CLASS_ID -> classId.value,
      GROUP_ID -> groupId.value,
      SUBJECT_NAME -> subject.toString
    )

    val futureFoundTermlyPlanDocuments = termlyPlanningCollection.find(findMatcher).toFuture()
    futureFoundTermlyPlanDocuments.map {
      foundTermlyPlanDocs: Seq[Document] => findLatestVersionOfTermlyPlan(foundTermlyPlanDocs.toList)
    }
  }

}
