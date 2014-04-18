package com.bimbr.choreo.model

import scala.concurrent.duration.Duration

sealed trait Event {
  def time: Duration
}

case class TempoChange(time: Duration, measuresPerMinute: Int) extends Event
case class MeterChange(time: Duration, measuresPerBar: Int) extends Event
case class Bar(time: Duration) extends Event
case class Measure(time: Duration) extends Event
case class Move2(time: Duration, symbol: String, name: String) extends Event