package com.bimbr.choreo.model

import scala.concurrent.duration._

final case class Tempo(beatsPerMinute: Int) extends AnyVal {
  def measureDuration: Duration = (1000 * 60 / beatsPerMinute).milliseconds
  def isUndefined: Boolean = this == Tempo.Undefined
}
object Tempo {
  val Undefined = Tempo(-1)
}

final case class Meter(measuresPerBar: Int) extends AnyVal {
  def isUndefined: Boolean = this == Meter.Undefined
}
object Meter {
  val Undefined = Meter(-1)
}

final case class Instant(msFromStart: Int) extends AnyVal with Ordered[Instant] {
  def compare(that: Instant) = msFromStart.compare(that.msFromStart)
}
object Instant {
  def apply(duration: Duration): Instant = Instant(duration.toMillis.toInt)

  val Start = Instant(0)
}

final case class MoveSymbol(symbol: String) extends AnyVal
