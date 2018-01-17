package shared.model.classdetail
import upickle.default.{ReadWriter => RW, macroRW}

case class ClassDetails(
                         id: ClassId,
                         className: ClassName,
                         groups: List[Group],
                         classTeachers: List[Teacher]
                       ) {
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

case class Group(groupId: GroupId, groupName: GroupName, groupType: GroupType, groupLevel: CurriculumLevel)
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
sealed trait CurriculumLevel {
  def value: String
}
object CurriculumLevel {
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

////////// Curriculum Level - END//////////////

case class Teacher(maybeId: Option[TeacherId], firstName: String, surname: String)
object Teacher{
  implicit def rw: RW[Teacher] = macroRW
}

case class TeacherId(id: String)
object TeacherId{
  implicit def rw: RW[TeacherId] = macroRW
}
