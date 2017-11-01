package shared.model

import java.time.LocalTime

case class SubjectDetail(
                          subject: SubjectName,
                          startTime: LocalTime,
                          endTime: LocalTime,
                          lessonSubHeading: String = "")
