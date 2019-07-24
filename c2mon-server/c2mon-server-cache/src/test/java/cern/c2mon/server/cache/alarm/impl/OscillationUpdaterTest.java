/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.cache.alarm.impl;

import java.sql.Timestamp;

import lombok.extern.slf4j.Slf4j;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.alarm.config.OscillationProperties;
import cern.c2mon.server.cache.alarm.oscillation.OscillationUpdater;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmCondition;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Slf4j
public class OscillationUpdaterTest {

  private AlarmCacheObject alarmCacheObject;
  private OscillationUpdater oscUpdater;
  private DataTagCacheObject dataTagCacheObject;

  @Autowired
  OscillationProperties oscillationProperties;

  @SuppressWarnings("serial")
  @Before
  public void setup() {
    alarmCacheObject = new AlarmCacheObject(1234L);
    oscUpdater = new OscillationUpdater();
    dataTagCacheObject = new DataTagCacheObject();
    oscillationProperties = new OscillationProperties();
    oscillationProperties.setOscNumbers(3);
    oscillationProperties.setTimeRange(50);
    oscUpdater.setOscillationProperties(oscillationProperties);

    dataTagCacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis() - 60000));
    dataTagCacheObject.setValue(0);
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

  /**
   * We test if the oscillation is detected. For loop will generate a sequence
   * of alarm ACTIVE/INACTIVE and the test should give a true assertion
   * (oscillation detected) since the oscillations number threshold is lower
   * than the for oscillations counter..
   */
  @Test
  public void testOscillDetected() {
    alarmCacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis() - 500000));
    assertFalse(alarmCacheObject.isOscillating());
    for (int i = 0; i < 20; i++) {
      alarmCacheObject.setActive(!alarmCacheObject.isActive());
      alarmCacheObject.setSourceTimestamp(new Timestamp(alarmCacheObject.getSourceTimestamp().getTime() + 1000));
      oscUpdater.updateOscillationStatus(alarmCacheObject);
      log.info("ALARM ACTIVE: {} - INTERNAL ACTIVE: {} - OSCILLATION: {}", alarmCacheObject.isActive(), alarmCacheObject.isInternalActive(), alarmCacheObject.isOscillating());
    }
    assertTrue(alarmCacheObject.isOscillating());
  }

  @Test
  public void testAlarmNotOscillating() {
    alarmCacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis() - 500000));
    assertFalse(alarmCacheObject.isOscillating());
    for (int i = 0; i < 20; i++) {
      alarmCacheObject.setActive(!alarmCacheObject.isActive());
      alarmCacheObject.setSourceTimestamp(new Timestamp(alarmCacheObject.getSourceTimestamp().getTime() + 30000L));
      oscUpdater.updateOscillationStatus(alarmCacheObject);
      log.info("SOURCE TIMESTAMP: {} - ALARM ACTIVE: {} - INTERNAL ACTIVE: {} - OSCILLATION: {}", alarmCacheObject.getSourceTimestamp(), alarmCacheObject.isActive(), alarmCacheObject.isInternalActive(), alarmCacheObject.isOscillating());
    }
    assertFalse(alarmCacheObject.isOscillating());
  }

  /**
   * we test if the oscillation is detected. For loop will generate a sequence
   * of alarm ACTIVE/INACTIVE and the test should give a true assertion in case
   * the oscillation is detected.
   *
   * 5 oscillation in 180 seconds are the trigger and we are just in
   */
  @Test
  public void testLowOscillChanges() {
    oscillationProperties.setOscNumbers(5);
    oscillationProperties.setTimeRange(180);
    oscillationProperties.setTimeOscillationAlive(180);

    alarmCacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis() - 500000));
    assertFalse(alarmCacheObject.isOscillating());
    for (int i = 0; i < 7; i++) {
      alarmCacheObject.setActive(!alarmCacheObject.isActive());
      alarmCacheObject.setSourceTimestamp(new Timestamp(alarmCacheObject.getSourceTimestamp().getTime() + 36000));
      oscUpdater.updateOscillationStatus(alarmCacheObject);
      log.info("ALARM ACTIVE: {} - INTERNAL ACTIVE: {} - OSCILLATION: {}", alarmCacheObject.isActive(), alarmCacheObject.isInternalActive(), alarmCacheObject.isOscillating());
    }
    assertTrue("The alarm should be set as oscillating", alarmCacheObject.isOscillating());
  }

  /**
   * 5 oscillation in 180 seconds are the trigger and we are just out
   */
  @Test
  public void testTimestampNotInRange() {
    oscillationProperties.setOscNumbers(5);
    oscillationProperties.setTimeRange(180);
    oscillationProperties.setTimeOscillationAlive(180);

    alarmCacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis() - 500000));
    assertFalse(alarmCacheObject.isOscillating());
    for (int i = 0; i < 7; i++) {
      alarmCacheObject.setActive(!alarmCacheObject.isActive());
      alarmCacheObject.setSourceTimestamp(new Timestamp(alarmCacheObject.getSourceTimestamp().getTime() + 36500));
      oscUpdater.updateOscillationStatus(alarmCacheObject);
      log.info("ALARM ACTIVE: {} - INTERNAL ACTIVE: {} - OSCILLATION: {}", alarmCacheObject.isActive(), alarmCacheObject.isInternalActive(), alarmCacheObject.isOscillating());
    }
    assertFalse("The alarm should NOT be set as oscillating", alarmCacheObject.isOscillating());
  }


  /**
   * 5 oscillation in 180 seconds are the trigger and we are just out. This time we start from an oscillating alarm
   */
  @Test
  public void testTimestampNotInRange2() {
    oscillationProperties.setOscNumbers(5);
    oscillationProperties.setTimeRange(180);
    oscillationProperties.setTimeOscillationAlive(180);

    alarmCacheObject.setOscillating(true);
    alarmCacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis() - 500000));
    assertTrue(alarmCacheObject.isOscillating());
    for (int i = 0; i < 7; i++) {
      alarmCacheObject.setActive(!alarmCacheObject.isActive());
      alarmCacheObject.setSourceTimestamp(new Timestamp(alarmCacheObject.getSourceTimestamp().getTime() + 36500));
      oscUpdater.updateOscillationStatus(alarmCacheObject);
      log.info("ALARM ACTIVE: {} - INTERNAL ACTIVE: {} - OSCILLATION: {}", alarmCacheObject.isActive(), alarmCacheObject.isInternalActive(), alarmCacheObject.isOscillating());
    }
    assertFalse("The alarm should NOT be set as oscillating", alarmCacheObject.isOscillating());
  }

  @Test
  public void testOnOffOscillation() {
    oscillationProperties.setOscNumbers(5);
    oscillationProperties.setTimeRange(180);
    oscillationProperties.setTimeOscillationAlive(180);

    alarmCacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis() - 500000));
    assertFalse(alarmCacheObject.isOscillating());
    for (int i = 0; i < 7; i++) {
      alarmCacheObject.setActive(!alarmCacheObject.isActive());
      alarmCacheObject.setSourceTimestamp(new Timestamp(alarmCacheObject.getSourceTimestamp().getTime() + 36000));
      oscUpdater.updateOscillationStatus(alarmCacheObject);
      log.info("ALARM ACTIVE: {} - INTERNAL ACTIVE: {} - OSCILLATION: {}", alarmCacheObject.isActive(), alarmCacheObject.isInternalActive(), alarmCacheObject.isOscillating());
    }
    assertTrue("The alarm should be set as oscillating", alarmCacheObject.isOscillating());

    // Now we slow down and the oscillation should be removed
    for (int i = 0; i < 7; i++) {
      alarmCacheObject.setActive(!alarmCacheObject.isActive());
      alarmCacheObject.setSourceTimestamp(new Timestamp(alarmCacheObject.getSourceTimestamp().getTime() + 36500));
      oscUpdater.updateOscillationStatus(alarmCacheObject);
      log.info("ALARM ACTIVE: {} - INTERNAL ACTIVE: {} - OSCILLATION: {}", alarmCacheObject.isActive(), alarmCacheObject.isInternalActive(), alarmCacheObject.isOscillating());
    }
    assertFalse("The alarm should NOT be set as oscillating", alarmCacheObject.isOscillating());
  }

  @Test
  public void testChangeConfig() {
    alarmCacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis() - 500000));
    assertFalse(alarmCacheObject.isOscillating());

    for (int i = 0; i < 6; i++) {
      alarmCacheObject.setActive(!alarmCacheObject.isActive());
      alarmCacheObject.setSourceTimestamp(new Timestamp(alarmCacheObject.getSourceTimestamp().getTime() + 36000));
      oscUpdater.updateOscillationStatus(alarmCacheObject);
      log.info("ALARM ACTIVE: {} - INTERNAL ACTIVE: {} - OSCILLATION: {}", alarmCacheObject.isActive(), alarmCacheObject.isInternalActive(), alarmCacheObject.isOscillating());
    }
    assertFalse("The alarm should NOT be set as oscillating", alarmCacheObject.isOscillating());

    oscillationProperties.setOscNumbers(5);
    oscillationProperties.setTimeRange(180);
    oscillationProperties.setTimeOscillationAlive(180);

    // Now should see an oscillation
    for (int i = 0; i < 6; i++) {
      alarmCacheObject.setActive(!alarmCacheObject.isActive());
      alarmCacheObject.setSourceTimestamp(new Timestamp(alarmCacheObject.getSourceTimestamp().getTime() + 36000));
      oscUpdater.updateOscillationStatus(alarmCacheObject);
      log.info("ALARM ACTIVE: {} - INTERNAL ACTIVE: {} - OSCILLATION: {}", alarmCacheObject.isActive(), alarmCacheObject.isInternalActive(), alarmCacheObject.isOscillating());
    }
    assertTrue("The alarm should be set as oscillating", alarmCacheObject.isOscillating());
  }

  @Test
  public void testOscillationInfo() throws Exception {
    AlarmCache myAlarmCache = createMock(AlarmCache.class);

    AlarmCacheUpdaterImpl myAlarmCacheUpdater = new AlarmCacheUpdaterImpl();
    myAlarmCacheUpdater.setAlarmCache(myAlarmCache);
    myAlarmCacheUpdater.setOscillationUpdater(this.oscUpdater);

    alarmCacheObject.setOscillating(false);
    for (int i = 1; i <= oscillationProperties.getOscNumbers(); i++) {
      dataTagCacheObject.setValue(i % 2);
      dataTagCacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis() + i));

      // Notify the mock object that we expect put() to be called
      if (i <= oscillationProperties.getOscNumbers()) {
        myAlarmCache.put(alarmCacheObject.getId(), alarmCacheObject);
      } else {
        myAlarmCache.putQuiet(alarmCacheObject);
      }
      EasyMock.expectLastCall();
      EasyMock.replay(myAlarmCache);

      myAlarmCacheUpdater.update(alarmCacheObject, dataTagCacheObject);
      log.info("ALARM ACTIVE: {} - INTERNAL ACTIVE: {} OSCILLATION: {} INFO: {} firstOscTS {} counter {} ", alarmCacheObject.isActive(), alarmCacheObject.isInternalActive(), alarmCacheObject.isOscillating(), alarmCacheObject.getInfo(), alarmCacheObject.getFifoSourceTimestamps().getFirst(), alarmCacheObject.getFifoSourceTimestamps().size());
      assertEquals(alarmCacheObject.isOscillating(), alarmCacheObject.getInfo().contains(Alarm.ALARM_INFO_OSC));
      if(alarmCacheObject.isOscillating()) {
        assertTrue("If an alarm is oscillating, it must be active", alarmCacheObject.isActive());
      }
      EasyMock.verify(myAlarmCache);
      EasyMock.reset(myAlarmCache);
    }
    log.info("===========================");
    alarmCacheObject.getFifoSourceTimestamps().clear();
    alarmCacheObject.setOscillating(false);
    alarmCacheObject.setActive(false);
    alarmCacheObject.setInternalActive(false);

    for (int i = 0; i < 6; i++) {

      dataTagCacheObject.setValue((i + 1) % 2);
      // Notify the mock object that we expect put() to be called
      if (i <= oscillationProperties.getOscNumbers()) {
        myAlarmCache.put(alarmCacheObject.getId(), alarmCacheObject);
      } else {
        myAlarmCache.putQuiet(alarmCacheObject);
      }
      EasyMock.expectLastCall();
      EasyMock.replay(myAlarmCache);

      myAlarmCacheUpdater.update(alarmCacheObject, dataTagCacheObject);
      log.info("ALARM ACTIVE: {} - INTERNAL ACTIVE: {} OSCILLATION: {} INFO: {} firstOscTS {} counter {} ", alarmCacheObject.isActive(), alarmCacheObject.isInternalActive(), alarmCacheObject.isOscillating(), alarmCacheObject.getInfo(), alarmCacheObject.getFifoSourceTimestamps().getFirst(), alarmCacheObject.getFifoSourceTimestamps().size());

      EasyMock.verify(myAlarmCache);
      EasyMock.reset(myAlarmCache);
    }
  }
}
