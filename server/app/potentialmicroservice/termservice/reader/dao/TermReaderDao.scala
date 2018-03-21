package potentialmicroservice.termservice.reader.dao

import com.google.inject.ImplementedBy
import io.sudostream.timetoteach.messages.systemwide.model.LocalAuthority
import models.timetoteach.term.SchoolTerm

import scala.concurrent.Future

@ImplementedBy(classOf[TermReaderDaoImpl])
trait TermReaderDao {
  def currentSchoolTerm(localAuthority: LocalAuthority): Future[Option[SchoolTerm]]
  def nextSchoolTerm(localAuthority: LocalAuthority): Future[Option[SchoolTerm]]
}
