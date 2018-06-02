package potentialmicroservice.planning.writer.dao

import duplicate.model.esandos.{CompletedEsAndOsByGroup, EandOSetSubSection, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel, NotStartedEsAndOsByGroup}
import duplicate.model.planning.{AttributeRowKey, LessonPlan, WeeklyPlanOfOneSubject}
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning.TermlyCurriculumSelection
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonString}
import potentialmicroservice.planning.sharedschema.TermlyCurriculumSelectionSchema._
import potentialmicroservice.planning.sharedschema.{EsAndOsStatusSchema, SingleLessonPlanSchema, WeeklyPlanningSchema}

import scala.annotation.tailrec

trait PlanWriterDaoTermlyCurriculumSelectionHelper {
  def convertTermlyCurriculumSelectionToMongoDbDocument(termlyCurriculumSelection: TermlyCurriculumSelection): Document = {
    val endYear = if (termlyCurriculumSelection.schoolTerm.schoolYear.maybeCalendarYearEnd.isDefined) {
      "-" + termlyCurriculumSelection.schoolTerm.schoolYear.maybeCalendarYearEnd.get
    } else ""
    val schoolYearValue: String = termlyCurriculumSelection.schoolTerm.schoolYear.calendarYearStart.toString + endYear

    val scottishCurriculaPlanningAreasAsBsonArray =
      convertListOfScottishCurriculumPlanningAreasToBsonArray(termlyCurriculumSelection.planningAreas)

    Document(
      TTT_USER_ID -> termlyCurriculumSelection.tttUserId.value,
      CLASS_ID -> termlyCurriculumSelection.classId.value,
      CREATED_TIMESTAMP -> termlyCurriculumSelection.createdTime.toString.replace("T", " "),
      CURRICULUM_PLANNING_AREAS -> scottishCurriculaPlanningAreasAsBsonArray,
      SCHOOL_TERM -> BsonDocument(
        SCHOOL_YEAR -> schoolYearValue,
        SCHOOL_TERM_NAME -> termlyCurriculumSelection.schoolTerm.schoolTermName.toString,
        SCHOOL_TERM_FIRST_DAY -> termlyCurriculumSelection.schoolTerm.termFirstDay.toString,
        SCHOOL_TERM_LAST_DAY -> termlyCurriculumSelection.schoolTerm.termLastDay.toString
      )
    )
  }


  private def createEsOsBenchiesAsDocumentsImpl(
                                                 weeklyPlansToSave: WeeklyPlanOfOneSubject,
                                                 status: String,
                                                 valueType: String,
                                                 groupId: String,
                                                 section: String,
                                                 subsection: String,
                                                 codes: List[String]
                                               ): List[Document] = {
    @tailrec
    def loop(remainingCodes: List[String], currentDocs: List[Document]): List[Document] = {
      if (remainingCodes.isEmpty) currentDocs
      else {
        val newDoc = Document(
          EsAndOsStatusSchema.TTT_USER_ID -> weeklyPlansToSave.tttUserId,
          EsAndOsStatusSchema.CLASS_ID -> weeklyPlansToSave.classId,
          EsAndOsStatusSchema.SUBJECT -> weeklyPlansToSave.subject,
          EsAndOsStatusSchema.CREATED_TIMESTAMP -> java.time.LocalDateTime.now().toString.replace("T", " "),

          EsAndOsStatusSchema.VALUE_TYPE -> valueType,
          EsAndOsStatusSchema.STATUS -> status,
          EsAndOsStatusSchema.GROUP_ID -> groupId,
          EsAndOsStatusSchema.SECTION -> section,
          EsAndOsStatusSchema.SUBSECTION -> subsection,

          EsAndOsStatusSchema.VALUE -> remainingCodes.head
        )
        loop(remainingCodes.tail, newDoc :: currentDocs)
      }
    }

    loop(codes, Nil)
  }

