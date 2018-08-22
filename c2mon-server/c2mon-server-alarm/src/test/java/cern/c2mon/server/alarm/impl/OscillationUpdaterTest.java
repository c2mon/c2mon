package cern.c2mon.server.alarm.impl;

import static org.junit.Assert.assertTrue;

import org.junit.Before;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import cern.c2mon.server.alarm.config.OscillationProperties;
import cern.c2mon.server.alarm.oscillation.OscillationUpdater;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmCondition;
import cern.c2mon.server.common.datatag.DataTagCacheObject;

public class OscillationUpdaterTest {

  private AlarmCacheObject aco;
  private OscillationUpdater acu;
  private DataTagCacheObject ee;

  @Test
  public void testOscillation() {
    OscillationProperties myOsc = new OscillationProperties();
    myOsc.setOscNumbers(6);
    myOsc.setTimeRange(60);
    acu.setOscillationProperties(myOsc);

    // First case : simulation active state changes within period of time
    // - assert that the oscillation is detected
    oscillDetected();

    // Second case : no oscillation if less than 5 state changes
    // keeping always the timestamp within the range
    lowOscillChanges();

    // Third case : time stamp not in the range
    tsNotInRange();
  }

  @Before
  public void setup() {
    aco = new AlarmCacheObject();
    acu = new OscillationUpdater();
    ee = new DataTagCacheObject();
    acu.update(aco, ee);
  }

  public void oscillDetected() {
    for (int i = 0; i < 7; i++) {
      if (i % 2 == 0) {
        aco.setState(AlarmCondition.ACTIVE);
      } else {
        aco.setState(AlarmCondition.TERMINATE);
      }
      acu.update(aco, ee);
    }
    assertTrue(aco.isOscillating());
  }

  public void lowOscillChanges() {
    for (int i = 0; i < 5; i++) {
      if (i % 2 == 0) {
        aco.setState(AlarmCondition.ACTIVE);
      } else {
        aco.setState(AlarmCondition.TERMINATE);
      }
      acu.update(aco, ee);
    }
    assertFalse(aco.isOscillating());
  }

  public void tsNotInRange() {
    for (int i = 0; i < 7; i++) {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      if (i % 2 == 0) {
        aco.setState(AlarmCondition.ACTIVE);
      } else {
        aco.setState(AlarmCondition.TERMINATE);
      }
      acu.update(aco, ee);
    }
    assertTrue(aco.isOscillating());
  }
}
