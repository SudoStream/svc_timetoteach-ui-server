package models.timetoteach.planning

import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea

case class ScottishCurriculumPlanningAreaWrapper(
                                                  value: ScottishCurriculumPlanningArea
                                                )
{
  def isNotCompositeValue: Boolean =
  {
    !isCompositeValue
  }

  def isCompositeValue: Boolean =
  {
    value.toString.contains("__")
  }

  def niceValue(): String =
  {
    value
      .toString
      .replace("__", " : ")
      .toLowerCase.replace("_", " ")
      .replace("rme", "RME")
      .split(" ")
      .toList
      .map(word => word.capitalize)
      .mkString(" ")
  }

  def niceHeaderValueIfPresent(): Option[String] =
  {
    if (isCompositeValue) {
      niceValue().split(" : ").headOption
    } else {
      None
    }
  }

  def niceSpecificValueIfPresent(): Option[String] =
  {
    if (isCompositeValue) {
      val values = niceValue().split(" : ")
      if (values.size > 1) {
        Some(values(1))
      } else {
        None
      }
    } else {
      None
    }
  }

}
