package models.timetoteach.planning

import duplicate.model._
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea

case class ScottishCurriculumPlanningAreaWrapper(
                                                  value: ScottishCurriculumPlanningArea
                                                ) {
  val orderNumber: Int = {
    niceSpecificValueIfPresent().getOrElse(niceValue()).charAt(0).toLower.toInt
  }

  def isNotCompositeValue: Boolean = {
    !isCompositeValue
  }

  def isCompositeValue: Boolean = {
    value.toString.contains("__")
  }

  def niceValue(): String = {
    value
      .toString
      .replace("__", " : ")
      .toLowerCase.replace("_", " ")
      .replace("rme", "RME")
      .replace("standard", "Religious & Moral Education")
      .replace("catholic", "Religious & Moral Education (Catholic)")
      .split(" ")
      .toList
      .map(word => word.capitalize)
      .mkString(" ")
  }

  def niceHeaderValueIfPresent(): Option[String] = {
    if (isCompositeValue) {
      niceValue().split(" : ").headOption
    } else {
      None
    }
  }

  def niceSpecificValueIfPresent(): Option[String] = {
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

  def niceSubjectLevelValue(): String = {
    value match {
      case ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__ART |
           ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__DANCE |
           ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__DRAMA |
           ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__MUSIC |
           ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION |
           ScottishCurriculumPlanningArea.LITERACY__READING |
           ScottishCurriculumPlanningArea.LITERACY__WRITING =>

        niceSpecificValueIfPresent().getOrElse("")

      case _ =>
        niceValue()
    }
  }

  def planAtGroupLevel: Boolean = {
    value match {
      case ScottishCurriculumPlanningArea.LITERACY__CLASSICAL_LANGUAGES |
           ScottishCurriculumPlanningArea.LITERACY__GAELIC_LEARNERS |
           ScottishCurriculumPlanningArea.LITERACY__LITERACY_AND_ENGLISH |
           ScottishCurriculumPlanningArea.LITERACY__LITERACY_AND_GAIDLIG |
           ScottishCurriculumPlanningArea.LITERACY__MODERN_LANGUAGES |
           ScottishCurriculumPlanningArea.LITERACY__READING |
           ScottishCurriculumPlanningArea.LITERACY__WRITING |
           ScottishCurriculumPlanningArea.MATHEMATICS => true
      case _ => false
    }
  }

  def groupType: GroupType = {
    value match {
      case ScottishCurriculumPlanningArea.LITERACY__CLASSICAL_LANGUAGES |
           ScottishCurriculumPlanningArea.LITERACY__GAELIC_LEARNERS |
           ScottishCurriculumPlanningArea.LITERACY__LITERACY_AND_ENGLISH |
           ScottishCurriculumPlanningArea.LITERACY__LITERACY_AND_GAIDLIG |
           ScottishCurriculumPlanningArea.LITERACY__MODERN_LANGUAGES |
           ScottishCurriculumPlanningArea.LITERACY__READING => ReadingGroupType
      case ScottishCurriculumPlanningArea.LITERACY__WRITING => WritingGroupType
      case ScottishCurriculumPlanningArea.MATHEMATICS => MathsGroupType
      case _ => OtherGroupType
    }
  }
}
