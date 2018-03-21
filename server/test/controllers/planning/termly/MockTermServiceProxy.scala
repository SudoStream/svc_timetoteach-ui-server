package controllers.planning.termly

import java.time.LocalDate

import controllers.serviceproxies.TermServiceProxy
import io.sudostream.timetoteach.messages.systemwide.model.LocalAuthority
import models.timetoteach.term.{SchoolTerm, SchoolTermName, SchoolYear}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MockTermServiceProxy extends TermServiceProxy {
  override def currentSchoolTerm(localAuthority: LocalAuthority): Future[Option[SchoolTerm]] = {
    Future {
      Some(
        SchoolTerm(
          SchoolYear(2017, Some(2018)),
          SchoolTermName.SPRING_SECOND_TERM,
          LocalDate.of(2018, 2, 19),
          LocalDate.of(2018, 3, 29)
        )
      )
    }
  }

  override def nextSchoolTerm(localAuthority: LocalAuthority): Future[Option[SchoolTerm]] = ???
}
