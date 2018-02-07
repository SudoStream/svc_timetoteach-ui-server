package curriculum.scotland

import javax.inject.{Inject, Singleton}

import controllers.serviceproxies.EsAndOsReaderServiceProxyImpl
import duplicate.model._
import duplicate.model.esandos.{CurriculumArea, EsAndOsPlusBenchmarksForSubjectAndLevel, _}
import io.sudostream.timetoteach.messages.scottish._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class EsOsAndBenchmarksBuilderImpl @Inject()(esAndOsReader: EsAndOsReaderServiceProxyImpl)
  extends EsOsAndBenchmarksBuilder {

  import EsOsAndBenchmarksBuilderImpl.buildTheEsOsAndBenchmarks

  lazy private val allEsOsAndBenchmarks: Future[Option[Map[CurriculumLevel, Map[CurriculumArea, EsAndOsPlusBenchmarksForSubjectAndLevel]]]] = buildAllEsOsAndBenchmarks()

  private def buildAllEsOsAndBenchmarks():
  Future[Option[Map[CurriculumLevel, Map[CurriculumArea, EsAndOsPlusBenchmarksForSubjectAndLevel]]]] = {

    val eventualMaybeAllEsAndOs = esAndOsReader.readAllEsAndos()

    for {
      maybeAllEsAndOs <- eventualMaybeAllEsAndOs
      if maybeAllEsAndOs.isDefined
      allEsAndOs = maybeAllEsAndOs.get
    } yield buildTheEsOsAndBenchmarks(allEsAndOs)
  }

  override def buildEsOsAndBenchmarks(curriculumLevel: CurriculumLevel,
                                      curriculumAreaName: CurriculumArea):
  Future[Option[EsAndOsPlusBenchmarksForSubjectAndLevel]] = {
    Future {
      None
    }
  }

}

object EsOsAndBenchmarksBuilderImpl extends EsOsAndBenchmarksBuilderHelper {

  private[scotland] def buildTheEsOsAndBenchmarks(scottishEsAndOsData: ScottishEsAndOsData):
  Option[Map[CurriculumLevel, Map[CurriculumArea, EsAndOsPlusBenchmarksForSubjectAndLevel]]] = {

    def buildTheEsOsAndBenchmarksLoop(
                                       nextEsAndOsBySubSection: ScottishEsAndOsBySubSection,
                                       restScottishEsAndOsBySubSection: List[ScottishEsAndOsBySubSection],
                                       currentMap: Map[CurriculumLevel, Map[CurriculumArea, EsAndOsPlusBenchmarksForSubjectAndLevel]]
                                     ): Option[Map[CurriculumLevel, Map[CurriculumArea, EsAndOsPlusBenchmarksForSubjectAndLevel]]] = {

      val maybeCurrentMapForCurriculumLevel = currentMap.get(nextEsAndOsBySubSection.scottishCurriculumLevel)
      val newMapForCurriculumLevel: Map[CurriculumArea, EsAndOsPlusBenchmarksForSubjectAndLevel] = maybeCurrentMapForCurriculumLevel match {
        case Some(currentMapForCurriculumLevel) => currentMapForCurriculumLevel // TODO
        case None =>
          Map(convertScottishCurriculumAreaName(nextEsAndOsBySubSection.curriculumAreaName) -> createNewEsAndOsPlusBenchmarksForSubjectAndLevel(nextEsAndOsBySubSection))
      }
      val newMap = currentMap + (convertScottishCurriculumLevel(nextEsAndOsBySubSection.scottishCurriculumLevel) -> newMapForCurriculumLevel)

      if (restScottishEsAndOsBySubSection.isEmpty) {
        Some(newMap)
      } else {
        buildTheEsOsAndBenchmarksLoop(restScottishEsAndOsBySubSection.head, restScottishEsAndOsBySubSection.tail, newMap)
      }
    }

    if (scottishEsAndOsData.allExperiencesAndOutcomes.isEmpty) None
    else {
      buildTheEsOsAndBenchmarksLoop(
        scottishEsAndOsData.allExperiencesAndOutcomes.head,
        scottishEsAndOsData.allExperiencesAndOutcomes.tail,
        Map()
      )
    }
  }


  private def convertToBulletPoints(bulletPoints: List[String]): List[EAndOBulletPoint] = {
    for {
      bulletPoint <- bulletPoints
    } yield EAndOBulletPoint(bulletPoint)
  }

  private def convertToCurriculumLevel(level: ScottishCurriculumLevel): CurriculumLevel = {
    level match {
      case ScottishCurriculumLevel.EARLY => EarlyLevel
      case ScottishCurriculumLevel.FIRST => FirstLevel
      case ScottishCurriculumLevel.SECOND => SecondLevel
      case ScottishCurriculumLevel.THIRD => ThirdLevel
      case ScottishCurriculumLevel.FOURTH => FourthLevel
    }
  }
}