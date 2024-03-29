package potentialmicroservice.planning.reader.dao

import java.time.LocalDate

import dao.MongoDbConnection
import duplicate.model.esandos._
import duplicate.model.planning.{AttributeRowKey, FullWeeklyPlanOfLessons, LessonPlan, WeeklyPlanOfOneSubject}
import models.timetoteach.planning.ScottishCurriculumPlanningAreaWrapper
import models.timetoteach.planning.weekly.WeeklyHighLevelPlanOfOneSubject
import models.timetoteach.{ClassId, TimeToTeachUserId}
import org.mongodb.scala.Document
import org.mongodb.scala.bson.{BsonArray, BsonDocument}
import play.api.Logger
import potentialmicroservice.planning.sharedschema.{EsAndOsStatusSchema, SingleLessonPlanSchema, WeeklyPlanningSchema}
import tttutils.CurriculumConverterUtil
import tttutils.mongodb.MongoDbSafety

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait RetrieveFullWeekOfLessonsDaoHelper {

  private val logger: Logger = Logger
  def getDbConnection: MongoDbConnection

  def retrieveFullWeekOfLessonsImpl(tttUserId: TimeToTeachUserId,
                                    classId: ClassId,
                                    mondayDateOfWeekIso: String): Future[FullWeeklyPlanOfLessons] = {
    logger.info(s"Retrieve full week of lessons: $tttUserId|$classId|$mondayDateOfWeekIso")

    val futureLatestHighLevelPlansForAllSubjects = readLatestHighLevelPlansForTheWeek(tttUserId, classId, mondayDateOfWeekIso)
    val futureLessonPlansForAllSubjects = readLatestLessonPlansForTheWeek(tttUserId, classId, mondayDateOfWeekIso)
    val futureLatestCombinedWeeklyPlans = combinePlans(futureLatestHighLevelPlansForAllSubjects, futureLessonPlansForAllSubjects)

    createFullWeeklyPlanOfLessons(
      tttUserId,
      classId,
      mondayDateOfWeekIso,
      futureLatestCombinedWeeklyPlans
    )
  }


  def completedEsOsBenchmarksImpl(
                                   tttUserId: TimeToTeachUserId,
                                   classId: ClassId,
                                   mondayDateOfWeekIso: String
                                 ): Future[CompletedEsAndOsByGroupBySubject] = {
    val futureAllEAndOsBenchmarksStatuses = readAllEAndOsBenchmarksStatuses(tttUserId, classId)

    for {
      allEAndOsBenchmarksStatuses <- futureAllEAndOsBenchmarksStatuses
      latestVersionsOfEachEandOBenchmark = latestVersionOfEachEandOBenchmark(allEAndOsBenchmarksStatuses.toList, mondayDateOfWeekIso)
      completedEAndOBenchmarks = filterEsAndOs(latestVersionsOfEachEandOBenchmark, List("COMPLETE"))
      completedEsAndOsByGroup = buildMapOfEAndOsBenchmarks(completedEAndOBenchmarks)
    } yield CompletedEsAndOsByGroupBySubject(completedEsAndOsByGroup)
  }

  def completedAndStartedEsOsBenchmarksImpl(
                                             tttUserId: TimeToTeachUserId,
                                             classId: ClassId,
                                             mondayDateOfWeekIso: String
                                           ): Future[(CompletedEsAndOsByGroupBySubject, StartedEsAndOsByGroupBySubject)] = {
    logger.info(s"\nRetrieve COMPLETE E&Os/Benchmarks: $tttUserId|$classId")

    val futureAllEAndOsBenchmarksStatuses = readAllEAndOsBenchmarksStatuses(tttUserId, classId)

    val eventualCompleteAndStarted  = for {
      allEAndOsBenchmarksStatuses <- futureAllEAndOsBenchmarksStatuses
      latestVersionsOfEachEandOBenchmark = latestVersionOfEachEandOBenchmark(allEAndOsBenchmarksStatuses.toList, mondayDateOfWeekIso)
      completedEAndOBenchmarks = filterEsAndOs(latestVersionsOfEachEandOBenchmark, List("COMPLETE", "STARTED"))
      (completedEsAndOsByGroupDoc, startedEsAndOsByGroupDoc) = completedEAndOBenchmarks.partition(elem => elem.getString(EsAndOsStatusSchema.STATUS) == "COMPLETE")
      completedEsAndOsByGroup = buildMapOfEAndOsBenchmarks(completedEsAndOsByGroupDoc)
      startedEsAndOsByGroup = buildMapOfEAndOsBenchmarks(startedEsAndOsByGroupDoc)
    } yield (CompletedEsAndOsByGroupBySubject(completedEsAndOsByGroup), StartedEsAndOsByGroupBySubject(startedEsAndOsByGroup))

    logger.debug("Got the eventualCompleteAndStarted\n")
    eventualCompleteAndStarted
  }

  ////////////////

  private[dao] def readAllEAndOsBenchmarksStatuses(
                                                    tttUserId: TimeToTeachUserId,
                                                    classId: ClassId
                                                  ): Future[Seq[Document]] = {
    if (tttUserId != null && classId != null) {
      val findMatcher = BsonDocument(
        EsAndOsStatusSchema.TTT_USER_ID -> tttUserId.value,
        EsAndOsStatusSchema.CLASS_ID -> classId.value
      )
      getDbConnection.getEAndOBenchmarkStatusCollection.find(findMatcher).toFuture()
    } else {
      Future {
        Nil
      }
    }
  }

  private[dao] def addToEandOSetSubSection(current: EandOSetSubSection, eitherEandOorBenchmark: Either[EandO, Benchmark]): EandOSetSubSection = {
    eitherEandOorBenchmark match {
      case Left(newEAndO) =>
        EandOSetSubSection(current.auxiliaryText, newEAndO :: current.eAndOs, current.benchmarks)
      case Right(newBenchmark) =>
        EandOSetSubSection(current.auxiliaryText, current.eAndOs, newBenchmark :: current.benchmarks)
    }
  }

  def createNewEAndOorBenchmark(nextDoc: Document): Either[EandO, Benchmark] = {
    if (nextDoc.getString(EsAndOsStatusSchema.VALUE_TYPE) == EsAndOsStatusSchema.VALUE_TYPE_EANDO) {
      Left(EandO(nextDoc.getString(EsAndOsStatusSchema.VALUE), Nil))
    } else {
      Right(Benchmark(nextDoc.getString(EsAndOsStatusSchema.VALUE)))
    }
  }

  private[dao] def buildMapOfEAndOsBenchmarks(
                                               latestVersionOfEachEandOBenchmark: List[Document]
                                             ): Map[String, Map[String, Map[String, Map[String, EandOSetSubSection]]]] = {
    logger.debug(s"latestVersionOfEachEandOBenchmark : ${latestVersionOfEachEandOBenchmark.size}")

    @tailrec
    def loop(
              remainingDocs: List[Document],
              currentHighLevelMap: Map[String, Map[String, Map[String, Map[String, EandOSetSubSection]]]]
            ): Map[String, Map[String, Map[String, Map[String, EandOSetSubSection]]]] = {
      if (remainingDocs.isEmpty) currentHighLevelMap
      else {
        val nextDoc = remainingDocs.head

        val subject = nextDoc.getString(EsAndOsStatusSchema.SUBJECT)
        val groupId = nextDoc.getString(EsAndOsStatusSchema.GROUP_ID)
        val section = nextDoc.getString(EsAndOsStatusSchema.SECTION)
        val subsection = nextDoc.getString(EsAndOsStatusSchema.SUBSECTION)

        val eitherEandOorBenchmark = createNewEAndOorBenchmark(nextDoc)

        logger.debug(s"Adding ... ${eitherEandOorBenchmark.toString}")

        val nextMap: Map[String, Map[String, Map[String, Map[String, EandOSetSubSection]]]] =
          if (!currentHighLevelMap.isDefinedAt(subject)) {
            logger.debug(s"1")
            currentHighLevelMap + (subject -> Map(groupId -> Map(section -> Map(subsection -> addToEandOSetSubSection(
              EandOSetSubSection("", Nil, Nil), eitherEandOorBenchmark)))))
          } else {
            logger.debug(s"2")
            val currentSubjectMap: Map[String, Map[String, Map[String, EandOSetSubSection]]] = currentHighLevelMap(subject)

            if (!currentSubjectMap.isDefinedAt(groupId)) {
              logger.debug(s"3")
              val nextSubjectMap = currentSubjectMap + (groupId -> Map(section -> Map(subsection -> addToEandOSetSubSection(
                EandOSetSubSection("", Nil, Nil), eitherEandOorBenchmark))))
              currentHighLevelMap + (subject -> nextSubjectMap)

            } else {
              logger.debug(s"4")
              val currentGroupMap: Map[String, Map[String, EandOSetSubSection]] = currentSubjectMap(groupId)

              if (!currentGroupMap.isDefinedAt(section)) {
                logger.debug(s"5")
                val nextGroupMap = currentGroupMap + (section -> Map(subsection -> addToEandOSetSubSection(
                  EandOSetSubSection("", Nil, Nil), eitherEandOorBenchmark)))
                val nextSubjectMap = currentSubjectMap + (groupId -> nextGroupMap)
                currentHighLevelMap + (subject -> nextSubjectMap)
              } else {
                logger.debug(s"6")
                val currentSectionMap: Map[String, EandOSetSubSection] = currentGroupMap(section)

                if (!currentSectionMap.isDefinedAt(subsection)) {
                  logger.debug(s"7")
                  val nextSectionMap: Map[String, EandOSetSubSection] = currentSectionMap +
                    (subsection -> addToEandOSetSubSection(EandOSetSubSection("", Nil, Nil), eitherEandOorBenchmark))
                  val nextGroupMap = currentGroupMap + (section -> nextSectionMap)
                  val nextSubjectMap = currentSubjectMap + (groupId -> nextGroupMap)
                  currentHighLevelMap + (subject -> nextSubjectMap)
                } else {

                  logger.debug(s"8")
                  val currentEAndOSetSubsection: EandOSetSubSection = currentSectionMap(subsection)
                  val nextEAndOSetSubsection = addToEandOSetSubSection(currentEAndOSetSubsection, eitherEandOorBenchmark)
                  val nextSectionMap: Map[String, EandOSetSubSection] = currentSectionMap + (subsection -> nextEAndOSetSubsection)
                  val nextGroupMap = currentGroupMap + (section -> nextSectionMap)
                  val nextSubjectMap = currentSubjectMap + (groupId -> nextGroupMap)
                  currentHighLevelMap + (subject -> nextSubjectMap)
                }
              }
            }
          }

        loop(remainingDocs.tail, nextMap)
      }
    }

    loop(latestVersionOfEachEandOBenchmark, Map())
  }

  private[dao] def latestVersionOfEachEandOBenchmark(
                                                      allEAndOsBenchmarksStatuses: List[Document],
                                                      mondayDateOfWeekIso: String
                                                    ): List[Document] = {
    val mondayDateOfWeek = LocalDate.parse(mondayDateOfWeekIso)

    logger.debug(s"allEAndOsBenchmarksStatuses : ${allEAndOsBenchmarksStatuses.size}")

    def loop(remainingElems: List[Document], currentMap: Map[String, Document]): List[Document] = {
      if (remainingElems.isEmpty) currentMap.values.toList
      else {
        val nextDoc = remainingElems.head
        val nextDocTimestamp = MongoDbSafety.safelyParseTimestamp(nextDoc.getString(EsAndOsStatusSchema.CREATED_TIMESTAMP))
        val nextDocKey = nextDoc.getString(EsAndOsStatusSchema.TTT_USER_ID) +
          nextDoc.getString(EsAndOsStatusSchema.CLASS_ID) +
          nextDoc.getString(EsAndOsStatusSchema.SUBJECT) +
          nextDoc.getString(EsAndOsStatusSchema.VALUE_TYPE) +
          nextDoc.getString(EsAndOsStatusSchema.GROUP_ID) +
          nextDoc.getString(EsAndOsStatusSchema.SECTION) +
          nextDoc.getString(EsAndOsStatusSchema.SUBSECTION) +
          nextDoc.getString(EsAndOsStatusSchema.VALUE)
        val nextMap = if (nextDocTimestamp.toLocalDate.isAfter(mondayDateOfWeek.plusDays(6))) {
          currentMap
        } else if (currentMap.isDefinedAt(nextDocKey)) {
          val currentDoc = currentMap(nextDocKey)
          val currentDocTimestamp = MongoDbSafety.safelyParseTimestamp(currentDoc.getString(EsAndOsStatusSchema.CREATED_TIMESTAMP))

          if (nextDocTimestamp.isAfter(currentDocTimestamp)) {
            currentMap + (nextDocKey -> nextDoc)
          } else currentMap
        } else {
          currentMap + (nextDocKey -> nextDoc)
        }
        loop(remainingElems.tail, nextMap)
      }
    }

    loop(allEAndOsBenchmarksStatuses, Map())
  }

  private[dao] def filterEsAndOs(latestVersionOfEachEandOBenchmark: List[Document], filterAnd: List[String]): List[Document] = {
    @tailrec
    def loop(remainingElems: List[Document], currentFiltered: List[Document]): List[Document] = {
      if (remainingElems.isEmpty) currentFiltered
      else {
        val nextDoc = remainingElems.head
        val nextFiltered = if (filterAnd.contains(nextDoc.getString(EsAndOsStatusSchema.STATUS))) {
          nextDoc :: currentFiltered
        } else currentFiltered
        loop(remainingElems.tail, nextFiltered)
      }
    }

    loop(latestVersionOfEachEandOBenchmark, Nil)
  }

  private def createFullWeeklyPlanOfLessons(
                                             tttUserId: TimeToTeachUserId,
                                             classId: ClassId,
                                             mondayDateOfWeekIso: String,
                                             futureLatestCombinedWeeklyPlans: Future[Map[String, WeeklyPlanOfOneSubject]]
                                           ): Future[FullWeeklyPlanOfLessons] = {

    for {
      latestCombinedWeeklyPlans <- futureLatestCombinedWeeklyPlans
    } yield FullWeeklyPlanOfLessons(
      tttUserId.value,
      classId.value,
      mondayDateOfWeekIso,
      latestCombinedWeeklyPlans
    )
  }

  private def combinePlanLevels(highLevelPlans: List[WeeklyHighLevelPlanOfOneSubject],
                                latestSubjects: Map[ScottishCurriculumPlanningAreaWrapper, List[LessonPlan]]
                               ): Map[String, WeeklyPlanOfOneSubject] = {
    {
      for {
        subjectHighLevelPlan <- highLevelPlans
        subjectLessons = if (latestSubjects.isDefinedAt(subjectHighLevelPlan.subject)) latestSubjects(subjectHighLevelPlan.subject) else Nil
      } yield (
        subjectHighLevelPlan.subject.value.toString,
        WeeklyPlanOfOneSubject(
          subjectHighLevelPlan.timeToTeachUserId.value,
          subjectHighLevelPlan.classId.value,
          subjectHighLevelPlan.subject.value.toString,
          subjectHighLevelPlan.weekBeginning.toString,
          subjectHighLevelPlan.selectedEsOsBenchmarksByGroup,
          subjectLessons
        )
      )
    }.toMap
  }

  private[dao] def combinePlans(
                                 latestHighLevelPlansForAllSubjects: Future[List[WeeklyHighLevelPlanOfOneSubject]],
                                 latestLessonPlansForAllSubjects: Future[Map[ScottishCurriculumPlanningAreaWrapper, List[LessonPlan]]]
                               ): Future[Map[String, WeeklyPlanOfOneSubject]] = {
    for {
      highLevelPlans <- latestHighLevelPlansForAllSubjects
      latestSubjects: Map[ScottishCurriculumPlanningAreaWrapper, List[LessonPlan]] <- latestLessonPlansForAllSubjects
      plansCombinedMap = combinePlanLevels(highLevelPlans, latestSubjects)
    } yield plansCombinedMap
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

  private def stringBsonArrayToList(bsonArray: BsonArray): List[AttributeRowKey] = {
    import scala.collection.JavaConverters._

    @tailrec
    def loop(remainingDocs: List[BsonDocument], currentList: List[AttributeRowKey]): List[AttributeRowKey] = {
      if (remainingDocs.isEmpty) {
        currentList
      } else {
        val nextDoc = remainingDocs.head
        val attribute = nextDoc.getString(SingleLessonPlanSchema.ATTRIBUTE_VALUE).getValue
        val attributeOrderNumber = nextDoc.getInt32(SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER).getValue

        val nextList = AttributeRowKey(attribute, attributeOrderNumber) :: currentList
        loop(remainingDocs.tail, nextList)
      }
    }

    val docs = for {
      bsonValue <- bsonArray.getValues.asScala.toList
    } yield bsonValue.asDocument()

    loop(docs, Nil)
  }

  private[dao] def attrGroupBsonArrayToMapStringToListString(bsonArray: BsonArray): Map[AttributeRowKey, List[String]] = {
    import scala.collection.JavaConverters._

    @tailrec
    def loop(remainingDocs: List[BsonDocument], currentMap: Map[AttributeRowKey, List[String]]): Map[AttributeRowKey, List[String]] = {
      if (remainingDocs.isEmpty) {
        currentMap
      } else {
        val nextDoc = remainingDocs.head
        val attribute = nextDoc.getString(SingleLessonPlanSchema.ATTRIBUTE_VALUE).getValue
        val attributeOrderNumber = nextDoc.getInt32(SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER).getValue

        val groupIds = for {
          groupValue <- nextDoc.getArray(SingleLessonPlanSchema.GROUP_IDS).getValues.asScala.toList
        } yield groupValue.asString().getValue

        val nextMap = currentMap + (AttributeRowKey(attribute, attributeOrderNumber) -> groupIds)
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
      createdTimestamp = doc.getString(SingleLessonPlanSchema.CREATED_TIMESTAMP),

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

  private[dao] def decomposeMap(
                                 originalMap: Map[ScottishCurriculumPlanningAreaWrapper, Map[String, LessonPlan]]
                               ): Map[ScottishCurriculumPlanningAreaWrapper, List[LessonPlan]] = {
    @tailrec
    def loop(remainingKeys: List[ScottishCurriculumPlanningAreaWrapper],
             currentMap: Map[ScottishCurriculumPlanningAreaWrapper, List[LessonPlan]])
    : Map[ScottishCurriculumPlanningAreaWrapper, List[LessonPlan]] = {
      if (remainingKeys.isEmpty) {
        currentMap
      } else {
        val subjectKey = remainingKeys.head
        val timeToLessonPlans = originalMap(subjectKey)
        val lessonPlansForSubject = timeToLessonPlans.values.toList
        val nextMap = currentMap + (subjectKey -> lessonPlansForSubject)
        loop(remainingKeys.tail, nextMap)
      }
    }

    loop(originalMap.keys.toList, Map())
  }

  private[dao] def buildSubjectToLatestLessonsForTheWeekMap(
                                                             allLessonPlans: List[LessonPlan]
                                                           ): Map[ScottishCurriculumPlanningAreaWrapper, List[LessonPlan]] = {
    def getNextLessonKey(date: String, time: String): String = {
      s"${date}_$time"
    }

    @tailrec
    def loop(
              remainingLessons: List[LessonPlan],
              currentMap: Map[ScottishCurriculumPlanningAreaWrapper, Map[String, LessonPlan]]
            ): Map[ScottishCurriculumPlanningAreaWrapper, List[LessonPlan]] = {
      if (remainingLessons.isEmpty) {
        decomposeMap(currentMap)
      } else {
        val nextLesson = remainingLessons.head
        val nextLessonKey = getNextLessonKey(nextLesson.lessonDateIso, nextLesson.startTimeIso)
        val nextSubjectWrapper = CurriculumConverterUtil.
          convertSubjectToScottishCurriculumPlanningAreaWrapper(nextLesson.subject)

        val nextMap = if (currentMap.isDefinedAt(nextSubjectWrapper)) {
          if (currentMap(nextSubjectWrapper).isDefinedAt(nextLessonKey)) {
            val currentTimeLesson = currentMap(nextSubjectWrapper)(nextLessonKey)
            val currentLessonTimestamp = MongoDbSafety.safelyParseTimestamp(currentTimeLesson.createdTimestamp)
            val nextLessonTimestamp = MongoDbSafety.safelyParseTimestamp(nextLesson.createdTimestamp)
            if (nextLessonTimestamp.isAfter(currentLessonTimestamp)) {
              val nextTimeMap = currentMap(nextSubjectWrapper) + (nextLessonKey -> nextLesson)
              currentMap + (nextSubjectWrapper -> nextTimeMap)
            } else {
              currentMap
            }
          } else {
            val currentTimeMap = currentMap(nextSubjectWrapper)
            val nextTimeMap = currentTimeMap + (nextLessonKey -> nextLesson)
            currentMap + (nextSubjectWrapper -> nextTimeMap)
          }
        } else {
          currentMap + (nextSubjectWrapper -> Map(nextLessonKey -> nextLesson))
        }

        loop(remainingLessons.tail, nextMap)
      }
    }

    loop(allLessonPlans, Map())
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
