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
                                                )

case class CurriculumAreaTermlyPlanForPdf(
                                           tttUserId: TimeToTeachUserId,
                                           planType: PlanType,
                                           schoolTerm: SchoolTerm,
                                           classId: ClassDetails,
                                           maybeGroup: Option[Group],
                                           planningArea: ScottishCurriculumPlanningArea,
                                           createdTime: LocalDateTime,
                                           eAndOsWithBenchmarks: List[EandOsWithBenchmarks]
                                         )
