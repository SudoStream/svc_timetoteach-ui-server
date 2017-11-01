package shared.model

object Subjects {
  println("AAAAAAAAAAAA")
  val values = Set(
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
  println(s"OOOOOOOOOOOOO ${Subjects.values}")
  require(Subjects.values.count(_ == value) == 1)
}