package cern.tim.driver.test.events
import java.util.Date
import java.util.Timer
import java.util.TimerTask
import cern.tim.driver.test.LogHelper

abstract class Event extends Ordered[Event] with LogHelper {
  var eventId = UniqueEventIdGenerator.generateId()
  var creationTime = new Date()
  var executionTime: Date = null
  
  var exceptionResult: Class[_ <: Exception] = null;

  def in(delay: Long) = {
    new EventWithDelay(delay, this)
  }
  
  def expectException(exception: Class[_ <: Exception]) = {
    exceptionResult = exception
    this
  }

  def compare(that: Event) = {
    if (executionTime != null)
      executionTime.compareTo(that.executionTime)
    else if (that.executionTime != null)
      that.executionTime.compareTo(executionTime)
    else
      0
  }

  def execute() = {
    try {
      executionTime = new Date()
      logger.info("Triggering " + this)
      run()
    } catch {
      case ex: Throwable =>
        if (exceptionResult == null) {
          EventErrors.addEventError(
              this, "Unexpected exception while "
            + "executing event.", ex)
        } else if (exceptionResult != ex.getClass()) {
          EventErrors.addEventError(
              this, "Wrong exception while "
            + "executing event. Expected " + exceptionResult.getClass().getName()
            + " but got ", ex)
        }
    }
    if (exceptionResult != null)
      EventErrors addEventError (
          this, "Event result was not expected!"
        + " Expected: '" + exceptionResult + "' but got no Exception at all.")
  }

  def run()

}
