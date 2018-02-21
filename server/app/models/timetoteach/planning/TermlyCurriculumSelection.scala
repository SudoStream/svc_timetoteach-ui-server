package models.timetoteach.planning

import java.time.LocalDateTime

import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.term.SchoolTerm
import models.timetoteach.{ClassId, TimeToTeachUserId}
import play.api.Logger

case class TermlyCurriculumSelection(
                                      tttUserId: TimeToTeachUserId,
                                      classId: ClassId,
                                      planningAreas: List[ScottishCurriculumPlanningArea],
                                      createdTime: LocalDateTime,
                                      schoolTerm: SchoolTerm
                                    )
{
  def planningAreasWrapped: List[ScottishCurriculumPlanningAreaWrapper] =
  {
    planningAreas.map(area => ScottishCurriculumPlanningAreaWrapper(area))
  }
}

object TermlyCurriculumSelection
{
  val logger: Logger = Logger

  def convertPlanningAreasStringToModel(planningArea: String): ScottishCurriculumPlanningArea =
  {
    logger.debug(s"Converting '$planningArea' to ScottishCurriculumPlanningArea")
    planningArea.toUpperCase match {
      case "EXPRESSIVE_ARTS" => ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS
      case "EXPRESSIVE_ARTS__ART" => ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__ART
      case "EXPRESSIVE_ARTS__DRAMA" => ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__DRAMA
      case "EXPRESSIVE_ARTS__DANCE" => ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__DANCE
      case "EXPRESSIVE_ARTS__MUSIC" => ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__MUSIC
      case "HEALTH_AND_WELLBEING" => ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING
      case "HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION" => ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION
      case "LITERACY__WRITING" => ScottishCurriculumPlanningArea.LITERACY__WRITING
      case "LITERACY__READING" => ScottishCurriculumPlanningArea.LITERACY__READING
      case "LITERACY__CLASSICAL_LANGUAGES" => ScottishCurriculumPlanningArea.LITERACY__CLASSICAL_LANGUAGES
      case "LITERACY__GAELIC_LEARNERS" => ScottishCurriculumPlanningArea.LITERACY__GAELIC_LEARNERS
      case "LITERACY__LITERACY_AND_ENGLISH" => ScottishCurriculumPlanningArea.LITERACY__LITERACY_AND_ENGLISH
      case "LITERACY__LITERACY_AND_GAIDLIG" => ScottishCurriculumPlanningArea.LITERACY__LITERACY_AND_GAIDLIG
      case "LITERACY__MODERN_LANGUAGES" => ScottishCurriculumPlanningArea.LITERACY__MODERN_LANGUAGES
      case "MATHEMATICS" => ScottishCurriculumPlanningArea.MATHEMATICS
      case "RME__STANDARD" => ScottishCurriculumPlanningArea.RME__STANDARD
      case "RME__CATHOLIC" => ScottishCurriculumPlanningArea.RME__CATHOLIC
      case "SCIENCE" => ScottishCurriculumPlanningArea.SCIENCE
      case "SOCIAL_STUDIES" => ScottishCurriculumPlanningArea.SOCIAL_STUDIES
      case "TECHNOLOGIES" => ScottishCurriculumPlanningArea.TECHNOLOGIES
      case "TOPIC" => ScottishCurriculumPlanningArea.TOPIC
      case "NONE" => ScottishCurriculumPlanningArea.NONE
      case other =>
        logger.warn(s"Could not match the curriculum area '$planningArea' ... setting to NONE")
        ScottishCurriculumPlanningArea.NONE
    }
  }
}