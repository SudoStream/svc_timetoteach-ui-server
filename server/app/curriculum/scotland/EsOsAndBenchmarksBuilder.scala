package curriculum.scotland

import duplicate.model.CurriculumLevel
import duplicate.model.esandos.{CurriculumArea, EandO, EsAndOsPlusBenchmarksForSubjectAndLevel}

import scala.concurrent.Future

trait EsOsAndBenchmarksBuilder {

  def buildEsOsAndBenchmarks(curriculumLevel: CurriculumLevel, curriculumAreaName: CurriculumArea): Future[Option[EsAndOsPlusBenchmarksForSubjectAndLevel]]

  def esAndOsCodeToEsAndOsDetailMap() : Future[Map[String, EandO]]

}
