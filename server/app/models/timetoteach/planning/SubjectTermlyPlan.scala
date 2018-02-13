package models.timetoteach.planning

import java.time.LocalDateTime

import duplicate.model.EandOsWithBenchmarks
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.subjectdetail.SubjectName
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
                              subject: SubjectName,
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

  def convertSubjectNameStringToSubjectName(name: String): Option[SubjectName] =
  {
    name.toUpperCase match {
      case "ART" => Some(SubjectName.ART)
      case "ASSEMBLY" => Some(SubjectName.ASSEMBLY)
      case "EMPTY" => Some(SubjectName.EMPTY)
      case "DRAMA" => Some(SubjectName.DRAMA)
      case "GOLDEN_TIME" => Some(SubjectName.GOLDEN_TIME)
      case "HEALTH" => Some(SubjectName.HEALTH)
      case "ICT" => Some(SubjectName.ICT)
      case "MATHS" => Some(SubjectName.MATHS)
      case "MUSIC" => Some(SubjectName.MUSIC)
      case "NUMERACY" => Some(SubjectName.NUMERACY)
      case "OTHER" => Some(SubjectName.OTHER)
      case "READING" => Some(SubjectName.READING)
      case "PHYSICAL_EDUCATION" => Some(SubjectName.PHYSICAL_EDUCATION)
      case "RME" => Some(SubjectName.RME)
      case "SOFT_START" => Some(SubjectName.SOFT_START)
      case "SPELLING" => Some(SubjectName.SPELLING)
      case "TEACHER_COVERTIME" => Some(SubjectName.TEACHER_COVERTIME)
      case "TOPIC" => Some(SubjectName.TOPIC)
      case "WRITING" => Some(SubjectName.WRITING)
      case "PLAY" => Some(SubjectName.PLAY)
      case "MODERN_LANGUAGES" => Some(SubjectName.MODERN_LANGUAGES)
      case "SCIENCE" => Some(SubjectName.SCIENCE)
      case "HAND_WRITING" => Some(SubjectName.HAND_WRITING)
      case "GEOGRAPHY" => Some(SubjectName.GEOGRAPHY)
      case other =>
        logger.warn(s"Did not recognise subject name '$other'")
        None
    }
  }
}