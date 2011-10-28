package cern.tim.driver.test.events
import cern.tim.driver.test.c2mon.C2MONConnector

object Connect {
  def apply(equipmentId: Long) = {
    new Connect(equipmentId)
  }
}
class Connect(equipmentId: Long) extends Event {
  def run() = {
    C2MONConnector.onConnect(equipmentId)
  }
  override def toString() = eventId + "|Connect(" + equipmentId + ")"
}
