package com.bimbr.choreo.model

import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import scala.concurrent.duration._
import scala.reflect.ClassTag

class ChoreographySpec extends Specification with NoTimeConversions {
  "A Choreography" should {
    "contain measure events at appropriate time intervals once tempo is set" in {
      import ChoreographyFixture._, TempoFixture._
      val choreo = newTestChoreography.setTempo(Instant.Start, tempo)
      val expectedMeasureTimes = (0 until (choreo.length.toMillis.toInt, step = msPerMeasure)).map(Instant(_))
      val actualMeasureTimes = eventTimes[Measure](choreo)
      actualMeasureTimes mustEqual expectedMeasureTimes
    }

    "contain bar events at appropriate measures once tempo and meter is set" in {
      import ChoreographyFixture._, TempoFixture._, MeterFixture._
      val choreo = newTestChoreography.setTempo(Instant.Start, tempo).setMeter(Instant.Start, meter)
      val expectedBarTimes = (0 until (choreo.length.toMillis.toInt, step = msPerBar)).map(Instant(_))
      val actualBarTimes = eventTimes[Bar](choreo)
      actualBarTimes mustEqual expectedBarTimes
    }
  }

  private def eventTimes[E <: Event : ClassTag](choreo: Choreography2) = choreo.events.asSequence.collect { case e: E => e.time }

  private object ChoreographyFixture {
    val choreographyTitle = "test choreography"
    val choreographyLength = 10.seconds
    def newTestChoreography = Choreography2(choreographyTitle, choreographyLength)
  }

  private object TempoFixture {
    val tempo = Tempo(120)
    val tempoBps = tempo.beatsPerMinute / 60
    val msPerMeasure = 1000 / tempoBps
  }

  private object MeterFixture {
    import TempoFixture._
    val meter = Meter(4)
    val msPerBar = msPerMeasure * meter.measuresPerBar
  }
}
