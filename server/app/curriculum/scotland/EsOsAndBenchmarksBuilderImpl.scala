package curriculum.scotland

import javax.inject.{Inject, Singleton}

import controllers.serviceproxies.EsAndOsReaderServiceProxyImpl
import duplicate.model._
import duplicate.model.esandos.{CurriculumArea, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel, _}
import io.sudostream.timetoteach.messages.scottish._
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class EsOsAndBenchmarksBuilderImpl @Inject()(esAndOsReader: EsAndOsReaderServiceProxyImpl)
  extends EsOsAndBenchmarksBuilder
{
  val logger: Logger = Logger

  import EsOsAndBenchmarksBuilderImpl.buildTheEsOsAndBenchmarks

  lazy private val allEsOsAndBenchmarks: Future[Option[Map[CurriculumLevel, Map[CurriculumArea, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]]]] = buildAllEsOsAndBenchmarks()

  private def buildAllEsOsAndBenchmarks():
  Future[Option[Map[CurriculumLevel, Map[CurriculumArea, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]]]] =
  {

    val eventualMaybeAllEsAndOs = esAndOsReader.readAllEsAndos()

    for {
      maybeAllEsAndOs <- eventualMaybeAllEsAndOs
      if maybeAllEsAndOs.isDefined
      allEsAndOs = maybeAllEsAndOs.get
    } yield buildTheEsOsAndBenchmarks(allEsAndOs)
  }

  override def buildEsOsAndBenchmarks(curriculumLevel: CurriculumLevel,
                                      curriculumAreaName: CurriculumArea):
  Future[Option[EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]] =
  {
    logger.info(s"Building Es,Os and benchmarks for $curriculumLevel|$curriculumAreaName")
    logger.info(s"allEsOsAndBenchmarks ${allEsOsAndBenchmarks.toString}")
    allEsOsAndBenchmarks map {
      case Some(allEsAndOs) =>
        logger.debug(s"allEsAndOs : ${allEsAndOs.keySet.toList.sortBy(elem => elem.order).toString()}")
        allEsAndOs.get(curriculumLevel) match {
          case Some(curriculumLevelMap) =>
            logger.debug(s"curriculumLevelMap : ${curriculumLevelMap.keySet.toString()} for level ${curriculumLevel.toString}")
            curriculumLevelMap.get(curriculumAreaName)
          case None => None
        }
      case None => None
    }
  }

  private def filterPerformanceEsAndOsForDramaAndMusic(
                                                        curriculumArea_to_EoBenchmark_Tuple: (CurriculumArea,
                                                          EsAndOsPlusBenchmarksForCurriculumAreaAndLevel),
                                                        curriculumAreaName: CurriculumArea)
  : Boolean =
  {
    if (curriculumArea_to_EoBenchmark_Tuple._1 == curriculumAreaName) {
      true
    } else {
      if (curriculumAreaName == ExpressiveArts__Drama || curriculumAreaName == ExpressiveArts__Music) {
        if (curriculumArea_to_EoBenchmark_Tuple._1 == ExpressiveArts__Only) {
          true
        } else {
          false
        }
      } else {
        false
      }
    }
  }

  private def filterListeningAndTalkingEsAndOs(
                                                curriculumArea_to_EoBenchmark_Tuple: (CurriculumArea,
                                                  EsAndOsPlusBenchmarksForCurriculumAreaAndLevel),
                                                curriculumAreaName: CurriculumArea)
  : Boolean =
  {
    if (curriculumArea_to_EoBenchmark_Tuple._1 == curriculumAreaName) {
      true
    } else {
      if (curriculumAreaName == Literacy_Reading || curriculumAreaName == Literacy_Writing ) {
        if (curriculumArea_to_EoBenchmark_Tuple._1 == Literacy_ListeningAndTalking ) {
          true
        } else {
          false
        }
      } else {
        false
      }
    }
  }


  override def buildEsOsAndBenchmarks(curriculumAreaName: CurriculumArea): Future[Option[List[EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]]] =
  {
    logger.info(s"Building Es,Os and benchmarks for $curriculumAreaName")

    allEsOsAndBenchmarks map {
      case Some(allEsAndOs: Map[CurriculumLevel, Map[CurriculumArea, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]]) =>
        Some({
          for {
            (theCurriculumLevel, theCurriculumAreaToEsAndOs) <- allEsAndOs
            filteredCurriculumAreaToEsAndOs = theCurriculumAreaToEsAndOs.filter {
              curriculumArea_to_EoBenchmark_Tuple =>
                filterPerformanceEsAndOsForDramaAndMusic(curriculumArea_to_EoBenchmark_Tuple, curriculumAreaName)
            }
            filteredEsOsAndBenchmarks <- filteredCurriculumAreaToEsAndOs.values
          } yield filteredEsOsAndBenchmarks
        }.toList
        )
      case None => None
    }
  }

  override def esAndOsCodeToEsAndOsDetailMap(): Future[Map[String, EandO]] =
  {
    var counter = 0
    val mutableEAndOCodeTodetailMap = scala.collection.mutable.Map[String, EandO]()
    logger.info(s"++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ Building Es,Os Code to Detail map")
    allEsOsAndBenchmarks map {
      case Some(allEsAndOs: Map[CurriculumLevel, Map[CurriculumArea, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]]) =>
        for (curriculumAreaToEsAndOs <- allEsAndOs.values) {
          for (esAndOsForSubjectLevel <- curriculumAreaToEsAndOs) {
            val esAndOsPlusBenchmark = esAndOsForSubjectLevel._2
            for (setSectionToEsAndOsMap <- esAndOsPlusBenchmark.setSectionNameToSubSections.values) {
              for (esAndOsSetSubSection <- setSectionToEsAndOsMap.values) {
                for (eAndO <- esAndOsSetSubSection.eAndOs) {
                  counter = counter + 1
//                  logger.debug(s"Building Es,Os Code to Detail map for ${eAndO.code} : counter = $counter")
                  mutableEAndOCodeTodetailMap += (eAndO.code -> eAndO)
                }
              }
            }
          }
        }
//        logger.debug(s"Returning map : ${mutableEAndOCodeTodetailMap.toString()}")
        mutableEAndOCodeTodetailMap.toMap
      case None => Map()
    }
  }

}

