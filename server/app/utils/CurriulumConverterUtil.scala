package utils

import duplicate.model.esandos.{CurriculumArea, Mathematics, NotDefined}
import play.api.Logger

object CurriulumConverterUtil {
  val logger: Logger = Logger

  implicit def convertSubjectToCurriculumArea(subject: String): CurriculumArea = {
    subject match {
      case "Maths" | "Mathematics" => Mathematics
      case somethingElse =>
        logger.error(s"Did not recognise ${somethingElse} to convert to curriculum area")
        NotDefined
    }
  }
}
