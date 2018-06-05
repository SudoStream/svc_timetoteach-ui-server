package models.timetoteach.planning.pdf

import java.time.LocalDateTime

import duplicate.model.{ClassDetails, EandOsWithBenchmarks, Group}
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.TimeToTeachUserId
import models.timetoteach.planning.PlanType.PlanType
import models.timetoteach.planning.ScottishCurriculumPlanningAreaWrapper
import models.timetoteach.term.SchoolTerm


case class CurriculumAreaTermlyPlanForPdfWrapper(
                                                  fullTermlyPlan: Map[ScottishCurriculumPlanningAreaWrapper, List[CurriculumAreaTermlyPlanForPdf]]
                                                ) {

  def plansAtSubjectLevel: Map[ScottishCurriculumPlanningAreaWrapper, List[CurriculumAreaTermlyPlanForPdf]] = {
    {
      def buildSubjectLevelPlans(curriculumAreaTermlyPlans: List[CurriculumAreaTermlyPlanForPdf])
      : List[(ScottishCurriculumPlanningAreaWrapper, List[CurriculumAreaTermlyPlanForPdf])] = {

        def loop(remainingPlans: List[CurriculumAreaTermlyPlanForPdf],
                 currentRetVal: Map[ScottishCurriculumPlanningAreaWrapper, List[CurriculumAreaTermlyPlanForPdf]]):
        List[(ScottishCurriculumPlanningAreaWrapper, List[CurriculumAreaTermlyPlanForPdf])] = {
          if (remainingPlans.isEmpty) {
            currentRetVal.toList
          } else {
            val nextCurriculumArea = remainingPlans.head

            println(s"${nextCurriculumArea.planningArea} : ${nextCurriculumArea.maybeGroup.toString}")

            val wrappedPlanArea = ScottishCurriculumPlanningAreaWrapper(nextCurriculumArea.planningArea)
            val nextMapVal = if (currentRetVal.contains(wrappedPlanArea)) {
              val nextListVal = nextCurriculumArea :: currentRetVal(ScottishCurriculumPlanningAreaWrapper(nextCurriculumArea.planningArea))
              currentRetVal + (wrappedPlanArea -> nextListVal)
            } else {
              currentRetVal + (wrappedPlanArea -> List(nextCurriculumArea) )
            }
            loop(remainingPlans.tail, nextMapVal)
          }
        }

        loop(curriculumAreaTermlyPlans, Map())
      }

      val retVal = {
        {
          for {
            key <- fullTermlyPlan.keys
//            hmm = println(s"The Key is $key")
            curriculumAreaTermlyPlans: List[CurriculumAreaTermlyPlanForPdf] = fullTermlyPlan(key)
            tuplesOfPlans = buildSubjectLevelPlans(curriculumAreaTermlyPlans)
          } yield tuplesOfPlans
        }.flatten
      }.toMap

//      println(" ------------- ------------- ------------- ------------- -------------")
//      for {
//        key <- retVal.keys
//        groupPlan <-  retVal(key)
//        hmm = println(s" key($key) : ${retVal(key).size}")
//        hmm2 = println(s" group(${groupPlan.maybeGroup.toString})")
//        hmm3 = println(s" ++ ")
//      }
//      println(" ------------- ------------- ------------- ------------- -------------")
//      println(" ------------- ------------- ------------- ------------- -------------\n")
      retVal
    }
  }

}

case class CurriculumAreaTermlyPlanForPdf(
                                           tttUserId: TimeToTeachUserId,
                                           planType: PlanType,
                                           schoolTerm: SchoolTerm,
                                           classId: ClassDetails,
                                           maybeGroup: Option[Group],
                                           planningArea: ScottishCurriculumPlanningArea,
                                           createdTime: LocalDateTime,
                                           eAndOsWithBenchmarks: List[EandOsWithBenchmarks]
                                         ) {
  val orderNumber = maybeGroup match {
    case Some(group) => group.groupLevel.order
    case None => ScottishCurriculumPlanningAreaWrapper(planningArea).orderNumber
  }
}


