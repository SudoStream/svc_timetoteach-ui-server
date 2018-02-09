package duplicate.model

import upickle.default.{macroRW, ReadWriter => RW}

case class TermlyPlansToSave(
                              schoolId: String,
                              tttUserId: String,
                              eAndOCodes: List[String],
                              benchmarks: List[String]
                            )

object TermlyPlansToSave {
  implicit def rw: RW[TermlyPlansToSave] = macroRW
}
