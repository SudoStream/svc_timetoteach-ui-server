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
      MondayLateMorningWwwSession()
    )
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-reading"),
        WwwTimeSlot(LocalTime.of(13,0), LocalTime.of(15,0))
      ),
      MondayAfternoonWwwSession()
    )

    // Tuesday
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-writing"),
        WwwTimeSlot(LocalTime.of(9,0), LocalTime.of(10,30))
      ),
      TuesdayEarlyMorningWwwSession()
    )
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-maths"),
        WwwTimeSlot(LocalTime.of(10,45), LocalTime.of(12,0))
      ),
      TuesdayLateMorningWwwSession()
    )
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-reading"),
        WwwTimeSlot(LocalTime.of(13,0), LocalTime.of(15,0))
      ),
      TuesdayAfternoonWwwSession()
    )

    // Wednesday
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-writing"),
        WwwTimeSlot(LocalTime.of(9,0), LocalTime.of(10,30))
      ),
      WednesdayEarlyMorningWwwSession()
    )
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-maths"),
        WwwTimeSlot(LocalTime.of(10,45), LocalTime.of(12,0))
      ),
      WednesdayLateMorningWwwSession()
    )
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-reading"),
        WwwTimeSlot(LocalTime.of(13,0), LocalTime.of(15,0))
      ),
      WednesdayAfternoonWwwSession()
    )

    // Thursday
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-writing"),
        WwwTimeSlot(LocalTime.of(9,0), LocalTime.of(10,30))
      ),
      ThursdayEarlyMorningWwwSession()
    )
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-maths"),
        WwwTimeSlot(LocalTime.of(10,45), LocalTime.of(12,0))
      ),
      ThursdayLateMorningWwwSession()
    )
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-reading"),
        WwwTimeSlot(LocalTime.of(13,0), LocalTime.of(15,0))
      ),
      ThursdayAfternoonWwwSession()
    )

    // Friday
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-writing"),
        WwwTimeSlot(LocalTime.of(9,0), LocalTime.of(10,30))
      ),
      FridayEarlyMorningWwwSession()
    )
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-maths"),
        WwwTimeSlot(LocalTime.of(10,45), LocalTime.of(12,0))
      ),
      FridayLateMorningWwwSession()
    )
    classTimetable.addSubject(
      WwwSubjectDetail(
        WwwSubjectName("subject-reading"),
        WwwTimeSlot(LocalTime.of(13,0), LocalTime.of(15,0))
      ),
      FridayAfternoonWwwSession()
    )


    classTimetable
  }

}
