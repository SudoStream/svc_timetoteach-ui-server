package controllers.planning.termly

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}

import controllers.serviceproxies.TermServiceProxy
import duplicate.model.TermlyPlansToSave
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning.{GroupId, PlanType, CurriculumAreaTermlyPlan}
import models.timetoteach.{ClassId, TimeToTeachUserId}

@Singleton
class TermPlansHelper @Inject()(termService: TermServiceProxy)
{

  import TermPlansHelper._

  def convertTermlyPlanToModel(
                                classId: String,
                                termlyPlansToSave: TermlyPlansToSave,
                                maybeGroupId: Option[GroupId],
                                curriculumArea: String
                              ): CurriculumAreaTermlyPlan =
  {
    val thisTerm = termService.currentSchoolTerm()
    val planType = if (maybeGroupId.isDefined) {
      PlanType.GROUP_LEVEL_PLAN
    } else {
      PlanType.CLASS_LEVEL_PLAN
    }

    CurriculumAreaTermlyPlan(
      termlyPlansToSave.tttUserId,
      planType,
      thisTerm,
      classId,
      maybeGroupId,
      curriculumArea,
      LocalDateTime.now(),
      termlyPlansToSave.eandOsWithBenchmarks
    )
  }

}

object TermPlansHelper
{

  implicit def schoolIdStringToSchoolId(schoolId: String): ClassId =
  {
    ClassId(schoolId)
  }

  implicit def convertSubjectStringToSubjectName(subjectName: String): ScottishCurriculumPlanningArea =
  {
    subjectName.toLowerCase match {
      case "empty" => ScottishCurriculumPlanningArea.NONE
      case "golden-time" => ScottishCurriculumPlanningArea.NONE
      case "other" => ScottishCurriculumPlanningArea.NONE
      case "ict" => ScottishCurriculumPlanningArea.TECHNOLOGIES
      case "music" | "expressive_arts__music" => ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__MUSIC
      case "drama" | "expressive_arts__drama" => ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__DRAMA
      case "health" => ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING
      case "teacher-covertime" => ScottishCurriculumPlanningArea.NONE
      case "assembly" => ScottishCurriculumPlanningArea.NONE
      case "reading" | "literacy__reading" => ScottishCurriculumPlanningArea.LITERACY__READING
      case "spelling" => ScottishCurriculumPlanningArea.NONE
      case "writing" | "literacy__writing" => ScottishCurriculumPlanningArea.LITERACY__WRITING
      case "maths" | "mathematics" => ScottishCurriculumPlanningArea.MATHEMATICS
      case "topic" => ScottishCurriculumPlanningArea.TOPIC
      case "physical-education" => ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION
      case "soft-start" => ScottishCurriculumPlanningArea.NONE
      case "numeracy" => ScottishCurriculumPlanningArea.MATHEMATICS
      case "art" | "expressive_arts__art" => ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__ART
      case "rme" => ScottishCurriculumPlanningArea.RME__STANDARD
      case "play" => ScottishCurriculumPlanningArea.NONE
      case "modern-languages" => ScottishCurriculumPlanningArea.LITERACY__MODERN_LANGUAGES
      case "science" => ScottishCurriculumPlanningArea.SCIENCE
      case "hand-writing" => ScottishCurriculumPlanningArea.NONE
      case "geography" => ScottishCurriculumPlanningArea.SOCIAL_STUDIES
      case "history" => ScottishCurriculumPlanningArea.SOCIAL_STUDIES
    }
  }

  implicit def convertTttUserIdStringToModel(tttUserId: String): TimeToTeachUserId =
  {
    TimeToTeachUserId(tttUserId)
  }

}