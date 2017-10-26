package models.timetoteach.classtimetable

import java.time.LocalTime

case class SchoolDayTimes(
                           schoolDayStarts: LocalTime,
                           morningBreakStarts: LocalTime,
                           morningBreakEnds: LocalTime,
                           lunchStarts: LocalTime,
                           lunchEnds: LocalTime,
                           schoolDayEnds: LocalTime
                         ) {

  require(schoolDayStarts.isBefore(morningBreakStarts))
  require(schoolDayStarts.isBefore(morningBreakEnds))
  require(schoolDayStarts.isBefore(lunchStarts))
  require(schoolDayStarts.isBefore(lunchEnds))
  require(schoolDayStarts.isBefore(schoolDayEnds))
  require(morningBreakStarts.isBefore(morningBreakEnds))
  require(morningBreakStarts.isBefore(lunchStarts))
  require(morningBreakStarts.isBefore(lunchEnds))
  require(morningBreakStarts.isBefore(schoolDayEnds))
  require(morningBreakEnds.isBefore(lunchStarts))
  require(morningBreakEnds.isBefore(lunchEnds))
  require(morningBreakEnds.isBefore(schoolDayEnds))
  require(lunchStarts.isBefore(lunchEnds))
  require(lunchStarts.isBefore(schoolDayEnds))
  require(lunchEnds.isBefore(schoolDayEnds))

}