package models.timetoteach.ui

sealed trait ClassIdContext

case object CLASS_SETUP extends ClassIdContext
case object CLASS_TIMETABLE extends ClassIdContext
case object CLASS_TERMLY_PLANS extends ClassIdContext
case object CLASS_TERMLY_PLANS_LOWER extends ClassIdContext
