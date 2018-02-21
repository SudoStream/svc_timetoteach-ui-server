package utils

import duplicate.model.Group
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumLevel
import io.sudostream.timetoteach.messages.systemwide.model.classes._
import models.timetoteach.School
import play.api.Logger

object ClassDetailsAvroConverter
{

  val logger: Logger.type = Logger

  def convertPickledClassToAvro(classDetails: duplicate.model.ClassDetails): ClassDetails =
  {
    val groups = for {
      groupUnpickled: Group <- classDetails.groups
      groupId = GroupId(groupUnpickled.groupId.id)
      groupType = groupUnpickled.groupType.value.toLowerCase match {
        case "maths" | "mathematics" => GroupType.MATHEMATICS
        case "reading" => GroupType.READING
        case "writing" => GroupType.WRITING
        case "spelling" => GroupType.SPELLING
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
      groupDescription = GroupDescription(groupUnpickled.groupDescription.name)
    } yield ClassGroupsWrapper(ClassGroup(groupId, groupName, groupDescription, groupType, groupLevel))

    ClassDetails(
      ClassId(classDetails.id.id),
      SchoolId(classDetails.schoolDetails.id),
      ClassName(classDetails.className.name),
      ClassDescription(classDetails.classDescription.name),
      classDetails.classTeachersWithWriteAccess,
      groups
    )
  }

  def convertAvroGroupsToModel(classGroups: List[ClassGroupsWrapper]): List[duplicate.model.Group] =
  {
    for {
      groupWrapper <- classGroups
      groupAvro = groupWrapper.group
      groupType = groupAvro.groupType match {
        case GroupType.SPELLING => duplicate.model.SpellingGroupType
        case GroupType.WRITING => duplicate.model.WritingGroupType
        case GroupType.READING => duplicate.model.ReadingGroupType
        case GroupType.MATHEMATICS => duplicate.model.MathsGroupType
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
      duplicate.model.GroupDescription(groupAvro.groupDescription.value),
      groupType,
      groupLevel
    )
  }

  def convertAvroClassDetailsCollectionToModel(classDetailsCollection: ClassDetailsCollection,
                                               schools: Seq[School]): List[duplicate.model.ClassDetails] =
  {
    val classDetailsListAvroStyle = classDetailsCollection.values.map { classDetailsWrapper => classDetailsWrapper.classDetails }

    logger.debug(s"classDetailsListAvroStyle size = ${classDetailsListAvroStyle.size}")
    logger.debug(s"classDetailsListAvroStyle = ${classDetailsListAvroStyle.toString}")
    logger.debug(s"schools size = ${schools.size}")

    for {
      classDetails <- classDetailsListAvroStyle
      classId = classDetails.classId.value
      schoolId = classDetails.schoolId.value
      className = classDetails.className.value
      classDescription = classDetails.classDescription.value
      groups = convertAvroGroupsToModel(classDetails.classGroups)
      schoolDetailsForClassSeq = schools.filter(theSchool => theSchool.id == schoolId)
      if schoolDetailsForClassSeq.nonEmpty
      schoolDetailsForClass = schoolDetailsForClassSeq.head
    } yield duplicate.model.ClassDetails(
      duplicate.model.ClassId(classId),
      duplicate.model.SchoolDetails(
        schoolDetailsForClass.id,
        schoolDetailsForClass.name,
        schoolDetailsForClass.address,
        schoolDetailsForClass.postCode,
        schoolDetailsForClass.telephone,
        schoolDetailsForClass.localAuthority.value.toString,
        schoolDetailsForClass.country.value.toString),
      duplicate.model.ClassName(className),
      duplicate.model.ClassDescription(classDescription),
      groups,
      classDetails.teachersWithWriteAccess,
      Map()
    )
  }


}
