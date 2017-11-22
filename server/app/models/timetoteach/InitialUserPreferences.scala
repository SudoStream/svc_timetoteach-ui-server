package models.timetoteach

import play.api.Logger

case class InitialUserPreferences(
                                   schoolId: String,
                                   schoolStartTime: String,
                                   morningBreakStartTime: String,
                                   morningBreakEndTime: String,
                                   lunchStartTime: String,
                                   lunchEndTime: String,
                                   schoolEndTime: String,
                                   className: String,
                                   checkEarlyCurriculum: String,
                                   checkFirstCurriculum: String,
                                   checkSecondCurriculum: String,
                                   checkThirdCurriculum: String,
                                   checkFourthCurriculum: String
                                 ) {
  val logger: Logger = Logger
  logger.debug("InitialUserPreferences - some values")
  logger.debug(s"checkEarlyCurriculum = $checkEarlyCurriculum")
  logger.debug(s"checkFirstCurriculum = $checkFirstCurriculum")
}

