package cern.tim.driver.test.events
import cern.tim.driver.test.c2mon.C2MONConnector
import cern.tim.driver.test.operations.Schedule

object Shutdown {
  def apply() = {
    new Shutdown()
  }
}
class Shutdown extends Event {
  def run() = {
    Schedule.timer.cancel()
    try {
    	C2MONConnector.onShutdown()
    } catch {
      case ex : Throwable =>
        logger.error("Shutdown failed.", ex);
    }
    EventErrors.printErrors()
  }
  override def toString() = eventId + "|Shutdown()"
}