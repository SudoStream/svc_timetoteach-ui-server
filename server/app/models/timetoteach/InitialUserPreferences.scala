package models.timetoteach

import play.api.Logger

case class InitialUserPreferences(
                                   schoolId: String,
                                   schoolStartTime: String,
                                   morningBreakStartTime: String,
                                   morningBreakEndTime: String,
                                   lunchStartTime: String,
                                   lunchEndTime: String,
                                   schoolEndTime: String
                                 ) {
  val logger: Logger = Logger
}

