package com.bimbr.choreo.model

import scala.concurrent.duration.Duration

case class Choreography2(name: String,
                         length: Duration,
                         events: Map[Duration, Event] = Map.empty) {
  def setTempo(time: Duration, measuresPerMinute: Int) = insertEvent(TempoChange(time, measuresPerMinute))

  private def insertEvent(event: Event) = copy(events = events + (event.time -> event))
}
