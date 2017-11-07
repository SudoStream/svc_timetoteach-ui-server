package shared.model.classtimetable

case class SubjectDetail(
                          subject: SubjectName,
                          timeSlot: TimeSlot,
                          lessonSubHeading: String = "")
