package shared.model.classtimetable

object WwwSessionOfTheWeek {
  def createSessionOfTheWeek(sessionOfTheWeekString:String): Option[WwwSessionOfTheWeek] = {
    sessionOfTheWeekString match {
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
  def createSessionOfTheWeek(
                              dayOfWeek: WwwDayOfWeek,
                              session: WwwSession
                            ): Option[WwwSessionOfTheWeek] = {
    val dayAndSession = dayOfWeek.value.toLowerCase + "-" + session.value
    createSessionOfTheWeek(dayAndSession)
  }
}

trait WwwSessionOfTheWeek {

  def dayOfTheWeek : WwwDayOfWeek

  def ordinalNumber: Int

  def value: String = {
    val fullClassName = this.getClass.getName
    val arr = fullClassName.split('.')
    val baseClassname = arr.last
    val dashFormat = baseClassname.replaceAll("([A-Z])", "-$1")
    dashFormat.toLowerCase().drop(1)
  }

  def valueWithoutDay: String = {
    value.split("-").tail.mkString("-")
  }

}

sealed case class MondayEarlyMorningSession() extends WwwSessionOfTheWeek {
  override def ordinalNumber = 1
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Monday")
}

sealed case class MondayLateMorningSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 2
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Monday")
}

sealed case class MondayAfternoonSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 3
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Monday")
}

sealed case class TuesdayEarlyMorningSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 4
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Tuesday")
}

sealed case class TuesdayLateMorningSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 5
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Tuesday")
}

sealed case class TuesdayAfternoonSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 6
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Tuesday")
}

sealed case class WednesdayEarlyMorningSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 7
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Wednesday")
}

sealed case class WednesdayLateMorningSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 8
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Wednesday")
}

sealed case class WednesdayAfternoonSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 9
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Wednesday")
}

sealed case class ThursdayEarlyMorningSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 10
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Thursday")
}

sealed case class ThursdayLateMorningSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 11
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Thursday")
}

sealed case class ThursdayAfternoonSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 12
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Thursday")
}

sealed case class FridayEarlyMorningSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 13
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Friday")
}

sealed case class FridayLateMorningSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 14
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Friday")
}

sealed case class FridayAfternoonSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 15
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Friday")
}
