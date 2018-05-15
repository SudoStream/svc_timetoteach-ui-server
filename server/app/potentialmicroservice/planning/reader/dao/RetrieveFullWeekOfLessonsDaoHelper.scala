package potentialmicroservice.planning.reader.dao

import java.time.LocalDate

import dao.MongoDbConnection
import duplicate.model.CurriculumLevel
import duplicate.model.esandos._
import duplicate.model.planning.FullWeeklyPlanOfLessons
import models.timetoteach.planning.weekly.WeeklyHighLevelPlan
import models.timetoteach.{ClassId, TimeToTeachUserId}
import org.mongodb.scala.Document
import org.mongodb.scala.bson.{BsonArray, BsonDocument}
import play.api.Logger
import potentialmicroservice.planning.sharedschema.WeeklyPlanningSchema
import utils.CurriculumConverterUtil
import utils.mongodb.MongoDbSafety

import scala.annotation.tailrec
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

  private def createEsAndOs(esOsBsonArray: BsonArray): List[EandO] = {
    import scala.collection.JavaConverters._

    @tailrec
    def loop(remaining: List[String], currentRet: List[EandO]): List[EandO] = {
      if (remaining.isEmpty) {
        currentRet
      } else {
        val nextEsAndOCode = remaining.head
        loop(remaining.tail, EandO(nextEsAndOCode, Nil) :: currentRet)
      }
    }

    val esAndOs = for {
      esOsBsonValue <- esOsBsonArray.getValues.asScala.toList
      esOsString = esOsBsonValue.asString()
    } yield esOsString.getValue

    loop(esAndOs, Nil)
  }

  private def createBenchies(esOsBsonArray: BsonArray): List[Benchmark] = {
    import scala.collection.JavaConverters._

    @tailrec
    def loop(remaining: List[String], currentRet: List[Benchmark]): List[Benchmark] = {
      if (remaining.isEmpty) {
        currentRet
      } else {
        val nextBenchmark = remaining.head
        loop(remaining.tail, Benchmark(nextBenchmark) :: currentRet)
      }
    }

    val benchies = for {
      benchmarkBsonValue <- esOsBsonArray.getValues.asScala.toList
      benchmarkString = benchmarkBsonValue.asString()
    } yield benchmarkString.getValue

    loop(benchies, Nil)
  }


  private def createEsAndOsPlusBenchmarksForCurriculumAreaAndLevelMap(
                                                                       esOsBenchmarksByGroup: BsonArray,
                                                                       curriculumArea: String):
  Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel] = {

    import scala.collection.JavaConverters._

    val groupId_to_selectedEsOsBenchies_tuple: List[(String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel)] = for {
      docAsValue <- esOsBenchmarksByGroup.getValues.asScala.toList
      doc = docAsValue.asDocument()
      groupId = doc.getString(WeeklyPlanningSchema.GROUP_ID)

      selectedEsAndOsWithBenchmarksArray = doc.getArray(WeeklyPlanningSchema.SELECTED_ES_AND_OS_WITH_BENCHMARKS)
      selectedEsAndOsWithBenchmarksBsonValue <- selectedEsAndOsWithBenchmarksArray.getValues.asScala.toList
      selectedEsAndOsWithBenchmarksDoc = selectedEsAndOsWithBenchmarksBsonValue.asDocument()

      sectionName = selectedEsAndOsWithBenchmarksDoc.getString(WeeklyPlanningSchema.SELECTED_SECTION_NAME).getValue
      subsectionName = selectedEsAndOsWithBenchmarksDoc.getString(WeeklyPlanningSchema.SELECTED_SUBSECTION_NAME).getValue
      esAndOs = createEsAndOs(selectedEsAndOsWithBenchmarksDoc.getArray(WeeklyPlanningSchema.SELECTED_ES_AND_OS))
      benchies = createBenchies(selectedEsAndOsWithBenchmarksDoc.getArray(WeeklyPlanningSchema.SELECTED_BENCHMARKS))

      eAndOSubSection = EandOSetSubSection("", esAndOs, benchies)


    } yield (groupId.getValue, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel(
      CurriculumLevel.createCurriculumLevelFromEAndOCode(esAndOs.headOption.getOrElse(EandO("", Nil)).code),
      CurriculumArea.createCurriculumAreaFromString(curriculumArea),
      Map(sectionName -> Map(subsectionName -> eAndOSubSection))
    ))

    // TODO: ANDY CHECK THAT THE MAP ABOVE MAY NEED TO BE BUILT MORE CLEVERLY

    groupId_to_selectedEsOsBenchies_tuple.toMap
  }

  private def createEsAndOsPlusBenchmarksForCurriculumAreaAndLevelMapFromOption(
                                                                                 maybeEsOsBenchmarksByGroup: Option[BsonArray],
                                                                                 curriculumArea: String
                                                                               ):
  Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel] = {
    maybeEsOsBenchmarksByGroup match {
      case Some(esOsBenchmarksByGroup) => createEsAndOsPlusBenchmarksForCurriculumAreaAndLevelMap(esOsBenchmarksByGroup, curriculumArea)
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
      createEsAndOsPlusBenchmarksForCurriculumAreaAndLevelMapFromOption(
        planDoc.get[BsonArray](WeeklyPlanningSchema.SELECTED_ES_OS_AND_BENCHMARKS_BY_GROUP),
        planDoc.getString(WeeklyPlanningSchema.SUBJECT)),
      createEsAndOsPlusBenchmarksForCurriculumAreaAndLevelMapFromOption(
        planDoc.get[BsonArray](WeeklyPlanningSchema.COMPLETED_ES_OS_AND_BENCHMARKS_BY_GROUP),
        planDoc.getString(WeeklyPlanningSchema.SUBJECT))
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
