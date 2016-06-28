/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************///  @RequestMapping(value = "/{path:[^\\.]*}")
//  public String redirect() {
//    // Forward to home page so that route is preserved.
//    return "forward:/";
//  }
package cern.c2mon.server.cache.alarm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import cern.c2mon.server.cache.AbstractCacheIntegrationTest;
import cern.c2mon.server.cache.junit.CachePopulationRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.dbaccess.AlarmMapper;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.test.CacheObjectComparison;
import cern.c2mon.shared.client.alarm.AlarmQuery;

/**
 * Alarm cache integration tests: starts the module (alarm cache part)
 * and checks that the loading of the cache and the public methods
 * behave correctly.
 *
 * @author Mark Brightwell
 */
public class AlarmCacheTest extends AbstractCacheIntegrationTest {

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
  @Test(expected = IllegalArgumentException.class)
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
    assertEquals("Search result != 4", 4, result.size());
  }

  @Test
  public void testGetActiveAlarms() {
    AlarmQuery query = AlarmQuery.builder().active(true).build();
    AlarmCacheObject toChange = (AlarmCacheObject) alarmCache.get(350000L);
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
    assertEquals("Search result != 4", 4, result.size());
    System.out.println(result.size());
  }
}
