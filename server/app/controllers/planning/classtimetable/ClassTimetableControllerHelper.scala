package controllers.planning.classtimetable

import duplicate.model._
import play.api.Logger

trait ClassTimetableControllerHelper
{
  private val logger: Logger.type = Logger

  def mergeEditedClassWithCurrent(currentClass: ClassDetails, editedClass: ClassDetails): ClassDetails =
  {
    val mergedClass = ClassDetails(
      id = currentClass.id,
      schoolDetails = currentClass.schoolDetails,
      className = editedClass.className,
      classDescription = editedClass.classDescription,
      groups = editedClass.groups,
      classTeachersWithWriteAccess = currentClass.classTeachersWithWriteAccess,
      subjectToMaybeGroupMap = currentClass.subjectToMaybeGroupMap
    )

    logger.debug(s"Merged Class = ${mergedClass.toString}")
    mergedClass
  }
}
