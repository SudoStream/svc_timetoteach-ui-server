package potentialmicroservice.planning.reader

import duplicate.model
import duplicate.model.ClassDetails
import duplicate.model.esandos.{CompletedEsAndOsByGroup, CompletedEsAndOsByGroupBySubject, StartedEsAndOsByGroupBySubject}
import duplicate.model.planning.FullWeeklyPlanOfLessons
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import javax.inject.{Inject, Singleton}
import models.timetoteach.planning.{CurriculumAreaTermlyPlan, CurriculumPlanProgressForClass, GroupId, TermlyCurriculumSelection}
import models.timetoteach.term.SchoolTerm
import models.timetoteach.{ClassId, TimeToTeachUserId}
import potentialmicroservice.planning.reader.dao.PlanReaderDao

import scala.concurrent.Future

@Singleton
class PlanningReaderServiceImpl @Inject()(planningReaderDao: PlanReaderDao) extends PlanningReaderService {

  override def currentTermlyCurriculumSelection(tttUserId: TimeToTeachUserId,
                                                classId: ClassId,
                                                term: SchoolTerm): Future[Option[TermlyCurriculumSelection]] = {
    planningReaderDao.currentTermlyCurriculumSelection(tttUserId, classId, term)
  }

  override def currentTermlyCurriculumSelection(tttUserId: TimeToTeachUserId,
                                                classIds: List[ClassId],
                                                term: SchoolTerm): Future[Map[ClassId, Option[TermlyCurriculumSelection]]] = {
    planningReaderDao.currentTermlyCurriculumSelection(tttUserId, classIds, term)
  }

  override def curriculumPlanProgress(tttUserId: TimeToTeachUserId,
                                      classDetails: ClassDetails,
                                      planningAreas: List[ScottishCurriculumPlanningArea],
                                      term: SchoolTerm): Future[Option[CurriculumPlanProgressForClass]] = {
    planningReaderDao.curriculumPlanProgress(tttUserId, classDetails, planningAreas, term)
  }


  override def readCurriculumAreaTermlyPlanForGroup(tttUserId: TimeToTeachUserId,
                                                    classId: ClassId,
                                                    groupId: GroupId,
                                                    planningArea: ScottishCurriculumPlanningArea): Future[Option[CurriculumAreaTermlyPlan]] = {
    planningReaderDao.readCurriculumAreaTermlyPlanForGroup(tttUserId, classId, groupId, planningArea)
  }


  override def readCurriculumAreaTermlyPlanForClassLevel(tttUserId: TimeToTeachUserId, classId: ClassId, planningArea: ScottishCurriculumPlanningArea): Future[Option[CurriculumAreaTermlyPlan]] = {
    planningReaderDao.readCurriculumAreaTermlyPlanForClassLevel(tttUserId, classId, planningArea)
  }

  override def curriculumPlanProgressForClasses(tttUserId: TimeToTeachUserId, classes: List[ClassDetails], classIdToPlanningSelection: Map[ClassId, List[ScottishCurriculumPlanningArea]], term: SchoolTerm): Future[Map[model.ClassId, Int]] = {
    planningReaderDao.curriculumPlanProgressForClasses(tttUserId, classes, classIdToPlanningSelection, term)
  }

  override def retrieveFullWeekOfLessons(tttUserId: TimeToTeachUserId, classId: ClassId, mondayDateOfWeekIso: String): Future[FullWeeklyPlanOfLessons] = {
    planningReaderDao.retrieveFullWeekOfLessons(tttUserId, classId, mondayDateOfWeekIso)
  }

  override def completedEsOsBenchmarks(
                                        tttUserId: TimeToTeachUserId,
                                        classId: ClassId,
                                        mondayDateOfWeekIso: String
                                      ): Future[CompletedEsAndOsByGroupBySubject] = {
    planningReaderDao.completedEsOsBenchmarks(tttUserId, classId, mondayDateOfWeekIso)
  }

  override def completedAndStartedEsOsBenchmarks(
                                                  tttUserId: TimeToTeachUserId,
                                                  classId: ClassId,
                                                  mondayDateOfWeekIso: String
                                                ): Future[(CompletedEsAndOsByGroupBySubject, StartedEsAndOsByGroupBySubject)] = {
    planningReaderDao.completedAndStartedEsOsBenchmarks(tttUserId, classId, mondayDateOfWeekIso)
  }

}
