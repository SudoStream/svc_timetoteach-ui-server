package models.timetoteach.ui

sealed trait ClassIdContext
case object CLASS_SETUP extends ClassIdContext
case object CLASS_TIMETABLE extends ClassIdContext
case object CLASS_TERMLY_PLANS extends ClassIdContext
case object CLASS_TERMLY_PLANS_LOWER extends ClassIdContext
case object CLASS_WEEKLY_PLANS extends ClassIdContext

sealed trait ClassIdSubContext
case object WEEKLY_VIEW extends ClassIdSubContext
case object WEEKLY_PLANNING extends ClassIdSubContext
