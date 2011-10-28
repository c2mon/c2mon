package cern.tim.driver.test.events

object UniqueEventIdGenerator {
  
  var eventIdCounter:Long = -1
  
  def generateId():Long = {
    eventIdCounter = (eventIdCounter + 1) % Long.MaxValue
    eventIdCounter
  }

}