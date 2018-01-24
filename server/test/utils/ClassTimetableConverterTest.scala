package utils

import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification
import shared.model.classtimetable._

class ClassTimetableConverterTest extends Specification {

  override def is =
    s2"""

  This is a specification for the 'ClassTimetableConverter' class

  The 'convertAvroClassTimeTableToModel' method should
        Convert an empty WWW ClassTimetable To an empty Avro ClassTimetable     $emptyWWWClassTimetableConvertsToEmptyAvroClassTimetable
  """

  def emptyWWWClassTimetableConvertsToEmptyAvroClassTimetable: MatchResult[Any] = {
    val wwwClassTimetable = createWwwClassTimetable()
    val avroClassTimetable = ClassTimetableConverterToAvro.convertWwwClassTimeTableToAvro("12345",WwwClassId("Some Class"), wwwClassTimetable)

    println(s"wwwClassTimetable  : ${wwwClassTimetable.allSessionsOfTheWeekInOrderByDay.toString}")
    println(s"avroClassTimetable : ${avroClassTimetable.allSessionsOfTheWeek.toString}")
    wwwClassTimetable.allSessionsOfTheWeek.size mustEqual avroClassTimetable.allSessionsOfTheWeek.size
    avroClassTimetable.timeToTeachId.value mustEqual "12345"
    avroClassTimetable.classId.value mustEqual "Some Class"
  }

  def createWwwClassTimetable(): WWWClassTimetable = {
    WWWClassTimetable(
      Some(Map(
        SchoolDayStarts() -> "09:00",
        MorningBreakStarts() -> "10:30",
        MorningBreakEnds() -> "10:45",
        LunchStarts() -> "12:00",
        LunchEnds() -> "13:00",
        SchoolDayEnds() -> "15:00"
      ))
    )
  }

}
