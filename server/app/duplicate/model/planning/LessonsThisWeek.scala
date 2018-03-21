package duplicate.model.planning

import java.time.LocalTime

import upickle.default.{macroRW, ReadWriter => RW}

case class LessonsThisWeek(
                            subjectToLessons: Map[String, List[LessonSummary]]
                          )

object LessonsThisWeek {
  implicit def rw: RW[LessonsThisWeek] = macroRW
}

case class LessonSummary(
                          subject: String,
                          dayOfWeek: String,
                          startTimeIso: String,
                          endTimeIso: String
                        )

object LessonSummary {
  implicit def rw: RW[LessonSummary] = macroRW
}

object LessonSummaryOrdering extends Ordering[LessonSummary] {
  override def compare(x: LessonSummary, y: LessonSummary): Int = {
    if(x.dayOfWeek == y.dayOfWeek) {
      LocalTime.parse(x.startTimeIso).compareTo(LocalTime.parse(y.startTimeIso))
    } else {
      dayOrdinalNumber(x.dayOfWeek).compareTo(dayOrdinalNumber(y.dayOfWeek))
    }
  }

  def dayOrdinalNumber(day: String): Int = {
    day.toLowerCase match {
      case "monday" => 1
      case "tuesday" => 2
      case "wednesday" => 3
      case "thursday" => 4
      case "friday" => 5
    }
  }
}
