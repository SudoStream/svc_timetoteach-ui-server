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

case object Languages_LiteracyAndEnglish extends CurriculumArea {
  val value = "LANGUAGES_LITERACY_AND_ENGLISH"
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
