package shared.model.classdetail

case class ClassDetails(
                         id: ClassId,
                         className: ClassName,
                         groups: List[Group],
                         classTeachers: List[Teacher]
                       ) {
  if (classTeachers.isEmpty) throw new IllegalArgumentException("Must specify at least one teacher")

}

case class ClassId(id: String)

case class ClassName(name: String)

////

case class Group(groupName: GroupName, groupType: GroupType, groupLevel: CurriculumLevel)

case class GroupName(name: String)

////////// Group Type //////////////
sealed trait GroupType {
  def value: String
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

case class TeacherId(id: String)