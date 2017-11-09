package shared.model.classtimetable

trait Fraction {
  def multiplier: Double
}

sealed case class Whole() extends Fraction {
  override def multiplier: Double = 1.0
}

sealed case class OneHalf() extends Fraction {
  override def multiplier: Double = 0.5
}

sealed case class OneThird() extends Fraction {
  override def multiplier: Double = 0.33
}

sealed case class TwoThirds() extends Fraction {
  override def multiplier: Double = 0.66
}
