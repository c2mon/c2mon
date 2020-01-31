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
package cern.c2mon.cache.actions.oscillation;

import cern.c2mon.cache.actions.alarm.AlarmService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.impl.SimpleCache;
import cern.c2mon.cache.config.collections.TagCacheCollection;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.test.factory.DataTagCacheObjectFactory;
import cern.c2mon.shared.client.alarm.condition.AlarmCondition;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.*;

@Slf4j
public class OscillationUpdaterTest {

  private AlarmCacheObject alarmCacheObject;
  private OscillationUpdater oscUpdater;
  private DataTagCacheObject dataTagCacheObject;

  private C2monCache<Alarm> alarmCache = new SimpleCache<>("alarmCache");
  private OscillationProperties oscillationProperties;

  @Before
  public void setup() {
    alarmCacheObject = new AlarmCacheObject(1234L);

    oscillationProperties = new OscillationProperties();
    oscillationProperties.setOscNumbers(3);
    oscillationProperties.setTimeRange(50);

    oscUpdater = new OscillationUpdater(oscillationProperties);
    dataTagCacheObject = new DataTagCacheObjectFactory().sampleBase();


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
    long sourceTimestamp = System.currentTimeMillis() - 500000L;
    alarmCacheObject.setSourceTimestamp(new Timestamp(sourceTimestamp));
    assertFalse(alarmCacheObject.isOscillating());
    for (int i = 0; i < 20; i++) {
      alarmCacheObject.setActive(!alarmCacheObject.isActive());
      sourceTimestamp = sourceTimestamp + 1000L;
      oscUpdater.updateOscillationStatus(alarmCacheObject, sourceTimestamp);
      log.info("ALARM ACTIVE: {} - INTERNAL ACTIVE: {} - OSCILLATION: {}", alarmCacheObject.isActive(), alarmCacheObject.isInternalActive(), alarmCacheObject.isOscillating());
    }
    assertTrue(alarmCacheObject.isOscillating());
  }

  @Test
  public void testAlarmNotOscillating() {
    long sourceTimestamp = System.currentTimeMillis() - 500000L;
    alarmCacheObject.setSourceTimestamp(new Timestamp(sourceTimestamp));
    assertFalse(alarmCacheObject.isOscillating());
    for (int i = 0; i < 20; i++) {
      alarmCacheObject.setActive(!alarmCacheObject.isActive());
      sourceTimestamp = sourceTimestamp + 30000L;
      oscUpdater.updateOscillationStatus(alarmCacheObject, sourceTimestamp);
      log.info("SOURCE TIMESTAMP: {} - ALARM ACTIVE: {} - INTERNAL ACTIVE: {} - OSCILLATION: {}", alarmCacheObject.getSourceTimestamp(), alarmCacheObject.isActive(), alarmCacheObject.isInternalActive(), alarmCacheObject.isOscillating());
    }
    assertFalse(alarmCacheObject.isOscillating());
  }

