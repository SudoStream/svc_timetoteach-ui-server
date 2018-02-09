package models.timetoteach.planning

import java.time.LocalDateTime

import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.subjectdetail.SubjectName
import models.timetoteach.{SchoolId, TimeToTeachUserId}
import models.timetoteach.planning.PlanType.PlanType
import models.timetoteach.term.SchoolTerm

case class SubjectTermlyPlan(
                              tttUserId: TimeToTeachUserId,
                              planType: PlanType,
                              schoolTerm: SchoolTerm,
                              schoolId: SchoolId,
                              maybeGroupId: Option[GroupId],
                              subject: SubjectName,
                              createdTime: LocalDateTime,
                              selectedEsAndOsCodes: List[String],
                              selectedBenchmarks: List[String]
                            )

object PlanType extends Enumeration {
  type PlanType = Value
  val CLASS_LEVEL_PLAN, GROUP_LEVEL_PLAN = Value
}

case class GroupId(value: String)
