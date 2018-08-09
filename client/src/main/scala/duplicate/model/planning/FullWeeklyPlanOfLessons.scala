package duplicate.model.planning

import upickle.default.{macroRW, ReadWriter => RW}

case class FullWeeklyPlanOfLessons(
                                    tttUserId: String,
                                    classId: String,
                                    weekBeginningIsoDate: String,
                                    subjectToWeeklyPlanOfSubject: Map[String, WeeklyPlanOfOneSubject]
                                  ) {

  def dayToLessons(): Map[String, List[LessonPlan]] = {
    val allLessonPlansForTheWeek = {
      for {
        subject <- subjectToWeeklyPlanOfSubject.keys
        weeklyPlanOfASubject = subjectToWeeklyPlanOfSubject(subject)
      } yield weeklyPlanOfASubject.lessons
    }.flatten

    def loop(allLessonPlans: List[LessonPlan], currentMap: Map[String, List[LessonPlan]]): Map[String, List[LessonPlan]] = {
      if (allLessonPlans.isEmpty) currentMap
      else {
        val nextLessonPlan = allLessonPlans.head
        val day = getDayOfWeek(nextLessonPlan.lessonDateIso)
        val nextMap = if (currentMap.isDefinedAt(day)) {
          val currentLessonPlansForDay = currentMap(day)
          val nextLessonPlans = nextLessonPlan :: currentLessonPlansForDay
          currentMap + (day -> nextLessonPlans)
        } else {
          currentMap + (day -> List(nextLessonPlan))
        }
        loop(allLessonPlans.tail, nextMap)
      }
    }

    loop(allLessonPlansForTheWeek.toList, Map())
  }

  private def getDayOfWeek(date: String): String = {
    val javaDate = java.time.LocalDate.parse(date)
    javaDate.getDayOfWeek.toString
  }

}

object FullWeeklyPlanOfLessons {
  implicit def rw: RW[FullWeeklyPlanOfLessons] = macroRW
}
