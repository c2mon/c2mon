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
import cern.c2mon.cache.api.impl.SimpleC2monCache;
import cern.c2mon.cache.config.tag.UnifiedTagCacheFacade;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.ValueAlarmCondition;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.test.cache.DataTagCacheObjectFactory;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.*;

public class OscillationUpdateCheckerTest {

  private OscillationUpdateChecker oscillationUpdateChecker;
  private DataTagCacheObject dataTagCacheObject;
  private C2monCache<Alarm> alarmCache;
  private C2monCache<DataTag> dataTagCache;
  private OscillationProperties oscillationProperties;

  @Before
  public void setup() {
    // Tag caches
    dataTagCache = new SimpleC2monCache<>("dataTag");
    UnifiedTagCacheFacade unifiedTagCacheFacade = new UnifiedTagCacheFacade(new SimpleC2monCache<>("ruleTag"), dataTagCache);
    dataTagCacheObject = new DataTagCacheObjectFactory().sampleBase();
    dataTagCacheObject.setValue(Boolean.FALSE);
    dataTagCacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis() - 100000L));
    dataTagCache.put(dataTagCacheObject.getId(), dataTagCacheObject);

    // Oscillation
    oscillationProperties = new OscillationProperties();
    OscillationUpdater oscillationUpdater = new OscillationUpdater(oscillationProperties);
    OscillationService oscillationService = new OscillationService(new SimpleC2monCache<>("lastAccess"));
    oscillationService.setLastOscillationCheck(1);

    // Alarms
    alarmCache = new SimpleC2monCache<>("alarmCache");
    AlarmService alarmCacheUpdater = new AlarmService(alarmCache, unifiedTagCacheFacade, oscillationUpdater);
    oscillationUpdateChecker = new OscillationUpdateChecker(oscillationService, oscillationUpdater, alarmCacheUpdater, dataTagCache);

  }

  @Test
  public void alarmWithoutOscillationIsUnaffected() {
    AlarmCacheObject alarm = createSample();

    alarmCache.put(alarm.getId(), alarm);
    oscillationUpdateChecker.run();

    assertEquals(alarm, alarmCache.get(alarm.getId())); // No changes happened
  }

  @Test
  public void alarmWithFinishedOscillationIsStopped() {
    AlarmCacheObject alarm = createSample();
    setOscillating(alarm);

    assertNotEquals(dataTagCacheObject.getTimestamp(), alarm.getSourceTimestamp());

    alarmCache.put(alarm.getId(), alarm);
    oscillationUpdateChecker.run();

    assertNotEquals(alarm, alarmCache.get(alarm.getId()));
    checkResult(alarmCache.get(alarm.getId()), false);
  }

  @Test
  public void alarmWithOngoingOscillationIsUnaffected() {
    AlarmCacheObject alarm = createSample();
    int n = 1;
    long currentTime = System.currentTimeMillis();
    alarm.getFifoSourceTimestamps().clear();
    while (alarm.getFifoSourceTimestamps().size() < oscillationProperties.getOscNumbers()) {
      alarm.getFifoSourceTimestamps().addFirst(currentTime - (10000 * n++));
    }
    setOscillating(alarm);

    // start test
    alarmCache.put(alarm.getId(), alarm);
    oscillationUpdateChecker.run();

    // Verify result
    assertEquals(alarm, alarmCache.get(alarm.getId())); // No changes happened
  }

  /**
   * Same as before except that tag is now set to <code>true</code>
   */
  @Test
  public void testRunWithOscillation2() {
    AlarmCacheObject alarm = createSample();
    setOscillating(alarm);

    dataTagCache.compute(dataTagCacheObject.getId(), dataTag -> ((DataTagCacheObject) dataTag).setValue(Boolean.TRUE));

    alarmCache.put(alarm.getId(), alarm);
    oscillationUpdateChecker.run();
    checkResult(alarmCache.get(alarm.getId()), true);
  }

  @Test
  public void testRunWithOscillationAndInternalActiveFalse() {
    AlarmCacheObject alarm = createSample();
    setOscillating(alarm);

    alarm.setInternalActive(false);

    assertTrue(alarm.isOscillating());
    assertFalse(alarm.isInternalActive());
    assertTrue(alarm.isActive());
    assertEquals("[OSC]", alarm.getInfo());
    assertNotEquals(dataTagCacheObject.getTimestamp(), alarm.getSourceTimestamp());

    alarmCache.put(alarm.getId(), alarm);
    oscillationUpdateChecker.run();
    checkResult(alarmCache.get(alarm.getId()), false);
  }

  /**
   * Same as before except that tag is now set to <code>true</code>
   */
  @Test
  public void testRunWithOscillationAndInternalActiveFalse2() {
    AlarmCacheObject alarm = createSample();
    setOscillating(alarm);
    dataTagCache.compute(dataTagCacheObject.getId(), dataTag -> ((DataTagCacheObject) dataTag).setValue(Boolean.TRUE));
    alarm.setInternalActive(false);

    alarmCache.put(alarm.getId(), alarm);
    oscillationUpdateChecker.run();

    checkResult(alarmCache.get(alarm.getId()), true);
  }

  private AlarmCacheObject createSample() {
    AlarmCacheObject alarm = new AlarmCacheObject(123L);
    alarm.setDataTagId(dataTagCacheObject.getId());
    alarm.setActive(true);
    alarm.setInternalActive(true);
    alarm.setTimestamp(new Timestamp(System.currentTimeMillis() - 500000L));
    alarm.setSourceTimestamp(alarm.getTimestamp());
    alarm.setCondition(new ValueAlarmCondition(Boolean.TRUE));
    alarm.setOscillating(false);
    int n = 1;
    while (alarm.getFifoSourceTimestamps().size() < oscillationProperties.getOscNumbers()) {
      alarm.getFifoSourceTimestamps().add(alarm.getSourceTimestamp().getTime() + (30000 * n++));
    }
    return alarm;
  }

  private void setOscillating(AlarmCacheObject alarm) {
    alarm.setActive(true);
    alarm.setOscillating(true);
    alarm.setInternalActive(true);
    alarm.setInfo("[OSC]");
  }

  private void checkResult(Alarm alarm, boolean expectedAlarmStatus) {
    assertFalse(alarm.isOscillating());
    assertEquals(expectedAlarmStatus, alarm.isActive());
    assertEquals("[T]", alarm.getInfo());
    assertEquals(dataTagCacheObject.getTimestamp(), alarm.getSourceTimestamp());
  }
}
