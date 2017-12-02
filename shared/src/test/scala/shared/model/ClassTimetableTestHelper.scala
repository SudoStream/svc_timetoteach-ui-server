package shared.model

import java.time.LocalTime

import shared.model.classtimetable._

trait ClassTimetableTestHelper {
  def createFullClassTimetable : WWWClassTimetable = {
    val classTimetable = WWWClassTimetable(None)

    // Monday
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-writing"),
        WwwTimeSlot(LocalTime.of(9,0), LocalTime.of(10,30))
      ),
      MondayEarlyMorningSession()
    )
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-maths"),
        WwwTimeSlot(LocalTime.of(10,45), LocalTime.of(12,0))
      ),
      MondayLateMorningSession()
    )
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-reading"),
        WwwTimeSlot(LocalTime.of(13,0), LocalTime.of(15,0))
      ),
      MondayAfternoonSession()
    )

    // Tuesday
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-writing"),
        WwwTimeSlot(LocalTime.of(9,0), LocalTime.of(10,30))
      ),
      TuesdayEarlyMorningSession()
    )
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-maths"),
        WwwTimeSlot(LocalTime.of(10,45), LocalTime.of(12,0))
      ),
      TuesdayLateMorningSession()
    )
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-reading"),
        WwwTimeSlot(LocalTime.of(13,0), LocalTime.of(15,0))
      ),
      TuesdayAfternoonSession()
    )

    // Wednesday
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-writing"),
        WwwTimeSlot(LocalTime.of(9,0), LocalTime.of(10,30))
      ),
      WednesdayEarlyMorningSession()
    )
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-maths"),
        WwwTimeSlot(LocalTime.of(10,45), LocalTime.of(12,0))
      ),
      WednesdayLateMorningSession()
    )
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-reading"),
        WwwTimeSlot(LocalTime.of(13,0), LocalTime.of(15,0))
      ),
      WednesdayAfternoonSession()
    )

    // Thursday
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-writing"),
        WwwTimeSlot(LocalTime.of(9,0), LocalTime.of(10,30))
      ),
      ThursdayEarlyMorningSession()
    )
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-maths"),
        WwwTimeSlot(LocalTime.of(10,45), LocalTime.of(12,0))
      ),
      ThursdayLateMorningSession()
    )
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-reading"),
        WwwTimeSlot(LocalTime.of(13,0), LocalTime.of(15,0))
      ),
      ThursdayAfternoonSession()
    )

    // Friday
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-writing"),
        WwwTimeSlot(LocalTime.of(9,0), LocalTime.of(10,30))
      ),
      FridayEarlyMorningSession()
    )
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-maths"),
        WwwTimeSlot(LocalTime.of(10,45), LocalTime.of(12,0))
      ),
      FridayLateMorningSession()
    )
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-reading"),
        WwwTimeSlot(LocalTime.of(13,0), LocalTime.of(15,0))
      ),
      FridayAfternoonSession()
    )


    classTimetable
  }

}
