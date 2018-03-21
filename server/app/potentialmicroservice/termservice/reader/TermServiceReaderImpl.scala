package potentialmicroservice.termservice.reader
import io.sudostream.timetoteach.messages.systemwide.model.LocalAuthority
import javax.inject.Singleton
import models.timetoteach.term.SchoolTerm

import scala.concurrent.Future

@Singleton
class TermServiceReaderImpl extends TermServiceReader {

  override def currentSchoolTerm(localAuthority: LocalAuthority): Future[Option[SchoolTerm]] = ???

  override def nextSchoolTerm(localAuthority: LocalAuthority): Future[Option[SchoolTerm]] = ???

}
