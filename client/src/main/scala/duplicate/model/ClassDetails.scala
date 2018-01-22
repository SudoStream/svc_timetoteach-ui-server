package duplicate.model

import upickle.default.{macroRW, ReadWriter => RW}

case class ClassDetails(
                         id: ClassId,
                         className: ClassName,
                         classDescription: String,
                         groups: List[Group],
                         classTeachersWithWriteAccess: List[String]
                       ) {
  if (classTeachersWithWriteAccess.isEmpty) throw new IllegalArgumentException(
    "Must have at least 1 teacher with write access"
  )
}

object ClassDetails{
  implicit def rw: RW[ClassDetails] = macroRW
}

case class ClassId(id: String)
object ClassId{
  implicit def rw: RW[ClassId] = macroRW
}

case class ClassName(name: String)
object ClassName{
  implicit def rw: RW[ClassName] = macroRW
}

////

case class Group(groupId: GroupId,
                 groupName: GroupName,
                 groupDescription: String,
                 groupType: GroupType,
                 groupLevel: CurriculumLevel)
object Group{
  implicit def rw: RW[Group] = macroRW
}

case class GroupId(id: String)
object GroupId{
  implicit def rw: RW[GroupId] = macroRW
}


case class GroupName(name: String)
object GroupName{
  implicit def rw: RW[GroupName] = macroRW
}

////////// Group Type //////////////
//object GroupType extends Enumeration {
//  type GroupType = Value
//  val Maths, Literacy, Other = Value
//}


sealed trait GroupType {
  def value: String
}
object GroupType{
  implicit def rw: RW[GroupType] = macroRW
}

case object MathsGroupType extends GroupType {
  val value = "Maths"
}

case object LiteracyGroupType extends GroupType {
  val value = "Literacy"
}

case object OtherGroupType extends GroupType {
  val value = "Other"
}

////////// Group Type  - END //////////////


////////// Curriculum Level //////////////
//object CurriculumLevel extends Enumeration {
//  type CurriculumLevel = Value
//  val EarlyLevel, FirstLevel, SecondLevel, ThirdLevel, FourthLevel = Value
//}


sealed trait CurriculumLevel {
  def value: String
}
object CurriculumLevel{
  implicit def rw: RW[CurriculumLevel] = macroRW
}

case object EarlyLevel extends CurriculumLevel {
  val value = "EarlyLevel"
}

case object FirstLevel extends CurriculumLevel {
  val value = "FirstLevel"
}

case object SecondLevel extends CurriculumLevel {
  val value = "SecondLevel"
}

case object ThirdLevel extends CurriculumLevel {
  val value = "ThirdLevel"
}

case object FourthLevel extends CurriculumLevel {
  val value = "FourthLevel"
}
//
//////// Curriculum Level - END//////////////
//
