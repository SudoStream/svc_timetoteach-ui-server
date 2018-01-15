package shared.model.classdetail

import shared.model.classdetail.CurriculumLevel.CurriculumLevel
import shared.model.classdetail.GroupType.GroupType

case class ScottishClassDetails(
                                 id: String,
                                 name: String,
                                 groups: List[Group],
                                 classTeachers: List[Teacher]
                               )

case class Teacher(maybeId: Option[String], firstName: String, surname: String)

case class Group(groupName: String, groupType: GroupType, groupLevel: CurriculumLevel)

object GroupType extends Enumeration {
  type GroupType = Value
  val Maths, Literacy, Other = Value
}

object CurriculumLevel extends Enumeration {
  type CurriculumLevel = Value
  val Early, First, Second, Third, Fourth = Value
}


