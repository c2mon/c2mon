/******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.alarm.config.OscillationProperties;
import cern.c2mon.server.cache.alarm.oscillation.OscillationUpdater;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.shared.client.alarm.condition.ValueAlarmCondition;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.util.TagQualityStatus;

public class AlarmCacheUpdaterImplTest {

  private AlarmCache alarmCache = createMock(AlarmCache.class);
  private OscillationProperties oscillationProperties;
  private OscillationUpdater oscUpdater;
  private AlarmCacheUpdaterImpl alarmCacheUpdaterImpl;
  private DataTagCacheObject tag;
  private AlarmCacheObject alarmCacheObject;

  @Before
  public void setup() {
    oscillationProperties = new OscillationProperties();
    oscillationProperties.setOscNumbers(3);
    oscillationProperties.setTimeRange(50);
    oscUpdater = new OscillationUpdater(alarmCache, oscillationProperties);
    alarmCacheUpdaterImpl = new AlarmCacheUpdaterImpl(alarmCache, oscUpdater);

    alarmCacheObject = new AlarmCacheObject();
    alarmCacheObject.setId(123L);
    alarmCacheObject.setDataTagId(987L);
    alarmCacheObject.setActive(true);
    alarmCacheObject.setInternalActive(true);
    alarmCacheObject.setTimestamp(new Timestamp(System.currentTimeMillis() - 500000L));
    alarmCacheObject.setSourceTimestamp(alarmCacheObject.getTimestamp());
    alarmCacheObject.setCondition(new ValueAlarmCondition(Boolean.TRUE));

    tag = new DataTagCacheObject();
    tag.setId(alarmCacheObject.getDataTagId());
    tag.setValue(Boolean.TRUE);
    DataTagQuality dataTagQuality = new DataTagQualityImpl();
    dataTagQuality.validate();
    tag.setDataTagQuality(dataTagQuality);
    tag.setSourceTimestamp(new Timestamp(System.currentTimeMillis() - 100000L));
  }

  /**
   * Test that the alarm timestamps are correctly changed
   */
  @Test
  public void testCommitAlarmStateChangeWithoutOscillation() {
    assertFalse(alarmCacheObject.isOscillating());
    assertTrue(alarmCacheObject.isActive());
    assertTrue((Boolean) tag.getValue());
    assertNotEquals(tag.getTimestamp(), alarmCacheObject.getSourceTimestamp());

    alarmCache.put(alarmCacheObject.getId(), alarmCacheObject);
    EasyMock.replay(alarmCache);

    // start test
    Timestamp oldAlarmTime = alarmCacheObject.getTimestamp();
    alarmCacheUpdaterImpl.doCommitAlarmStateChange(alarmCacheObject, tag, false);

    // Check result
    EasyMock.verify(alarmCache);
    assertFalse(alarmCacheObject.isOscillating());
    assertEquals(tag.getTimestamp(), alarmCacheObject.getSourceTimestamp());
    assertNotEquals(oldAlarmTime, alarmCacheObject.getTimestamp());
    assertEquals(Long.valueOf(tag.getTimestamp().getTime()), alarmCacheObject.getFifoSourceTimestamps().getFirst());
  }

  /**
   * Test that the alarm timestamps are correctly changed and alarm is set as oscillating
   */
  @Test
  public void testCommitAlarmStateChangeWithOscillationDetect() {
    testCommitAlarmStateChangeWithOscillationDetect(DataTagConstants.MODE_OPERATIONAL);
    assertEquals("[OSC]", alarmCacheObject.getInfo());
  }

  /**
   * Test that the alarm timestamps are correctly changed and alarm is set as oscillating
   */
  @Test
  public void testCommitAlarmStateChangeWithOscillationDetectForNotConfigured() {
    testCommitAlarmStateChangeWithOscillationDetect(DataTagConstants.MODE_NOTCONFIGURED);
    assertEquals("[OSC]", alarmCacheObject.getInfo());
  }

  /**
   * Test that the alarm timestamps are correctly changed and alarm is set as oscillating
   */
  @Test
  public void testCommitAlarmStateChangeWithOscillationDetectForTestTag() {
    testCommitAlarmStateChangeWithOscillationDetect(DataTagConstants.MODE_TEST);
    assertEquals("[T][OSC]", alarmCacheObject.getInfo());
  }

  /**
   * Test that the alarm timestamps are correctly changed and alarm is set as oscillating
   */
  @Test
  public void testCommitAlarmStateChangeWithOscillationDetectForMaintenanceTag() {
    testCommitAlarmStateChangeWithOscillationDetect(DataTagConstants.MODE_MAINTENANCE);
    assertEquals("[M][OSC]", alarmCacheObject.getInfo());
  }

  /**
   * Test that the alarm timestamps are correctly changed and alarm is set as oscillating
   */
  @Test
  public void testCommitAlarmStateChangeWithOscillationDetectForSimulationTags() {
    tag.setSimulated(true);
    testCommitAlarmStateChangeWithOscillationDetect(DataTagConstants.MODE_OPERATIONAL);
    assertEquals("[OSC][SIM]", alarmCacheObject.getInfo());
  }

  @Test
  public void testCommitAlarmStateChangeWithOscillationDetectForSimulationTagsInTest() {
    tag.setSimulated(true);
    testCommitAlarmStateChangeWithOscillationDetect(DataTagConstants.MODE_TEST);
    assertEquals("[T][OSC][SIM]", alarmCacheObject.getInfo());
  }

  @Test
  public void testCommitAlarmStateChangeWithOscillationDetectForSimulationTagsInMaintenance() {
    tag.setSimulated(true);
    testCommitAlarmStateChangeWithOscillationDetect(DataTagConstants.MODE_MAINTENANCE);
    assertEquals("[M][OSC][SIM]", alarmCacheObject.getInfo());
  }

  /**
   * Test that the alarm timestamps are correctly changed and the FIFO list is still empty
   */
  @Test
  public void testCommitAlarmStateChangeWithOscillationReset() {
    alarmCacheObject.setInfo("[OSC]");

    assertFalse(alarmCacheObject.isOscillating());
    assertTrue(alarmCacheObject.isActive());
    assertTrue((Boolean) tag.getValue());
    assertNotEquals(tag.getTimestamp(), alarmCacheObject.getSourceTimestamp());

    alarmCache.put(alarmCacheObject.getId(), alarmCacheObject);
    EasyMock.replay(alarmCache);

    // start test
    Timestamp oldAlarmTime = alarmCacheObject.getTimestamp();
    alarmCacheUpdaterImpl.doCommitAlarmStateChange(alarmCacheObject, tag, true);

    // Check result
    EasyMock.verify(alarmCache);
    assertEquals("", alarmCacheObject.getInfo());
    assertFalse(alarmCacheObject.isOscillating());
    assertEquals(tag.getTimestamp(), alarmCacheObject.getSourceTimestamp());
    assertNotEquals(oldAlarmTime, alarmCacheObject.getTimestamp());
    assertTrue(alarmCacheObject.getFifoSourceTimestamps().isEmpty());
  }
  
  /**
   * Test that the alarm timestamps are correctly changed and the FIFO list is still empty
   */
  @Test
  public void testCommitAlarmStateChangeWithOscillationResetForTagInvalid() {
    alarmCacheObject.setInfo("[OSC]");
    tag.setDataTagQuality(new DataTagQualityImpl(TagQualityStatus.UNKNOWN_REASON));

    assertFalse(alarmCacheObject.isOscillating());
    assertTrue(alarmCacheObject.isActive());
    assertTrue((Boolean) tag.getValue());
    assertFalse((Boolean) tag.isValid());
    assertNotEquals(tag.getTimestamp(), alarmCacheObject.getSourceTimestamp());

    alarmCache.put(alarmCacheObject.getId(), alarmCacheObject);
    EasyMock.replay(alarmCache);

    // start test
    Timestamp oldAlarmTime = alarmCacheObject.getTimestamp();
    alarmCacheUpdaterImpl.doCommitAlarmStateChange(alarmCacheObject, tag, true);

    // Check result
    EasyMock.verify(alarmCache);
    assertEquals("[?]", alarmCacheObject.getInfo());
    assertFalse(alarmCacheObject.isOscillating());
    assertEquals(tag.getTimestamp(), alarmCacheObject.getSourceTimestamp());
    assertNotEquals(oldAlarmTime, alarmCacheObject.getTimestamp());
    assertTrue(alarmCacheObject.getFifoSourceTimestamps().isEmpty());
  }

  /**
   * Test that the alarm timestamps remain for oscillating alarms
   */
  @Test
  public void testCommitAlarmStateChangeWithOscillation() {
    alarmCacheObject.setOscillating(true);
    alarmCacheObject.setInfo("[OSC]");
    assertTrue(alarmCacheObject.isOscillating());
    assertTrue(alarmCacheObject.isActive());
    assertTrue((Boolean) tag.getValue());
    assertNotEquals(tag.getTimestamp(), alarmCacheObject.getSourceTimestamp());

    alarmCache.putQuiet(alarmCacheObject);
    EasyMock.replay(alarmCache);

    // start test
    Timestamp oldAlarmTime = alarmCacheObject.getTimestamp();
    alarmCacheUpdaterImpl.doCommitAlarmStateChange(alarmCacheObject, tag, false);

    // Check result
    EasyMock.verify(alarmCache);
    assertTrue(alarmCacheObject.isOscillating());
    assertNotEquals(tag.getTimestamp(), alarmCacheObject.getSourceTimestamp());
    assertEquals(oldAlarmTime, alarmCacheObject.getTimestamp());
    assertEquals(Long.valueOf(tag.getTimestamp().getTime()), alarmCacheObject.getFifoSourceTimestamps().getFirst());
  }

  private void testCommitAlarmStateChangeWithOscillationDetect(short mode) {
    tag.setMode(mode);

    assertFalse(alarmCacheObject.isOscillating());
    assertEquals("", alarmCacheObject.getInfo());
    assertTrue(alarmCacheObject.isActive());
    assertTrue((Boolean) tag.getValue());
    assertNotEquals(tag.getTimestamp(), alarmCacheObject.getSourceTimestamp());

    int n = 1;
    while(alarmCacheObject.getFifoSourceTimestamps().size() < oscillationProperties.getOscNumbers()) {
      alarmCacheObject.getFifoSourceTimestamps().addFirst(tag.getSourceTimestamp().getTime() - (1000 * n++));
    }

    alarmCache.put(alarmCacheObject.getId(), alarmCacheObject);
    EasyMock.replay(alarmCache);

    // start test
    Timestamp oldAlarmTime = alarmCacheObject.getTimestamp();
    alarmCacheUpdaterImpl.doCommitAlarmStateChange(alarmCacheObject, tag, false);

    // Check result
    EasyMock.verify(alarmCache);
    assertTrue(alarmCacheObject.isOscillating());
    assertEquals(tag.getTimestamp(), alarmCacheObject.getSourceTimestamp());
    assertNotEquals(oldAlarmTime, alarmCacheObject.getTimestamp());
    assertEquals(Long.valueOf(tag.getTimestamp().getTime()), alarmCacheObject.getFifoSourceTimestamps().getLast());
  }
}
