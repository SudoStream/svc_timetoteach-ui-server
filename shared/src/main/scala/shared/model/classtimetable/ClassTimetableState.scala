package shared.model.classtimetable

trait ClassTimetableState

sealed case class EntirelyEmpty() extends ClassTimetableState
sealed case class CompletelyFull() extends ClassTimetableState
sealed case class PartiallyComplete() extends ClassTimetableState
