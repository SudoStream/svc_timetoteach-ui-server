package utils

import java.time.LocalTime

import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.subjectdetail.SubjectDetail
import shared.model.classtimetable.{WwwSubjectDetail, WwwSubjectName, WwwTimeSlot}

object SubjectDetailConverter {
  def converToWwwClassDetail(subjectDetail: SubjectDetail): WwwSubjectDetail = {

    val wwwSubjectName = WwwSubjectName("subject-" + subjectDetail.subjectName.toString.toLowerCase.replace("_", "-"))
    val timeSlot = WwwTimeSlot(
      startTime = LocalTime.parse(subjectDetail.startTime.timeIso8601),
      endTime = LocalTime.parse(subjectDetail.endTime.timeIso8601)
    )
    val lessonAdditionalInfo = subjectDetail.additionalInfo.value
    WwwSubjectDetail(wwwSubjectName, timeSlot, lessonAdditionalInfo)
  }
}
