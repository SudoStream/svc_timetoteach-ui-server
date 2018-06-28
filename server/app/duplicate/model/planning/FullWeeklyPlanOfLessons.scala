package duplicate.model.planning

import upickle.default.{macroRW, ReadWriter => RW}

case class FullWeeklyPlanOfLessons(
                                    tttUserId: String,
                                    classId: String,
                                    weekBeginningIsoDate: String,
                                    subjectToWeeklyPlanOfSubject: Map[String, WeeklyPlanOfOneSubject]
                                  ) {

}

object FullWeeklyPlanOfLessons {
  implicit def rw: RW[FullWeeklyPlanOfLessons] = macroRW
}
