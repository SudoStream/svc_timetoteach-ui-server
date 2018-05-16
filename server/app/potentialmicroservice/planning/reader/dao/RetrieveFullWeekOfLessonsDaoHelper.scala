package potentialmicroservice.planning.reader.dao

import java.time.LocalDate

import dao.MongoDbConnection
import duplicate.model.esandos._
import duplicate.model.planning.{FullWeeklyPlanOfLessons, LessonPlan}
import models.timetoteach.planning.ScottishCurriculumPlanningAreaWrapper
import models.timetoteach.planning.weekly.WeeklyHighLevelPlanOfOneSubject
import models.timetoteach.{ClassId, TimeToTeachUserId}
import org.mongodb.scala.Document
import org.mongodb.scala.bson.{BsonArray, BsonDocument}
import play.api.Logger
import potentialmicroservice.planning.sharedschema.{SingleLessonPlanSchema, WeeklyPlanningSchema}
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
    // TODO: 4) Get the latest version of each separate lesson plan .... for each subject
    val latestLessonPlansForAllSubjects = readLatestLessonPlansForTheWeek(tttUserId, classId, mondayDateOfWeekIso)

    // TODO: 5) Stitch together into appropriate "WeeklyPlanOfOneSubject"s

    // TODO: 6) Create a "FullWeeklyPlanOfLessons"
    // TODO: 7) Done!

    Future {
      None
    }
  }

  private[dao] def readLatestHighLevelPlansForTheWeek(tttUserId: TimeToTeachUserId,
                                                      classId: ClassId,
                                                      mondayDateOfWeekIso: String): Future[List[WeeklyHighLevelPlanOfOneSubject]] = {
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


  private[dao] def readLatestLessonPlansForTheWeek(tttUserId: TimeToTeachUserId,
                                                   classId: ClassId,
                                                   mondayDateOfWeekIso: String):
  Future[Map[ScottishCurriculumPlanningAreaWrapper, List[LessonPlan]]] = {
    val allLessonPlansForTheWeekAsDocs = readAllLessonPlansForTheWeek(tttUserId, classId, mondayDateOfWeekIso)

    for {
      allLessonPlansAsDocs <- allLessonPlansForTheWeekAsDocs
      allLessonPlans = convertLessonPlansForTheWeekToModel(allLessonPlansAsDocs.toList)
      subjectToLatestLessonsForTheWeek = buildSubjectToLatestLessonsForTheWeekMap(allLessonPlans)
    } yield subjectToLatestLessonsForTheWeek
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private def readAllLessonPlansForTheWeek(tttUserId: TimeToTeachUserId,
                                           classId: ClassId,
                                           mondayDateOfWeekIso: String
                                          ): Future[Seq[Document]] = {
    val findMatcher = BsonDocument(
      WeeklyPlanningSchema.TTT_USER_ID -> tttUserId.value,
      WeeklyPlanningSchema.CLASS_ID -> classId.value,
      WeeklyPlanningSchema.WEEK_BEGINNING_ISO_DATE -> mondayDateOfWeekIso
    )

    getDbConnection.getLessonPlanningCollection.find(findMatcher).toFuture()
  }

  private def stringBsonArrayToList(bsonArray: BsonArray): List[String] = {
    import scala.collection.JavaConverters._

    for {
      bsonValue <- bsonArray.getValues.asScala.toList
    } yield bsonValue.asString().getValue
  }

  private[dao] def attrGroupBsonArrayToMapStringToListString(bsonArray: BsonArray): Map[String, List[String]] = {
    import scala.collection.JavaConverters._

    @tailrec
    def loop(remainingDocs: List[BsonDocument], currentMap: Map[String, List[String]]): Map[String, List[String]] = {
      if (remainingDocs.isEmpty) {
        currentMap
      } else {
        val nextDoc = remainingDocs.head
        val attribute = nextDoc.getString(SingleLessonPlanSchema.ATTRIBUTE_VALUE).getValue

        val groupIds = for {
          groupValue <- nextDoc.getArray(SingleLessonPlanSchema.GROUP_IDS).getValues.asScala.toList
        } yield groupValue.asString().getValue

        val nextMap = currentMap + (attribute -> groupIds)
        loop(remainingDocs.tail, nextMap)
      }
    }

    val docs = for {
      bsonValue <- bsonArray.getValues.asScala.toList
    } yield bsonValue.asDocument()

    loop(docs, Map())
  }

  private[dao] def convertDocumentToLessonPlan(doc: Document): LessonPlan = {
    LessonPlan(
      subject = doc.getString(SingleLessonPlanSchema.SUBJECT),
      subjectAdditionalInfo = doc.getString(SingleLessonPlanSchema.SUBJECT_ADDITIONAL_INFO),
      weekBeginningIsoDate = doc.getString(SingleLessonPlanSchema.WEEK_BEGINNING_ISO_DATE),
      lessonDateIso = doc.getString(SingleLessonPlanSchema.LESSON_DATE),
      startTimeIso = doc.getString(SingleLessonPlanSchema.LESSON_START_TIME),
      endTimeIso = doc.getString(SingleLessonPlanSchema.LESSON_END_TIME),

      activitiesPerGroup = attrGroupBsonArrayToMapStringToListString(
        doc.get[BsonArray](SingleLessonPlanSchema.ACTIVITIES_PER_GROUP).getOrElse(BsonArray())
      ),
      resources = stringBsonArrayToList(
        doc.get[BsonArray](SingleLessonPlanSchema.RESOURCES).getOrElse(BsonArray())
      ),
      learningIntentionsPerGroup = attrGroupBsonArrayToMapStringToListString(
        doc.get[BsonArray](SingleLessonPlanSchema.LEARNING_INTENTIONS_PER_GROUP).getOrElse(BsonArray())
      ),
      successCriteriaPerGroup = attrGroupBsonArrayToMapStringToListString(
        doc.get[BsonArray](SingleLessonPlanSchema.SUCCESS_CRITERIA_PER_GROUP).getOrElse(BsonArray())
      ),
      plenary = stringBsonArrayToList(
        doc.get[BsonArray](SingleLessonPlanSchema.PLENARIES).getOrElse(BsonArray())
      ),
      formativeAssessmentPerGroup = attrGroupBsonArrayToMapStringToListString(
        doc.get[BsonArray](SingleLessonPlanSchema.FORMATIVE_ASSESSMENT_PER_GROUP).getOrElse(BsonArray())
      ),
      notesBefore = stringBsonArrayToList(
        doc.get[BsonArray](SingleLessonPlanSchema.NOTES_BEFORE).getOrElse(BsonArray())
      ),
      notesAfter = stringBsonArrayToList(
        doc.get[BsonArray](SingleLessonPlanSchema.NOTES_AFTER).getOrElse(BsonArray())
      )

    )
  }

  private[dao] def convertLessonPlansForTheWeekToModel(allLessonPlansAsDocs: List[Document]): List[LessonPlan] = {
   for {
     lessonPlanDoc <- allLessonPlansAsDocs
   } yield convertDocumentToLessonPlan(lessonPlanDoc)
  }

  private[dao] def buildSubjectToLatestLessonsForTheWeekMap(
                                                             allLessonPlans: List[LessonPlan]
                                                           ): Map[ScottishCurriculumPlanningAreaWrapper, List[LessonPlan]] = {
    Map()
  }

  private[dao] def readAllHighLevelPlansForTheWeek(tttUserId: TimeToTeachUserId,
                                                   classId: ClassId,
                                                   mondayDateOfWeekIso: String): Future[Seq[Document]] = {
    val findMatcher = BsonDocument(
      SingleLessonPlanSchema.TTT_USER_ID -> tttUserId.value,
      SingleLessonPlanSchema.CLASS_ID -> classId.value,
      SingleLessonPlanSchema.WEEK_BEGINNING_ISO_DATE -> mondayDateOfWeekIso
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


  private[dao] def createSectionNameToSubSections(selectedEsAndOsWithBenchmarksDocs: List[BsonDocument]): Map[String, Map[String, EandOSetSubSection]] = {
    @tailrec
    def loop(
              remainingBsonDocs: List[BsonDocument],
              currentMap: Map[String, Map[String, EandOSetSubSection]]
            ): Map[String, Map[String, EandOSetSubSection]] = {
      if (remainingBsonDocs.isEmpty) {
        currentMap
      } else {
        val nextDoc = remainingBsonDocs.head

        val sectionName = nextDoc.getString(WeeklyPlanningSchema.SELECTED_SECTION_NAME).getValue
        val subsectionName = nextDoc.getString(WeeklyPlanningSchema.SELECTED_SUBSECTION_NAME).getValue

        val esAndOs = createEsAndOs(nextDoc.getArray(WeeklyPlanningSchema.SELECTED_ES_AND_OS))
        val benchies = createBenchies(nextDoc.getArray(WeeklyPlanningSchema.SELECTED_BENCHMARKS))
        val eAndOSubSection = EandOSetSubSection("", esAndOs, benchies)

        val nextMap: Map[String, Map[String, EandOSetSubSection]] = if (currentMap.isDefinedAt(sectionName)) {
          val currentSubSectionMap = currentMap(sectionName)
          val nextSubSectionMap = currentSubSectionMap + (subsectionName -> eAndOSubSection)
          currentMap + (sectionName -> nextSubSectionMap)
        } else {
          currentMap + (sectionName -> Map(subsectionName -> eAndOSubSection))
        }

        loop(remainingBsonDocs.tail, nextMap)
      }
    }

    loop(selectedEsAndOsWithBenchmarksDocs, Map())
  }

  private def createEsAndOsPlusBenchmarksForCurriculumAreaAndLevelMap(
                                                                       esOsBenchmarksByGroup: BsonArray,
                                                                       curriculumArea: String
                                                                     ): Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel] = {
    import scala.collection.JavaConverters._

    @tailrec
    def loop(
              remainingEsOsBenchmarksByGroup: List[BsonDocument],
              currentMap: Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel])
    : Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel] = {
      if (remainingEsOsBenchmarksByGroup.isEmpty) {
        currentMap
      } else {
        val doc = remainingEsOsBenchmarksByGroup.head
        val groupId = doc.getString(WeeklyPlanningSchema.GROUP_ID).getValue

        val selectedEsAndOsWithBenchmarksArray = doc.getArray(WeeklyPlanningSchema.SELECTED_ES_AND_OS_WITH_BENCHMARKS)
        val selectedEsAndOsWithBenchmarksDocs = selectedEsAndOsWithBenchmarksArray.getValues.asScala.toList.map(elem => elem.asDocument())
        val selectedSectionNameToSubSections: Map[String, Map[String, EandOSetSubSection]] = createSectionNameToSubSections(selectedEsAndOsWithBenchmarksDocs)

        val completedEsAndOsWithBenchmarksArray = doc.getArray(WeeklyPlanningSchema.COMPLETED_ES_AND_OS_WITH_BENCHMARKS)
        val completedEsAndOsWithBenchmarksDocs = completedEsAndOsWithBenchmarksArray.getValues.asScala.toList.map(elem => elem.asDocument())
        val completedSectionNameToSubSections: Map[String, Map[String, EandOSetSubSection]] = createSectionNameToSubSections(completedEsAndOsWithBenchmarksDocs)

        val nextVal: EsAndOsPlusBenchmarksForCurriculumAreaAndLevel = EsAndOsPlusBenchmarksForCurriculumAreaAndLevel(
          null,
          CurriculumArea.createCurriculumAreaFromString(curriculumArea),
          completedSectionNameToSubSections
        )

        val nextMap = currentMap + (groupId -> nextVal)
        loop(remainingEsOsBenchmarksByGroup.tail, nextMap)
      }
    }

    loop(esOsBenchmarksByGroup.getValues.asScala.toList.map(bsonValue => bsonValue.asDocument()), Map())
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

  def convertWeekPlanDocToModel(planDoc: Document): WeeklyHighLevelPlanOfOneSubject = {
    WeeklyHighLevelPlanOfOneSubject(
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
  List[WeeklyHighLevelPlanOfOneSubject] = {
    if (plansForTheWeekAsDoc == null) {
      Nil
    } else {
      for {
        planOfTheWeekDoc <- plansForTheWeekAsDoc
      } yield convertWeekPlanDocToModel(planOfTheWeekDoc)
    }
  }

  private[dao] def latestValueForEachSubject(allPlansAllSubjectsForTheWeek: List[WeeklyHighLevelPlanOfOneSubject]): List[WeeklyHighLevelPlanOfOneSubject] = {
    def loop(
              remainingPlans: List[WeeklyHighLevelPlanOfOneSubject],
              currentLatestSubjectPlans: Map[ScottishCurriculumPlanningAreaWrapper, WeeklyHighLevelPlanOfOneSubject]
            ): List[WeeklyHighLevelPlanOfOneSubject] = {
      if (remainingPlans.isEmpty) {
        currentLatestSubjectPlans.values.toList
      } else {
        val nextPlan = remainingPlans.head
        val maybeComparablePlan = currentLatestSubjectPlans.get(nextPlan.subject)
        val nextLatestSubjectMap = maybeComparablePlan match {
          case Some(currentPlan) => if (nextPlan.timestamp.isAfter(currentPlan.timestamp)) {
            currentLatestSubjectPlans + (nextPlan.subject -> nextPlan)
          } else {
            currentLatestSubjectPlans
          }
          case None => currentLatestSubjectPlans + (nextPlan.subject -> nextPlan)
        }

        loop(remainingPlans.tail, nextLatestSubjectMap)
      }
    }

    loop(allPlansAllSubjectsForTheWeek, Map())
  }

}
