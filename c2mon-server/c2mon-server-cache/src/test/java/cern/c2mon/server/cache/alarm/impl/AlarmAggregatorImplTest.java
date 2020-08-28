/******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.cache.alarm.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.cache.TagFacadeGateway;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.cache.alarm.AlarmAggregatorListener;
import cern.c2mon.server.cache.alarm.impl.AlarmAggregatorImpl;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.shared.common.datatag.DataTagConstants;


/**
 * Unit test of this class only, all else are mocked.
 * @author Mark Brightwell
 *
 */
public class AlarmAggregatorImplTest {

  /**
   * Class to test.
   */
  private AlarmAggregatorImpl alarmAggregator;
  
  private AlarmAggregatorNotifier notifier;
  
  /**
   * mock
   */
  private TagFacadeGateway tagFacadeGateway;
  
  /**
   * mock
   */
  private TagLocationService tagLocationService;
  
  /**
   * mock
   */
  private CacheRegistrationService cacheRegistrationService;
  
  /**
   * Mocks.
   */
  private AlarmAggregatorListener listener1;
  private AlarmAggregatorListener listener2;
  
  @Before
  public void init() {      
   tagFacadeGateway = createMock(TagFacadeGateway.class);
   tagLocationService = createMock(TagLocationService.class);
   cacheRegistrationService = createMock(CacheRegistrationService.class);   
   notifier = new AlarmAggregatorNotifier();
   alarmAggregator = new AlarmAggregatorImpl(cacheRegistrationService, tagFacadeGateway, notifier);
   
   //register 2 listeners
   listener1 = createMock(AlarmAggregatorListener.class);
   listener2 = createMock(AlarmAggregatorListener.class);
   notifier.registerForTagUpdates(listener1);
   notifier.registerForTagUpdates(listener2);
  }
    
  /**
   * Tests that a the AlarmAggregatorImpl notifies
   * (2) registered listeners if it receives a
   * cache update notification. The tag has 2 alarms
   * attached in this test.
   */
  @Test
  public void testNotifyElementUpdated() {
    DataTag tag = new DataTagCacheObject(5L, "test tag", "Float", DataTagConstants.MODE_OPERATIONAL);
    ((DataTagCacheObject)tag).setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    List<Long> alarmIds = new ArrayList<Long>();
    alarmIds.add(10L);
    alarmIds.add(20L);
    ((DataTagCacheObject) tag).setAlarmIds(alarmIds);
    List<Alarm> alarmList = new ArrayList<Alarm>();
    alarmList.add(new AlarmCacheObject(10L));
    alarmList.add(new AlarmCacheObject(20L));    

    expect(tagFacadeGateway.evaluateAlarms(tag)).andReturn(alarmList);
    listener1.notifyOnUpdate(tag, alarmList);
    listener2.notifyOnUpdate(tag, alarmList);

    expect(tagFacadeGateway.evaluateAlarms(tag)).andReturn(alarmList);
    listener1.notifyOnSupervisionChange(tag, alarmList);
    listener2.notifyOnSupervisionChange(tag, alarmList);

    replay(tagLocationService);
    replay(tagFacadeGateway);
    replay(listener1);
    replay(listener2);

    alarmAggregator.notifyElementUpdated(tag);
    alarmAggregator.onSupervisionChange(tag);
    
    verify(tagLocationService);
    verify(tagFacadeGateway);
    verify(listener1);
    verify(listener2);
  }
  
}
