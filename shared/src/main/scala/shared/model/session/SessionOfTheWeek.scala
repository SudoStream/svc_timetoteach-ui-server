package shared.model.session

trait SessionOfTheWeek {
  def value: String = {
    val fullClassName = this.getClass.getName
    val arr = fullClassName.split('.')
    val baseClassname = arr.last
    val dashFormat = baseClassname.replaceAll("([A-Z])", "-$1")
    dashFormat.toLowerCase().drop(1)
  }
}

sealed case class MondayEarlyMorningSession() extends SessionOfTheWeek
sealed case class MondayLateMorningSession() extends SessionOfTheWeek
sealed case class MondayAfternoonSession() extends SessionOfTheWeek
sealed case class TuesdayEarlyMorningSession() extends SessionOfTheWeek
sealed case class TuesdayLateMorningSession() extends SessionOfTheWeek
sealed case class TuesdayAfternoonSession() extends SessionOfTheWeek
sealed case class WednesdayEarlyMorningSession() extends SessionOfTheWeek
sealed case class WednesdayLateMorningSession() extends SessionOfTheWeek
sealed case class WednesdayAfternoonSession() extends SessionOfTheWeek
sealed case class ThursdayEarlyMorningSession() extends SessionOfTheWeek
sealed case class ThursdayLateMorningSession() extends SessionOfTheWeek
sealed case class ThursdayAfternoonSession() extends SessionOfTheWeek
sealed case class FridayEarlyMorningSession() extends SessionOfTheWeek
sealed case class FridayLateMorningSession() extends SessionOfTheWeek
sealed case class FridayAfternoonSession() extends SessionOfTheWeek
