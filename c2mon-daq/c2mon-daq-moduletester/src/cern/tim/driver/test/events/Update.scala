package cern.tim.driver.test.events
import java.util.Date

class Update(execTime:Date) extends Event {
  executionTime = execTime
  def run() = {}
  override def toString() = eventId + "|Update()"
}