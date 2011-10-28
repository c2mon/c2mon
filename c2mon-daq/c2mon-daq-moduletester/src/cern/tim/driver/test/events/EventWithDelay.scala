package cern.tim.driver.test.events
import java.util.Date
import cern.tim.driver.test.LogHelper

class EventWithDelay(val delayTime:Long, event:Event) 
    extends Event with LogHelper {
  
  exceptionResult  = event.exceptionResult
  creationTime = event.creationTime
  eventId = event.eventId
  
  var factor = 1000L
  
  def s() = {
    this
  }

  def ms() = {
    factor = 1
    this
  }
  
  def run() = {
    event.executionTime = new Date()
    event.run()
  }
  
  def delayTimeMs = delayTime * factor
  
  override def toString() = event.toString()
}