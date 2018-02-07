package duplicate.model.esandos

import duplicate.model.CurriculumLevel
import upickle.default.{macroRW, ReadWriter => RW}

case class EsAndOsPlusBenchmarksForSubjectAndLevel(
                                                    curriculumLevel: CurriculumLevel,
                                                    curriculumArea: CurriculumArea,
                                                    setSectionNameToSubSections: Map[String, Map[String, EandOSetSubSection]]
                                                  )

object EsAndOsPlusBenchmarksForSubjectAndLevel {
  implicit def rw: RW[EsAndOsPlusBenchmarksForSubjectAndLevel] = macroRW
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

case object ExpressiveArts extends CurriculumArea {
  val value = "EXPRESSIVE_ARTS"
}

case object HealthAndWellbeing extends CurriculumArea {
  val value = "HEALTH_AND_WELLBEING"
}

case object Languages extends CurriculumArea {
  val value = "LANGUAGES"
}

case object Mathematics extends CurriculumArea {
  val value = "MATHEMATICS"
}

case object ReligionAndMoralEducation extends CurriculumArea {
  val value = "RELIGION_AND_MORAL_EDUCATION"
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
