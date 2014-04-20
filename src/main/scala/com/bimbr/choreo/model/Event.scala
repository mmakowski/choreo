package com.bimbr.choreo.model

sealed trait Event {
  def time: Instant
}

final case class TempoChange(time: Instant, tempo: Tempo) extends Event
final case class MeterChange(time: Instant, meter: Meter) extends Event
final case class Bar(time: Instant) extends Event
final case class Measure(time: Instant) extends Event
final case class Move2(time: Instant, symbol: MoveSymbol) extends Event
