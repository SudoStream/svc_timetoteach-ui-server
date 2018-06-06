package duplicate.model.esandos

import upickle.default.{macroRW, ReadWriter => RW}

case class StartedEsAndOsByGroupBySubject(
                                           startedEsAndOsByGroupBySubject: Map[String, Map[String, Map[String, Map[String, EandOSetSubSection]]]]
                                         )

object StartedEsAndOsByGroupBySubject {
  implicit def rw: RW[StartedEsAndOsByGroupBySubject] = macroRW
}

