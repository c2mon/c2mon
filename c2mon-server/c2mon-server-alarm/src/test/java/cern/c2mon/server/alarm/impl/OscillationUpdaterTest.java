package cern.c2mon.server.alarm.impl;

import static org.junit.Assert.assertTrue;

import org.junit.Before;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.alarm.config.OscillationProperties;
import cern.c2mon.server.alarm.oscillation.OscillationUpdater;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmCondition;
import cern.c2mon.server.common.datatag.DataTagCacheObject;

public class OscillationUpdaterTest {

  private AlarmCacheObject aco;
  private OscillationUpdater acu;
  private DataTagCacheObject ee;

  @Autowired
  OscillationProperties oscillationProperties;
  
  @Before
  public void setup() {
    aco = new AlarmCacheObject();
    acu = new OscillationUpdater();
    ee = new DataTagCacheObject();
    OscillationProperties myOsc = new OscillationProperties();
    myOsc.setOscNumbers(6);
    myOsc.setTimeRange(60);
    acu.setOscillationProperties(myOsc);
  }

  @Test
  public void testOscillDetected() {
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

  @Test
  public void testLowOscillChanges() {
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

  @Test
  public void testTimestampNotInRange() {
    for (int i = 0; i < 7; i++) {
      try {
        Thread.sleep(1000);
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
  
  @Test
  public void testTimeOscillationAlive() throws Exception {
    for (int i = 0; i < 7; i++) {
      if (i % 2 == 0) {
        aco.setState(AlarmCondition.ACTIVE);
      } else {
        aco.setState(AlarmCondition.TERMINATE);
      }
      acu.update(aco, ee);
    }
//    System.out.println(oscillationProperties.getTimeOscillationAlive());
    Thread.sleep(1000);
//    aco.setState(AlarmCondition.TERMINATE);
//    acu.update(aco, ee);
    assertTrue(acu.checkOscillAlive(aco));
  }
}
