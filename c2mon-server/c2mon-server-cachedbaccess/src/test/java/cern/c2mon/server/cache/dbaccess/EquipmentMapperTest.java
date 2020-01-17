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

import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.shared.common.command.CommandTag;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class EquipmentMapperTest extends AbstractMapperTest {

  /**
   * Class to test
   */
  @Autowired
  private EquipmentMapper equipmentMapper;

  /**
   * Used to create subequipments of the test equipment.
   */
  @Autowired
  private SubEquipmentMapper subEquipmentMapper;

  /**
   * Used to create test datatag.
   */
  @Autowired
  private DataTagMapper dataTagMapper;

  @Autowired
  private CommandTagMapper commandTagMapper;

  private EquipmentCacheObject equipmentCacheObject;

  private SubEquipmentCacheObject subEquipmentCacheObject;

//  private SubEquipmentCacheObject subEquipmentCacheObject2;

  private DataTagCacheObject dataTagCacheObject1;

  private DataTagCacheObject dataTagCacheObject2;

  private CommandTag commandTag;

  @Before
  public void insertTestData() {
    equipmentCacheObject = (EquipmentCacheObject) equipmentMapper.getItem(150L);
    subEquipmentCacheObject = (SubEquipmentCacheObject) subEquipmentMapper.getItem(250L);
//    subEquipmentCacheObject2 = CacheObjectCreation.createTestSubEquipment2();
    dataTagCacheObject1 = (DataTagCacheObject) dataTagMapper.getItem(200000L);
    dataTagCacheObject2 = (DataTagCacheObject) dataTagMapper.getItem(200001L);
    commandTag = commandTagMapper.getItem(11000L);
  }


//    @After
//    public void deleteTestProcess() {
//      equipmentMapper.deleteProcess(Long.valueOf(100000));
//    }

  /**
   * Test does not insert associated equipment yet and test equipment retrieval
   *  - add later TODO
   */
//    @Test
//    public void testInsertAndRetrieve() {
//      ProcessCacheObject originalProcess = createTestProcess1();
//      equipmentMapper.insertProcess(originalProcess);
//
//      ProcessCacheObject retrievedProcess = (ProcessCacheObject) equipmentMapper.getItem(originalProcess.getId());
//
//      assertNotNull(retrievedProcess);
//
//      assertEquals(originalProcess.getId(), retrievedProcess.getId());
//      assertEquals(originalProcess.getName(), retrievedProcess.getName());
//      assertEquals(originalProcess.getDescription(), retrievedProcess.getDescription());
//      assertEquals(originalProcess.getMaxMessageDelay(), retrievedProcess.getMaxMessageDelay());
//      assertEquals(originalProcess.getMaxMessageSize(), retrievedProcess.getMaxMessageSize());
//      assertEquals(originalProcess.getStateTagId(), retrievedProcess.getStateTagId());
//      assertEquals(originalProcess.getAliveInterval(), retrievedProcess.getAliveInterval());
//      assertEquals(originalProcess.getAliveTagId(), retrievedProcess.getAliveTagId());
//      assertEquals(originalProcess.getState(), retrievedProcess.getState());
//      assertEquals(originalProcess.getStartupTime(), retrievedProcess.getStartupTime());
//      assertEquals(originalProcess.getCurrentHost(), retrievedProcess.getCurrentHost());
//
//    }

  /**
   * Tests the result set is none empty.
   */
  @Test
  public void testGetAll() {
    List<Equipment> returnList = equipmentMapper.getAll();
    assertTrue(returnList.size() > 0);
  }


  @Test
  public void testInsertAndRetrieve() {
//      EquipmentCacheObject equipmentCacheObject = createTestEquipment();
//      equipmentMapper.insertEquipment(equipmentCacheObject);
//      SubEquipmentCacheObject subEquipmentCacheObject = createTestSubEquipment();
//      subEquipmentMapper.insertSubEquipment(subEquipmentCacheObject);
//      DataTagCacheObject dataTagCacheObject = DataTagMapperTest.createTestDataTag();
//      dataTagMapper.insertDataTag(dataTagCacheObject);
    EquipmentCacheObject retrievedObject = (EquipmentCacheObject) equipmentMapper.getItem(equipmentCacheObject.getId());
    assertEquals(equipmentCacheObject.getId(), retrievedObject.getId());
    assertEquals(equipmentCacheObject.getName(), retrievedObject.getName());
    assertEquals(equipmentCacheObject.getAddress(), retrievedObject.getAddress());
    assertEquals(equipmentCacheObject.getAliveInterval(), retrievedObject.getAliveInterval());
    assertEquals(equipmentCacheObject.getAliveTagId(), retrievedObject.getAliveTagId());
    assertEquals(equipmentCacheObject.getStatusDescription(), retrievedObject.getStatusDescription());
    assertEquals(equipmentCacheObject.getStatusTime(), retrievedObject.getStatusTime());
    assertEquals(equipmentCacheObject.getSupervisionStatus(), retrievedObject.getSupervisionStatus());
    assertEquals(equipmentCacheObject.getCommFaultTagId(), retrievedObject.getCommFaultTagId());
    assertEquals(equipmentCacheObject.getProcessId(), retrievedObject.getProcessId());
    assertEquals(equipmentCacheObject.getStateTagId(), retrievedObject.getStateTagId());
    assertEquals(equipmentCacheObject.getHandlerClassName(), retrievedObject.getHandlerClassName());
    assertEquals(equipmentCacheObject.getDescription(), retrievedObject.getDescription());

    List<Long> subEquipmentIds = new LinkedList<Long>();
    subEquipmentIds.add(subEquipmentCacheObject.getId());
//    subEquipmentIds.add(subEquipmentCacheObject2.getId());
    assertEquals(subEquipmentIds, retrievedObject.getSubEquipmentIds());
    assertTrue(retrievedObject.getCommandTagIds().contains(commandTag.getId())); //check it contains the commandtag

    //assertEquals(dataTagCacheObject.getId(), retrievedObject.getDataTagIds().iterator().next()); //TODO just one in array so far
  }

  @Test
  public void getByName() {
    Long retrievedId = equipmentMapper.getIdByName("E_TESTHANDLER_TESTHANDLER03");
    assertTrue(retrievedId == 150L);
  }

  @Test
  public void getByNameFailure() {
    Long retrievedId = equipmentMapper.getIdByName("Test Equipment not there");

    assertNull(retrievedId);

  }

  /**
   * May fail if changes done to test DB.
   */
  @Test
  public void testTagCollectionLoading() {
    EquipmentCacheObject equipment = (EquipmentCacheObject) equipmentMapper.getItem(150L); //using test DB data for this!! TODO use permanent
    // data instead
    assertEquals(2, equipment.getCommandTagIds().size());
  }

  @Test
  public void testUpdateConfig() {
    assertEquals(new Long(1224), equipmentCacheObject.getAliveTagId());
    equipmentCacheObject.setAliveTagId(1251L);
    equipmentCacheObject.setCommFaultTagId(1252L);
    equipmentCacheObject.setStateTagId(1250L);
    equipmentMapper.updateEquipmentConfig(equipmentCacheObject);
    Equipment updatedEquipment = equipmentMapper.getItem(equipmentCacheObject.getId());
    assertEquals(new Long(1251), updatedEquipment.getAliveTagId());
    assertEquals(new Long(1252), updatedEquipment.getCommFaultTagId());
    assertEquals(new Long(1250), updatedEquipment.getStateTagId());
  }

  @Test
  public void testGetNumberItems() {
    int nbItems = equipmentMapper.getNumberItems();
    assertEquals(3, nbItems);
  }

  /**
   * Tests the cache persistence method.
   */
  @Test
  public void testUpdate() {
    assertFalse(equipmentCacheObject.getSupervisionStatus().equals(SupervisionStatus.RUNNING));
    Timestamp ts = new Timestamp(System.currentTimeMillis() + 1000);
//    equipmentCacheObject.setSupervision(SupervisionStatus.RUNNING,"New status description.", ts);
    equipmentMapper.updateCacheable(equipmentCacheObject);

    EquipmentCacheObject retrievedEquipment = (EquipmentCacheObject) equipmentMapper.getItem(equipmentCacheObject.getId());
    assertEquals(SupervisionStatus.RUNNING, retrievedEquipment.getSupervisionStatus());
    assertEquals(ts, retrievedEquipment.getStatusTime());
    assertEquals("New status description.", retrievedEquipment.getStatusDescription());
  }

  @Test
  public void testIsInDB() {
    assertTrue(equipmentMapper.isInDb(150L));
  }

  @Test
  public void testNotInDB() {
    assertFalse(equipmentMapper.isInDb(250L));
  }

}

//    /**
//     * Uses JECTEST01 alive id and state id (for FK constraints).
//     */
//    public static ProcessCacheObject createTestProcess1() {
//      ProcessCacheObject processCacheObject = new ProcessCacheObject();
//      processCacheObject.setId(new Long(100000));
//      processCacheObject.setName("Test Process");
//      processCacheObject.setDescription("Test process description");
//      processCacheObject.setMaxMessageDelay(100);
//      processCacheObject.setMaxMessageSize(100);
//      processCacheObject.setAliveInterval(60);
//      processCacheObject.setAliveTagId(new Long(100123)); //FK ref
//      processCacheObject.setStateTagId(new Long(100122)); //FK ref
//      processCacheObject.setState(ProcessState.PROCESS_DOWN);
//      processCacheObject.setStartupTime(new Timestamp(0));
//      processCacheObject.setCurrentHost("test host");
//      return processCacheObject;
//    }
//
//    public static ProcessCacheObject createTestProcess2() {
//      return null;
//    }

