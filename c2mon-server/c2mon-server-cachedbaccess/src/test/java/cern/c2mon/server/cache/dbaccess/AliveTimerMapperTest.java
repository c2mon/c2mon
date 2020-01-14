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
 *****************************************************************************/
package cern.c2mon.server.cache.dbaccess;

import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.alive.AliveTagCacheObject;
import cern.c2mon.shared.common.Cacheable;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

public class AliveTimerMapperTest extends AbstractMapperTest {

  private static final Long TEST_ALIVE_ID = 500000L;

  /**
   * Class to test.
   */
  @Autowired
  private AliveTimerMapper aliveTimerMapper;

  //need tests inserting process, equipment and check appear in retrieved view
  @Test
  public void testRetrieveProcessAlive() {
//    Process process = CacheObjectCreation.createTestProcess1();
//    ControlTag aliveTag = CacheObjectCreation.createTestProcessAlive();
//    Equipment equipment = CacheObjectCreation.createTestEquipment();
    //id in control tag cache is the same as in alivetimer cache
    AliveTagCacheObject retrievedCacheObject = (AliveTagCacheObject) aliveTimerMapper.getItem(1221L);
//    assertEquals(aliveTag.getId(), retrievedCacheObject.getId());
    assertEquals(60000L, (int) retrievedCacheObject.getAliveInterval());
    assertEquals("PROC", retrievedCacheObject.getAliveType());
    assertEquals(50L, (long) retrievedCacheObject.getRelatedId());
    assertEquals("P_TESTHANDLER03", retrievedCacheObject.getRelatedName());
    assertEquals(1220L, (long) retrievedCacheObject.getRelatedStateTagId());
    assertEquals(1, retrievedCacheObject.getDependentAliveTimerIds().size()); //2 dependent alive timers (eq and subeq)
    assertTrue(retrievedCacheObject.getDependentAliveTimerIds().contains(1224L));
    //assertTrue(retrievedCacheObject.getDependentAliveTimerIds().contains(testDataHelper.getSubEquipment().getAliveTagId())); only contains equipment alives!
  }

  /**
   * So far, only tests retrieved list of values is not empty.
   */
  @Test
  public void testGetAll() {
    List<AliveTag> returnList = aliveTimerMapper.getAll();
    assertTrue(returnList.size() > 0);
  }

  @Test
  public void testGetOne() {
    Cacheable item = aliveTimerMapper.getItem(1221L);
    assertNotNull(item);
  }

  @Test
  public void testIsInDB() {
    assertTrue(aliveTimerMapper.isInDb(1224L));
  }

  @Test
  public void testNotInDB() {
    assertFalse(aliveTimerMapper.isInDb(1263L));
  }


}
