package cern.tim.driver.test.events
import java.util.Date
import cern.tim.driver.test.LogHelper
import cern.tim.driver.test.c2mon.C2MONConnector
object Command {
  def apply(commandId:Long) = {
    new Command(commandId)
  }
}
class Command(commandId: Long)
  extends Event with LogHelper {

  var eqId = -1L
  var normalResult = "";
  var value:AnyRef = null;

  def onEquipment(equipmentId:Long) = {
    eqId = equipmentId; 
    this
  }
  
  def withValue(theValue:Any) = {
    value = theValue.asInstanceOf[AnyRef]
    this
  }
  
  def expectResult(result:String) = {
    normalResult = result
    this
  }
  
  def run() = {
    val result =
      if (eqId > -1)
        C2MONConnector.onCommand(eqId, commandId, value)
      else
        C2MONConnector.onCommand(commandId, value)
   
    if (normalResult != "" && normalResult != result)
      EventErrors addEventError(this, "Command result was not expected!"
        + " Expected: '" + normalResult + "' but got '" + result + "'")
  }
  
  override def toString() = eventId + "|Command(" + commandId + ", '" + value + "')"
}