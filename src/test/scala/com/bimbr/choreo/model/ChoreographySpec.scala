package com.bimbr.choreo.model

import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import scala.concurrent.duration._

class ChoreographySpec extends Specification with NoTimeConversions {
  "A Choreography" should {
    "contain measure events at appropriate time intervals once tempo is set" in {
      val tempoBpm = 120
      val tempoBps = tempoBpm / 60
      val msPerMeasure = 1000 / tempoBps
      val choreo = new Choreography2("test", 10.seconds).setTempo(0.seconds, 120)
      val expectedMeasureTimes = (0 to (choreo.length.toSeconds.toInt * tempoBps)).map(t => (t * msPerMeasure).milliseconds)
      val actualMeasureTimes = choreo.events.collect {
        case Measure(t) => t
      }
      actualMeasureTimes mustEqual expectedMeasureTimes
    }
  }
}
