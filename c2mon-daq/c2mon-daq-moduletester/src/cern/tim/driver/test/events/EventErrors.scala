package cern.tim.driver.test.events
import scala.collection.mutable.Set
import scala.collection.immutable.TreeSet
import java.text.SimpleDateFormat
import cern.tim.driver.test.LogHelper

object EventErrors extends LogHelper{
  
  val formater = new SimpleDateFormat("HH:mm:ss,SS")
  
  var eventErrors:TreeSet[(Event, (String, Throwable))] = 
    TreeSet.empty(Ordering.fromLessThan[(Event, (String, Throwable))](_._1 < _._1))
  
  def addEventError(event : Event, errorMessage : String, exception : Throwable) = {
    eventErrors += (event -> (errorMessage -> exception))
  }
  
  def addEventError(event : Event, errorMessage : String) = {
    eventErrors += (event -> (errorMessage -> null))
  }
  
  def printErrors() = {
    logger.info(eventErrors.size + " error(s)")
    for ((event, (message, ex)) <- eventErrors) {
      logger.error("ExecutionTime: '" + formater.format(event.executionTime) 
          + "' - " + event + " failed or had unexpected result")
      logger.error(message)
      if (ex != null) {
          logger.error("\t" + ex.getMessage())
          logger.error("\t" + ex.getClass())
          ex.getStackTrace().foreach(t => logger.error("\t" + t))
      }
    }
  }

}