object EsOsAndBenchmarksBuilderImpl extends EsOsAndBenchmarksBuilderHelper
{
  val logger: Logger = Logger

  private[scotland] def buildTheEsOsAndBenchmarks(scottishEsAndOsData: ScottishEsAndOsData):
  Option[Map[CurriculumLevel, Map[CurriculumArea, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]]] =
  {

    def buildTheEsOsAndBenchmarksLoop(
                                       nextEsAndOsBySubSection: ScottishEsAndOsBySubSection,
                                       restScottishEsAndOsBySubSection: List[ScottishEsAndOsBySubSection],
                                       currentMap: Map[CurriculumLevel, Map[CurriculumArea, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]]
                                     ): Option[Map[CurriculumLevel, Map[CurriculumArea, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]]] =
    {

      val maybeCurrentMapForCurriculumLevel = currentMap.get(nextEsAndOsBySubSection.scottishCurriculumLevel)
      val newMapForCurriculumLevel = createNewVersionOfMap(nextEsAndOsBySubSection, maybeCurrentMapForCurriculumLevel)
      val newMap = currentMap + (convertScottishCurriculumLevel(nextEsAndOsBySubSection.scottishCurriculumLevel) -> newMapForCurriculumLevel)
      loop(buildTheEsOsAndBenchmarksLoop, restScottishEsAndOsBySubSection, newMap)
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

  private def loop(buildTheEsOsAndBenchmarksLoop: (ScottishEsAndOsBySubSection, List[ScottishEsAndOsBySubSection], Map[CurriculumLevel, Map[CurriculumArea, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]]) => Option[Map[CurriculumLevel, Map[CurriculumArea, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]]],
                   restScottishEsAndOsBySubSection: List[ScottishEsAndOsBySubSection],
                   newMap: Map[CurriculumLevel, Map[CurriculumArea, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]]) =
  {
    if (restScottishEsAndOsBySubSection.isEmpty) {
      Some(newMap)
    } else {
      buildTheEsOsAndBenchmarksLoop(restScottishEsAndOsBySubSection.head, restScottishEsAndOsBySubSection.tail, newMap)
    }
  }


  private def createNewVersionOfMap(nextEsAndOsBySubSection: ScottishEsAndOsBySubSection,
                                    maybeCurrentMapForCurriculumLevel: Option[Map[CurriculumArea, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]])
  : Map[CurriculumArea, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel] =
  {

    logger.debug(s"Wanting to add ... ${nextEsAndOsBySubSection.curriculumAreaName} ")
    val newMapForCurriculumLevel: Map[CurriculumArea, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel] = maybeCurrentMapForCurriculumLevel match {
      case Some(currentMapForCurriculumLevel: Map[CurriculumArea, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]) =>

        val maybeCurrentEsAndOsPlusBenchmarks = currentMapForCurriculumLevel.get(nextEsAndOsBySubSection.curriculumAreaName)

        val newEsAndOsPlusBenchmarks: EsAndOsPlusBenchmarksForCurriculumAreaAndLevel = maybeCurrentEsAndOsPlusBenchmarks match {
          case Some(currentEsAndOsPlusBenchmarks: EsAndOsPlusBenchmarksForCurriculumAreaAndLevel) =>
            val maybeSubSectionMap: Option[Map[String, EandOSetSubSection]] = currentEsAndOsPlusBenchmarks.setSectionNameToSubSections.get(nextEsAndOsBySubSection.eAndOSetSectionName)

            val newSubSectionMap: Map[String, EandOSetSubSection] = maybeSubSectionMap match {
              case Some(subSectionMap) =>
                val maybeEAndOSetSubSection: Option[EandOSetSubSection] = subSectionMap.get(nextEsAndOsBySubSection.eAndOSetSubSectionName.getOrElse(NO_SUBSECTION_NAME))
                val newEAndOSetSubSection: EandOSetSubSection = maybeEAndOSetSubSection match {
                  case Some(eAndOSetSubSection) =>
                    logger.warn(s"Would not expect this line to execute because EandOSetSubSection's should be only created once, for ${eAndOSetSubSection.toString}")
                    eAndOSetSubSection
                  case None => EandOSetSubSection(
                    nextEsAndOsBySubSection.eAndOSetSubSectionAuxiliaryText.getOrElse(""),
                    nextEsAndOsBySubSection.allExperienceAndOutcomesAtTheSubSectionLevel,
                    nextEsAndOsBySubSection.associatedBenchmarks
                  )
                }

                subSectionMap + (nextEsAndOsBySubSection.eAndOSetSubSectionName.getOrElse(NO_SUBSECTION_NAME) -> newEAndOSetSubSection)

              case None =>
                Map(nextEsAndOsBySubSection.eAndOSetSubSectionName.getOrElse(NO_SUBSECTION_NAME) -> EandOSetSubSection(
                  nextEsAndOsBySubSection.eAndOSetSubSectionAuxiliaryText.getOrElse(""),
                  nextEsAndOsBySubSection.allExperienceAndOutcomesAtTheSubSectionLevel,
                  nextEsAndOsBySubSection.associatedBenchmarks
                ))
            }

            EsAndOsPlusBenchmarksForCurriculumAreaAndLevel(
              currentEsAndOsPlusBenchmarks.curriculumLevel,
              currentEsAndOsPlusBenchmarks.curriculumArea,
              currentEsAndOsPlusBenchmarks.setSectionNameToSubSections + (nextEsAndOsBySubSection.eAndOSetSectionName -> newSubSectionMap)
            )
          case None =>
            createNewEsAndOsPlusBenchmarksForSubjectAndLevel(nextEsAndOsBySubSection)
        }
        currentMapForCurriculumLevel + (convertScottishCurriculumAreaName(nextEsAndOsBySubSection.curriculumAreaName) -> newEsAndOsPlusBenchmarks)
      case None =>
        Map(convertScottishCurriculumAreaName(nextEsAndOsBySubSection.curriculumAreaName) -> createNewEsAndOsPlusBenchmarksForSubjectAndLevel(nextEsAndOsBySubSection))
    }
    newMapForCurriculumLevel
  }


  private def convertToBulletPoints(bulletPoints: List[String]): List[EAndOBulletPoint] =
  {
    for {
      bulletPoint <- bulletPoints
    } yield EAndOBulletPoint(bulletPoint)
  }

  private def convertToCurriculumLevel(level: ScottishCurriculumLevel): CurriculumLevel =
  {
    level match {
      case ScottishCurriculumLevel.EARLY => EarlyLevel
      case ScottishCurriculumLevel.FIRST => FirstLevel
      case ScottishCurriculumLevel.SECOND => SecondLevel
      case ScottishCurriculumLevel.THIRD => ThirdLevel
      case ScottishCurriculumLevel.FOURTH => FourthLevel
    }
  }
}