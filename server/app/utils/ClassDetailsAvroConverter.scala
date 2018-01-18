package utils

import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumLevel
import io.sudostream.timetoteach.messages.systemwide.model.classes._

object ClassDetailsAvroConverter {

  def convertPickledClassToAvro(classDetails: duplicate.model.ClassDetails): ClassDetails = {
    val groups = for {
      groupUnpickled <- classDetails.groups
      groupId = GroupId(groupUnpickled.groupId.id)
      groupType = groupUnpickled.groupType.value match {
        case "Maths" => GroupType.MATHS
        case "Literacy" => GroupType.LITERACY
        case _ => GroupType.OTHER
      }
      groupLevel = groupUnpickled.groupLevel.value match {
        case "EarlyLevel" => ScottishCurriculumLevel.EARLY
        case "FirstLevel" => ScottishCurriculumLevel.FIRST
        case "SecondLevel" => ScottishCurriculumLevel.SECOND
        case "ThirdLevel" => ScottishCurriculumLevel.THIRD
        case "FourthLevel" => ScottishCurriculumLevel.FOURTH
      }
      groupName = GroupName(groupUnpickled.groupName.name)
    } yield ClassGroupsWrapper(ClassGroup(groupId, groupName, groupType, groupLevel))

    ClassDetails(
      ClassId(classDetails.id.id),
      ClassName(classDetails.className.name),
      classDetails.classTeachersWithWriteAccess,
      groups
    )
  }

}