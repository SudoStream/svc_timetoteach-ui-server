package models.timetoteach.planning

import duplicate.model.Group

case class CurriculumPlanProgressForClass(
                                           curriculumProgressMap: Map[ScottishCurriculumPlanningAreaWrapper,
                                             (OverallClassLevelProgressPercent, Map[Group, GroupLevelProgressPercent])]
                                         )

case class OverallClassLevelProgressPercent(percentValue: Int)

case class GroupLevelProgressPercent(percentValue: Int)