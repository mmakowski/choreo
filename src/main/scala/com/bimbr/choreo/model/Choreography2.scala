package com.bimbr.choreo.model

import scala.concurrent.duration._

case class Choreography2(name: String,
                         length: Duration,
                         events: Seq[Event] = Seq.empty) {
  def setTempo(time: Duration, measuresPerMinute: Int): Choreography2 = {
    //insertEvent(TempoChange(time, measuresPerMinute))
    recalculateMeasures(measuresPerMinute, timeFrom = time)
  }

  private def insertEvent(event: Event): Choreography2 = copy(events = events :+ event)

  private def recalculateMeasures(measuresPerMinute: Int, timeFrom: Duration): Choreography2 = {
    val measureLength = (1000 * 60 / measuresPerMinute).milliseconds
    val measureTimesMs = timeFrom.toMillis.to(length.toMillis, step = measureLength.toMillis)
    val measureEvents = measureTimesMs.map(t => Measure(t.milliseconds))
    measureEvents.foldLeft(this)((c, m) => c.insertEvent(m))
  }
}
