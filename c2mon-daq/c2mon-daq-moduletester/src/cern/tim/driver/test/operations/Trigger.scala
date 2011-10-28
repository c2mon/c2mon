package cern.tim.driver.test.operations
import cern.tim.driver.test.events._
import java.util.Timer
import java.util.TimerTask
import java.util.Date

object Trigger {
  def apply(events: Event*) = {
    for (event <- events) {
      event.execute()
    }
  }

  def apply(events: IndexedSeq[Event]): Unit = {
    apply(events: _*)
  }
}