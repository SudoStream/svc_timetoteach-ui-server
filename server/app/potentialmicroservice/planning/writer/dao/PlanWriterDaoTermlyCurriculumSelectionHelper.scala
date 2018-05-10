package potentialmicroservice.planning.writer.dao

import duplicate.model.esandos.{EandOSetSubSection, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel}
import duplicate.model.planning.WeeklyPlanOfOneSubject
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning.TermlyCurriculumSelection
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonString}
import potentialmicroservice.planning.sharedschema.TermlyCurriculumSelectionSchema._
import potentialmicroservice.planning.sharedschema.WeeklyPlanningSchema

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
      WeeklyPlanningSchema.SELECTED_ES_OS_AND_BENCHMARKS_BY_GROUP -> convertGroupToEsAndOsBenchmarksToBsonDocument(weeklyPlansToSave.groupToEsOsBenchmarks),
      WeeklyPlanningSchema.COMPLETED_ES_OS_AND_BENCHMARKS_BY_GROUP -> BsonArray()
    )
  }

  def extractAllLessonPlansAsMongoDbDocuments(weeklyPlansToSave: WeeklyPlanOfOneSubject): List[Document] = {
    Nil
  }

  //////////// Implementation ///////////////

  private def convertListStringsToBsonArray(listOfStrings: List[String]) : BsonArray = {
    BsonArray{
      for {
        elem <- listOfStrings
      } yield BsonString(elem)
    }
  }

  private def createBsonArrayOfSelectedEsOsAndBenchmarks(esOsBenchmarks: EsAndOsPlusBenchmarksForCurriculumAreaAndLevel): BsonArray = {
    BsonArray(
      for {
        sectionName <- esOsBenchmarks.setSectionNameToSubSections.keys.toList
        subSectionName <- esOsBenchmarks.setSectionNameToSubSections(sectionName).keys.toList
        esOsBenchies  : EandOSetSubSection = esOsBenchmarks.setSectionNameToSubSections(sectionName)(subSectionName)
        esAndOs = esOsBenchies.eAndOs.map(elem => elem.code)
        benchies = esOsBenchies.benchmarks.map(elem => elem.value)
      } yield Document(
        WeeklyPlanningSchema.SELECTED_SECTION_NAME -> sectionName,
        WeeklyPlanningSchema.SELECTED_SUBSECTION_NAME -> subSectionName,
        WeeklyPlanningSchema.SELECTED_ES_AND_OS -> convertListStringsToBsonArray(esAndOs ),
        WeeklyPlanningSchema.SELECTED_BENCHMARKS -> convertListStringsToBsonArray(benchies )
      )
    )
  }

  private def createDocumentForGroupToEsOsAndBenchmarks(groupId: String, esOsBenchmarks: EsAndOsPlusBenchmarksForCurriculumAreaAndLevel): Document = {
    Document(
      WeeklyPlanningSchema.GROUP_ID -> groupId,
      WeeklyPlanningSchema.SELECTED_ES_AND_OS_WITH_BENCHMARKS -> createBsonArrayOfSelectedEsOsAndBenchmarks(esOsBenchmarks)
    )
  }

  private def convertGroupToEsAndOsBenchmarksToBsonDocument(groupToEsOsBenchmarks: Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]): BsonArray = {
    BsonArray({
      for {
        groupId <- groupToEsOsBenchmarks.keys
        documentGroupIdToEsOsBenchmarks = createDocumentForGroupToEsOsAndBenchmarks(groupId, groupToEsOsBenchmarks(groupId))
      } yield documentGroupIdToEsOsBenchmarks
    }.toList)
  }

  private def convertListOfScottishCurriculumPlanningAreasToBsonArray(scottishCurriculaPlanningAreas: List[ScottishCurriculumPlanningArea]): BsonArray = {
    BsonArray({
      for {
        planningArea <- scottishCurriculaPlanningAreas
      } yield BsonString(planningArea.toString)
    })
  }

}
