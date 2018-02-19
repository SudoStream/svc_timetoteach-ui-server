package curriculum.scotland

import duplicate.model.CurriculumLevel
import duplicate.model.esandos.{CurriculumArea, EandO, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel}

import scala.concurrent.Future

trait EsOsAndBenchmarksBuilder {

  def buildEsOsAndBenchmarks(curriculumAreaName: CurriculumArea): Future[Option[List[EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]]]

  def buildEsOsAndBenchmarks(curriculumLevel: CurriculumLevel, curriculumAreaName: CurriculumArea): Future[Option[EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]]

  def esAndOsCodeToEsAndOsDetailMap() : Future[Map[String, EandO]]

}
