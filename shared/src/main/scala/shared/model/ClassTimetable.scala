package shared.model

import scala.scalajs.js.Dictionary

case class ClassTimetable(private val schoolDayTimesOption: Option[Dictionary[String]]) {


  /**
    *
    */
  private val allowedDictionaryEntries = Set(
    "schoolDayStarts", "morningBreakStarts", "morningBreakEnds", "lunchStarts", "lunchEnds", "schoolDayEnds")

  val schoolDayTimes: Dictionary[String] = schoolDayTimesOption match {
    case Some(dictionary) =>
      if (dictionary.size != allowedDictionaryEntries.size) {
        val errorMsg = s"Require ${allowedDictionaryEntries.size} entries for dictionary, specifically '" +
          s"${allowedDictionaryEntries.mkString(" ")}' but there were ${dictionary.size} passed in, specifically '" +
          s"${dictionary.keys.mkString(" ")}'"
        throw new RuntimeException(errorMsg)
      }
      dictionary.keys.foreach(
        keyname => if (!allowedDictionaryEntries.contains(keyname)) {
          val errorMsg = "Require ${allowedDictionaryEntries.size} entries for dictionary, specifically '" +
            s"${allowedDictionaryEntries.mkString(" ")}', but there was an erroneous entry = '$keyname'"
          throw new RuntimeException(errorMsg)
        }
      )
      dictionary

    case _ =>
      val defaultSchoolDayTimes = Dictionary[String]()
      defaultSchoolDayTimes.update("schoolDayStarts", "09:00")
      defaultSchoolDayTimes.update("morningBreakStarts", "10:30")
      defaultSchoolDayTimes.update("morningBreakEnds", "10:45")
      defaultSchoolDayTimes.update("lunchStarts", "12:00")
      defaultSchoolDayTimes.update("lunchEnds", "13:00")
      defaultSchoolDayTimes.update("schoolDayEnds", "15:00")
      defaultSchoolDayTimes
  }


  /**
    *
    */
  private val allowedStateValues = Set(
    "ENTIRELY_EMPTY", "PARTIALLY_COPMLETE", "COMPLETE")
  private var currentState = "ENTIRELY_EMPTY"
  def getCurrentState: String = new String(currentState)


  /**
    *
    */
  private val beenEdits = false
  def hasBeenEdited: Boolean = beenEdits


}
