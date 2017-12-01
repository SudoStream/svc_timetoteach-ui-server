package shared.model.classtimetable

import java.time.LocalTime

case class WwwTimeSlot(startTime: LocalTime,
                       endTime: LocalTime
                   ) {
  require(startTime.isBefore(endTime))
}
