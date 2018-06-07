package duplicate.model.planning

import java.time.LocalDate

import duplicate.model.esandos.EsAndOsPlusBenchmarksForCurriculumAreaAndLevel
import upickle.default.{macroRW, ReadWriter => RW}

case class WeeklyPlanOfOneSubject(
                                   tttUserId: String,
                                   classId: String,
                                   subject: String,
                                   weekBeginningIsoDate: String,
                                   groupToEsOsBenchmarks: Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel],
                                   lessons: List[LessonPlan]
                                 ) {

  def findLessonPlan(startTimeIsoToCheck: String, dayOfTheWeekToCheck: String) : Option[LessonPlan] = {
    val filteredLessons = for {
      lesson <- lessons
      if lesson.startTimeIso == startTimeIsoToCheck
      lessonDate = LocalDate.parse(lesson.lessonDateIso)
      lessonDay = lessonDate.getDayOfWeek.toString
      if lessonDay == dayOfTheWeekToCheck
    } yield lesson

    if (filteredLessons.size == 1) {
     Some(filteredLessons.head)
    } else {
      None
    }
  }

}

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