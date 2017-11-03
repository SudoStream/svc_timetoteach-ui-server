package shared.model

import java.time.LocalTime

import shared.model.classtimetable._

trait ClassTimetableTestHelper {
  def createFullClassTimetable : ClassTimetable = {
    val classTimetable = ClassTimetable(None)

    // Monday
    classTimetable.addSubject(
      SubjectDetail(
        SubjectName("subject-writing"),
        TimeSlot(LocalTime.of(9,0), LocalTime.of(10,30))
      ),
      MondayEarlyMorningSession()
    )
    classTimetable.addSubject(
      SubjectDetail(
        SubjectName("subject-maths"),
        TimeSlot(LocalTime.of(10,45), LocalTime.of(12,0))
      ),
      MondayLateMorningSession()
    )
    classTimetable.addSubject(
      SubjectDetail(
        SubjectName("subject-reading"),
        TimeSlot(LocalTime.of(13,0), LocalTime.of(15,0))
      ),
      MondayAfternoonSession()
    )

    // Tuesday
    classTimetable.addSubject(
      SubjectDetail(
        SubjectName("subject-writing"),
        TimeSlot(LocalTime.of(9,0), LocalTime.of(10,30))
      ),
      TuesdayEarlyMorningSession()
    )
    classTimetable.addSubject(
      SubjectDetail(
        SubjectName("subject-maths"),
        TimeSlot(LocalTime.of(10,45), LocalTime.of(12,0))
      ),
      TuesdayLateMorningSession()
    )
    classTimetable.addSubject(
      SubjectDetail(
        SubjectName("subject-reading"),
        TimeSlot(LocalTime.of(13,0), LocalTime.of(15,0))
      ),
      TuesdayAfternoonSession()
    )

    // Wednesday
    classTimetable.addSubject(
      SubjectDetail(
        SubjectName("subject-writing"),
        TimeSlot(LocalTime.of(9,0), LocalTime.of(10,30))
      ),
      WednesdayEarlyMorningSession()
    )
    classTimetable.addSubject(
      SubjectDetail(
        SubjectName("subject-maths"),
        TimeSlot(LocalTime.of(10,45), LocalTime.of(12,0))
      ),
      WednesdayLateMorningSession()
    )
    classTimetable.addSubject(
      SubjectDetail(
        SubjectName("subject-reading"),
        TimeSlot(LocalTime.of(13,0), LocalTime.of(15,0))
      ),
      WednesdayAfternoonSession()
    )

    // Thursday
    classTimetable.addSubject(
      SubjectDetail(
        SubjectName("subject-writing"),
        TimeSlot(LocalTime.of(9,0), LocalTime.of(10,30))
      ),
      ThursdayEarlyMorningSession()
    )
    classTimetable.addSubject(
      SubjectDetail(
        SubjectName("subject-maths"),
        TimeSlot(LocalTime.of(10,45), LocalTime.of(12,0))
      ),
      ThursdayLateMorningSession()
    )
    classTimetable.addSubject(
      SubjectDetail(
        SubjectName("subject-reading"),
        TimeSlot(LocalTime.of(13,0), LocalTime.of(15,0))
      ),
      ThursdayAfternoonSession()
    )

    // Friday
    classTimetable.addSubject(
      SubjectDetail(
        SubjectName("subject-writing"),
        TimeSlot(LocalTime.of(9,0), LocalTime.of(10,30))
      ),
      FridayEarlyMorningSession()
    )
    classTimetable.addSubject(
      SubjectDetail(
        SubjectName("subject-maths"),
        TimeSlot(LocalTime.of(10,45), LocalTime.of(12,0))
      ),
      FridayLateMorningSession()
    )
    classTimetable.addSubject(
      SubjectDetail(
        SubjectName("subject-reading"),
        TimeSlot(LocalTime.of(13,0), LocalTime.of(15,0))
      ),
      FridayAfternoonSession()
    )


    classTimetable
  }

}
