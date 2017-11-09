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
    "subject-physical-education",
    "subject-soft-start",
    "subject-numeracy",
    "subject-art",
    "subject-rme"
  )
}

case class SubjectName(value: String) {
  require(Subjects.values.count(_ == value) == 1)
  def niceValue : String = {
    val translatedValue = value match {
      case "subject-teacher-covertime" => "RCCT"
      case "subject-physical-education" => "PE"
      case "subject-topic" => "IDL"
      case "subject-ict" => "ICT"
      case "subject-rme" => "RME"
      case otherValue: String => otherValue
    }

    translatedValue.replace("subject-","").replace("-"," ").capitalize
  }
}