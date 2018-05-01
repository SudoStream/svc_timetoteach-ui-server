package duplicate.model.esandos

import duplicate.model.CurriculumLevel
import upickle.default.{macroRW, ReadWriter => RW}

case class EsAndOsPlusBenchmarksForCurriculumAreaAndLevel(
                                                    curriculumLevel: CurriculumLevel,
                                                    curriculumArea: CurriculumArea,
                                                    setSectionNameToSubSections: Map[String, Map[String, EandOSetSubSection]]
                                                  )

object EsAndOsPlusBenchmarksForCurriculumAreaAndLevel {
  implicit def rw: RW[EsAndOsPlusBenchmarksForCurriculumAreaAndLevel] = macroRW
}

case class EandOSetSubSection(
                               auxiliaryText: String,
                               eAndOs: List[EandO],
                               benchmarks: List[Benchmark]
                             )

object EandOSetSubSection {
  implicit def rw: RW[EandOSetSubSection] = macroRW
}

case class EandO(
                  code: String,
                  eAndOSentences: List[EAndOSentence]
                )

object EandO {
  implicit def rw: RW[EandO] = macroRW
}


case class EAndOSentence(
                          value: String,
                          bulletPoints: List[EAndOBulletPoint]
                        )

object EAndOSentence {
  implicit def rw: RW[EAndOSentence] = macroRW
}

case class EAndOBulletPoint(
                             value: String
                           )

object EAndOBulletPoint {
  implicit def rw: RW[EAndOBulletPoint] = macroRW
}

case class Benchmark(
                      value: String
                    )

object Benchmark {
  implicit def rw: RW[Benchmark] = macroRW
}

/////

sealed trait CurriculumArea {
  def value: String
}

object CurriculumArea {
  implicit def rw: RW[CurriculumArea] = macroRW

  def createCurriculumAreaFromString(areaAsString: String): CurriculumArea = {
    areaAsString match {
      case "EXPRESSIVE_ARTS" => ExpressiveArts__Only
      case "EXPRESSIVE_ARTS__ART" => ExpressiveArts__Art
      case "EXPRESSIVE_ARTS__DRAMA" => ExpressiveArts__Drama
      case "EXPRESSIVE_ARTS__DANCE" => ExpressiveArts__Dance
      case "EXPRESSIVE_ARTS__MUSIC" => ExpressiveArts__Music
      case "HEALTH_AND_WELLBEING" => HealthAndWellbeing
      case "HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION" => HealthAndWellbeing
      case "LITERACY__WRITING" => Literacy_Writing
      case "LITERACY__READING" => Literacy_Reading
      case "LITERACY__LISTENING_AND_TALKING" => Literacy_ListeningAndTalking
      case "LITERACY__CLASSICAL_LANGUAGES" => Languages_ClassicalLanguages
      case "LITERACY__GAELIC_LEARNERS" => Languages_Gaelic
      case "LITERACY__LITERACY_AND_GAIDLIG" => Languages_LiteracyAndGaidhlig
      case "LITERACY__MODERN_LANGUAGES" => Languages_ModernLanguages
      case "MATHEMATICS" => Mathematics
      case "RME__STANDARD" => ReligionAndMoralEducationStandard
      case "RME__CATHOLIC" => ReligionAndMoralEducationStandard
      case "SCIENCE" => Sciences
      case "SOCIAL_STUDIES" => SocialStudies
      case "TECHNOLOGIES" => Technologies
      case "TOPIC" => SocialStudies
      case "NONE" => NotDefined
    }
  }
}

case object ExpressiveArts__Only extends CurriculumArea {
  val value = "EXPRESSIVE_ARTS"
}

case object ExpressiveArts__Art extends CurriculumArea {
  val value = "EXPRESSIVE_ARTS__ART"
}

case object ExpressiveArts__Drama extends CurriculumArea {
  val value = "EXPRESSIVE_ARTS__DRAMA"
}

case object ExpressiveArts__Dance extends CurriculumArea {
  val value = "EXPRESSIVE_ARTS__DANCE"
}

case object ExpressiveArts__Music extends CurriculumArea {
  val value = "EXPRESSIVE_ARTS__MUSIC"
}

case object HealthAndWellbeing extends CurriculumArea {
  val value = "HEALTH_AND_WELLBEING"
}

case object Languages_ClassicalLanguages extends CurriculumArea {
  val value = "LANGUAGES_CLASSICAL_LANGUAGES"
}

case object Languages_Gaelic extends CurriculumArea {
  val value = "LANGUAGES_GAELIC"
}

case object Literacy_Writing extends CurriculumArea {
  val value = "LITERACY__WRITING"
}

case object Literacy_Reading extends CurriculumArea {
  val value = "LITERACY__READING"
}

case object Literacy_ListeningAndTalking extends CurriculumArea {
  val value = "LITERACY__LISTENING_AND_TALKING"
}

case object Languages_LiteracyAndGaidhlig extends CurriculumArea {
  val value = "LANGUAGES_LITERACY_AND_GAIDHLIG"
}

case object Languages_ModernLanguages extends CurriculumArea {
  val value = "LANGUAGES_MODERN_LANGUAGES"
}

case object Mathematics extends CurriculumArea {
  val value = "MATHEMATICS"
}

case object ReligionAndMoralEducationStandard extends CurriculumArea {
  val value = "RELIGION_AND_MORAL_EDUCATION_STANDARD"
}

case object ReligionAndMoralEducationCatholic extends CurriculumArea {
  val value = "RELIGION_AND_MORAL_EDUCATION_CATHOLIC"
}


case object Sciences extends CurriculumArea {
  val value = "SCIENCES"
}

case object SocialStudies extends CurriculumArea {
  val value = "SOCIAL_STUDIES"
}

case object Technologies extends CurriculumArea {
  val value = "TECHNOLOGIES"
}

case object Literacy extends CurriculumArea {
  val value = "LITERACY"
}

case object Numeracy extends CurriculumArea {
  val value = "NUMERACY"
}

case object NotDefined extends CurriculumArea {
  val value = "NOT_DEFINED"
}
