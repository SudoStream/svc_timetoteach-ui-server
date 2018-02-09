package controllers.serviceproxies

import java.time.LocalDate

import com.google.inject.Singleton
import models.timetoteach.term.SchoolTermName.SchoolTermName
import models.timetoteach.term.{SchoolTerm, SchoolTermName, SchoolYear}

@Singleton
class TermServiceProxyImpl extends TermServiceProxy {

  override def currentSchoolTerm(): SchoolTerm = {
    SchoolTerm(
      SchoolYear(2017,Some(2018)),
      SchoolTermName.SPRING_SECOND_TERM,
      LocalDate.of(2018,2,19),
      LocalDate.of(2018,3,29)
    )
  }

  override def nextSchoolTerm(): SchoolTerm = ???

}
