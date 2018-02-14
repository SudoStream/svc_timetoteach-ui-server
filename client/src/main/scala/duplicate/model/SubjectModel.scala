package duplicate.model

import upickle.default.{macroRW, ReadWriter => RW}

sealed trait SubjectModel
{
  def value: String
}

object SubjectModel
{
  implicit def rw: RW[SubjectModel] = macroRW
}

case object SubjectArt extends SubjectModel {
  val value = "ART"
}
case object SubjectAssembly extends SubjectModel {
  val value = "ASSEMBLY"
}
case object SubjectEmpty extends SubjectModel {
  val value = "EMPTY"
}
case object SubjectDrama extends SubjectModel {
  val value = "DRAMA"
}
case object SubjectGoldenTime extends SubjectModel {
  val value = "GOLDEN_TIME"
}
case object SubjectHealth extends SubjectModel {
  val value = "HEALTH"
}
case object SubjectIct extends SubjectModel {
  val value = "ICT"
}
case object SubjectMaths extends SubjectModel {
  val value = "MATHS"
}
case object SubjectMusic extends SubjectModel {
  val value = "MUSIC"
}
case object SubjectNumeracy extends SubjectModel {
  val value = "NUMERACY"
}
case object SubjectOther extends SubjectModel {
  val value = "OTHER"
}
case object SubjectReading extends SubjectModel {
  val value = "READING"
}
case object SubjectPhysicalEducation extends SubjectModel {
  val value = "PHYSICAL_EDUCATION"
}
case object SubjectRme extends SubjectModel {
  val value = "RME"
}
case object SubjectSoftStart extends SubjectModel {
  val value = "SOFT_START"
}
case object SubjectSpelling extends SubjectModel {
  val value = "SPELLING"
}
case object SubjectTeacherCovertime extends SubjectModel {
  val value = "TEACHER_COVERTIME"
}
case object SubjectTopic extends SubjectModel {
  val value = "TOPIC"
}
case object SubjectWriting extends SubjectModel {
  val value = "WRITING"
}
case object SubjectPlay extends SubjectModel {
  val value = "PLAY"
}
case object SubjectModernLanguages extends SubjectModel {
  val value = "MODERN_LANGUAGES"
}
case object SubjectScience extends SubjectModel {
  val value = "SCIENCE"
}
case object SubjectHandWriting extends SubjectModel {
  val value = "HAND_WRITING"
}
case object SubjectGeography extends SubjectModel {
  val value = "GEOGRAPHY"
}
case object SubjectHistory extends SubjectModel {
  val value = "HISTORY"
}
