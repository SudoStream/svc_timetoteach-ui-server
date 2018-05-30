package duplicate.model.esandos

import upickle.default.{macroRW, ReadWriter => RW}

case class NotStartedEsAndOsByGroup(
                               completedEsAndOsByGroup: Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]
                             )


object NotStartedEsAndOsByGroup {
  implicit def rw: RW[CompletedEsAndOsByGroup] = macroRW
}

