package controllers.planning.termly

import java.time.LocalDateTime

import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning.TermlyCurriculumSelection
import models.timetoteach.term.SchoolTerm
import models.timetoteach.{ClassId, TimeToTeachUserId}
import play.api.data.Form
import play.api.data.Forms._

object TermlyPlansControllerFormHelper
{

  def createScottishCurriculumPlanningAreaList(curriculumAreaSelectionData: CurriculumAreaSelectionData):
  List[ScottishCurriculumPlanningArea] =
  {
    val selectedScottishCurriculumPlanningAreas = scala.collection.mutable.ArrayBuffer.empty[ScottishCurriculumPlanningArea]
    if (curriculumAreaSelectionData.EXPRESSIVE_ARTS__ART == "On") {
      selectedScottishCurriculumPlanningAreas += ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__ART
    }
    if (curriculumAreaSelectionData.EXPRESSIVE_ARTS__DRAMA == "On") {
      selectedScottishCurriculumPlanningAreas += ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__DRAMA
    }
    if (curriculumAreaSelectionData.EXPRESSIVE_ARTS__MUSIC == "On") {
      selectedScottishCurriculumPlanningAreas += ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__MUSIC
    }
    if (curriculumAreaSelectionData.HEALTH_AND_WELLBEING == "On") {
      selectedScottishCurriculumPlanningAreas += ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING
    }
    if (curriculumAreaSelectionData.HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION == "On") {
      selectedScottishCurriculumPlanningAreas += ScottishCurriculumPlanningArea.HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION
    }
    if (curriculumAreaSelectionData.LITERACY__WRITING == "On") {
      selectedScottishCurriculumPlanningAreas += ScottishCurriculumPlanningArea.LITERACY__WRITING
    }
    if (curriculumAreaSelectionData.LITERACY__READING == "On") {
      selectedScottishCurriculumPlanningAreas += ScottishCurriculumPlanningArea.LITERACY__READING
    }
    if (curriculumAreaSelectionData.LITERACY__CLASSICAL_LANGUAGES == "On") {
      selectedScottishCurriculumPlanningAreas += ScottishCurriculumPlanningArea.LITERACY__CLASSICAL_LANGUAGES
    }
    if (curriculumAreaSelectionData.LITERACY__GAELIC_LEARNERS == "On") {
      selectedScottishCurriculumPlanningAreas += ScottishCurriculumPlanningArea.LITERACY__GAELIC_LEARNERS
    }
    if (curriculumAreaSelectionData.LITERACY__LITERACY_AND_GAIDLIG == "On") {
      selectedScottishCurriculumPlanningAreas += ScottishCurriculumPlanningArea.LITERACY__LITERACY_AND_GAIDLIG
    }
    if (curriculumAreaSelectionData.LITERACY__MODERN_LANGUAGES == "On") {
      selectedScottishCurriculumPlanningAreas += ScottishCurriculumPlanningArea.LITERACY__MODERN_LANGUAGES
    }
    if (curriculumAreaSelectionData.MATHEMATICS == "On") {
      selectedScottishCurriculumPlanningAreas += ScottishCurriculumPlanningArea.MATHEMATICS
    }
    if (curriculumAreaSelectionData.RME__STANDARD == "On") {
      selectedScottishCurriculumPlanningAreas += ScottishCurriculumPlanningArea.RME__STANDARD
    }
    if (curriculumAreaSelectionData.RME__CATHOLIC == "On") {
      selectedScottishCurriculumPlanningAreas += ScottishCurriculumPlanningArea.RME__CATHOLIC
    }
    if (curriculumAreaSelectionData.SCIENCE == "On") {
      selectedScottishCurriculumPlanningAreas += ScottishCurriculumPlanningArea.SCIENCE
    }
    if (curriculumAreaSelectionData.SOCIAL_STUDIES == "On") {
      selectedScottishCurriculumPlanningAreas += ScottishCurriculumPlanningArea.SOCIAL_STUDIES
    }
    if (curriculumAreaSelectionData.TECHNOLOGIES == "On") {
      selectedScottishCurriculumPlanningAreas += ScottishCurriculumPlanningArea.TECHNOLOGIES
    }

    selectedScottishCurriculumPlanningAreas.toList
  }

  def createTermlyCurriculumSelection(
                                       tttUserId: TimeToTeachUserId,
                                       createdTime: LocalDateTime,
                                       schoolTerm: SchoolTerm,
                                       curriculumAreaSelectionData: CurriculumAreaSelectionData)
  : TermlyCurriculumSelection =
  {
    val planningAreas: List[ScottishCurriculumPlanningArea] = createScottishCurriculumPlanningAreaList(curriculumAreaSelectionData)

    TermlyCurriculumSelection(
      tttUserId,
      ClassId(curriculumAreaSelectionData.classId),
      planningAreas,
      createdTime,
      schoolTerm
    )
  }

  case class CurriculumAreaSelectionData(
                                          classId: String,
                                          EXPRESSIVE_ARTS__ART: String,
                                          EXPRESSIVE_ARTS__DRAMA: String,
                                          EXPRESSIVE_ARTS__MUSIC: String,
                                          HEALTH_AND_WELLBEING: String,
                                          HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION: String,
                                          LITERACY__WRITING: String,
                                          LITERACY__READING: String,
                                          LITERACY__CLASSICAL_LANGUAGES: String,
                                          LITERACY__GAELIC_LEARNERS: String,
                                          LITERACY__LITERACY_AND_GAIDLIG: String,
                                          LITERACY__MODERN_LANGUAGES: String,
                                          MATHEMATICS: String,
                                          RME__STANDARD: String,
                                          RME__CATHOLIC: String,
                                          SCIENCE: String,
                                          SOCIAL_STUDIES: String,
                                          TECHNOLOGIES: String
                                        )


  val curriculumAreaSelectionDataForm: Form[CurriculumAreaSelectionData] = Form(
    mapping(
      "classId" -> text,
      "EXPRESSIVE_ARTS__ART" -> text,
      "EXPRESSIVE_ARTS__DRAMA" -> text,
      "EXPRESSIVE_ARTS__MUSIC" -> text,
      "HEALTH_AND_WELLBEING" -> text,
      "HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION" -> text,
      "LITERACY__WRITING" -> text,
      "LITERACY__READING" -> text,
      "LITERACY__CLASSICAL_LANGUAGES" -> text,
      "LITERACY__GAELIC_LEARNERS" -> text,
      "LITERACY__LITERACY_AND_GAIDLIG" -> text,
      "LITERACY__MODERN_LANGUAGES" -> text,
      "MATHEMATICS" -> text,
      "RME__STANDARD" -> text,
      "RME__CATHOLIC" -> text,
      "SCIENCE" -> text,
      "SOCIAL_STUDIES" -> text,
      "TECHNOLOGIES" -> text
    )(CurriculumAreaSelectionData.apply)(CurriculumAreaSelectionData.unapply)
  )


}
