package models.timetoteach.term

import java.time.LocalDate

import models.timetoteach.term.SchoolTermName.SchoolTermName

case class SchoolTerm(
                       schoolYear: SchoolYear,
                       termType: SchoolTermName,
                       termFirstDay: LocalDate,
                       termLastDay: LocalDate
                     )

case class SchoolYear(calendarYearStart: Int, maybeCalendarYearEnd: Option[Int])

object SchoolTermName extends Enumeration {
  type SchoolTermName = Value
  val AUTUMN_FIRST_TERM, AUTUMN_SECOND_TERM, WINTER_TERM, SPRING_FIRST_TERM, SPRING_SECOND_TERM, SUMMER_TERM = Value

  def niceValue(schoolTermName: SchoolTermName) : String = {
    schoolTermName.toString.split("_").map(word => word.toLowerCase.capitalize).mkString(" ")
  }
}