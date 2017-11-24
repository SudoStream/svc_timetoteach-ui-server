package controllers.serviceproxies

import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumLevel
import io.sudostream.timetoteach.messages.systemwide.model.{Country, CurriculumLevelWrapper}
import models.timetoteach.InitialUserPreferences
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification

class UserWriterServiceProxyImplTest extends Specification {

  override def is =
    s2"""

  This is a specification for the 'UserWriterServiceProxyImpl' class

  The 'createCustomLevelWrappers' method should
    be create a list of 2 itmes if 'early' & 'first' curriculums are checked        $earlyAndFirstCurriculumChecked
    be create a list of 5 itmes if ALL curriculums are checked                      $allCurriculumChecked
  """

  def earlyAndFirstCurriculumChecked: MatchResult[Any] = {
    val curriculumLevels = UserWriterServiceProxyImpl.createCustomLevelWrappers(
      createUserPreferencesWithEarlyAndFirstCurriculumsChecked())

    curriculumLevels.size mustEqual 2
    curriculumLevels.count(_.curriculumLevel.country == Country.SCOTLAND) mustEqual 2
    curriculumLevels.count(_.curriculumLevel.scottishCurriculumLevel.get == ScottishCurriculumLevel.EARLY) mustEqual 1
    curriculumLevels.count(_.curriculumLevel.scottishCurriculumLevel.get == ScottishCurriculumLevel.FIRST) mustEqual 1
    curriculumLevels.count(_.curriculumLevel.scottishCurriculumLevel.get == ScottishCurriculumLevel.SECOND) mustEqual 0
  }

  def allCurriculumChecked: MatchResult[Any] = {
    val curriculumLevels = UserWriterServiceProxyImpl.createCustomLevelWrappers(
      createUserPreferencesWithAllCurriculumsChecked())

    curriculumLevels.size mustEqual 5
    curriculumLevels.count(_.curriculumLevel.country == Country.SCOTLAND) mustEqual 5
    curriculumLevels.count(_.curriculumLevel.scottishCurriculumLevel.get == ScottishCurriculumLevel.EARLY) mustEqual 1
    curriculumLevels.count(_.curriculumLevel.scottishCurriculumLevel.get == ScottishCurriculumLevel.FIRST) mustEqual 1
    curriculumLevels.count(_.curriculumLevel.scottishCurriculumLevel.get == ScottishCurriculumLevel.SECOND) mustEqual 1
    curriculumLevels.count(_.curriculumLevel.scottishCurriculumLevel.get == ScottishCurriculumLevel.THIRD) mustEqual 1
    curriculumLevels.count(_.curriculumLevel.scottishCurriculumLevel.get == ScottishCurriculumLevel.FOURTH) mustEqual 1

  }



  /////


  def createUserPreferencesWithEarlyAndFirstCurriculumsChecked(): InitialUserPreferences = {
    InitialUserPreferences(
      schoolId = "school1234",
      schoolStartTime = "9:00 AM",
      morningBreakStartTime = "10:30 AM",
      morningBreakEndTime = "10:45 AM",
      lunchStartTime = "12:00 PM",
      lunchEndTime = "1:00 PM",
      schoolEndTime = "3:00 PM",
      className = "P1AB",
      checkEarlyCurriculum = "On",
      checkFirstCurriculum = "On",
      checkSecondCurriculum = "Off",
      checkThirdCurriculum = "Off",
      checkFourthCurriculum = "Off"
    )
  }

  def createUserPreferencesWithAllCurriculumsChecked(): InitialUserPreferences = {
    InitialUserPreferences(
      schoolId = "school1234",
      schoolStartTime = "9:00 AM",
      morningBreakStartTime = "10:30 AM",
      morningBreakEndTime = "10:45 AM",
      lunchStartTime = "12:00 PM",
      lunchEndTime = "1:00 PM",
      schoolEndTime = "3:00 PM",
      className = "P1AB",
      checkEarlyCurriculum = "On",
      checkFirstCurriculum = "On",
      checkSecondCurriculum = "On",
      checkThirdCurriculum = "On",
      checkFourthCurriculum = "On"
    )
  }

}