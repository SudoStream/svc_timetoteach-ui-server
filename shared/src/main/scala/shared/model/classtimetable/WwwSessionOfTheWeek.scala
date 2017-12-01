package shared.model.classtimetable

object WwwSessionOfTheWeek {
  def createSessionOfTheWeek(
                              dayOfWeek: WwwDayOfWeek,
                              session: WwwSession
                            ): Option[WwwSessionOfTheWeek] = {
    val dayAndSession = dayOfWeek.value.toLowerCase + "-" + session.value
    dayAndSession match {
      case "monday-early-morning-session" => Some(MondayEarlyMorningWwwSession())
      case "monday-late-morning-session" => Some(MondayLateMorningWwwSession())
      case "monday-afternoon-session" => Some(MondayAfternoonWwwSession())
      case "tuesday-early-morning-session" => Some(TuesdayEarlyMorningWwwSession())
      case "tuesday-late-morning-session" => Some(TuesdayLateMorningWwwSession())
      case "tuesday-afternoon-session" => Some(TuesdayAfternoonWwwSession())
      case "wednesday-early-morning-session" => Some(WednesdayEarlyMorningWwwSession())
      case "wednesday-late-morning-session" => Some(WednesdayLateMorningWwwSession())
      case "wednesday-afternoon-session" => Some(WednesdayAfternoonWwwSession())
      case "thursday-early-morning-session" => Some(ThursdayEarlyMorningWwwSession())
      case "thursday-late-morning-session" => Some(ThursdayLateMorningWwwSession())
      case "thursday-afternoon-session" => Some(ThursdayAfternoonWwwSession())
      case "friday-early-morning-session" => Some(FridayEarlyMorningWwwSession())
      case "friday-late-morning-session" => Some(FridayLateMorningWwwSession())
      case "friday-afternoon-session" => Some(FridayAfternoonWwwSession())
      case _ => None
    }
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

sealed case class MondayEarlyMorningWwwSession() extends WwwSessionOfTheWeek {
  override def ordinalNumber = 1
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Monday")
}

sealed case class MondayLateMorningWwwSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 2
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Monday")
}

sealed case class MondayAfternoonWwwSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 3
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Monday")
}

sealed case class TuesdayEarlyMorningWwwSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 4
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Tuesday")
}

sealed case class TuesdayLateMorningWwwSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 5
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Tuesday")
}

sealed case class TuesdayAfternoonWwwSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 6
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Tuesday")
}

sealed case class WednesdayEarlyMorningWwwSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 7
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Wednesday")
}

sealed case class WednesdayLateMorningWwwSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 8
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Wednesday")
}

sealed case class WednesdayAfternoonWwwSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 9
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Wednesday")
}

sealed case class ThursdayEarlyMorningWwwSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 10
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Thursday")
}

sealed case class ThursdayLateMorningWwwSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 11
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Thursday")
}

sealed case class ThursdayAfternoonWwwSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 12
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Thursday")
}

sealed case class FridayEarlyMorningWwwSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 13
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Friday")
}

sealed case class FridayLateMorningWwwSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 14
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Friday")
}

sealed case class FridayAfternoonWwwSession() extends WwwSessionOfTheWeek {
  override def  ordinalNumber = 15
  override def dayOfTheWeek: WwwDayOfWeek = WwwDayOfWeek("Friday")
}
