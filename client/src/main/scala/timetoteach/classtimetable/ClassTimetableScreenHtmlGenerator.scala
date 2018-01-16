package timetoteach.classtimetable

import java.time.LocalTime

import org.scalajs.dom.html.{Button, Div}
import shared.model.classtimetable._
import shared.util.LocalTimeUtil

import scala.scalajs.js.Dynamic.global
import scalatags.JsDom
import scalatags.JsDom.all.{p, _}

trait ClassTimetableScreenHtmlGenerator {

  private var removeBehaviourTuple: Option[(WwwSubjectDetail, WwwSessionOfTheWeek)] = None
  def getRemoveBehaviourTuple: Option[(WwwSubjectDetail, WwwSessionOfTheWeek)] = removeBehaviourTuple
  def setRemoveBehaviourTupleToNone(): Unit = removeBehaviourTuple = None

  def getClassTimetable: WWWClassTimetable

  def generateSubjectButtons(breakdown: WwwSessionBreakdown): List[JsDom.TypedTag[Button]] = {
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
            attr("data-lesson-end-time") := endTime,
            attr("data-toggle") := "collapse",
            attr("data-target") := "#subject-summary-in-timetable",
            attr("aria-expanded") := "false",
            attr("aria-controls") := "subject-summary-in-timetable"
          )(smallStartTime, smallEndTime, p(`class` := "clear-both subject-main-header", entry._1.subject.niceValue),
            p(`class` := "subject-additional-info rounded mx-auto text-center", entry._1.lessonAdditionalInfo)
          )
        }
    }
    buttons
  }

  def generateHtmlForClassTimetable(classTimetable: WWWClassTimetable): String = {
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
        ).toString
    }

    html.mkString
  }

  def createSubjectSummary(subjectCode: String,
                           startTime: String,
                           endTime: String,
                           timetableSession: String,
                           day: String,
                           currentAdditionalInfoValue: String
                          ): JsDom.TypedTag[Div] = {

    val dayOfWeek = WwwDayOfWeek(day)
    val session = WwwSession(timetableSession)

    val subjectSummary = div(
      `class` := "collapse mx-auto",
      id := "subject-summary-in-timetable",
      div(`class` := "card-header")(
        h4(`class` := "card-title subject-name-title", WwwSubjectName(subjectCode).niceValue)
      ),
      div(
        `class` := "card card-body",
        label(`for` := "input-for-additional-info", "Additional Info"),
        input(id := "input-for-additional-info", `type` := "text", autofocus := true, value:= currentAdditionalInfoValue)
      ),
      div(
        `class` := "card-footer text-muted",
        button(id := "remove-lesson-from-timetable-button", `class` := "btn btn-outline-info")("Remove Lesson"),
        button(id := "ok-update-for-timetable-button", `class` := "btn btn-outline-success")("Ok")
      )
    )

    val startTimeAsLocalTime = LocalTimeUtil.convertStringTimeToLocalTime(startTime).getOrElse(LocalTime.MIDNIGHT)
    val endTimeAsLocalTime = LocalTimeUtil.convertStringTimeToLocalTime(endTime).getOrElse(LocalTime.MIDNIGHT)

    val subjectDetail = WwwSubjectDetail(
      WwwSubjectName(subjectCode),
      WwwTimeSlot(startTimeAsLocalTime, endTimeAsLocalTime),
      currentAdditionalInfoValue
    )

    WwwSessionOfTheWeek.createSessionOfTheWeek(dayOfWeek, session) match {
      case Some(sessionOfTheWeek) =>
        val proposedRemoveBehaviourTuple = (subjectDetail, sessionOfTheWeek)
        global.console.log(
            s"current removeBehaviourTuple: ${removeBehaviourTuple.toString}\n" +
            s"proposed removeBehaviourTuple: ${proposedRemoveBehaviourTuple.toString}\n"
        )

        removeBehaviourTuple match {
          case Some(currentValueOfBehaviour) =>
            if ( currentValueOfBehaviour != proposedRemoveBehaviourTuple) {
              removeBehaviourTuple = Some(proposedRemoveBehaviourTuple)
            } else {
              removeBehaviourTuple = None
            }
          case None => removeBehaviourTuple = Some(proposedRemoveBehaviourTuple)
        }
      case None => global.console.error(s"Could not create session of week from ${dayOfWeek.value} & ${session.value}")
    }

    subjectSummary
  }


  def addUpdateBehaviour(): Unit = {

  }

}