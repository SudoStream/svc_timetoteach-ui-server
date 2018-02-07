package curriculum.scotland

import duplicate.model._
import duplicate.model.esandos.{EAndOSentence, _}
import io.sudostream.timetoteach.messages.scottish.{CurriculumArea => _, _}

trait EsOsAndBenchmarksBuilderHelper {

  val NO_SUBSECTION_NAME = "NO_SUBSECTION_NAME"

  implicit def convertScottishCurriculumLevel(scottishLevel: ScottishCurriculumLevel): CurriculumLevel = {
    scottishLevel match {
      case ScottishCurriculumLevel.EARLY => EarlyLevel
      case ScottishCurriculumLevel.FIRST => FirstLevel
      case ScottishCurriculumLevel.SECOND => SecondLevel
      case ScottishCurriculumLevel.THIRD => ThirdLevel
      case ScottishCurriculumLevel.FOURTH => FourthLevel
    }
  }

  implicit def convertScottishCurriculumAreaName(scottishAreaName: ScottishCurriculumAreaName): CurriculumArea = {
    scottishAreaName match {
      case ScottishCurriculumAreaName.EXPRESSIVE_ARTS => ExpressiveArts
      case ScottishCurriculumAreaName.HEALTH_AND_WELLBEING => HealthAndWellbeing
      case ScottishCurriculumAreaName.LANGUAGES => Languages
      case ScottishCurriculumAreaName.LITERACY => Literacy
      case ScottishCurriculumAreaName.MATHEMATICS => Mathematics
      case ScottishCurriculumAreaName.NUMERACY => Numeracy
      case ScottishCurriculumAreaName.RELIGION_AND_MORAL_EDUCATION => ReligionAndMoralEducation
      case ScottishCurriculumAreaName.SCIENCES => Sciences
      case ScottishCurriculumAreaName.SOCIAL_STUDIES => SocialStudies
      case ScottishCurriculumAreaName.TECHNOLOGIES => Technologies
    }
  }

  implicit def convertBulletPoints(bullets: List[String]): List[EAndOBulletPoint] = {
    for {
      bullet <- bullets
    } yield EAndOBulletPoint(bullet)
  }

  implicit def convertBenchmarks(benchmarks: List[String]): List[Benchmark] = {
    for {
      benchmark <- benchmarks
    } yield Benchmark(benchmark)
  }

  implicit def convertSentences(eAndOLines: List[ScottishExperienceAndOutcomeLine]): List[EAndOSentence] = {
    for {
      line <- eAndOLines
    } yield EAndOSentence(
      line.sentence,
      line.bulletPoints
    )
  }

  implicit def convertEAndO(allEAndOs: List[SingleScottishExperienceAndOutcome]): List[EandO] = {
    for {
      singleEAndO <- allEAndOs
    } yield EandO(
      singleEAndO.code,
      singleEAndO.eAndOLines
    )
  }

  def createNewEsAndOsPlusBenchmarksForSubjectAndLevel(esAndOsBySubSection: ScottishEsAndOsBySubSection): EsAndOsPlusBenchmarksForSubjectAndLevel = {
    EsAndOsPlusBenchmarksForSubjectAndLevel(
      esAndOsBySubSection.scottishCurriculumLevel,
      esAndOsBySubSection.curriculumAreaName,
      Map(esAndOsBySubSection.eAndOSetSectionName ->
        Map(esAndOsBySubSection.eAndOSetSubSectionName.getOrElse(NO_SUBSECTION_NAME) ->
          EandOSetSubSection(
            esAndOsBySubSection.eAndOSetSubSectionAuxiliaryText.getOrElse(""),
            esAndOsBySubSection.experienceAndOutcomes,
            esAndOsBySubSection.associatedBenchmarks
          )
        )
      )
    )
  }


}
