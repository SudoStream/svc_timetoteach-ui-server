package controllers.serviceproxies

import duplicate.model
import duplicate.model.ClassDetails
import duplicate.model.esandos.{CompletedEsAndOsByGroup, CompletedEsAndOsByGroupBySubject}
import duplicate.model.planning.FullWeeklyPlanOfLessons
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import javax.inject.{Inject, Singleton}
import models.timetoteach.planning.{CurriculumAreaTermlyPlan, CurriculumPlanProgressForClass, GroupId, TermlyCurriculumSelection}
import models.timetoteach.term.SchoolTerm
import models.timetoteach.{ClassId, TimeToTeachUserId}
import play.api.Logger
import potentialmicroservice.planning.reader.PlanningReaderService
import utils.SchoolConverter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

@Singleton
class PlanningReaderServiceProxyImpl @Inject()(planningReaderService: PlanningReaderService,
                                               classTimetableReaderProxy: ClassTimetableReaderServiceProxyImpl,
                                               termService: TermServiceProxy) extends PlanningReaderServiceProxy {
  private val logger: Logger = Logger

  override def currentTermlyCurriculumSelection(tttUserId: TimeToTeachUserId, classId: ClassId, term: SchoolTerm): Future[Option[TermlyCurriculumSelection]] = {
    planningReaderService.currentTermlyCurriculumSelection(tttUserId, classId, term)
  }

  override def curriculumPlanProgress(tttUserId: TimeToTeachUserId,
                                      classDetails: ClassDetails,
                                      maybeCurrentTermlyCurriculumSelection: Option[TermlyCurriculumSelection]
                                     ): Future[Option[CurriculumPlanProgressForClass]] = {
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
                                                    planningArea: ScottishCurriculumPlanningArea): Future[Option[CurriculumAreaTermlyPlan]] = {
    planningReaderService.readCurriculumAreaTermlyPlanForGroup(tttUserId, classId, groupId, planningArea)
  }


  override def readCurriculumAreaTermlyPlanForClassLevel(tttUserId: TimeToTeachUserId, classId: ClassId, planningArea: ScottishCurriculumPlanningArea): Future[Option[CurriculumAreaTermlyPlan]] = {
    planningReaderService.readCurriculumAreaTermlyPlanForClassLevel(tttUserId, classId, planningArea)
  }

  override def allClassTermlyPlans(tttUserId: TimeToTeachUserId,
                                   classDetails: ClassDetails,
                                   planningAreas: List[ScottishCurriculumPlanningArea]): Future[List[CurriculumAreaTermlyPlan]] = {
    //TODO: Performance - this can be imporoved to be one DB call rather than a db call for every curriculum area
    logger.debug(s"readCurriculumAreaTermlyPlanAtClassLevelForPlanningAreas() - " +
      s"${tttUserId.value} ${classDetails.id.id} ${planningAreas.toString()}")

    val eventualMaybePlansAtClassLevel = extractPlansForClassLevelAreas(tttUserId, ClassId(classDetails.id.id), planningAreas)
    val eventualMaybePlansAtGroupLevel = extractPlansForGroupLevelAreas(tttUserId, classDetails, planningAreas)

    /////////////

    val eventualMaybeTermlyPlans = Future.sequence(eventualMaybePlansAtClassLevel ++ eventualMaybePlansAtGroupLevel)

    for {
      maybeTermlyPlans <- eventualMaybeTermlyPlans
    } yield maybeTermlyPlans.flatten
  }

  private def extractPlansForGroupLevelAreas(tttUserId: TimeToTeachUserId,
                                             classDetails: ClassDetails,
                                             planningAreas: List[ScottishCurriculumPlanningArea])
  : List[Future[Option[CurriculumAreaTermlyPlan]]] = {
    val eventualMaybeCurriculumTermlyPlansAtGroupLevel: List[Future[Option[CurriculumAreaTermlyPlan]]] =
      for {
        planningArea <- planningAreas
        group <- classDetails.groups
        eventualMaybeCurriculumTermlyPlan = readCurriculumAreaTermlyPlanForGroup(
          tttUserId,
          ClassId(classDetails.id.id),
          GroupId(group.groupId.id),
          planningArea)
      } yield eventualMaybeCurriculumTermlyPlan

    val eventualMaybeCurriculumTermlyPlansWithHandledErrors = eventualMaybeCurriculumTermlyPlansAtGroupLevel.map { elem =>
      elem.recover {
        case NonFatal(e) =>
          logger.warn(s"Had an issue processing plan progress: ${e.getMessage}")
          None
      }
    }
    eventualMaybeCurriculumTermlyPlansWithHandledErrors
  }
  private def extractPlansForClassLevelAreas(tttUserId: TimeToTeachUserId,
                                             classId: ClassId,
                                             planningAreas: List[ScottishCurriculumPlanningArea]): List[Future[Option[CurriculumAreaTermlyPlan]]] = {
    val eventualMaybeCurriculumTermlyPlansAtClassLevel: List[Future[Option[CurriculumAreaTermlyPlan]]] =
      for {
        planningArea <- planningAreas
        eventualMaybeCurriculumTermlyPlan = readCurriculumAreaTermlyPlanForClassLevel(tttUserId, classId, planningArea)
      } yield eventualMaybeCurriculumTermlyPlan

    val eventualMaybeCurriculumTermlyPlansWithHandledErrors = eventualMaybeCurriculumTermlyPlansAtClassLevel.map { elem =>
      elem.recover {
        case NonFatal(e) =>
          logger.warn(s"Had an issue processing plan progress: ${e.getMessage}")
          None
      }
    }
    eventualMaybeCurriculumTermlyPlansWithHandledErrors
  }
  private def convertToPlanningAreas(classIdToTermlyCurriculumSelection: Map[ClassId, Option[TermlyCurriculumSelection]]):
  Map[ClassId, List[ScottishCurriculumPlanningArea]] = {
    logger.debug(s"incoming map to convertToPlanningAreas() is ${classIdToTermlyCurriculumSelection.toString}")

    def safeConvert(maybeSelection: Option[TermlyCurriculumSelection]): List[ScottishCurriculumPlanningArea] = {
      maybeSelection match {
        case Some(termlySelection) => termlySelection.planningAreas
        case None => Nil
      }
    }

    classIdToTermlyCurriculumSelection.toList.map(entryTuple => (entryTuple._1, safeConvert(entryTuple._2))).toMap
  }

  override def curriculumPlanProgressForClasses(tttUserId: TimeToTeachUserId, classes: List[ClassDetails], term: SchoolTerm): Future[Map[model.ClassId, Int]] = {
    val classIds = classes.map(aClass => models.timetoteach.ClassId(aClass.id.id))
    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(tttUserId)

    for {
      classes <- eventualClasses
      futureMaybeSchoolTerm = termService.currentSchoolTerm(SchoolConverter.convertLocalAuthorityStringToAvroVersion(classes.head.schoolDetails.localAuthority))
      maybeSchoolTerm <- futureMaybeSchoolTerm
      if maybeSchoolTerm.isDefined

      classIdToTermlyCurriculumSelection <- planningReaderService.currentTermlyCurriculumSelection(
        tttUserId,
        classIds,
        maybeSchoolTerm.get
      )
      classIdToPlanningAreas = convertToPlanningAreas(classIdToTermlyCurriculumSelection)
      classIdToProgressPercent <- planningReaderService.curriculumPlanProgressForClasses(tttUserId, classes, classIdToPlanningAreas, term)
    } yield classIdToProgressPercent
  }


  //////////////

  override def retrieveFullWeekOfLessons(
                                          tttUserId: TimeToTeachUserId,
                                          classId: ClassId,
                                          mondayDateOfWeekIso: String): Future[FullWeeklyPlanOfLessons] = {
    planningReaderService.retrieveFullWeekOfLessons(tttUserId, classId, mondayDateOfWeekIso)
  }

  override def completedEsOsBenchmarks(
                                        tttUserId: TimeToTeachUserId,
                                        classId: ClassId
                                      ): Future[CompletedEsAndOsByGroupBySubject] = {
    planningReaderService.completedEsOsBenchmarks(tttUserId, classId)
  }

}
