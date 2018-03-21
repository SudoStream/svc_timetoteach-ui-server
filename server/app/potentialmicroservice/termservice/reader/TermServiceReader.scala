package potentialmicroservice.termservice.reader

import com.google.inject.ImplementedBy
import io.sudostream.timetoteach.messages.systemwide.model.LocalAuthority
import models.timetoteach.term.SchoolTerm

import scala.concurrent.Future

@ImplementedBy(classOf[TermServiceReaderImpl])
trait TermServiceReader {

  def currentSchoolTerm(localAuthority: LocalAuthority): Future[Option[SchoolTerm]]

  def nextSchoolTerm(localAuthority: LocalAuthority): Future[Option[SchoolTerm]]

}
