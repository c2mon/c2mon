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
package cern.c2mon.server.cache.dbaccess;

import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.*;

public class SubEquipmentMapperTest extends AbstractMapperTest {

  /**
   * Class to test
   */
  @Inject private SubEquipmentMapper subEquipmentMapper;

  private SubEquipmentCacheObject subEquipmentCacheObject;

  @Before
  public void loadTestData() {
    subEquipmentCacheObject = (SubEquipmentCacheObject) subEquipmentMapper.getItem(250L);
  }

  /**
   * Tests the result set is none empty.
   */
  @Test
  public void testGetAll() {
    List<SubEquipment> returnList = subEquipmentMapper.getAll();
    assertTrue(returnList.size() > 0);
  }

  @Test
  public void testRetrieve() {
//    EquipmentCacheObject equipmentCacheObject = EquipmentMapperTest.createTestEquipment();
//    equipmentMapper.insertEquipment(equipmentCacheObject);
//    SubEquipmentCacheObject subEquipmentCacheObject = EquipmentMapperTest.createTestSubEquipment();
//    subEquipmentMapper.insertSubEquipment(subEquipmentCacheObject);
    SubEquipmentCacheObject retrievedObject = (SubEquipmentCacheObject) subEquipmentMapper.getItem(subEquipmentCacheObject.getId());
    assertEquals(subEquipmentCacheObject.getId(), retrievedObject.getId());
    assertEquals(subEquipmentCacheObject.getName(), retrievedObject.getName());
    assertEquals(subEquipmentCacheObject.getAliveInterval(), retrievedObject.getAliveInterval());
    assertEquals(subEquipmentCacheObject.getAliveTagId(), retrievedObject.getAliveTagId());
    assertEquals(subEquipmentCacheObject.getCommFaultTagId(), retrievedObject.getCommFaultTagId());
    assertEquals(subEquipmentCacheObject.getStateTagId(), retrievedObject.getStateTagId());
    assertEquals(subEquipmentCacheObject.getHandlerClassName(), retrievedObject.getHandlerClassName());
    assertEquals(subEquipmentCacheObject.getDescription(), retrievedObject.getDescription());
    assertEquals(subEquipmentCacheObject.getParentId(), retrievedObject.getParentId());
//    assertEquals(subEquipmentCacheObject.getStatusDescription(), retrievedObject.getStatusDescription());
//    assertEquals(subEquipmentCacheObject.getStatusTime(), retrievedObject.getStatusTime());
//    assertEquals(subEquipmentCacheObject.getSupervisionStatus(), retrievedObject.getSupervisionStatus());
//    TODO (Alex) Turn these on if we can recover status from DB
  }


  @Test
  public void testSelectSubEquipmentsByEquipment() {
    List<SubEquipment> subEquipmentList = subEquipmentMapper.selectSubEquipmentsByEquipment(subEquipmentCacheObject.getParentId());
    assertEquals(1, subEquipmentList.size());
  }

  @Test
  public void testUpdateConfig() {
    subEquipmentCacheObject.setAliveTagId(1224L);
    subEquipmentCacheObject.setCommFaultTagId(1223L);
    subEquipmentCacheObject.setStateTagId(1222L);
    subEquipmentMapper.updateSubEquipmentConfig(subEquipmentCacheObject);
    SubEquipment updatedEquipment = subEquipmentMapper.getItem(subEquipmentCacheObject.getId());
    assertEquals(1224L, updatedEquipment.getAliveTagId().longValue());
    assertEquals(1223L, updatedEquipment.getCommFaultTagId().longValue());
    assertEquals(1222L, updatedEquipment.getStateTagId().longValue());
  }

  /**
   * Tests the cache persistence method.
   */
  @Test
  @Ignore
  @DirtiesContext
  public void testUpdate() {
//    assertFalse(subEquipmentCacheObject.getSupervisionStatus().equals(SupervisionStatus.RUNNING));
//    Timestamp ts = new Timestamp(System.currentTimeMillis() + 1000);
//    subEquipmentCacheObject.setSupervision(SupervisionStatus.RUNNING,"New status description.", ts);
//    subEquipmentMapper.updateCacheable(subEquipmentCacheObject);

//    SubEquipmentCacheObject retrievedEquipment = (SubEquipmentCacheObject) subEquipmentMapper.getItem(subEquipmentCacheObject.getId());
//    assertEquals(SupervisionStatus.RUNNING, retrievedEquipment.getSupervisionStatus());
//    assertEquals(ts, retrievedEquipment.getStatusTime());
//    assertEquals("New status description.", retrievedEquipment.getStatusDescription());
//    TODO (Alex) Turn these on if we can recover status from DB
  }

  @Test
  public void testIsInDB() {
    assertTrue(subEquipmentMapper.isInDb(250L));
  }

  @Test
  public void testNotInDB() {
    assertFalse(subEquipmentMapper.isInDb(150L));
  }
}
