package timetoteach.screens

import org.scalajs.dom.Node
import shared.model.classtimetable.{ClassTimetable, SessionBreakdown}

import scalatags.Text
import scalatags.Text.all._

object ClassTimetableScreenHtmlGenerator {

  def generateSubjectButtons(breakdown: SessionBreakdown): List[Text.TypedTag[String]] = {
    val buttons = breakdown.subjectsWithTimeFractionInTwelves.map {
      entry =>
        val subjectCode = entry._1.subject.value
        val sessionType = breakdown.sessionOfTheWeek.valueWithoutDay
        val dayOfWeek = breakdown.sessionOfTheWeek.dayOfTheWeek.value

        if (subjectCode == "subject-empty") {
          button(disabled := true, `class` := "col-" + entry._2 + " rounded subject " + subjectCode,
            attr("data-timetable-session") := sessionType,
            attr("data-day-of-the-week") := dayOfWeek,
            attr("data-subject-code") := subjectCode
          )(entry._1.subject.niceValue)
        } else {
          val startTime = entry._1.timeSlot.startTime.toString
          val smallStartTime = small(`class` := "lesson-start-time align-text-top")(startTime)
          val endTime = entry._1.timeSlot.endTime.toString
          val smallEndTime = small(`class` := "lesson-end-time align-text-top")(endTime)

          button(`class` := "col-" + entry._2 + " rounded subject non-empty-subject " + subjectCode,
            attr("data-timetable-session") := sessionType,
            attr("data-day-of-the-week") := dayOfWeek,
            attr("data-subject-code") := subjectCode,
            attr("data-lesson-start-time") := startTime,
            attr("data-lesson-end-time") := endTime
          )(smallStartTime, smallEndTime, br, p(`class` := "clear-both", entry._1.subject.niceValue))
        }
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

  def createSubjectSummary(subjectCode: String,
                           startTime: String,
                           endTime: String,
                           timetableSession: String,
                           day: String): Text.TypedTag[String] = {
    div(
      p(s"subject: $subjectCode"),
      p(s"startTime: $startTime"),
      p(s"endTime: $endTime"),
      p(s"timetableSession: $timetableSession"),
      p(s"day: $day")
    )
  }

}