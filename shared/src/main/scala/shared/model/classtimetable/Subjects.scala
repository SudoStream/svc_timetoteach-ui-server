package shared.model.classtimetable

object Subjects {
  val values = Set(
    "subject-empty",
    "subject-golden-time",
    "subject-other",
    "subject-ict",
    "subject-music",
    "subject-drama",
    "subject-health",
    "subject-teacher-covertime",
    "subject-assembly",
    "subject-reading",
    "subject-spelling",
    "subject-writing",
    "subject-maths",
    "subject-topic",
    "subject-physical-education"
  )
}

case class SubjectName(value: String) {
  require(Subjects.values.count(_ == value) == 1)
}