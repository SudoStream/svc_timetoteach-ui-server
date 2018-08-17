package controllers.planning.termly

import java.time.LocalDateTime

import javax.inject.{Inject, Singleton}
import controllers.serviceproxies.TermServiceProxy
import duplicate.model.TermlyPlansToSave
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import io.sudostream.timetoteach.messages.systemwide.model.LocalAuthority
import models.timetoteach.planning.{CurriculumAreaTermlyPlan, GroupId, PlanType}
import models.timetoteach.{ClassId, TimeToTeachUserId}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TermPlansHelper @Inject()(termService: TermServiceProxy) {

  import TermPlansHelper._

  def convertTermlyPlanToModel(
                                classId: String,
                                termlyPlansToSave: TermlyPlansToSave,
                                maybeGroupId: Option[GroupId],
                                curriculumArea: String,
                                localAuthority: LocalAuthority
                              ): Future[CurriculumAreaTermlyPlan] = {
    val futureMaybeThisTerm = termService.currentSchoolTerm(localAuthority)

    val planType = if (maybeGroupId.isDefined) {
      PlanType.GROUP_LEVEL_PLAN
    } else {
      PlanType.CLASS_LEVEL_PLAN
    }

    for {
      maybeThisTerm <- futureMaybeThisTerm
      if maybeThisTerm.isDefined
    } yield CurriculumAreaTermlyPlan(
      termlyPlansToSave.tttUserId,
      planType,
      maybeThisTerm.get,
      classId,
      maybeGroupId,
      curriculumArea,
      LocalDateTime.now(),
      termlyPlansToSave.eandOsWithBenchmarks
    )
  }

}

object TermPlansHelper {

  implicit def schoolIdStringToSchoolId(schoolId: String): ClassId = {
    ClassId(schoolId)
  }

  implicit def convertSubjectStringToSubjectName(subjectName: String): ScottishCurriculumPlanningArea = {
    subjectName.toLowerCase match {
      case "empty" => ScottishCurriculumPlanningArea.NONE
      case "golden-time" | "golden_time" => ScottishCurriculumPlanningArea.NONE
      case "other" => ScottishCurriculumPlanningArea.NONE
      case "ict" | "technologies" => ScottishCurriculumPlanningArea.TECHNOLOGIES
      case "music" | "expressive_arts__music" => ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__MUSIC
      case "drama" | "expressive_arts__drama" => ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__DRAMA
      case "health" | "health_and_wellbeing" => ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING
      case "teacher-covertime" | "teacher_covertime" => ScottishCurriculumPlanningArea.NONE
      case "assembly" => ScottishCurriculumPlanningArea.NONE
      case "reading" | "literacy__reading" => ScottishCurriculumPlanningArea.LITERACY__READING
      case "spelling" => ScottishCurriculumPlanningArea.NONE
      case "writing" | "literacy__writing" => ScottishCurriculumPlanningArea.LITERACY__WRITING
      case "maths" | "mathematics" => ScottishCurriculumPlanningArea.MATHEMATICS
      case "topic" => ScottishCurriculumPlanningArea.TOPIC
      case "physical-education" | "physical_education" | "health_and_wellbeing__physical_education" => ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION
      case "soft-start" => ScottishCurriculumPlanningArea.NONE
      case "numeracy" => ScottishCurriculumPlanningArea.MATHEMATICS
      case "art" | "expressive_arts__art" => ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__ART
      case "rme" | "rme__standard" => ScottishCurriculumPlanningArea.RME__STANDARD
      case "rme__catholic" => ScottishCurriculumPlanningArea.RME__CATHOLIC
      case "play" => ScottishCurriculumPlanningArea.NONE
      case "modern-languages" | "modern_languages" => ScottishCurriculumPlanningArea.LITERACY__MODERN_LANGUAGES
      case "science" => ScottishCurriculumPlanningArea.SCIENCE
      case "hand-writing" | "hand_writing" => ScottishCurriculumPlanningArea.NONE
      case "geography" => ScottishCurriculumPlanningArea.SOCIAL_STUDIES
      case "history" => ScottishCurriculumPlanningArea.SOCIAL_STUDIES
      case "social_studies" => ScottishCurriculumPlanningArea.SOCIAL_STUDIES
      case "literacy__gaelic_learners" => ScottishCurriculumPlanningArea.LITERACY__GAELIC_LEARNERS
    }
  }

  implicit def convertTttUserIdStringToModel(tttUserId: String): TimeToTeachUserId = {
    TimeToTeachUserId(tttUserId)
  }

}