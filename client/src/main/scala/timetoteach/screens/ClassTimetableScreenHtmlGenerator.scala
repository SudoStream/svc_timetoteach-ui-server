package timetoteach.screens

import shared.model.classtimetable.{ClassTimetable, SessionBreakdown}

import scalatags.Text
import scalatags.Text.all._

object ClassTimetableScreenHtmlGenerator {

  def generateSubjectButtons(breakdown: SessionBreakdown): List[Text.TypedTag[String]] = {
    val buttons = breakdown.subjectsWithTimeFractionInTwelves.map {
      entry =>
        val sessionType =  breakdown.sessionOfTheWeek.valueWithoutDay
        val dayOfWeek = breakdown.sessionOfTheWeek.dayOfTheWeek.value
        button(`class` := s"col-${entry._2} rounded subject ${entry._1.subject.value} " +
          s"data-timetable-session='$sessionType' data-day-of-the-week='$dayOfWeek'")(entry._1.subject.niceValue)
    }
    buttons
  }


  def generateHtmlForClassTimetable(classTimetable: ClassTimetable): String = {
    val sortedDays = classTimetable.allSessionsOfTheWeekInOrderByDay.keys.toList.sortBy(day => day.ordinalNumber)
    val html = sortedDays.map {
      day =>
        val dayOfTheWeekRowContainer = div(`class` := "row dayoftheweek-row align-items-center")
        val dayOfTheWeekRow = div(`class` := "col-sm-1 text-secondary timetable-dayofweek-header align-self-center")(day.shortValue)
        val sessionContainer = div(`class` := "col-sm-3")
        val sessionRow = div(`class` := "row")
        val break = div(`class` := "col-sm-1 small-column")

        val sessionBreakdowns = classTimetable.allSessionsOfTheWeekInOrderByDay.getOrElse(day, Nil)
        val subjectButtonsForEarlyMorning = generateSubjectButtons(sessionBreakdowns.head)
        val subjectButtonsForLateMorning = generateSubjectButtons(sessionBreakdowns(1))
        val subjectButtonsForAfternoon = generateSubjectButtons(sessionBreakdowns(2))

        dayOfTheWeekRowContainer(
          dayOfTheWeekRow,
          sessionContainer(
            sessionRow(
              subjectButtonsForEarlyMorning
            )
          ),
          break,
          sessionContainer(
            sessionRow(
              subjectButtonsForLateMorning)
          ),
          break,
          sessionContainer(
            sessionRow(
              subjectButtonsForAfternoon
            )
          )
        ).render
    }

    html.mkString
  }


}