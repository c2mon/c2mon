/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.cache.alarm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.dbaccess.AlarmMapper;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmCondition;
import cern.c2mon.server.common.alarm.AlarmCacheObject.AlarmChangeState;
import cern.c2mon.server.test.CacheObjectComparison;
import cern.c2mon.shared.client.alarm.AlarmQuery;


@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration({"classpath:cern/c2mon/server/cache/config/server-cache-alarm-test.xml"} )                     
 /**
 * Alarm cache integration tests: starts the module (alarm cache part)
 * and checks that the loading of the cache and the public methods
 * behave correctly.
 * 
 * @author Mark Brightwell
 *
 */
public class AlarmCacheTest {

  @Autowired
  private AlarmCacheImpl alarmCache;
  
  @Autowired
  private AlarmMapper alarmMapper;
  
  @Test
  public void testCacheLoading() {
    assertNotNull(alarmCache);
    
    List<Alarm> alarmList = alarmMapper.getAll();
    
    //test the cache was loaded correctly
    assertEquals(alarmList.size(), alarmCache.getCache().getKeys().size());
    //compare all the objects from the cache and buffer
    Iterator<Alarm> it = alarmList.iterator();
    while (it.hasNext()) {
      Alarm alarm = (Alarm) it.next();
      //compare ids of associated datatags
      assertEquals(alarm.getTagId(), ((Alarm) alarmCache.getCopy(alarm.getId())).getTagId());
    }
  }
  
  /**
   * If null is used as  a key, an exception should be thrown. 
   */
  @Test(expected=IllegalArgumentException.class)
  public void testGetWithNull() {
    //test robustness to null call
    ((AlarmCache) alarmCache).getCopy(null);
  }
  
  /**
   * Tests the getCopy method retrieves an existing Alarm correctly. 
   */
  @Test
  public void testGet() {
    AlarmCacheObject cacheObject = (AlarmCacheObject) alarmCache.getCopy(350000L);
    AlarmCacheObject objectInDb = (AlarmCacheObject) alarmMapper.getItem(350000L);
    CacheObjectComparison.equals(cacheObject, objectInDb);
  }
  
  @Test
  public void testFindAlarms() {
      AlarmQuery query = AlarmQuery.builder().faultFamily("TEST_*").build();
      
      Collection<Long> result = alarmCache.findAlarm(query);
      assertNotNull(result);
      assertEquals("Search result != 2", 2, result.size());
  }
  
  @Test
  @DirtiesContext
  public void testGetActiveAlarms() {
      AlarmQuery query = AlarmQuery.builder().active(true).build();
      AlarmCacheObject toChange = (AlarmCacheObject)alarmCache.get(350000L);
      toChange.setState("ACTIVE");
      
      alarmCache.putQuiet(toChange);
      Collection<Long> result = alarmCache.findAlarm(query);
      assertNotNull(result);
      assertEquals("Search result != 1", 1, result.size());
  }
  
  @Test
  public void testGetAlarmsByCodeAndFamily() {
      AlarmQuery query = AlarmQuery.builder().faultFamily("TEST_*").faultCode(20).build();
      Collection<Long> result = alarmCache.findAlarm(query);
      assertNotNull(result);
      assertEquals("Search result != 2", 2, result.size());
      System.out.println(result.size());
  }
  
  
  
}
