package utils

import duplicate.model.esandos._
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning.ScottishCurriculumPlanningAreaWrapper
import play.api.Logger

object CurriculumConverterUtil
{
  private val logger: Logger = Logger

  implicit def convertCurriculumAreaToModel(curriculumArea: String): CurriculumArea =
  {
    curriculumArea.toUpperCase match {
      case "MATHS" | "MATHEMATICS" => Mathematics
      case "EXPRESSIVE_ARTS" => ExpressiveArts__Only
      case "EXPRESSIVE_ARTS__ART" => ExpressiveArts__Art
      case "EXPRESSIVE_ARTS__DRAMA" => ExpressiveArts__Drama
      case "EXPRESSIVE_ARTS__DANCE" => ExpressiveArts__Dance
      case "EXPRESSIVE_ARTS__MUSIC" => ExpressiveArts__Music
      case "HEALTH_AND_WELLBEING" | "HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION" => HealthAndWellbeing
      case "WRITING" | "LITERACY__WRITING" => Literacy_Writing
      case "READING" | "LITERACY__READING" => Literacy_Reading
      case "LITERACY__LISTENING_AND_TALKING" => Literacy_ListeningAndTalking
      case "LITERACY__CLASSICAL_LANGUAGES" => Languages_ClassicalLanguages
      case "LITERACY__GAELIC_LEARNERS" => Languages_Gaelic
      case "LITERACY__LITERACY_AND_GAIDLIG" => Languages_LiteracyAndGaidhlig
      case "LITERACY__MODERN_LANGUAGES" => Languages_ModernLanguages
      case "RME__STANDARD" => ReligionAndMoralEducationStandard
      case "RME__CATHOLIC" => ReligionAndMoralEducationCatholic
      case "SCIENCE" => Sciences
      case "SOCIAL_STUDIES" | "TOPIC" => SocialStudies
      case "TECHNOLOGIES" => Technologies
      case "TOPIC" => SocialStudies
      case somethingElse =>
        logger.error(s"While convertSubjectToCurriculumArea did not recognise $somethingElse to convert to curriculum area")
        NotDefined
    }
  }

  implicit def convertSubjectToScottishCurriculumPlanningAreaWrapper(curriculumArea: String): ScottishCurriculumPlanningAreaWrapper =
  {
    val theScottishCurriculumPlanningArea: ScottishCurriculumPlanningArea = curriculumArea.toUpperCase match {
      case "MATHS" | "MATHEMATICS" => ScottishCurriculumPlanningArea.MATHEMATICS
      case "EXPRESSIVE_ARTS" => ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS
      case "EXPRESSIVE_ARTS__ART" => ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__ART
      case "EXPRESSIVE_ARTS__DRAMA" => ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__DRAMA
      case "EXPRESSIVE_ARTS__DANCE" => ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__DANCE
      case "EXPRESSIVE_ARTS__MUSIC" => ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__MUSIC
      case "HEALTH_AND_WELLBEING" => ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING
      case "HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION" => ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING
      case "WRITING" | "LITERACY__WRITING" => ScottishCurriculumPlanningArea.LITERACY__WRITING
      case "READING" | "LITERACY__READING" => ScottishCurriculumPlanningArea.LITERACY__READING
      case "LITERACY__LISTENING_AND_TALKING" => ScottishCurriculumPlanningArea.LITERACY__LISTENING_AND_TALKING
      case "LITERACY__LITERACY_AND_ENGLISH" => ScottishCurriculumPlanningArea.LITERACY__WRITING
      case "LITERACY__CLASSICAL_LANGUAGES" => ScottishCurriculumPlanningArea.LITERACY__CLASSICAL_LANGUAGES
      case "LITERACY__GAELIC_LEARNERS" => ScottishCurriculumPlanningArea.LITERACY__GAELIC_LEARNERS
      case "LITERACY__LITERACY_AND_GAIDLIG" => ScottishCurriculumPlanningArea.LITERACY__LITERACY_AND_GAIDLIG
      case "LITERACY__MODERN_LANGUAGES" => ScottishCurriculumPlanningArea.LITERACY__MODERN_LANGUAGES
      case "RME__STANDARD" => ScottishCurriculumPlanningArea.RME__STANDARD
      case "RME__CATHOLIC" => ScottishCurriculumPlanningArea.RME__CATHOLIC
      case "SCIENCE" => ScottishCurriculumPlanningArea.SCIENCE
      case "SOCIAL_STUDIES" | "TOPIC" => ScottishCurriculumPlanningArea.SOCIAL_STUDIES
      case "TECHNOLOGIES" => ScottishCurriculumPlanningArea.TECHNOLOGIES
      case "TOPIC" => ScottishCurriculumPlanningArea.SOCIAL_STUDIES
      case somethingElse =>
        logger.error(s"While convertSubjectToCurriculumArea did not recognise $somethingElse to convert to curriculum area")
        ScottishCurriculumPlanningArea.NONE
    }

    ScottishCurriculumPlanningAreaWrapper(theScottishCurriculumPlanningArea)
  }

}
