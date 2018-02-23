package controllers.serviceproxies

import duplicate.model
import duplicate.model.ClassDetails
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import javax.inject.{Inject, Singleton}
import models.timetoteach.planning.{CurriculumAreaTermlyPlan, CurriculumPlanProgressForClass, GroupId, TermlyCurriculumSelection}
import models.timetoteach.term.SchoolTerm
import models.timetoteach.{ClassId, TimeToTeachUserId}
import potentialmicroservice.planning.reader.PlanningReaderService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PlanningReaderServiceProxyImpl @Inject()(planningReaderService: PlanningReaderService, termService: TermServiceProxy) extends PlanningReaderServiceProxy
{

  override def currentTermlyCurriculumSelection(tttUserId: TimeToTeachUserId, classId: ClassId, term: SchoolTerm): Future[Option[TermlyCurriculumSelection]] =
  {
    planningReaderService.currentTermlyCurriculumSelection(tttUserId, classId, term)
  }

  override def curriculumPlanProgress(tttUserId: TimeToTeachUserId,
                                      classDetails: ClassDetails,
                                      maybeCurrentTermlyCurriculumSelection: Option[TermlyCurriculumSelection]
                                     ): Future[Option[CurriculumPlanProgressForClass]] =
  {
    maybeCurrentTermlyCurriculumSelection match {
      case Some(currentTermlyCurriculumSelection) =>
        planningReaderService.curriculumPlanProgress(
          tttUserId,
          classDetails,
          currentTermlyCurriculumSelection.planningAreas,
          currentTermlyCurriculumSelection.schoolTerm)
      case None => Future {
        None
      }
    }
  }

  override def readCurriculumAreaTermlyPlanForGroup(tttUserId: TimeToTeachUserId,
                                                    classId: ClassId,
                                                    groupId: GroupId,
                                                    planningArea: ScottishCurriculumPlanningArea): Future[Option[CurriculumAreaTermlyPlan]] =
  {
    planningReaderService.readCurriculumAreaTermlyPlanForGroup(tttUserId, classId, groupId, planningArea)
  }


  override def readCurriculumAreaTermlyPlanForClassLevel(tttUserId: TimeToTeachUserId, classId: ClassId, planningArea: ScottishCurriculumPlanningArea): Future[Option[CurriculumAreaTermlyPlan]] =
  {
    planningReaderService.readCurriculumAreaTermlyPlanForClassLevel(tttUserId, classId, planningArea)
  }

  private def convertToPlanningAreas(classIdToTermlyCurriculumSelection: Map[ClassId, Option[TermlyCurriculumSelection]]):
  Map[ClassId, List[ScottishCurriculumPlanningArea]] =
  {
    def safeConvert(maybeSelection: Option[TermlyCurriculumSelection]): List[ScottishCurriculumPlanningArea] =
    {
      maybeSelection match {
        case Some(termlySelection) => termlySelection.planningAreas
        case None => Nil
      }
    }

    classIdToTermlyCurriculumSelection.toList.map(entryTuple => (entryTuple._1, safeConvert(entryTuple._2))).toMap
  }

  override def curriculumPlanProgressForClasses(tttUserId: TimeToTeachUserId, classes: List[ClassDetails], term: SchoolTerm): Future[Map[model.ClassId, Int]] =
  {
    val classIds = classes.map(aClass => models.timetoteach.ClassId(aClass.id.id))

    for {
      classIdToTermlyCurriculumSelection <- planningReaderService.currentTermlyCurriculumSelection(
        tttUserId,
        classIds,
        termService.currentSchoolTerm()
      )
      classIdToPlanningAreas = convertToPlanningAreas(classIdToTermlyCurriculumSelection)
      classIdToProgressPercent <- planningReaderService.curriculumPlanProgressForClasses(tttUserId, classes, classIdToPlanningAreas, term)
    } yield classIdToProgressPercent
  }

}
