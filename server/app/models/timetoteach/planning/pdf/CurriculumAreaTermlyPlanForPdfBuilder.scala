package models.timetoteach.planning.pdf

import duplicate.model.ClassDetails
import models.timetoteach.planning.{CurriculumAreaTermlyPlan, ScottishCurriculumPlanningAreaWrapper}
import play.api.Logger

import scala.annotation.tailrec


object CurriculumAreaTermlyPlanForPdfBuilder
{
  private val logger: Logger = Logger

  def buildCurriculumAreaTermlyPlanForPdf(fullTermlyPlan: List[CurriculumAreaTermlyPlan], classDetails: ClassDetails): CurriculumAreaTermlyPlanForPdfWrapper =
  {
    val fullTermlyPlanMap = buildFullTermlyPlanMap(fullTermlyPlan, classDetails)
    CurriculumAreaTermlyPlanForPdfWrapper(fullTermlyPlanMap)
  }

  private def buildFullTermlyPlanMap(fullTermlyPlan: List[CurriculumAreaTermlyPlan], classDetails: ClassDetails): Map[ScottishCurriculumPlanningAreaWrapper, List[CurriculumAreaTermlyPlanForPdf]] =
  {
    @tailrec
    def buildFullTermlyPlanLoop(
                                 remainingPlans: List[CurriculumAreaTermlyPlan],
                                 currentMap: Map[ScottishCurriculumPlanningAreaWrapper, List[CurriculumAreaTermlyPlanForPdf]]
                               )
    : Map[ScottishCurriculumPlanningAreaWrapper, List[CurriculumAreaTermlyPlanForPdf]] =
    {
      if (remainingPlans.isEmpty) currentMap
      else {
        val nextPlan: CurriculumAreaTermlyPlanForPdf = convert(remainingPlans.head, classDetails)
        val nextPlanCurriculum = ScottishCurriculumPlanningAreaWrapper(nextPlan.planningArea)
        val nextPlanList = currentMap.get(nextPlanCurriculum) match {
          case Some(listAlreadyPresent) => nextPlan :: listAlreadyPresent
          case None => nextPlan :: Nil
        }
        buildFullTermlyPlanLoop(remainingPlans.tail, currentMap + (nextPlanCurriculum -> nextPlanList))
      }
    }

    buildFullTermlyPlanLoop(fullTermlyPlan, Map())
  }

  private def convert(head: CurriculumAreaTermlyPlan, classDetails: ClassDetails): CurriculumAreaTermlyPlanForPdf =
  {
    logger.error(s"convert(...) ${head.maybeGroupId.toString} ?= ${classDetails.groups.toString()} ")
    val groupIdToSearchFor = head.maybeGroupId.getOrElse(models.timetoteach.planning.GroupId("NOPE")).value
    val maybeGroup = classDetails.groups.find(group => group.groupId.id == groupIdToSearchFor)

    CurriculumAreaTermlyPlanForPdf(
      head.tttUserId,
      head.planType,
      head.schoolTerm,
      classDetails,
      maybeGroup,
      head.planningArea,
      head.createdTime,
      head.eAndOsWithBenchmarks
    )
  }

}
