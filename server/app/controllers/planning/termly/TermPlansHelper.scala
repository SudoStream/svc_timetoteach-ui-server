package controllers.planning.termly

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}

import controllers.serviceproxies.TermServiceProxy
import duplicate.model.TermlyPlansToSave
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.subjectdetail.SubjectName
import models.timetoteach.{SchoolId, TimeToTeachUserId}
import models.timetoteach.planning.{GroupId, PlanType, SubjectTermlyPlan}

@Singleton
class TermPlansHelper @Inject()(termService: TermServiceProxy) {

  import TermPlansHelper._

  def convertTermlyPlanToModel(
                                termlyPlansToSave: TermlyPlansToSave,
                                maybeGroupId: Option[GroupId],
                                subjectName: String
                              ): SubjectTermlyPlan = {
    val thisTerm = termService.currentSchoolTerm()
    val planType = if (maybeGroupId.isDefined) {
      PlanType.GROUP_LEVEL_PLAN
    } else {
      PlanType.CLASS_LEVEL_PLAN
    }

    SubjectTermlyPlan(
      termlyPlansToSave.tttUserId,
      planType,
      thisTerm,
      termlyPlansToSave.schoolId,
      maybeGroupId,
      subjectName,
      LocalDateTime.now(),
      termlyPlansToSave.eAndOCodes,
      termlyPlansToSave.benchmarks
    )
  }

}

object TermPlansHelper {

  implicit def schoolIdStringToSchoolId(schoolId: String) : SchoolId = {
    SchoolId(schoolId)
  }

  implicit def convertSubjectStringToSubjectName(subjectName: String): SubjectName = {
    subjectName.toLowerCase match {
      case "empty" => SubjectName.EMPTY
      case "golden-time" => SubjectName.GOLDEN_TIME
      case "other" => SubjectName.OTHER
      case "ict" => SubjectName.ICT
      case "music" => SubjectName.MUSIC
      case "drama" => SubjectName.DRAMA
      case "health" => SubjectName.HEALTH
      case "teacher-covertime" => SubjectName.TEACHER_COVERTIME
      case "assembly" => SubjectName.ASSEMBLY
      case "reading" => SubjectName.READING
      case "spelling" => SubjectName.SPELLING
      case "writing" => SubjectName.WRITING
      case "maths" | "mathematics" => SubjectName.MATHS
      case "topic" => SubjectName.TOPIC
      case "physical-education" => SubjectName.PHYSICAL_EDUCATION
      case "soft-start" => SubjectName.SOFT_START
      case "numeracy" => SubjectName.NUMERACY
      case "art" => SubjectName.ART
      case "rme" => SubjectName.RME
      case "play" => SubjectName.PLAY
      case "modern-languages" => SubjectName.MODERN_LANGUAGES
      case "science" => SubjectName.SCIENCE
      case "hand-writing" => SubjectName.HAND_WRITING
      case "geography" => SubjectName.GEOGRAPHY
      case "history" => SubjectName.HISTORY
    }
  }

  implicit def convertTttUserIdStringToModel(tttUserId: String): TimeToTeachUserId = {
    TimeToTeachUserId(tttUserId)
  }

}