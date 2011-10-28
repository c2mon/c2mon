package cern.tim.driver.test.events

import cern.tim.driver.test.LogHelper
import cern.tim.driver.test.c2mon.C2MONConnector
import cern.tim.shared.daq.config.ChangeReport

object Change {
  def apply(changeId:Long) = {
    new Change(changeId);
  }
}
class Change(changeId:Long) extends Event with LogHelper {
  var changeReport:ChangeReport = null

  def run(): Unit = { 
    if (changeReport == null)
    	C2MONConnector.applyChange(changeId, new ChangeReport(changeId));
  }
  
  override def toString() = eventId + "|Change(" + changeId + ")"

}