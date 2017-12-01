package shared.model.classtimetable

case class WwwSubjectDetail(
                             subject: WwwSubjectName,
                             timeSlot: WwwTimeSlot,
                             lessonAdditionalInfo: String = "") {

  def canEqual(a: Any): Boolean = a.isInstanceOf[WwwSubjectDetail]

  override def equals(that: Any): Boolean =
    that match {
      case that: WwwSubjectDetail =>
        that.canEqual(this) &&
        this.hashCode == that.hashCode &&
        this.subject == that.subject &&
        this.timeSlot == that.timeSlot
      case _ => false
    }

  override def hashCode: Int = {
    val prime = 31
    prime * subject.hashCode() * timeSlot.hashCode()
  }

}
