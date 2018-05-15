package potentialmicroservice.planning.reader.dao

import java.time.LocalDate

import dao.MongoDbConnection
import duplicate.model.esandos.EsAndOsPlusBenchmarksForCurriculumAreaAndLevel
import duplicate.model.planning.FullWeeklyPlanOfLessons
import models.timetoteach.planning.weekly.WeeklyHighLevelPlan
import models.timetoteach.{ClassId, TimeToTeachUserId}
import org.mongodb.scala.Document
import org.mongodb.scala.bson.{BsonArray, BsonDocument}
import play.api.Logger
import potentialmicroservice.planning.sharedschema.WeeklyPlanningSchema
import utils.CurriculumConverterUtil
import utils.mongodb.MongoDbSafety

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait RetrieveFullWeekOfLessonsDaoHelper {

  private val logger: Logger = Logger
  def getDbConnection: MongoDbConnection

  def retrieveFullWeekOfLessonsImpl(tttUserId: TimeToTeachUserId,
                                    classId: ClassId,
                                    mondayDateOfWeekIso: String): Future[Option[FullWeeklyPlanOfLessons]] = {
    logger.info(s"Retrieve full week of lessons: $tttUserId|$classId|$mondayDateOfWeekIso")

    val latestHighLevelPlansForAllSubjects = readLatestHighLevelPlansForTheWeek(tttUserId, classId, mondayDateOfWeekIso)

    // TODO: 3) Read all the Single Lesson Plans  for the "class" and "week" in question
    // TODO: 4) Get the latest version of each seperate lesson plan .... for each subject

    // TODO: 5) Stitch together into appropriate "WeeklyPlanOfOneSubject"s

    // TODO: 6) Create a "FullWeeklyPlanOfLessons"
    // TODO: 7) Done!

    Future {
      None
    }
  }

  private[dao] def readLatestHighLevelPlansForTheWeek(tttUserId: TimeToTeachUserId,
                                                      classId: ClassId,
                                                      mondayDateOfWeekIso: String): Future[List[WeeklyHighLevelPlan]] = {
    if (tttUserId == null || classId == null || mondayDateOfWeekIso == null) {
      Future {
        Nil
      }
    } else {

      val allPlansAllSubjectsForTheWeekAsDocs = readAllHighLevelPlansForTheWeek(tttUserId, classId, mondayDateOfWeekIso)

      for {
        allWeeklyPlansAsDocs <- allPlansAllSubjectsForTheWeekAsDocs
        allWeeklyPlans = convertPlanAllSubjectsForTheWeekToModel(allWeeklyPlansAsDocs.toList)
        latestWeeklyPlansForEachSubject = latestValueForEachSubject(allWeeklyPlans)
      } yield latestWeeklyPlansForEachSubject
    }
  }

  private[dao] def readAllHighLevelPlansForTheWeek(tttUserId: TimeToTeachUserId,
                                                   classId: ClassId,
                                                   mondayDateOfWeekIso: String): Future[Seq[Document]] = {
    val findMatcher = BsonDocument(
      WeeklyPlanningSchema.TTT_USER_ID -> tttUserId.value,
      WeeklyPlanningSchema.CLASS_ID -> classId.value,
      WeeklyPlanningSchema.WEEK_BEGINNING_ISO_DATE -> mondayDateOfWeekIso
    )

    getDbConnection.getWeeklyPlanningCollection.find(findMatcher).toFuture()
  }

  private def createEsAndOsPlusBenchmarksForCurriculumAreaAndLevelMap(esOsBenchmarksByGroup: BsonArray):
  Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel] = {
    // TODO:
    Map()
  }

  private def createEsAndOsPlusBenchmarksForCurriculumAreaAndLevelMapFromOption(maybeEsOsBenchmarksByGroup: Option[BsonArray]):
  Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel] = {
    maybeEsOsBenchmarksByGroup match {
      case Some(esOsBenchmarksByGroup) => createEsAndOsPlusBenchmarksForCurriculumAreaAndLevelMap(esOsBenchmarksByGroup)
      case None => Map()
    }
  }

  def convertWeekPlanDocToModel(planDoc: Document): WeeklyHighLevelPlan = {
    WeeklyHighLevelPlan(
      TimeToTeachUserId(planDoc.getString(WeeklyPlanningSchema.TTT_USER_ID)),
      ClassId(planDoc.getString(WeeklyPlanningSchema.CLASS_ID)),
      CurriculumConverterUtil.convertSubjectToScottishCurriculumPlanningAreaWrapper(
        planDoc.getString(WeeklyPlanningSchema.SUBJECT)
      ),
      LocalDate.parse(planDoc.getString(WeeklyPlanningSchema.WEEK_BEGINNING_ISO_DATE)),
      MongoDbSafety.safelyParseTimestamp(planDoc.getString(WeeklyPlanningSchema.CREATED_TIMESTAMP)),
      createEsAndOsPlusBenchmarksForCurriculumAreaAndLevelMapFromOption(planDoc.get[BsonArray](WeeklyPlanningSchema.SELECTED_ES_OS_AND_BENCHMARKS_BY_GROUP)),
      createEsAndOsPlusBenchmarksForCurriculumAreaAndLevelMapFromOption(planDoc.get[BsonArray](WeeklyPlanningSchema.COMPLETED_ES_OS_AND_BENCHMARKS_BY_GROUP))
    )
  }


  private[dao] def convertPlanAllSubjectsForTheWeekToModel(plansForTheWeekAsDoc: List[Document]):
  List[WeeklyHighLevelPlan] = {
    if (plansForTheWeekAsDoc == null) {
      Nil
    } else {
      for {
        planOfTheWeekDoc <- plansForTheWeekAsDoc
      } yield convertWeekPlanDocToModel(planOfTheWeekDoc)
    }
  }

  private[dao] def latestValueForEachSubject(allPlansAllSubjectsForTheWeek: List[WeeklyHighLevelPlan]): List[WeeklyHighLevelPlan] = {
    Nil
  }

}
