package cern.tim.driver.test

import cern.tim.driver.test.events._
import cern.tim.driver.test.operations._

object ModuleTest {
  def main(args: Array[String]): Unit = {
    Load process "P_VCVRMTDEV01_CLASSIC.xml" // equipment id 1
//	Load process "P_CVRMTVENTLHC01"
//    Load changes "changes.xml"
//
    // Normal sequential startup
    Trigger(
//      Connect(5302L),
//      Refresh(5302L)
      Connect(1L),
      Refresh(1L)
    )

    // DAQ should handle reconnect
    Schedule(
      Change(7L) in 10 s,
      Change(8L) in 20 s,
      Change(4L) in 30 s,
      Change(5L) in 35 s,
      Change(6L) in 40 s,
      Disconnect(1L) in 30 s,
      Connect(1L) in 40 s,
      Refresh(1L) in 45 s,
      Change(1L) in 55 s,
      Change(2L) in 65 s,
      Change(3L) in 67 s,
      Refresh(1L, 0L) in 69 s
    )

    // Shutdown of all equipments and print of possible errors
    Schedule(Shutdown() in 30 s)
  }
}