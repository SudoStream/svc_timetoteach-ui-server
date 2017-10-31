package shared.model

object Subjects {
  val values = Set(
    Subject("subject-golden-time"),
    Subject("subject-other"),
    Subject("subject-ict"),
    Subject("subject-music"),
    Subject("subject-drama"),
    Subject("subject-health"),
    Subject("subject-teacher-covertime"),
    Subject("subject-assembly-button"),
    Subject("subject-reading"),
    Subject("subject-spelling"),
    Subject("subject-writing"),
    Subject("subject-maths"),
    Subject("subject-topic"),
    Subject("subject-physical-education")
  )
}

case class Subject(value: String)