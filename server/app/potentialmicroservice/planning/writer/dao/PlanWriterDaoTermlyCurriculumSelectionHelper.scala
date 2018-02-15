package potentialmicroservice.planning.writer.dao

import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning.TermlyCurriculumSelection
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonString}
import potentialmicroservice.planning.sharedschema.TermlyCurriculumSelectionSchema._

trait PlanWriterDaoTermlyCurriculumSelectionHelper
{
  def convertTermlyCurriculumSelectionToMongoDbDocument(termlyCurriculumSelection: TermlyCurriculumSelection): Document =
  {
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

  //////////// Implementation ///////////////

  private def convertListOfScottishCurriculumPlanningAreasToBsonArray(scottishCurriculaPlanningAreas: List[ScottishCurriculumPlanningArea]): BsonArray =
  {
    BsonArray({
      for {
        planningArea <- scottishCurriculaPlanningAreas
      } yield BsonString(planningArea.toString)
    })
  }

}