  /**
   * we test if the oscillation is detected. For loop will generate a sequence
   * of alarm ACTIVE/INACTIVE and the test should give a true assertion in case
   * the oscillation is detected.
   * <p>
   * 5 oscillation in 180 seconds are the trigger and we are just in
   */
  @Test
  public void testLowOscillChanges() {
    oscillationProperties.setOscNumbers(5);
    oscillationProperties.setTimeRange(180);
    oscillationProperties.setTimeOscillationAlive(180);

    long sourceTimestamp = System.currentTimeMillis() - 500000L;
    alarmCacheObject.setSourceTimestamp(new Timestamp(sourceTimestamp));
    assertFalse(alarmCacheObject.isOscillating());
    for (int i = 0; i < 7; i++) {
      alarmCacheObject.setActive(!alarmCacheObject.isActive());
      sourceTimestamp = sourceTimestamp + 36000L;
      oscUpdater.updateOscillationStatus(alarmCacheObject, sourceTimestamp);
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

    long sourceTimestamp = System.currentTimeMillis() - 500000L;
    alarmCacheObject.setSourceTimestamp(new Timestamp(sourceTimestamp));
    assertFalse(alarmCacheObject.isOscillating());
    for (int i = 0; i < 7; i++) {
      alarmCacheObject.setActive(!alarmCacheObject.isActive());
      sourceTimestamp = sourceTimestamp + 36500L;
      oscUpdater.updateOscillationStatus(alarmCacheObject, sourceTimestamp);
      log.info("ALARM ACTIVE: {} - INTERNAL ACTIVE: {} - OSCILLATION: {}", alarmCacheObject.isActive(), alarmCacheObject.isInternalActive(), alarmCacheObject.isOscillating());
    }
    assertFalse("The alarm should NOT be set as oscillating", alarmCacheObject.isOscillating());
  }


  /**
   * 5 oscillation in 180 seconds are the trigger and we are just out. This time we start from an oscillating alarm, but
   * as only the {@link OscillationUpdateChecker} is allowed to reset the flag, the alarm should remain with the
   * oscillation flag on.
   */
  @Test
  public void testTimestampNotInRange2() {
    oscillationProperties.setOscNumbers(5);
    oscillationProperties.setTimeRange(180);
    oscillationProperties.setTimeOscillationAlive(180);

    alarmCacheObject.setOscillating(true);
    long sourceTimestamp = System.currentTimeMillis() - 500000L;
    alarmCacheObject.setSourceTimestamp(new Timestamp(sourceTimestamp));
    assertTrue(alarmCacheObject.isOscillating());
    for (int i = 0; i < 7; i++) {
      alarmCacheObject.setActive(!alarmCacheObject.isActive());
      sourceTimestamp = sourceTimestamp + 36500L;
      oscUpdater.updateOscillationStatus(alarmCacheObject, sourceTimestamp);
      log.info("ALARM ACTIVE: {} - INTERNAL ACTIVE: {} - OSCILLATION: {}", alarmCacheObject.isActive(), alarmCacheObject.isInternalActive(), alarmCacheObject.isOscillating());
    }
    assertTrue("The alarm should remain with the oscillating flag on", alarmCacheObject.isOscillating());
  }

  @Test
  public void testOnOffOscillation() {
    oscillationProperties.setOscNumbers(5);
    oscillationProperties.setTimeRange(180);
    oscillationProperties.setTimeOscillationAlive(180);

    long sourceTimestamp = System.currentTimeMillis() - 500000L;
    alarmCacheObject.setSourceTimestamp(new Timestamp(sourceTimestamp));
    assertFalse(alarmCacheObject.isOscillating());
    for (int i = 0; i < 7; i++) {
      alarmCacheObject.setActive(!alarmCacheObject.isActive());
      sourceTimestamp = sourceTimestamp + 36000L;
      oscUpdater.updateOscillationStatus(alarmCacheObject, sourceTimestamp);
      log.info("ALARM ACTIVE: {} - INTERNAL ACTIVE: {} - OSCILLATION: {}", alarmCacheObject.isActive(), alarmCacheObject.isInternalActive(), alarmCacheObject.isOscillating());
    }
    assertTrue("The alarm should be set as oscillating", alarmCacheObject.isOscillating());

    // Now we slow down and the oscillation, but the oscillation flag should remain as only the OscillationUpdateChecker
    // is allowed to remove the oscillation flag.
    for (int i = 0; i < 7; i++) {
      alarmCacheObject.setActive(!alarmCacheObject.isActive());
      sourceTimestamp = sourceTimestamp + 36500L;
      oscUpdater.updateOscillationStatus(alarmCacheObject, sourceTimestamp);
      log.info("ALARM ACTIVE: {} - INTERNAL ACTIVE: {} - OSCILLATION: {}", alarmCacheObject.isActive(), alarmCacheObject.isInternalActive(), alarmCacheObject.isOscillating());
    }
    assertTrue("The alarm should remain as oscillating", alarmCacheObject.isOscillating());
  }

  @Test
  public void testChangeConfig() {
    long sourceTimestamp = System.currentTimeMillis() - 500000L;
    alarmCacheObject.setSourceTimestamp(new Timestamp(sourceTimestamp));
    assertFalse(alarmCacheObject.isOscillating());

    for (int i = 0; i < 6; i++) {
      alarmCacheObject.setActive(!alarmCacheObject.isActive());
      sourceTimestamp = sourceTimestamp + 36000L;
      oscUpdater.updateOscillationStatus(alarmCacheObject, sourceTimestamp);
      log.info("ALARM ACTIVE: {} - INTERNAL ACTIVE: {} - OSCILLATION: {}", alarmCacheObject.isActive(), alarmCacheObject.isInternalActive(), alarmCacheObject.isOscillating());
    }
    assertFalse("The alarm should NOT be set as oscillating", alarmCacheObject.isOscillating());

    oscillationProperties.setOscNumbers(5);
    oscillationProperties.setTimeRange(180);
    oscillationProperties.setTimeOscillationAlive(180);

    // Now should see an oscillation
    for (int i = 0; i < 6; i++) {
      alarmCacheObject.setActive(!alarmCacheObject.isActive());
      sourceTimestamp = sourceTimestamp + 36000L;
      oscUpdater.updateOscillationStatus(alarmCacheObject, sourceTimestamp);
      log.info("ALARM ACTIVE: {} - INTERNAL ACTIVE: {} - OSCILLATION: {}", alarmCacheObject.isActive(), alarmCacheObject.isInternalActive(), alarmCacheObject.isOscillating());
    }
    assertTrue("The alarm should be set as oscillating", alarmCacheObject.isOscillating());
  }

  @Test
  public void testOscillationInfo() throws Exception {
    C2monCache<DataTag> dataTagCache = new SimpleCache<>("dataTagCache");
    TagCacheCollection unifiedTagCacheFacade = new TagCacheCollection(
      new SimpleCache<>("ruleTagCache"), dataTagCache, null, null, null);
    AlarmService myAlarmCacheUpdater = new AlarmService(alarmCache, unifiedTagCacheFacade, oscUpdater);

    alarmCacheObject.setOscillating(false);
    for (int i = 1; i <= oscillationProperties.getOscNumbers(); i++) {
      dataTagCacheObject.setValue(i % 2);
      dataTagCacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis() + i));

      // Notify the mock object that we expect put() to be called
      if (i <= oscillationProperties.getOscNumbers()) {
        alarmCache.put(alarmCacheObject.getId(), alarmCacheObject);
      } else {
        alarmCache.putQuiet(alarmCacheObject.getId(), alarmCacheObject);
      }
//      EasyMock.expectLastCall();
//      EasyMock.replay(alarmCache);

      myAlarmCacheUpdater.update(alarmCacheObject, dataTagCacheObject, true);
      log.info("ALARM ACTIVE: {} - INTERNAL ACTIVE: {} OSCILLATION: {} INFO: {} firstOscTS {} counter {} ", alarmCacheObject.isActive(), alarmCacheObject.isInternalActive(), alarmCacheObject.isOscillating(), alarmCacheObject.getInfo(), alarmCacheObject.getFifoSourceTimestamps().getFirst(), alarmCacheObject.getFifoSourceTimestamps().size());
      assertEquals(alarmCacheObject.isOscillating(), alarmCacheObject.getInfo().contains(Alarm.ALARM_INFO_OSC));
      if (alarmCacheObject.isOscillating()) {
        assertTrue("If an alarm is oscillating, it must be active", alarmCacheObject.isActive());
      }
//      EasyMock.verify(alarmCache);
//      EasyMock.reset(alarmCache);
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
        alarmCache.put(alarmCacheObject.getId(), alarmCacheObject);
      } else {
        alarmCache.putQuiet(alarmCacheObject.getId(), alarmCacheObject);
      }
//      EasyMock.expectLastCall();
//      EasyMock.replay(alarmCache);

      myAlarmCacheUpdater.update(alarmCacheObject, dataTagCacheObject, true);
      log.info("ALARM ACTIVE: {} - INTERNAL ACTIVE: {} OSCILLATION: {} INFO: {} firstOscTS {} counter {} ", alarmCacheObject.isActive(), alarmCacheObject.isInternalActive(), alarmCacheObject.isOscillating(), alarmCacheObject.getInfo(), alarmCacheObject.getFifoSourceTimestamps().getFirst(), alarmCacheObject.getFifoSourceTimestamps().size());

//      EasyMock.verify(alarmCache);
//      EasyMock.reset(alarmCache);
    }
  }
}
