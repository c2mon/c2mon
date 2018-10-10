package cern.c2mon.server.alarm.impl;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.alarm.config.OscillationProperties;
import cern.c2mon.server.alarm.oscillation.OscillationUpdater;
import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmCondition;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OscillationUpdaterTest {

  private AlarmCacheObject alarmCacheObject;
  private OscillationUpdater oscUpdater;
  private DataTagCacheObject dataTagCacheObject;

  private AlarmCache alarmCache;

  @Autowired
  OscillationProperties oscillationProperties;

  @Before
  public void setup() {
    alarmCacheObject = new AlarmCacheObject();
    oscUpdater = new OscillationUpdater();
    dataTagCacheObject = new DataTagCacheObject();
    oscillationProperties = new OscillationProperties();
    oscillationProperties.setOscNumbers(3);
    oscillationProperties.setTimeRange(2);
    oscUpdater.setOscillationProperties(oscillationProperties);

    dataTagCacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis() - 60000));
    dataTagCacheObject.setValue(new Integer(0));
    DataTagQuality qual = new DataTagQualityImpl();
    qual.validate();
    dataTagCacheObject.setDataTagQuality(qual);

    alarmCacheObject.setCondition(new AlarmCondition() {

      @Override
      public boolean evaluateState(Object value) {
        return 1 == (Integer) value;
      }

      @Override
      public Object clone() {
        return null;
      }
    });

  }

  // We test if the oscillation is detected. For loop will generate a sequence
  // of alarm ACTIVE/INACTIVE and the test should give a true assertion
  // (oscillation detected) since the oscillations number threshold is lower
  // than the for oscillations counter..
  @Test
  public void testOscillDetected() {
    for (int i = 0; i < 20; i++) {
      dataTagCacheObject.setValue(i % 2);
      oscUpdater.update(alarmCacheObject, dataTagCacheObject);
    }
    assertTrue(alarmCacheObject.isOscillating());
  }

  // we test if the oscillation is detected. For loop will generate a sequence
  // of alarm ACTIVE/INACTIVE and the test should give a true assertion in case
  // the oscillation is detected
  @Test
  public void testLowOscillChanges() {
    for (int i = 0; i < 5; i++) {
      dataTagCacheObject.setValue(i % 2);
      oscUpdater.update(alarmCacheObject, dataTagCacheObject);
    }
    assertTrue(alarmCacheObject.isOscillating());
  }

  @Test
  public void testTimestampNotInRange() {
    for (int i = 0; i < 7; i++) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      dataTagCacheObject.setValue(i % 2);
      oscUpdater.update(alarmCacheObject, dataTagCacheObject);
    }
    assertTrue(alarmCacheObject.isOscillating());
  }

  @Test
  public void testTimeOscillationAlive() throws Exception {
    for (int i = 0; i < 7; i++) {
      dataTagCacheObject.setValue(i % 2);
      oscUpdater.update(alarmCacheObject, dataTagCacheObject);
    }
    Thread.sleep(100);
    assertTrue(oscUpdater.checkOscillAlive(alarmCacheObject));
  }

  @Test
  public void testOscillationInfo() throws Exception {
    AlarmCache myAlarmCache = createMock(AlarmCache.class);

    AlarmCacheUpdaterImpl myAlarmCacheUpdater = new AlarmCacheUpdaterImpl();
    myAlarmCacheUpdater.setAlarmCache(myAlarmCache);
    myAlarmCacheUpdater.setOscillationUpdater(this.oscUpdater);

    for (int i = 1; i < 5; i++) {
      dataTagCacheObject.setValue(i % 2);

      // Notify the mock object that we expect put() to be called
      if (i <= oscillationProperties.getOscNumbers()) {
        myAlarmCache.put(alarmCacheObject.getId(), alarmCacheObject);
      } else {
        myAlarmCache.putQuiet(alarmCacheObject);
      }
      EasyMock.expectLastCall(); // to call just once previous instruction

      EasyMock.replay(myAlarmCache);

      //
      myAlarmCacheUpdater.update(alarmCacheObject, dataTagCacheObject);
      log.info("ALARM ACTIVE: {} - OSCILLATION: {}", alarmCacheObject.isActive(), alarmCacheObject.isOscillating()); // it
                                                                                                                     // is
                                                                                                                     // printed
                                                                                                                     // just
                                                                                                                     // 1
                                                                                                                     // time

      assertEquals(alarmCacheObject.isOscillating(), alarmCacheObject.getInfo().contains(Alarm.ALARM_INFO_OSC));
      EasyMock.verify(myAlarmCache);
      EasyMock.reset(myAlarmCache);
    }
    log.info("===========================");
    oscUpdater.resetOscillCounter(alarmCacheObject);
    alarmCacheObject.setOscillating(false);
    alarmCacheObject.setActive(false);

    for (int i = 1; i < 9; i++) {

      dataTagCacheObject.setValue(i % 2);
      // Notify the mock object that we expect put() to be called
      if (i <= oscillationProperties.getOscNumbers()) {
        myAlarmCache.put(alarmCacheObject.getId(), alarmCacheObject);
      } else {
        myAlarmCache.putQuiet(alarmCacheObject);
      }
      // to call just once previous instruction
      EasyMock.expectLastCall();
      EasyMock.replay(myAlarmCache);

      myAlarmCacheUpdater.update(alarmCacheObject, dataTagCacheObject);
      log.info("ALARM ACTIVE: {} - OSCILLATION: {} INFO: {}", alarmCacheObject.isActive(), alarmCacheObject.isOscillating(), alarmCacheObject.getInfo());

      EasyMock.verify(myAlarmCache);
      EasyMock.reset(myAlarmCache);
    }

    log.info("===========================");

    // for (int i = 1; i < 6; i++) {
    //
    // dataTagCacheObject.setValue(0);
    //
    // if (i == 1) {
    // myAlarmCache.put(alarmCacheObject.getId(), alarmCacheObject);
    // EasyMock.expectLastCall(); // to call just once previous instruction
    // EasyMock.replay(myAlarmCache);
    //
    // }
    // myAlarmCacheUpdater.update(alarmCacheObject, dataTagCacheObject);
    // log.info("ALARM ACTIVE: {} - OSCILLATION: {}",
    // alarmCacheObject.isActive(), alarmCacheObject.isOscillating());
    // if (i == 1) {
    // EasyMock.verify(myAlarmCache);
    // EasyMock.reset(myAlarmCache);
    // }
    //
    // }
    // log.info("===========================");

  }
}
