package shared.model.classtimetable

case class SubjectDetail(
                          subject: SubjectName,
                          timeSlot: TimeSlot,
                          lessonAdditionalInfo: String = "") {

  def canEqual(a: Any): Boolean = a.isInstanceOf[SubjectDetail]

  override def equals(that: Any): Boolean =
    that match {
      case that: SubjectDetail =>
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
