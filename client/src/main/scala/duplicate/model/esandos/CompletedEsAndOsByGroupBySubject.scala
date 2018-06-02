package duplicate.model.esandos

import upickle.default.{macroRW, ReadWriter => RW}

case class CompletedEsAndOsByGroupBySubject(
                                             completedEsAndOsByGroupBySubject: Map[String, Map[String, Map[String, Map[String, EandOSetSubSection]]]]
                                           )

object CompletedEsAndOsByGroupBySubject {
  implicit def rw: RW[CompletedEsAndOsByGroupBySubject] = macroRW
}
