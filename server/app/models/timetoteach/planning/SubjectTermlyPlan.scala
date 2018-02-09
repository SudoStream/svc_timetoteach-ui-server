package models.timetoteach.planning

import java.time.LocalDateTime

import duplicate.model.EandOsWithBenchmarks
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.subjectdetail.SubjectName
import models.timetoteach.{ClassId, TimeToTeachUserId}
import models.timetoteach.planning.PlanType.PlanType
import models.timetoteach.term.SchoolTerm

case class SubjectTermlyPlan(
                              tttUserId: TimeToTeachUserId,
                              planType: PlanType,
                              schoolTerm: SchoolTerm,
                              classId: ClassId,
                              maybeGroupId: Option[GroupId],
                              subject: SubjectName,
                              createdTime: LocalDateTime,
                              eandOsWithBenchmarks: List[EandOsWithBenchmarks]
                            )

object PlanType extends Enumeration {
  type PlanType = Value
  val CLASS_LEVEL_PLAN, GROUP_LEVEL_PLAN = Value
}

case class GroupId(value: String)
