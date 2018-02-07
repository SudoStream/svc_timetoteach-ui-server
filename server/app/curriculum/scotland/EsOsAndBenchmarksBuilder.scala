package curriculum.scotland

import duplicate.model.CurriculumLevel
import duplicate.model.esandos.{CurriculumArea, EsAndOsPlusBenchmarksForSubjectAndLevel}

import scala.concurrent.Future

trait EsOsAndBenchmarksBuilder {

  def buildEsOsAndBenchmarks(curriculumLevel: CurriculumLevel, curriculumAreaName: CurriculumArea): Future[Option[EsAndOsPlusBenchmarksForSubjectAndLevel]]

}
