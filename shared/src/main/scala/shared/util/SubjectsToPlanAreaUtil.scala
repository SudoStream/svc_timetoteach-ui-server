package shared.util

object SubjectsToPlanAreaUtil {
  def convertSubjectNameToPlanAreaName(subjectName: String): String = {
    subjectName.toLowerCase match {
      case "empty" => "EMPTY"
      case "golden-time" => "GOLDEN_TIME"
      case "other" => "OTHER"
      case "ict" => "TECHNOLOGIES"
      case "music" | "expressive_arts__music" => "EXPRESSIVE_ARTS__MUSIC"
      case "drama" | "expressive_arts__drama" => "EXPRESSIVE_ARTS__DRAMA"
      case "health" => "HEALTH_AND_WELLBEING"
      case "teacher-covertime" => "TEACHER_COVERTIME"
      case "assembly" => "ASSEMBLY"
      case "reading" | "literacy__reading" => "LITERACY__READING"
      case "spelling" => "SPELLING"
      case "writing" | "literacy__writing" => "LITERACY__WRITING"
      case "maths" | "mathematics" => "MATHEMATICS"
      case "topic" => "TOPIC"
      case "physical-education" => "HEALTH_AND_WELLBEING__PHYSICAL_EDUCATION"
      case "soft-start" => "SOFT_START"
      case "numeracy" => "MATHEMATICS"
      case "art" | "expressive_arts__art" => "EXPRESSIVE_ARTS__ART"
      case "rme" => "RME__STANDARD"
      case "play" => "PLAY"
      case "modern-languages" => "LITERACY__MODERN_LANGUAGES"
      case "science" => "SCIENCE"
      case "hand-writing" => "HAND_WRITING"
      case "geography" => "SOCIAL_STUDIES"
      case "history" => "SOCIAL_STUDIES"
      case _ => "OTHER"
    }
  }
}
