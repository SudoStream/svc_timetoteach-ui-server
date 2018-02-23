package potentialmicroservice.planning.reader.dao

import com.google.inject.ImplementedBy
import duplicate.model.ClassDetails
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

}
