package duplicate.model

import upickle.default.{macroRW, ReadWriter => RW}

case class TermlyPlansToSave(
                              tttUserId: String,
                              eandOsWithBenchmarks: List[EandOsWithBenchmarks]
                            )

object TermlyPlansToSave {
  implicit def rw: RW[TermlyPlansToSave] = macroRW
}

case class EandOsWithBenchmarks(
                                 eAndOCodes: List[String],
                                 benchmarks: List[String]
                               )

object EandOsWithBenchmarks {
  implicit def rw: RW[EandOsWithBenchmarks] = macroRW
}
