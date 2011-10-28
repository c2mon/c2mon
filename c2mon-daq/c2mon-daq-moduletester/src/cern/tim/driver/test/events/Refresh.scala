package cern.tim.driver.test.events
import cern.tim.driver.test.c2mon.C2MONConnector

object Refresh {
  def apply(equipmentId:Long) = {
    new Refresh(equipmentId)
  }
  def apply(equipmentId:Long, dataTagId:Long) = {
    val refresh = new Refresh(equipmentId)
    refresh.dataTagId = dataTagId
    refresh
  }
}
class Refresh(val equipmentId:Long) extends Event {
  
  var dataTagId:Long = -1
  
  def this(equipmentId:Long, tagId:Long) = {
    this(equipmentId)
    dataTagId = tagId
  }
  
  def run(): Unit = {
    if (dataTagId > -1)
      C2MONConnector.onRefresh(equipmentId, dataTagId)
    else
      C2MONConnector.onRefresh(equipmentId)
  }
  
  override def toString() = {
    var result = eventId + "|Refresh(" + equipmentId
    if (dataTagId > -1)
    	result += ", " + dataTagId
    result += ")"
    result
  }

}