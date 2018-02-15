package models.timetoteach.planning

import java.time.LocalDateTime

import duplicate.model.EandOsWithBenchmarks
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning.PlanType.PlanType
import models.timetoteach.term.SchoolTerm
import models.timetoteach.{ClassId, TimeToTeachUserId}
import play.api.Logger

case class SubjectTermlyPlan(
                              tttUserId: TimeToTeachUserId,
                              planType: PlanType,
                              schoolTerm: SchoolTerm,
                              classId: ClassId,
                              maybeGroupId: Option[GroupId],
                              planningArea: ScottishCurriculumPlanningArea,
                              createdTime: LocalDateTime,
                              eAndOsWithBenchmarks: List[EandOsWithBenchmarks]
                            )
{
  if (planType == PlanType.GROUP_LEVEL_PLAN) {
    if (maybeGroupId.isEmpty) {
      throw new IllegalArgumentException("If this is a group level plan then group id MUST be defined")
    }
  } else {
    if (maybeGroupId.isDefined) {
      throw new IllegalArgumentException("If this is a class level plan then group id MUST NOT be defined")
    }
  }
}


object PlanType extends Enumeration
{
  val logger: Logger = Logger
  type PlanType = Value
  val CLASS_LEVEL_PLAN, GROUP_LEVEL_PLAN = Value

  def createPlanTypeFromString(planTypeString: String): Option[PlanType] =
  {
    planTypeString.toUpperCase match {
      case "CLASS_LEVEL_PLAN" => Some(CLASS_LEVEL_PLAN)
      case "GROUP_LEVEL_PLAN" => Some(GROUP_LEVEL_PLAN)
      case other =>
        logger.warn(s"Did not recognise plan name '$other'")
        None
    }
  }
}

case class GroupId(value: String)

object SubjectNameConverter
{
  val logger: Logger = Logger

  def convertSubjectNameStringToSubjectName(name: String): Option[ScottishCurriculumPlanningArea] =
  {
    name.toUpperCase match {
      case "ART" | "EXPRESSIVE_ARTS__ART" => Some(ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__ART)
      case "ASSEMBLY" => None
      case "EMPTY" => None
      case "DRAMA" | "EXPRESSIVE_ARTS__DRAMA" => Some(ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__DRAMA)
      case "GOLDEN_TIME" => None
      case "HEALTH" | "HEALTH_AND_WELLBEING" => Some(ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING)
      case "ICT" | "TECHNOLOGIES" => Some(ScottishCurriculumPlanningArea.TECHNOLOGIES)
      case "MATHS" | "MATHEMATICS" => Some(ScottishCurriculumPlanningArea.MATHEMATICS)
      case "MUSIC" | "EXPRESSIVE_ARTS__MUSIC" => Some(ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__MUSIC)
      case "NUMERACY" => Some(ScottishCurriculumPlanningArea.MATHEMATICS)
      case "OTHER" => None
      case "READING" | "LITERACY__READING" => Some(ScottishCurriculumPlanningArea.LITERACY__READING)
      case "PHYSICAL_EDUCATION" | "HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION" =>
        Some(ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION)
      case "RME" | "RME__STANDARD" => Some(ScottishCurriculumPlanningArea.RME__STANDARD)
      case "RME__CATHOLIC" => Some(ScottishCurriculumPlanningArea.RME__CATHOLIC)
      case "SOFT_START" => None
      case "SPELLING" => None
      case "TEACHER_COVERTIME" => None
      case "TOPIC" => Some(ScottishCurriculumPlanningArea.TOPIC)
      case "WRITING" | "LITERACY__WRITING" => Some(ScottishCurriculumPlanningArea.LITERACY__WRITING)
      case "PLAY" => None
      case "MODERN_LANGUAGES" | "LITERACY__MODERN_LANGUAGES"=> Some(ScottishCurriculumPlanningArea.LITERACY__MODERN_LANGUAGES)
      case "SCIENCE" => Some(ScottishCurriculumPlanningArea.SCIENCE)
      case "HAND_WRITING" => None
      case "GEOGRAPHY" | "SOCIAL_STUDIES" => Some(ScottishCurriculumPlanningArea.SOCIAL_STUDIES)
      case "HISTORY" => Some(ScottishCurriculumPlanningArea.SOCIAL_STUDIES)
      case other =>
        logger.warn(s"Did not recognise subject name '$other'")
        None
    }
  }
}