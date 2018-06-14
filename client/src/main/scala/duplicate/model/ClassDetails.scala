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

  def findGroupName(groupIdToSearch: String): String = {
    val filteredGroups = groups.filter(elem => elem.groupId.id == groupIdToSearch)
    filteredGroups.headOption match {
      case Some(group) => group.groupName.name
      case None => ""
    }
  }

  def getSubjectGroups(subjectToSearchFor: String): List[Group] = {
    val groupTypeToSearchFor = GroupType.createGroupTypeFromName(subjectToSearchFor)
    groups.filter(group => group.groupType == groupTypeToSearchFor)
  }
}

object ClassDetails {
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

object SchoolDetails {
  implicit def rw: RW[SchoolDetails] = macroRW
}

case class ClassId(id: String)

object ClassId {
  implicit def rw: RW[ClassId] = macroRW
}

case class ClassName(name: String)

object ClassName {
  implicit def rw: RW[ClassName] = macroRW
}

case class ClassDescription(name: String)

object ClassDescription {
  implicit def rw: RW[ClassDescription] = macroRW
}

////

case class Group(groupId: GroupId, groupName: GroupName, groupDescription: GroupDescription, groupType: GroupType, groupLevel: CurriculumLevel)

object Group {
  implicit def rw: RW[Group] = macroRW
}

case class GroupId(id: String)

object GroupId {
  implicit def rw: RW[GroupId] = macroRW
}


case class GroupName(name: String)

object GroupName {
  implicit def rw: RW[GroupName] = macroRW
}

case class GroupDescription(name: String)

object GroupDescription {
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

object GroupType {
  implicit def rw: RW[GroupType] = macroRW

  def createGroupTypeFromName(name: String): GroupType = {
    val nameLowerCase = name.toLowerCase
    if (nameLowerCase.contains("math")) MathsGroupType
    else if (nameLowerCase.contains("reading")) ReadingGroupType
    else if (nameLowerCase.contains("writing")) WritingGroupType
    else if (nameLowerCase.contains("spelling")) SpellingGroupType
    else OtherGroupType
  }
}

case object MathsGroupType extends GroupType {
  val value = "Mathematics"
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


sealed trait CurriculumLevel extends Ordered[CurriculumLevel] {
  def value: String
  def order: Int

  def compare(that: CurriculumLevel): Int = {
    this.order compareTo that.order
  }

}

object CurriculumLevel {
  implicit def rw: RW[CurriculumLevel] = macroRW

  def createCurriculumLevelFromEAndOCode(eAndOCode: String): CurriculumLevel = {
    if (!eAndOCode.contains(" ")) {
      EarlyLevel
    } else {
      val code2ndPart = eAndOCode.split(" ")(1)
      val levelAsString = code2ndPart.split("-")(0)
      levelAsString match {
        case "0" => EarlyLevel
        case "1" => FirstLevel
        case "2" => SecondLevel
        case "3" => ThirdLevel
        case "4" => FourthLevel
        case _ => EarlyLevel
      }
    }
  }

}

case object EarlyLevel extends CurriculumLevel {
  val value = "EarlyLevel"
  val order = 1
}

case object FirstLevel extends CurriculumLevel {
  val value = "FirstLevel"
  val order = 2
}

case object SecondLevel extends CurriculumLevel {
  val value = "SecondLevel"
  val order = 3
}

case object ThirdLevel extends CurriculumLevel {
  val value = "ThirdLevel"
  val order = 4
}

case object FourthLevel extends CurriculumLevel {
  val value = "FourthLevel"
  val order = 5
}

//
//////// Curriculum Level - END//////////////
//
