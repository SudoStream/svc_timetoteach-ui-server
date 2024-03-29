package curriculum.scotland

import duplicate.model._
import duplicate.model.esandos.{EAndOSentence, _}
import io.sudostream.timetoteach.messages.scottish.{CurriculumArea => _, _}

trait EsOsAndBenchmarksBuilderHelper
{

  val NO_SUBSECTION_NAME = "NO_SUBSECTION_NAME"

  implicit def convertScottishCurriculumLevel(scottishLevel: ScottishCurriculumLevel): CurriculumLevel =
  {
    scottishLevel match {
      case ScottishCurriculumLevel.EARLY => EarlyLevel
      case ScottishCurriculumLevel.FIRST => FirstLevel
      case ScottishCurriculumLevel.SECOND => SecondLevel
      case ScottishCurriculumLevel.THIRD => ThirdLevel
      case ScottishCurriculumLevel.FOURTH => FourthLevel
    }
  }

  implicit def convertScottishCurriculumAreaName(scottishAreaName: ScottishCurriculumPlanningArea): CurriculumArea =
  {
    scottishAreaName match {
      case ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS => ExpressiveArts__Only
      case ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__ART => ExpressiveArts__Art
      case ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__DRAMA => ExpressiveArts__Drama
      case ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__DANCE => ExpressiveArts__Dance
      case ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__MUSIC => ExpressiveArts__Music
      case ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING => HealthAndWellbeing
      case ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION => HealthAndWellbeing
      case ScottishCurriculumPlanningArea.LITERACY__WRITING => Literacy_Writing
      case ScottishCurriculumPlanningArea.LITERACY__READING => Literacy_Reading
      case ScottishCurriculumPlanningArea.LITERACY__LISTENING_AND_TALKING => Literacy_ListeningAndTalking
      case ScottishCurriculumPlanningArea.LITERACY__CLASSICAL_LANGUAGES => Languages_ClassicalLanguages
      case ScottishCurriculumPlanningArea.LITERACY__GAELIC_LEARNERS => Languages_Gaelic
      case ScottishCurriculumPlanningArea.LITERACY__LITERACY_AND_GAIDLIG => Languages_LiteracyAndGaidhlig
      case ScottishCurriculumPlanningArea.LITERACY__MODERN_LANGUAGES => Languages_ModernLanguages
      case ScottishCurriculumPlanningArea.MATHEMATICS => Mathematics
      case ScottishCurriculumPlanningArea.RME__STANDARD => ReligionAndMoralEducationStandard
      case ScottishCurriculumPlanningArea.RME__CATHOLIC => ReligionAndMoralEducationCatholic
      case ScottishCurriculumPlanningArea.SCIENCE => Sciences
      case ScottishCurriculumPlanningArea.SOCIAL_STUDIES => SocialStudies
      case ScottishCurriculumPlanningArea.TECHNOLOGIES => Technologies
      case ScottishCurriculumPlanningArea.TOPIC => SocialStudies
      case ScottishCurriculumPlanningArea.NONE => NotDefined

    }
  }

  implicit def convertBulletPoints(bullets: List[String]): List[EAndOBulletPoint] =
  {
    for {
      bullet <- bullets
    } yield EAndOBulletPoint(bullet)
  }

  implicit def convertBenchmarks(benchmarks: List[String]): List[Benchmark] =
  {
    for {
      benchmark <- benchmarks
    } yield Benchmark(benchmark)
  }

  implicit def convertSentences(eAndOLines: List[ScottishExperienceAndOutcomeLine]): List[EAndOSentence] =
  {
    for {
      line <- eAndOLines
    } yield EAndOSentence(
      line.sentence,
      line.bulletPoints
    )
  }

  implicit def convertEAndO(allEAndOs: List[SingleScottishExperienceAndOutcome]): List[EandO] =
  {
    for {
      singleEAndO <- allEAndOs
    } yield EandO(
      singleEAndO.code,
      singleEAndO.eAndOLines
    )
  }

  def createNewEsAndOsPlusBenchmarksForSubjectAndLevel(esAndOsBySubSection: ScottishEsAndOsBySubSection): EsAndOsPlusBenchmarksForCurriculumAreaAndLevel =
  {
    EsAndOsPlusBenchmarksForCurriculumAreaAndLevel(
      esAndOsBySubSection.scottishCurriculumLevel,
      esAndOsBySubSection.curriculumAreaName,
      Map(esAndOsBySubSection.eAndOSetSectionName ->
        Map(esAndOsBySubSection.eAndOSetSubSectionName.getOrElse(NO_SUBSECTION_NAME) ->
          EandOSetSubSection(
            esAndOsBySubSection.eAndOSetSubSectionAuxiliaryText.getOrElse(""),
            esAndOsBySubSection.allExperienceAndOutcomesAtTheSubSectionLevel,
            esAndOsBySubSection.associatedBenchmarks
          )
        )
      )
    )
  }


}
