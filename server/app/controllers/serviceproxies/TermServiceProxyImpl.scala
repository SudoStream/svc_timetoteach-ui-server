package controllers.serviceproxies

import com.google.inject.Singleton
import io.sudostream.timetoteach.messages.systemwide.model.LocalAuthority
import javax.inject.Inject
import models.timetoteach.term.SchoolTerm
import potentialmicroservice.termservice.reader.dao.TermReaderDao

import scala.concurrent.Future

@Singleton
class TermServiceProxyImpl @Inject()(termReaderDao: TermReaderDao) extends TermServiceProxy {

  override def currentSchoolTerm(localAuthority: LocalAuthority): Future[Option[SchoolTerm]] = {
    termReaderDao.currentSchoolTerm(localAuthority)
  }

  override def nextSchoolTerm(localAuthority: LocalAuthority): Future[Option[SchoolTerm]] = ???

}
