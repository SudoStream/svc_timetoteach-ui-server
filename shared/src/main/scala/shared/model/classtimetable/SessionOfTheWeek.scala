package shared.model.classtimetable

object SessionOfTheWeek {
  def createSessionOfTheWeek(
                              dayOfWeek: DayOfWeek,
                              session: Session
                            ): Option[SessionOfTheWeek] = {
    val dayAndSession = dayOfWeek.value.toLowerCase + "-" + session.value
    dayAndSession match {
      case "monday-early-morning-session" => Some(MondayEarlyMorningSession())
      case "monday-late-morning-session" => Some(MondayLateMorningSession())
      case "monday-afternoon-session" => Some(MondayAfternoonSession())
      case "tuesday-early-morning-session" => Some(TuesdayEarlyMorningSession())
      case "tuesday-late-morning-session" => Some(TuesdayLateMorningSession())
      case "tuesday-afternoon-session" => Some(TuesdayAfternoonSession())
      case "wednesday-early-morning-session" => Some(WednesdayEarlyMorningSession())
      case "wednesday-late-morning-session" => Some(WednesdayLateMorningSession())
      case "wednesday-afternoon-session" => Some(WednesdayAfternoonSession())
      case "thursday-early-morning-session" => Some(ThursdayEarlyMorningSession())
      case "thursday-late-morning-session" => Some(ThursdayLateMorningSession())
      case "thursday-afternoon-session" => Some(ThursdayAfternoonSession())
      case "friday-early-morning-session" => Some(FridayEarlyMorningSession())
      case "friday-late-morning-session" => Some(FridayLateMorningSession())
      case "friday-afternoon-session" => Some(FridayAfternoonSession())
      case _ => None
    }
  }
}

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
