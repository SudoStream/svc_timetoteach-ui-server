package potentialmicroservice.planning.reader.dao

import java.time.format.DateTimeFormatter

import models.timetoteach.planning.TermlyCurriculumSelection
import org.mongodb.scala.Document
import play.api.Logger

trait PlanReaderTermlyCurriculumSelectionHelper
{
  private val logger: Logger = Logger
  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

  def findLatestVersionOfTermlyCurriculumSelection(foundTermlyPlanDocs: List[Document]): Option[TermlyCurriculumSelection] =
  {
    None
  }


}
