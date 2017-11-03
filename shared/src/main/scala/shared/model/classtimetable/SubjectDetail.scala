package shared.model.classtimetable

import java.time.LocalTime

case class SubjectDetail(
                          subject: SubjectName,
                          timeSlot: TimeSlot,
                          lessonSubHeading: String = "")
