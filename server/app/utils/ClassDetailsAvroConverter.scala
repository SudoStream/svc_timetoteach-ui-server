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

  def convertAvroGroupsToModel(classGroups: List[ClassGroupsWrapper]): List[duplicate.model.Group] = {
    for {
      groupWrapper <- classGroups
      groupAvro = groupWrapper.group
      groupType = groupAvro.groupType match {
        case GroupType.LITERACY => duplicate.model.LiteracyGroupType
        case GroupType.MATHS => duplicate.model.MathsGroupType
        case _ => duplicate.model.OtherGroupType
      }
      groupLevel = groupAvro.groupLevel match {
        case ScottishCurriculumLevel.EARLY => duplicate.model.EarlyLevel
        case ScottishCurriculumLevel.FIRST => duplicate.model.FirstLevel
        case ScottishCurriculumLevel.SECOND => duplicate.model.SecondLevel
        case ScottishCurriculumLevel.THIRD => duplicate.model.ThirdLevel
        case ScottishCurriculumLevel.FOURTH => duplicate.model.FourthLevel
      }
    } yield duplicate.model.Group(
      duplicate.model.GroupId(groupAvro.groupId.value),
      duplicate.model.GroupName(groupAvro.groupName.value),
      groupType,
      groupLevel
    )
  }

  def convertAvroClassDetailsCollectionToModel(classDetailsCollection: ClassDetailsCollection): List[duplicate.model.ClassDetails] = {
    val classDetailsListAvroStyle = classDetailsCollection.values.map { classDetailsWrapper => classDetailsWrapper.classDetails }

    for {
      classDetails <- classDetailsListAvroStyle
      classId = classDetails.classId.value
      className = classDetails.className.value
      groups = convertAvroGroupsToModel(classDetails.classGroups)
    } yield duplicate.model.ClassDetails(
      duplicate.model.ClassId(classId),
      duplicate.model.ClassName(className),
      groups,
      classDetails.teachersWithWriteAccess
    )
  }


}
