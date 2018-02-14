package duplicate.model

import upickle.default.{macroRW, ReadWriter => RW}

case class ClassDetails(
                         id: ClassId,
                         schoolDetails: SchoolDetails,
                         className: ClassName,
                         classDescription: ClassDescription,
                         groups: List[Group],
                         classTeachersWithWriteAccess: List[String],
                         subjectToMaybeGroupMap: Map[SubjectModel, Option[Group]]
                       ) {
  if (classTeachersWithWriteAccess.isEmpty) throw new IllegalArgumentException(
    "Must have at least 1 teacher with write access"
  )
}
object ClassDetails{
  implicit def rw: RW[ClassDetails] = macroRW
}

case class SchoolDetails(
                          id: String,
                          name: String,
                          address: String,
                          postCode: String,
                          telephone: String,
                          localAuthority: String,
                          country: String
                        )

object SchoolDetails{
  implicit def rw: RW[SchoolDetails] = macroRW
}

case class ClassId(id: String)
object ClassId{
  implicit def rw: RW[ClassId] = macroRW
}

case class ClassName(name: String)
object ClassName{
  implicit def rw: RW[ClassName] = macroRW
}

case class ClassDescription(name: String)
object ClassDescription{
  implicit def rw: RW[ClassDescription] = macroRW
}

////

case class Group(groupId: GroupId, groupName: GroupName, groupDescription: GroupDescription, groupType: GroupType, groupLevel: CurriculumLevel)
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

case class GroupDescription(name: String)
object GroupDescription{
  implicit def rw: RW[GroupDescription] = macroRW
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

case object ReadingGroupType extends GroupType {
  val value = "Reading"
}

case object WritingGroupType extends GroupType {
  val value = "Writing"
}

case object SpellingGroupType extends GroupType {
  val value = "Spelling"
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
