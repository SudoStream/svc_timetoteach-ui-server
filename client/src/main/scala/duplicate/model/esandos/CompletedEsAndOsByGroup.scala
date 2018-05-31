package duplicate.model.esandos

import upickle.default.{macroRW, ReadWriter => RW}

case class CompletedEsAndOsByGroup(
                                    completedEsAndOsByGroup: Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]
                                  )

object CompletedEsAndOsByGroup {
  implicit def rw: RW[CompletedEsAndOsByGroup] = macroRW
}
