package models.timetoteach.planning

import duplicate.model.Group

case class CurriculumPlanProgressForClass(
                                           curriculumProgressMap: Map[ScottishCurriculumPlanningAreaWrapper,
                                             (OverallClassLevelProgressPercent, Map[Group, GroupLevelProgressPercent])]
                                         )
{
  def overallClassPercentageProgress: Int =
  {
    val numerator = {
      {
        for {
          planningArea <- curriculumProgressMap.keys
          planningAreaOverallPercent = curriculumProgressMap(planningArea)._1
        } yield planningAreaOverallPercent.percentValue
      }.toList.sum
    }
    val denominator = curriculumProgressMap.keys.size

    numerator / denominator
  }
}


case class OverallClassLevelProgressPercent(percentValue: Int)

case class GroupLevelProgressPercent(percentValue: Int)