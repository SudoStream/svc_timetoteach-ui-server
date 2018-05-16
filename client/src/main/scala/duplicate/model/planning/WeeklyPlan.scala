package duplicate.model.planning

import duplicate.model.esandos.EsAndOsPlusBenchmarksForCurriculumAreaAndLevel
import upickle.default.{macroRW, ReadWriter => RW}

case class WeeklyPlanOfOneSubject(
                                   tttUserId: String,
                                   classId: String,
                                   subject: String,
                                   weekBeginningIsoDate: String,
                                   groupToEsOsBenchmarks: Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel],
                                   lessons: List[LessonPlan]
                                 )

object WeeklyPlanOfOneSubject {
  implicit def rw: RW[WeeklyPlanOfOneSubject] = macroRW
}

case class LessonPlan(
                       subject: String,
                       subjectAdditionalInfo: String,
                       weekBeginningIsoDate: String,
                       lessonDateIso: String,
                       startTimeIso: String,
                       endTimeIso: String,

                       activitiesPerGroup: Map[String, List[String]],
                       resources: List[String],
                       learningIntentionsPerGroup: Map[String, List[String]],
                       successCriteriaPerGroup: Map[String, List[String]],
                       plenary: List[String],
                       formativeAssessmentPerGroup: Map[String, List[String]],
                       notesBefore: List[String],
                       notesAfter: List[String]
                     )

object LessonPlan {
  implicit def rw: RW[LessonPlan] = macroRW
}

