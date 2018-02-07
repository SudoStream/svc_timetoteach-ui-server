package curriculum.scotland

import javax.inject.{Inject, Singleton}

import controllers.serviceproxies.EsAndOsReaderServiceProxyImpl
import duplicate.model._
import duplicate.model.esandos.{CurriculumArea, EsAndOsPlusBenchmarksForSubjectAndLevel, _}
import io.sudostream.timetoteach.messages.scottish._
import play.api.Logger

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
  val logger: Logger = Logger

  private[scotland] def buildTheEsOsAndBenchmarks(scottishEsAndOsData: ScottishEsAndOsData):
  Option[Map[CurriculumLevel, Map[CurriculumArea, EsAndOsPlusBenchmarksForSubjectAndLevel]]] = {

    def buildTheEsOsAndBenchmarksLoop(
                                       nextEsAndOsBySubSection: ScottishEsAndOsBySubSection,
                                       restScottishEsAndOsBySubSection: List[ScottishEsAndOsBySubSection],
                                       currentMap: Map[CurriculumLevel, Map[CurriculumArea, EsAndOsPlusBenchmarksForSubjectAndLevel]]
                                     ): Option[Map[CurriculumLevel, Map[CurriculumArea, EsAndOsPlusBenchmarksForSubjectAndLevel]]] = {

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

  private def loop(buildTheEsOsAndBenchmarksLoop: (ScottishEsAndOsBySubSection, List[ScottishEsAndOsBySubSection], Map[CurriculumLevel, Map[CurriculumArea, EsAndOsPlusBenchmarksForSubjectAndLevel]]) => Option[Map[CurriculumLevel, Map[CurriculumArea, EsAndOsPlusBenchmarksForSubjectAndLevel]]],
                   restScottishEsAndOsBySubSection: List[ScottishEsAndOsBySubSection],
                   newMap: Map[CurriculumLevel, Map[CurriculumArea, EsAndOsPlusBenchmarksForSubjectAndLevel]]) = {
    if (restScottishEsAndOsBySubSection.isEmpty) {
      Some(newMap)
    } else {
      buildTheEsOsAndBenchmarksLoop(restScottishEsAndOsBySubSection.head, restScottishEsAndOsBySubSection.tail, newMap)
    }
  }


  private def createNewVersionOfMap(nextEsAndOsBySubSection: ScottishEsAndOsBySubSection,
                                    maybeCurrentMapForCurriculumLevel: Option[Map[CurriculumArea, EsAndOsPlusBenchmarksForSubjectAndLevel]])
  : Map[CurriculumArea, EsAndOsPlusBenchmarksForSubjectAndLevel] = {

    val newMapForCurriculumLevel: Map[CurriculumArea, EsAndOsPlusBenchmarksForSubjectAndLevel] = maybeCurrentMapForCurriculumLevel match {
      case Some(currentMapForCurriculumLevel: Map[CurriculumArea, EsAndOsPlusBenchmarksForSubjectAndLevel]) =>

        val maybeCurrentEsAndOsPlusBenchmarks = currentMapForCurriculumLevel.get(nextEsAndOsBySubSection.curriculumAreaName)

        val newEsAndOsPlusBenchmarks: EsAndOsPlusBenchmarksForSubjectAndLevel = maybeCurrentEsAndOsPlusBenchmarks match {
          case Some(currentEsAndOsPlusBenchmarks: EsAndOsPlusBenchmarksForSubjectAndLevel) =>
            val maybeSubSectionMap: Option[Map[String, EandOSetSubSection]] = currentEsAndOsPlusBenchmarks.setSectionNameToSubSections.get(nextEsAndOsBySubSection.eAndOSetSectionName)

            val newSubSectionMap: Map[String, EandOSetSubSection] = maybeSubSectionMap match {
              case Some(subSectionMap) =>
                val maybeEAndOSetSubSection: Option[EandOSetSubSection] = subSectionMap.get(nextEsAndOsBySubSection.eAndOSetSubSectionName.getOrElse(NO_SUBSECTION_NAME))
                val newEAndOSetSubSection: EandOSetSubSection = maybeEAndOSetSubSection match {
                  case Some(eAndOSetSubSection) =>
                    logger.warn("Would not expect this line to execute because EandOSetSubSection's should be only created once")
                    eAndOSetSubSection
                  case None => EandOSetSubSection(
                    nextEsAndOsBySubSection.eAndOSetSubSectionAuxiliaryText.getOrElse(""),
                    nextEsAndOsBySubSection.allExperienceAndOutcomesAtTheSubSectionLevel,
                    nextEsAndOsBySubSection.associatedBenchmarks
                  )
                }

                subSectionMap + (nextEsAndOsBySubSection.eAndOSetSubSectionName.getOrElse(NO_SUBSECTION_NAME) -> EandOSetSubSection(
                  nextEsAndOsBySubSection.eAndOSetSubSectionAuxiliaryText.getOrElse(""),
                  nextEsAndOsBySubSection.allExperienceAndOutcomesAtTheSubSectionLevel,
                  nextEsAndOsBySubSection.associatedBenchmarks
                ))

              case None =>
                Map(nextEsAndOsBySubSection.eAndOSetSubSectionName.getOrElse(NO_SUBSECTION_NAME) -> EandOSetSubSection(
                  nextEsAndOsBySubSection.eAndOSetSubSectionAuxiliaryText.getOrElse(""),
                  nextEsAndOsBySubSection.allExperienceAndOutcomesAtTheSubSectionLevel,
                  nextEsAndOsBySubSection.associatedBenchmarks
                ))
            }

            EsAndOsPlusBenchmarksForSubjectAndLevel(
              currentEsAndOsPlusBenchmarks.curriculumLevel,
              currentEsAndOsPlusBenchmarks.curriculumArea,
              currentEsAndOsPlusBenchmarks.setSectionNameToSubSections + (nextEsAndOsBySubSection.eAndOSetSectionName -> newSubSectionMap)
            )
          case None =>
            createNewEsAndOsPlusBenchmarksForSubjectAndLevel(nextEsAndOsBySubSection)
        }
        Map(convertScottishCurriculumAreaName(nextEsAndOsBySubSection.curriculumAreaName) -> newEsAndOsPlusBenchmarks)
      case None =>
        Map(convertScottishCurriculumAreaName(nextEsAndOsBySubSection.curriculumAreaName) -> createNewEsAndOsPlusBenchmarksForSubjectAndLevel(nextEsAndOsBySubSection))
    }
    newMapForCurriculumLevel
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