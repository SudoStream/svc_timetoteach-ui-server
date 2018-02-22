package models.timetoteach.term

import java.time.LocalDate

import models.timetoteach.term.SchoolTermName.SchoolTermName
import play.api.Logger

case class SchoolTerm(
                       schoolYear: SchoolYear,
                       schoolTermName: SchoolTermName,
                       termFirstDay: LocalDate,
                       termLastDay: LocalDate
                     )

case class SchoolYear(calendarYearStart: Int, maybeCalendarYearEnd: Option[Int])
{
  def niceValue: String =
  {
    val maybeEndYear = if (maybeCalendarYearEnd.isDefined) {
      "-" + maybeCalendarYearEnd.get
    } else ""
    calendarYearStart.toString + maybeEndYear
  }
}

object SchoolTermName extends Enumeration
{
  val logger: Logger = Logger

  type SchoolTermName = Value
  val AUTUMN_FIRST_TERM, AUTUMN_SECOND_TERM, WINTER_TERM, SPRING_FIRST_TERM, SPRING_SECOND_TERM, SUMMER_TERM = Value

  def niceValue(schoolTermName: SchoolTermName): String =
  {
    schoolTermName.toString.split("_").map(word => word.toLowerCase.capitalize).mkString(" ")
  }

  def convertToSchoolTermName(termNameString: String): Option[SchoolTermName] =
  {
    termNameString.toUpperCase match {
      case "AUTUMN_FIRST_TERM" => Some(AUTUMN_FIRST_TERM)
      case "AUTUMN_SECOND_TERM" => Some(AUTUMN_SECOND_TERM)
      case "WINTER_TERM" => Some(WINTER_TERM)
      case "SPRING_FIRST_TERM" => Some(SPRING_FIRST_TERM)
      case "SPRING_SECOND_TERM" => Some(SPRING_SECOND_TERM)
      case "SUMMER_TERM" => Some(SUMMER_TERM)
      case other =>
        logger.error(s"Did not recognise school term name '$other'")
        None
    }
  }
}