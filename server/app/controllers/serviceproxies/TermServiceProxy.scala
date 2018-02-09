package controllers.serviceproxies

import com.google.inject.ImplementedBy
import models.timetoteach.term.SchoolTerm

@ImplementedBy(classOf[TermServiceProxyImpl])
trait TermServiceProxy {

  def currentSchoolTerm(): SchoolTerm
  def nextSchoolTerm(): SchoolTerm

}
