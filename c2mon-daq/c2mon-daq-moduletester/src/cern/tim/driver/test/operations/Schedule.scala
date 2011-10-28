package cern.tim.driver.test.operations
import cern.tim.driver.test.events._
import java.util.Timer
import java.util.TimerTask
import java.util.Date
import cern.tim.driver.test.LogHelper

object Schedule extends LogHelper {

  val timer = new Timer("Scheduler")

  def apply(eventsWithDelay: EventWithDelay*): Unit = {
    for (eventWithDelay <- eventsWithDelay)
      schedule(
        eventWithDelay.delayTimeMs,
        eventWithDelay);
  }

  def apply(eventsWithDelay: IndexedSeq[EventWithDelay]): Unit = {
    apply(eventsWithDelay: _*)
  }

  private def schedule(time: Long, event: Event) = {
    logger.info(
        "Scheduling " + event + " in " + time.floatValue / 1000.f + "s.")
    timer.schedule(new TimerTask {
      def run() = {
        event.execute()
      }
    }, time)
  }
}