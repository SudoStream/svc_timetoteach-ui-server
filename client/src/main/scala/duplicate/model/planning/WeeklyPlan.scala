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
                       createdTimestamp: String,

                       activitiesPerGroup: Map[AttributeRowKey , List[String]],
                       resources: List[AttributeRowKey ],
                       learningIntentionsPerGroup: Map[AttributeRowKey , List[String]],
                       successCriteriaPerGroup: Map[AttributeRowKey , List[String]],
                       plenary: List[AttributeRowKey ],
                       formativeAssessmentPerGroup: Map[AttributeRowKey , List[String]],
                       notesBefore: List[AttributeRowKey ],
                       notesAfter: List[AttributeRowKey ]
                     )

object LessonPlan {
  implicit def rw: RW[LessonPlan] = macroRW
}

case class AttributeRowKey (
                             attributeValue: String,
                             attributeOrderNumber: Int
                           )

object AttributeRowKey {
  implicit def rw: RW[AttributeRowKey] = macroRW
}