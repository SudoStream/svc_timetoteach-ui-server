package potentialmicroservice.planning.writer.dao

import duplicate.model.esandos.{EandOSetSubSection, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel}
import duplicate.model.planning.{LessonPlan, WeeklyPlanOfOneSubject}
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning.TermlyCurriculumSelection
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonString}
import potentialmicroservice.planning.sharedschema.TermlyCurriculumSelectionSchema._
import potentialmicroservice.planning.sharedschema.{SingleLessonPlanSchema, WeeklyPlanningSchema}

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
      weeklyPlansToSave.weekBeginningIsoDate,
      lessonPlan
    )
  }

  //////////// Implementation ///////////////

  private def createDocumentFromLessonPlan(tttUserId: String,
                                           classId: String,
                                           weekBeginningIsoDate: String,
                                           lessonPlan: LessonPlan): Document = {
    Document(
      SingleLessonPlanSchema.TTT_USER_ID -> tttUserId,
      SingleLessonPlanSchema.CLASS_ID -> classId,
      SingleLessonPlanSchema.SUBJECT -> lessonPlan.subject,
      SingleLessonPlanSchema.SUBJECT_ADDITIONAL_INFO -> lessonPlan.subjectAdditionalInfo,
      SingleLessonPlanSchema.WEEK_BEGINNING_ISO_DATE -> weekBeginningIsoDate,
      SingleLessonPlanSchema.LESSON_DATE -> lessonPlan.lessonDateIso,
      SingleLessonPlanSchema.CREATED_TIMESTAMP -> java.time.LocalDateTime.now().toString.replace("T", " "),
      SingleLessonPlanSchema.LESSON_START_TIME -> lessonPlan.startTimeIso,
      SingleLessonPlanSchema.LESSON_END_TIME -> lessonPlan.endTimeIso,

      SingleLessonPlanSchema.ACTIVITIES_PER_GROUP -> convertGroupAttributesToBsonArray(lessonPlan.activitiesPerGroup),
      SingleLessonPlanSchema.RESOURCES -> convertListStringsToBsonArray(lessonPlan.resources),
      SingleLessonPlanSchema.LEARNING_INTENTIONS_PER_GROUP -> convertGroupAttributesToBsonArray(lessonPlan.learningIntentionsPerGroup),
      SingleLessonPlanSchema.SUCCESS_CRITERIA_PER_GROUP -> convertGroupAttributesToBsonArray(lessonPlan.successCriteriaPerGroup),
      SingleLessonPlanSchema.PLENARIES -> convertListStringsToBsonArray(lessonPlan.plenary),
      SingleLessonPlanSchema.FORMATIVE_ASSESSMENT_PER_GROUP -> convertGroupAttributesToBsonArray(lessonPlan.formativeAssessmentPerGroup),
      SingleLessonPlanSchema.NOTES_BEFORE -> convertListStringsToBsonArray(lessonPlan.notesBefore),
      SingleLessonPlanSchema.NOTES_AFTER -> convertListStringsToBsonArray(lessonPlan.notesAfter)
    )
  }

  private def convertGroupAttributesToBsonArray(attributeMap: Map[String, List[String]]): BsonArray = {
    BsonArray(
      for {
        attributeKeyValue <- attributeMap.keys.toList
      } yield Document(
        SingleLessonPlanSchema.ATTRIBUTE_VALUE -> attributeKeyValue,
        SingleLessonPlanSchema.GROUP_IDS -> convertListStringsToBsonArray(attributeMap(attributeKeyValue))
      )
    )
  }

  private def convertListStringsToBsonArray(listOfStrings: List[String]): BsonArray = {
    BsonArray {
      for {
        elem <- listOfStrings
      } yield BsonString(elem)
    }
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

    for(doc <- docsToAdd) {
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
