package timetoteach.screens

import shared.model.classtimetable.ClassTimetable
import scalatags.Text.all._

object ClassTimetableScreenHtmlGenerator {
  def generateHtmlForClassTimetable(classTimetable: ClassTimetable): String = {
    val html = classTimetable.allSessionsOfTheWeekInOrderByDay.map {
      sessionBreakdown =>
        div(`class`:="row dayoftheweek-row align-items-center")(
          div(`class`:="col-sm-1 text-secondary timetable-dayofweek-header align-self-center")(),
          div(`class`:="col-sm-3")(
            div(`class`:="row")(

            )
          )
        )
    }

    html.toString()
  }
}