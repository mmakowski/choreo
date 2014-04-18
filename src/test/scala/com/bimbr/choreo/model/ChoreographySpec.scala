package com.bimbr.choreo.model

import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import scala.concurrent.duration._

class ChoreographySpec extends Specification with NoTimeConversions {
  "A Choreography" should {
    "contain measure events once tempo is set" in {
      val choreo = new Choreography2("test", 10.seconds).setTempo(0.seconds, 120)
      choreo.events must not be empty // TODO: proper test
    }
  }
}