  private def createEsOsBenchiesAsDocuments(
                                             weeklyPlansToSave: WeeklyPlanOfOneSubject,
                                             status: String,
                                             groupsToEsOsBenchiesMap: Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]
                                           ): List[Document] = {
    {
      {
        for {
          groupId <- groupsToEsOsBenchiesMap.keys
          sectionToSubsectionToValues = groupsToEsOsBenchiesMap(groupId).setSectionNameToSubSections
          section <- sectionToSubsectionToValues.keys
          subsection <- sectionToSubsectionToValues(section).keys

          esAndOs = sectionToSubsectionToValues(section)(subsection).eAndOs.map(elem => elem.code)
          esAndOsAsDocs = createEsOsBenchiesAsDocumentsImpl(weeklyPlansToSave, status, EsAndOsStatusSchema.VALUE_TYPE_EANDO, groupId, section, subsection, esAndOs)

          benchmarks = sectionToSubsectionToValues(section)(subsection).benchmarks.map(elem => elem.value)
          benchmarksAsDocs = createEsOsBenchiesAsDocumentsImpl(weeklyPlansToSave, status, EsAndOsStatusSchema.VALUE_TYPE_BENCHMARK, groupId, section, subsection, benchmarks)
        } yield esAndOsAsDocs ::: benchmarksAsDocs
      }.toList
    }.flatten
  }

  def extractEsOsBenchiesAsDocuments(
                                      groupsToEsOsBenchies: Either[CompletedEsAndOsByGroup, NotStartedEsAndOsByGroup],
                                      weeklyPlansToSave: WeeklyPlanOfOneSubject
                                    ): List[Document] = {
    val groupsToEsOsBenchiesMap: Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel] = if (groupsToEsOsBenchies.isLeft) groupsToEsOsBenchies.left.get.completedEsAndOsByGroup else
      groupsToEsOsBenchies.right.get.completedEsAndOsByGroup

    val status = if (groupsToEsOsBenchies.isLeft) "COMPLETE" else "NOT_STARTED"
    createEsOsBenchiesAsDocuments(weeklyPlansToSave, status, groupsToEsOsBenchiesMap)
  }

  def extractWeeklyPlanHighLevelAsMongoDbDocument(weeklyPlansToSave: WeeklyPlanOfOneSubject): Document = {
    Document(
      WeeklyPlanningSchema.TTT_USER_ID -> weeklyPlansToSave.tttUserId,
      WeeklyPlanningSchema.CLASS_ID -> weeklyPlansToSave.classId,
      WeeklyPlanningSchema.SUBJECT -> weeklyPlansToSave.subject,
      WeeklyPlanningSchema.WEEK_BEGINNING_ISO_DATE -> weeklyPlansToSave.weekBeginningIsoDate,
      WeeklyPlanningSchema.CREATED_TIMESTAMP -> java.time.LocalDateTime.now().toString.replace("T", " "),
      WeeklyPlanningSchema.SELECTED_ES_OS_AND_BENCHMARKS_BY_GROUP -> convertGroupToEsAndOsBenchmarksToBsonArray(weeklyPlansToSave.groupToEsOsBenchmarks),
      WeeklyPlanningSchema.COMPLETED_ES_OS_AND_BENCHMARKS_BY_GROUP -> BsonArray()
    )
  }

  def extractAllLessonPlansAsMongoDbDocuments(weeklyPlansToSave: WeeklyPlanOfOneSubject): List[Document] = {
    for {
      lessonPlan: LessonPlan <- weeklyPlansToSave.lessons
    } yield createDocumentFromLessonPlan(
      weeklyPlansToSave.tttUserId,
      weeklyPlansToSave.classId,
      lessonPlan
    )
  }

  //////////// Implementation ///////////////

  private def createDocumentFromLessonPlan(tttUserId: String,
                                           classId: String,
                                           lessonPlan: LessonPlan): Document = {
    Document(
      SingleLessonPlanSchema.TTT_USER_ID -> tttUserId,
      SingleLessonPlanSchema.CLASS_ID -> classId,
      SingleLessonPlanSchema.SUBJECT -> lessonPlan.subject,
      SingleLessonPlanSchema.SUBJECT_ADDITIONAL_INFO -> lessonPlan.subjectAdditionalInfo,
      SingleLessonPlanSchema.WEEK_BEGINNING_ISO_DATE -> lessonPlan.weekBeginningIsoDate,
      SingleLessonPlanSchema.LESSON_DATE -> lessonPlan.lessonDateIso,
      SingleLessonPlanSchema.CREATED_TIMESTAMP -> java.time.LocalDateTime.now().toString.replace("T", " "),
      SingleLessonPlanSchema.LESSON_START_TIME -> lessonPlan.startTimeIso,
      SingleLessonPlanSchema.LESSON_END_TIME -> lessonPlan.endTimeIso,

      SingleLessonPlanSchema.ACTIVITIES_PER_GROUP -> convertGroupAttributesToBsonArray(lessonPlan.activitiesPerGroup),
      SingleLessonPlanSchema.RESOURCES -> convertListAttributeRowKeyToBsonArray(lessonPlan.resources),
      SingleLessonPlanSchema.LEARNING_INTENTIONS_PER_GROUP -> convertGroupAttributesToBsonArray(lessonPlan.learningIntentionsPerGroup),
      SingleLessonPlanSchema.SUCCESS_CRITERIA_PER_GROUP -> convertGroupAttributesToBsonArray(lessonPlan.successCriteriaPerGroup),
      SingleLessonPlanSchema.PLENARIES -> convertListAttributeRowKeyToBsonArray(lessonPlan.plenary),
      SingleLessonPlanSchema.FORMATIVE_ASSESSMENT_PER_GROUP -> convertGroupAttributesToBsonArray(lessonPlan.formativeAssessmentPerGroup),
      SingleLessonPlanSchema.NOTES_BEFORE -> convertListAttributeRowKeyToBsonArray(lessonPlan.notesBefore),
      SingleLessonPlanSchema.NOTES_AFTER -> convertListAttributeRowKeyToBsonArray(lessonPlan.notesAfter)
    )
  }

  private def convertGroupAttributesToBsonArray(attributeMap: Map[AttributeRowKey, List[String]]): BsonArray = {
    val bsonArrayToreturn = BsonArray()

    val docsToAdd = for {
      attributeKeyValue <- attributeMap.keys.toList
    } yield Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> attributeKeyValue.attributeValue,
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> attributeKeyValue.attributeOrderNumber,
      SingleLessonPlanSchema.GROUP_IDS -> convertListStringsToBsonArray(attributeMap(attributeKeyValue))
    )

    for (doc <- docsToAdd) {
      bsonArrayToreturn.add(doc.toBsonDocument)
    }

    bsonArrayToreturn
  }

  private def convertListAttributeRowKeyToBsonArray(attributeRowKeys: List[AttributeRowKey]): BsonArray = {
    val bsonArrayToreturn = BsonArray()

    val docsToAdd = for {
      key <- attributeRowKeys
    } yield Document(
      SingleLessonPlanSchema.ATTRIBUTE_VALUE -> key.attributeValue,
      SingleLessonPlanSchema.ATTRIBUTE_ORDER_NUMBER -> key.attributeOrderNumber
    )

    for (doc <- docsToAdd) {
      bsonArrayToreturn.add(doc.toBsonDocument)
    }

    bsonArrayToreturn
  }

  private def convertListStringsToBsonArray(listOfStrings: List[String]): BsonArray = {
    val bsonArrayToreturn = BsonArray()

    val docsToAdd = for {
      key <- listOfStrings
    } yield BsonString(key)

    for (doc <- docsToAdd) {
      bsonArrayToreturn.add(doc)
    }

    bsonArrayToreturn
  }

  private def createBsonArrayOfSelectedEsOsAndBenchmarks(esOsBenchmarks: EsAndOsPlusBenchmarksForCurriculumAreaAndLevel): BsonArray = {
    val bsonArrayToreturn = BsonArray()

    val docsToAdd = for {
      sectionName <- esOsBenchmarks.setSectionNameToSubSections.keys.toList
      subSectionName <- esOsBenchmarks.setSectionNameToSubSections(sectionName).keys.toList
      esOsBenchies: EandOSetSubSection = esOsBenchmarks.setSectionNameToSubSections(sectionName)(subSectionName)
      esAndOs = esOsBenchies.eAndOs.map(elem => elem.code)
      benchies = esOsBenchies.benchmarks.map(elem => elem.value)
    } yield Document(
      WeeklyPlanningSchema.SELECTED_SECTION_NAME -> sectionName,
      WeeklyPlanningSchema.SELECTED_SUBSECTION_NAME -> subSectionName,
      WeeklyPlanningSchema.SELECTED_ES_AND_OS -> convertListStringsToBsonArray(esAndOs),
      WeeklyPlanningSchema.SELECTED_BENCHMARKS -> convertListStringsToBsonArray(benchies)
    )

    for (doc <- docsToAdd) {
      bsonArrayToreturn.add(doc.toBsonDocument)
    }

    bsonArrayToreturn
  }

  private def createDocumentForGroupToEsOsAndBenchmarks(groupId: String, esOsBenchmarks: EsAndOsPlusBenchmarksForCurriculumAreaAndLevel): Document = {
    Document(
      WeeklyPlanningSchema.GROUP_ID -> groupId,
      WeeklyPlanningSchema.SELECTED_ES_AND_OS_WITH_BENCHMARKS -> createBsonArrayOfSelectedEsOsAndBenchmarks(esOsBenchmarks)
    )
  }

  def convertGroupToEsAndOsBenchmarksToBsonArray(groupToEsOsBenchmarks: Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]): BsonArray = {
    val groupToEsOsAndBenchiesBsonArray = BsonArray()

    for (groupId <- groupToEsOsBenchmarks.keys) {
      val docToAdd = createDocumentForGroupToEsOsAndBenchmarks(groupId, groupToEsOsBenchmarks(groupId))
      groupToEsOsAndBenchiesBsonArray.add(docToAdd.toBsonDocument)
    }

    groupToEsOsAndBenchiesBsonArray
  }


  private def convertListOfScottishCurriculumPlanningAreasToBsonArray(scottishCurriculaPlanningAreas: List[ScottishCurriculumPlanningArea]): BsonArray = {
    BsonArray({
      for {
        planningArea <- scottishCurriculaPlanningAreas
      } yield BsonString(planningArea.toString)
    })
  }

}
