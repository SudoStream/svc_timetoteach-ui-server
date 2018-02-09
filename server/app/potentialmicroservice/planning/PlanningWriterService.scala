package potentialmicroservice.planning

import com.google.inject.ImplementedBy
import models.timetoteach.planning.SubjectTermlyPlan
import org.mongodb.scala.Completed

import scala.concurrent.Future

@ImplementedBy(classOf[PlanningWriterServiceImpl])
trait PlanningWriterService {

  def saveSubjectTermlyPlan(planToSave: SubjectTermlyPlan): Future[Completed]

}
