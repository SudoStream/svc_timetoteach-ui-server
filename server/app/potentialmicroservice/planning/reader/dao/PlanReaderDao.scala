package potentialmicroservice.planning.reader.dao

import com.google.inject.ImplementedBy
import duplicate.model.ClassDetails
import duplicate.model.esandos.{CompletedEsAndOsByGroup, CompletedEsAndOsByGroupBySubject, StartedEsAndOsByGroupBySubject}
import duplicate.model.planning.FullWeeklyPlanOfLessons
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning.{CurriculumAreaTermlyPlan, CurriculumPlanProgressForClass, GroupId, TermlyCurriculumSelection}
import models.timetoteach.term.SchoolTerm
import models.timetoteach.{ClassId, TimeToTeachUserId}

import scala.concurrent.Future

@ImplementedBy(classOf[PlanReaderDaoImpl])
trait PlanReaderDao
{

  def currentTermlyCurriculumSelection(
                                        tttUserId: TimeToTeachUserId,
                                        classId: ClassId,
                                        term: SchoolTerm
                                      ): Future[Option[TermlyCurriculumSelection]]

  def currentTermlyCurriculumSelection(
                                        tttUserId: TimeToTeachUserId,
                                        classes: List[ClassId],
                                        term: SchoolTerm
                                      ): Future[Map[ClassId, Option[TermlyCurriculumSelection]]]

  def curriculumPlanProgress(
                              tttUserId: TimeToTeachUserId,
                              classDetails: ClassDetails,
                              planningAreas: List[ScottishCurriculumPlanningArea],
                              term: SchoolTerm
                            ): Future[Option[CurriculumPlanProgressForClass]]

  def curriculumPlanProgressForClasses(
                                        tttUserId: TimeToTeachUserId,
                                        classes: List[ClassDetails],
                                        classIdToPlanningSelection: Map[ClassId, List[ScottishCurriculumPlanningArea]],
                                        term: SchoolTerm
                                      ): Future[Map[duplicate.model.ClassId, Int]]

  def readCurriculumAreaTermlyPlanForGroup(
                                            tttUserId: TimeToTeachUserId,
                                            classId: ClassId,
                                            groupId: GroupId,
                                            planningArea: ScottishCurriculumPlanningArea
                                          ): Future[Option[CurriculumAreaTermlyPlan]]

  def readCurriculumAreaTermlyPlanForClassLevel(
                                                 tttUserId: TimeToTeachUserId,
                                                 classId: ClassId,
                                                 planningArea: ScottishCurriculumPlanningArea
                                               ): Future[Option[CurriculumAreaTermlyPlan]]

  def retrieveFullWeekOfLessons(tttUserId: TimeToTeachUserId, classId: ClassId, mondayDateOfWeekIso: String): Future[FullWeeklyPlanOfLessons]

  def completedEsOsBenchmarks(
                               tttUserId: TimeToTeachUserId,
                               classId: ClassId,
                               mondayDateOfWeekIso: String
                             ): Future[CompletedEsAndOsByGroupBySubject]

  def completedAndStartedEsOsBenchmarks(
                               tttUserId: TimeToTeachUserId,
                               classId: ClassId,
                               mondayDateOfWeekIso: String
                             ): Future[(CompletedEsAndOsByGroupBySubject, StartedEsAndOsByGroupBySubject)]

}
