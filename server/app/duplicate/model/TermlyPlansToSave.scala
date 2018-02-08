package duplicate.model

import upickle.default.{macroRW, ReadWriter => RW}

case class TermlyPlansToSave(
                              eAndOCodes: List[String],
                              benchmarks: List[String]
                            )

object TermlyPlansToSave{
  implicit def rw: RW[TermlyPlansToSave] = macroRW
}
