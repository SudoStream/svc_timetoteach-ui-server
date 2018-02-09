package potentialmicroservice.planning.dao

import com.google.inject.ImplementedBy
import models.timetoteach.planning.SubjectTermlyPlan
import org.mongodb.scala.Completed

import scala.concurrent.Future

@ImplementedBy(classOf[PlanWriterDaoImpl])
trait PlanWriterDao {

  def saveSubjectTermlyPlan(planToSave: SubjectTermlyPlan): Future[Completed]

}
