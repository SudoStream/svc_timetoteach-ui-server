package shared.model.classtimetable

import java.time.LocalTime

case class TimeSlot(startTime: LocalTime,
                    endTime: LocalTime
                   ) {
  require(startTime.isBefore(endTime))
}
