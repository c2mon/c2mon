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
package cern.c2mon.server.cache.alarm.oscillation;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.AlarmFacade;
import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.TagFacadeGateway;
import cern.c2mon.server.cache.alarm.config.OscillationProperties;
import cern.c2mon.server.cache.alarm.impl.AlarmCacheUpdaterImpl;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmCacheUpdater;
import cern.c2mon.server.common.alarm.ValueAlarmCondition;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class OscillationUpdateCheckerTest {

  private ClusterCache clusterCache;
  private OscillationUpdateChecker oscillationUpdateChecker;
  private AlarmCacheObject alarm;
  private DataTagCacheObject tag;
  private AlarmCache alarmCache;
  private TagFacadeGateway tagFacadeGateway;
  OscillationProperties oscillationProperties;

  @Before
  public void setup() {
    alarmCache = EasyMock.createStrictMock(AlarmCache.class);
    clusterCache = EasyMock.createNiceMock(ClusterCache.class);
    tagFacadeGateway = EasyMock.createStrictMock(TagFacadeGateway.class);
    oscillationProperties = new OscillationProperties();
    OscillationUpdater oscillationUpdater = new OscillationUpdater(alarmCache, oscillationProperties);
    AlarmCacheUpdater alarmCacheUpdater = new AlarmCacheUpdaterImpl(alarmCache, oscillationUpdater);
    AlarmFacade alarmFacade = EasyMock.createNiceMock(AlarmFacade.class);
    oscillationUpdateChecker = new OscillationUpdateChecker(alarmCache, clusterCache, oscillationUpdater, alarmCacheUpdater, tagFacadeGateway, alarmFacade);

    tag = new DataTagCacheObject();
    tag.setId(987L);
    tag.setValue(Boolean.FALSE);
    DataTagQuality dataTagQuality = new DataTagQualityImpl();
    dataTagQuality.validate();
    tag.setDataTagQuality(dataTagQuality);
    tag.setSourceTimestamp(new Timestamp(System.currentTimeMillis() - 100000L));

    alarm = new AlarmCacheObject();
    alarm.setId(123L);
    alarm.setDataTagId(tag.getId());
    alarm.setActive(true);
    alarm.setInternalActive(true);
    alarm.setTimestamp(new Timestamp(System.currentTimeMillis() - 500000L));
    alarm.setSourceTimestamp(alarm.getTimestamp());
    alarm.setCondition(new ValueAlarmCondition(Boolean.TRUE));
    int n = 1;
    while(alarm.getFifoSourceTimestamps().size() < oscillationProperties.getOscNumbers()) {
      alarm.getFifoSourceTimestamps().add(alarm.getSourceTimestamp().getTime() + (30000 * n++));
    }
  }

  @Test
  public void testRunWithNoOscillation() {
    assertFalse(alarm.isOscillating());
    assertTrue(alarm.isInternalActive());
    assertTrue(alarm.isActive());
    assertEquals("", alarm.getInfo());
    assertNotEquals(tag.getTimestamp(), alarm.getSourceTimestamp());

    recordMockCallsForNoOscillatingAlarm();
    oscillationUpdateChecker.run();
    EasyMock.verify(clusterCache, alarmCache);

    assertFalse(alarm.isOscillating());
    assertTrue(alarm.isInternalActive());
    assertTrue(alarm.isActive());
    assertEquals("", alarm.getInfo());
    assertNotEquals(tag.getTimestamp(), alarm.getSourceTimestamp());
  }

  @Test
  public void testRunWithOscillation() {
    setOscillating(alarm);
    assertTrue(alarm.isOscillating());
    assertTrue(alarm.isInternalActive());
    assertTrue(alarm.isActive());
    assertEquals("[OSC]", alarm.getInfo());
    assertNotEquals(tag.getTimestamp(), alarm.getSourceTimestamp());

    recordMockCallsForOscillatingAlarm();
    oscillationUpdateChecker.run();
    checkResult(false);
  }

  @Test
  public void testRunWithKeepingOscillation() {
    int n = 1;
    long currentTime = System.currentTimeMillis();
    alarm.getFifoSourceTimestamps().clear();
    while(alarm.getFifoSourceTimestamps().size() < oscillationProperties.getOscNumbers()) {
      alarm.getFifoSourceTimestamps().addFirst(currentTime - (10000 * n++));
    }
    setOscillating(alarm);
    assertTrue(alarm.isOscillating());
    assertTrue(alarm.isInternalActive());
    assertTrue(alarm.isActive());
    assertEquals("[OSC]", alarm.getInfo());
    assertNotEquals(tag.getTimestamp(), alarm.getSourceTimestamp());

    // record mocks
    EasyMock.reset(clusterCache, alarmCache, tagFacadeGateway);
    clusterCache.acquireWriteLockOnKey(OscillationUpdateChecker.LAST_CHECK_LONG);
    EasyMock.expect(clusterCache.getCopy(OscillationUpdateChecker.LAST_CHECK_LONG))
        .andReturn(Long.valueOf(OscillationUpdateChecker.SCAN_INTERVAL - 1000L));
    EasyMock.expect(alarmCache.findAlarm(oscillationUpdateChecker.alarmCacheQuery)).andReturn(Arrays.asList(alarm.getId()));
    EasyMock.expect(alarmCache.getCopy(alarm.getId())).andReturn(alarm);
    EasyMock.replay(clusterCache, alarmCache, tagFacadeGateway);

    // start test
    oscillationUpdateChecker.run();

    // Verify result
    EasyMock.verify(clusterCache, alarmCache, tagFacadeGateway);
    assertTrue(alarm.isOscillating());
    assertTrue(alarm.isInternalActive()); // remains unchanged as the evaluation does not take place
    assertEquals(true, alarm.isActive());
    assertEquals("[OSC]", alarm.getInfo());
    assertNotEquals(tag.getTimestamp(), alarm.getSourceTimestamp());
  }

  /**
   * Same as before except that tag is now set to <code>true</code>
   */
  @Test
  public void testRunWithOscillation2() {
    setOscillating(alarm);
    tag.setValue(Boolean.TRUE);
    assertTrue(alarm.isOscillating());
    assertTrue(alarm.isInternalActive());
    assertTrue(alarm.isActive());
    assertEquals("[OSC]", alarm.getInfo());
    assertNotEquals(tag.getTimestamp(), alarm.getSourceTimestamp());

    recordMockCallsForOscillatingAlarm();
    oscillationUpdateChecker.run();
    checkResult(true);
  }

  @Test
  public void testRunWithOscillationAndInternalActiveFalse() {
    setOscillating(alarm);
    alarm.setInternalActive(false);

    assertTrue(alarm.isOscillating());
    assertFalse(alarm.isInternalActive());
    assertTrue(alarm.isActive());
    assertEquals("[OSC]", alarm.getInfo());
    assertNotEquals(tag.getTimestamp(), alarm.getSourceTimestamp());

    recordMockCallsForOscillatingAlarm();
    oscillationUpdateChecker.run();
    checkResult(false);
  }

  /**
   * Same as before except that tag is now set to <code>true</code>
   */
  @Test
  public void testRunWithOscillationAndInternalActiveFalse2() {
    setOscillating(alarm);
    tag.setValue(Boolean.TRUE);
    alarm.setInternalActive(false);

    assertTrue(alarm.isOscillating());
    assertFalse(alarm.isInternalActive());
    assertTrue(alarm.isActive());
    assertEquals("[OSC]", alarm.getInfo());
    assertNotEquals(tag.getTimestamp(), alarm.getSourceTimestamp());

    recordMockCallsForOscillatingAlarm();
    oscillationUpdateChecker.run();
    checkResult(true);
  }

  private void recordMockCallsForOscillatingAlarm() {
    EasyMock.reset(clusterCache, alarmCache, tagFacadeGateway);
    clusterCache.acquireWriteLockOnKey(OscillationUpdateChecker.LAST_CHECK_LONG);
    EasyMock.expect(clusterCache.getCopy(OscillationUpdateChecker.LAST_CHECK_LONG))
        .andReturn(Long.valueOf(OscillationUpdateChecker.SCAN_INTERVAL - 1000L));
    EasyMock.expect(alarmCache.findAlarm(oscillationUpdateChecker.alarmCacheQuery)).andReturn(Arrays.asList(alarm.getId()));
    EasyMock.expect(alarmCache.getCopy(alarm.getId())).andReturn(alarm);
    alarmCache.put(alarm.getId(), alarm);
    EasyMock.expect(tagFacadeGateway.getTag(alarm.getDataTagId())).andReturn(tag);
    EasyMock.replay(clusterCache, alarmCache, tagFacadeGateway);
  }

  private void recordMockCallsForNoOscillatingAlarm() {
    EasyMock.reset(clusterCache, alarmCache, tagFacadeGateway);
    clusterCache.acquireWriteLockOnKey(OscillationUpdateChecker.LAST_CHECK_LONG);
    EasyMock.expect(clusterCache.getCopy(OscillationUpdateChecker.LAST_CHECK_LONG))
        .andReturn(Long.valueOf(OscillationUpdateChecker.SCAN_INTERVAL - 1000L));
    EasyMock.expect(alarmCache.findAlarm(oscillationUpdateChecker.alarmCacheQuery)).andReturn(new ArrayList<Long>());
    EasyMock.replay(clusterCache, alarmCache, tagFacadeGateway);
  }

  private void setOscillating(AlarmCacheObject alarm) {
    alarm.setActive(true);
    alarm.setOscillating(true);
    alarm.setInternalActive(true);
    alarm.setInfo("[OSC]");
  }

  private void checkResult(boolean expectedAlarmStatus) {
    EasyMock.verify(clusterCache, alarmCache, tagFacadeGateway);

    assertFalse(alarm.isOscillating());
    assertEquals(expectedAlarmStatus, alarm.isActive());
    assertEquals("", alarm.getInfo());
    assertEquals(tag.getTimestamp(), alarm.getSourceTimestamp());
  }
}
