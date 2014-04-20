package com.bimbr.choreo.model

import scala.concurrent.duration._

final case class Choreography2 private (name: String,
                                  length: Duration,
                                  events: Events) {

  def setTempo(instant: Instant, tempo: Tempo): Choreography2 =
    insertEvent(TempoChange(instant, tempo)).recalculateGrid

  def setMeter(instant: Instant, meter: Meter): Choreography2 =
    insertEvent(MeterChange(instant, meter)).recalculateGrid

  private def insertEvent(event: Event): Choreography2 = copy(events = events :+ event)

  private def recalculateGrid: Choreography2 = recalculateMeasures.recalculateBars

  private def recalculateMeasures: Choreography2 = {
    val tempoChanges = events.asSequence.collect { case tc: TempoChange => tc }
    val intervals = tempoChanges.zip(tempoChanges.map(_.time).tail)
    val updatedEvents = intervals.foldLeft(events.without[Measure]) { (events, interval) =>
      val (TempoChange(start, tempo), end) = interval
      insertMeasures(events, tempo, start, end)
    }
    copy(events = updatedEvents)
  }

  private def insertMeasures(events: Events, tempo: Tempo, start: Instant, end: Instant): Events =
    insertEventsAtFixedInterval(events, start, end, tempo.measureDuration, Measure)

  private def insertEventsAtFixedInterval(events: Events, start: Instant, end: Instant, interval: Duration, event: Instant => Event): Events = {
    val measureTimesMs = start.msFromStart.until(end.msFromStart, step = interval.toMillis.toInt)
    val measureEvents = measureTimesMs.map(t => event(Instant(t)))
    events :++ measureEvents
  }

  private def recalculateBars: Choreography2 = {
    val intervals = tempoOrMeterChanges.zip(tempoOrMeterChanges.map(_._1).tail)
    val updatedEvents = intervals.foldLeft(events.without[Bar]) { (events, interval) =>
      val ((start, TempoOrMeterChange(tempo, meter)), end) = interval
      insertBars(events, tempo, meter, start, end)
    }
    copy(events = updatedEvents)
  }

  private def insertBars(events: Events, tempo: Tempo, meter: Meter, start: Instant, end: Instant): Events =
    if (tempo.isUndefined || meter.isUndefined) events
    else {
      val barDuration = tempo.measureDuration * meter.measuresPerBar
      insertEventsAtFixedInterval(events, start, end, barDuration, Bar)
    }

  private lazy val tempoOrMeterChanges: Seq[(Instant, TempoOrMeterChange)] = { // SortedMap[Instant, TempoOrMeterChange] causes compilation error in scala 2.10.4
    def isTempoOrMeterChange(event: Event) = event match {
      case _: TempoChange => true
      case _: MeterChange => true
      case _              => false
    }
    def addChange(mappings: Seq[(Instant, TempoOrMeterChange)], timeAndBucket: (Instant, Seq[Event])): Seq[(Instant, TempoOrMeterChange)] = {
      val last = mappings.lastOption.map(_._2).getOrElse(TempoOrMeterChange.Undefined)
      val (time, bucket) = timeAndBucket
      val tempo = bucket.collect { case tc: TempoChange => tc.tempo }.lastOption.getOrElse(last.tempo)
      val meter = bucket.collect { case mc: MeterChange => mc.meter }.lastOption.getOrElse(last.meter)
      val change = TempoOrMeterChange(tempo, meter)
      ((time -> change) :: mappings.toList).reverse // mappings :+ (time -> change) causes compilation error in scala 2.10.4
    }
    events.filter(isTempoOrMeterChange).asMap.foldLeft(Seq.empty[(Instant, TempoOrMeterChange)])(addChange)
  }
}

case class TempoOrMeterChange(tempo: Tempo, meter: Meter)
object TempoOrMeterChange {
  val Undefined = TempoOrMeterChange(Tempo.Undefined, Meter.Undefined)
}

object Choreography2 {
  def apply(name: String, length: Duration): Choreography2 =
    Choreography2(name, length, initialEvents(length))

  private def initialEvents(length: Duration) = {
    val end = Instant(length)
    Events(TempoChange(end, Tempo.Undefined), MeterChange(end, Meter.Undefined))
  }
}

