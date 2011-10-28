package cern.tim.driver.test.events
import cern.tim.driver.test.c2mon.C2MONConnector

object Disconnect {
  def apply(equipmentId: Long) = {
    new Disconnect(equipmentId)
  }
}
class Disconnect(equipmentId: Long) extends Event {
  def run() = {
    C2MONConnector.onDisconnect(equipmentId)
  }
  override def toString() = eventId + "|Disconnect(" + equipmentId + ")"
}