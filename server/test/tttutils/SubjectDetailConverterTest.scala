package tttutils

import java.time.LocalTime

import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.subjectdetail.{SubjectDetail, SubjectDetailAdditionalInfo, SubjectName}
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.time.{EndTime, StartTime}
import org.specs2.mutable.Specification

class SubjectDetailConverterTest extends Specification {

  override def is =
    s2"""

  This is a specification for the 'SubjectDetailConverter' class

  The 'converToWwwClassDetail' method should
        convert a 'ClassTimetable' instance to a Www version                   $subjectDetailConvertHappy
  """


  def subjectDetailConvertHappy() = {
    val subjectDetail = SubjectDetail(
      SubjectName.PHYSICAL_EDUCATION,
      StartTime("09:00"),
      EndTime("10:00"),
      SubjectDetailAdditionalInfo("lots of running")
    )
    val wwwSubjectDetail = SubjectDetailConverter.converToWwwClassDetail(subjectDetail)

    wwwSubjectDetail.lessonAdditionalInfo mustEqual "lots of running"
    wwwSubjectDetail.subject.value mustEqual "subject-physical-education"
    wwwSubjectDetail.timeSlot.startTime mustEqual LocalTime.of(9,0)
    wwwSubjectDetail.timeSlot.endTime mustEqual LocalTime.of(10,0)
  }
}
