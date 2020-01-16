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
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.*;

public class SubEquipmentMapperTest extends AbstractMapperTest {

  /**
   * Class to test
   */
  @Autowired
  private SubEquipmentMapper subEquipmentMapper;

  @Autowired
  private EquipmentMapper equipmentMapper;

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
    assertEquals(subEquipmentCacheObject.getStatusDescription(), retrievedObject.getStatusDescription());
    assertEquals(subEquipmentCacheObject.getStatusTime(), retrievedObject.getStatusTime());
    assertEquals(subEquipmentCacheObject.getSupervisionStatus(), retrievedObject.getSupervisionStatus());
  }


  @Test
  public void testSelectSubEquipmentsByEquipment() {
    List<SubEquipment> subEquipmentList = subEquipmentMapper.selectSubEquipmentsByEquipment(subEquipmentCacheObject.getParentId());
    assertEquals(1, subEquipmentList.size());
  }

  @Test
  public void testUpdateConfig() {
    assertEquals(new Long(1231), subEquipmentCacheObject.getAliveTagId());
    subEquipmentCacheObject.setAliveTagId(1251L);
    subEquipmentCacheObject.setCommFaultTagId(1252L);
    subEquipmentCacheObject.setStateTagId(1250L);
    subEquipmentMapper.updateSubEquipmentConfig(subEquipmentCacheObject);
    SubEquipment updatedEquipment = subEquipmentMapper.getItem(subEquipmentCacheObject.getId());
    assertEquals(new Long(1251), updatedEquipment.getAliveTagId());
    assertEquals(new Long(1252), updatedEquipment.getCommFaultTagId());
    assertEquals(new Long(1250), updatedEquipment.getStateTagId());
  }

  /**
   * Tests the cache persistence method.
   */
  @Test
  @DirtiesContext
  public void testUpdate() {
    assertFalse(subEquipmentCacheObject.getSupervisionStatus().equals(SupervisionStatus.RUNNING));
    Timestamp ts = new Timestamp(System.currentTimeMillis() + 1000);
    subEquipmentCacheObject.setSupervision(SupervisionStatus.RUNNING,"New status description.", ts);
    subEquipmentMapper.updateCacheable(subEquipmentCacheObject);

    SubEquipmentCacheObject retrievedEquipment = (SubEquipmentCacheObject) subEquipmentMapper.getItem(subEquipmentCacheObject.getId());
    assertEquals(SupervisionStatus.RUNNING, retrievedEquipment.getSupervisionStatus());
    assertEquals(ts, retrievedEquipment.getStatusTime());
    assertEquals("New status description.", retrievedEquipment.getStatusDescription());
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
