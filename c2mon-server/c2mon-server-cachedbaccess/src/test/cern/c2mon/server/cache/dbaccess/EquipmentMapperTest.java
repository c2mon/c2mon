package cern.c2mon.server.cache.dbaccess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.cache.dbaccess.structure.DBBatch;
import cern.c2mon.server.cache.dbaccess.test.TestDataHelper;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.server.test.CacheObjectComparison;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;
import cern.c2mon.shared.daq.command.CommandTag;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/cache/dbaccess/config/server-cachedbaccess-test.xml"})
@TransactionConfiguration(transactionManager="cacheTransactionManager", defaultRollback=true)
@Transactional
public class EquipmentMapperTest {   

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
    private TestDataHelper testDataHelper;

    private EquipmentCacheObject equipmentCacheObject;

    private SubEquipmentCacheObject subEquipmentCacheObject;
    
    private SubEquipmentCacheObject subEquipmentCacheObject2;

    private DataTagCacheObject dataTagCacheObject1;
    
    private DataTagCacheObject dataTagCacheObject2;
    
    private CommandTag commandTag;
    
    
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
    
    /**
     * Tests the batch loading method.
     */
    @Test
    public void testGetBatch() {
      DBBatch dbBatch = new DBBatch(150L, 160L);
      List<Equipment> equipments = equipmentMapper.getRowBatch(dbBatch);
      assertNotNull(equipments);
      assertTrue(equipments.size() == 2); 
      
      DBBatch dbBatch2 = new DBBatch(150L, 159L);
      List<Equipment> equipments2 = equipmentMapper.getRowBatch(dbBatch2);
      assertNotNull(equipments2);
      assertTrue(equipments2.size() == 1);   
    }
    
    @Test
    public void testInsertAndRetrieve() {
//      EquipmentCacheObject equipmentCacheObject = createTestEquipment();
//      equipmentMapper.insertEquipment(equipmentCacheObject);
//      SubEquipmentCacheObject subEquipmentCacheObject = createTestSubEquipment();
//      subEquipmentMapper.insertSubEquipment(subEquipmentCacheObject);      
//      DataTagCacheObject dataTagCacheObject = DataTagMapperTest.createTestDataTag();
//      dataTagMapper.testInsertDataTag(dataTagCacheObject);
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
      //2 sub-equipments defined for this equipment
      List<Long> subEquipmentIds = new LinkedList<Long>();
      subEquipmentIds.add(subEquipmentCacheObject.getId());
      subEquipmentIds.add(subEquipmentCacheObject2.getId());      
      assertEquals(subEquipmentIds, retrievedObject.getSubEquipmentIds());
      assertTrue(retrievedObject.getDataTagIds().contains(dataTagCacheObject1.getId())); //contains 2 datatags - check for one ...
      assertTrue(retrievedObject.getDataTagIds().contains(dataTagCacheObject2.getId())); // ... check for the other
      assertTrue(retrievedObject.getCommandTagIds().contains(commandTag.getId())); //check it contains the commandtag
      
      //assertEquals(dataTagCacheObject.getId(), retrievedObject.getDataTagIds().iterator().next()); //TODO just one in array so far
    }
    
    /**
     * May fail if changes done to test DB.
     */
    @Test
    public void testTagCollectionLoading() {
      EquipmentCacheObject equipment = (EquipmentCacheObject) equipmentMapper.getItem(Long.valueOf(150)); //using test DB data for this!! TODO use permanent data instead
      assertEquals(6, equipment.getDataTagIds().size());
      assertEquals(2, equipment.getCommandTagIds().size());
    }
    
    @Test
    public void testUpdateConfig() {
      assertEquals(new Long(5000200), equipmentCacheObject.getAliveTagId());
      equipmentCacheObject.setAliveTagId(1251L);
      equipmentCacheObject.setCommFaultTagId(1252L);
      equipmentCacheObject.setStateTagId(1250L);
      equipmentMapper.updateEquipmentConfig(equipmentCacheObject);
      Equipment updatedEquipment = equipmentMapper.getItem(equipmentCacheObject.getId());
      assertEquals(new Long(1251), updatedEquipment.getAliveTagId());
      assertEquals(new Long(1252), updatedEquipment.getCommFaultTagId());
      assertEquals(new Long(1250), updatedEquipment.getStateTagId());      
    }
    
    
    @Before
    public void insertTestData() {
      testDataHelper.removeTestData();
      testDataHelper.createTestData();
      testDataHelper.insertTestDataIntoDB();
      equipmentCacheObject = testDataHelper.getEquipment();
      subEquipmentCacheObject = testDataHelper.getSubEquipment();
      subEquipmentCacheObject2 = testDataHelper.getSubEquipment2();      
      dataTagCacheObject1 = testDataHelper.getDataTag();
      dataTagCacheObject2 = testDataHelper.getDataTag2();
      commandTag = testDataHelper.getCommandTag();
    }
    
    @After
    public void cleanDatabase() {
      testDataHelper.removeTestData();
//      dataTagMapper.deleteDataTag(new Long(1000000));
//      subEquipmentMapper.deleteSubEquipment(new Long(300100));
//      equipmentMapper.deleteEquipment(new Long(300000));            
    } 
    
    @Test
    public void testGetMaxId() {
      //only based on data already in DB
      cleanDatabase();
      Long max = equipmentMapper.getMaxId();
      assertEquals(max, Long.valueOf(170));
    }
    
    @Test
    public void testGetMinId() {
      cleanDatabase();
      Long max = equipmentMapper.getMinId();
      assertEquals(max, Long.valueOf(150));
    }
    
    @Test
    public void testGetNumberItems() {
      cleanDatabase();
      int nbItems = equipmentMapper.getNumberItems();
      assertEquals(3, nbItems);
    }
    
    /**
     * Tests the cache persistence method.
     */
    @Test
    @DirtiesContext
    public void testUpdate() {
      assertFalse(equipmentCacheObject.getSupervisionStatus().equals(SupervisionStatus.RUNNING));
      equipmentCacheObject.setSupervisionStatus(SupervisionStatus.RUNNING); 
      Timestamp ts = new Timestamp(System.currentTimeMillis() + 1000);
      equipmentCacheObject.setStatusDescription("New status description.");
      equipmentCacheObject.setStatusTime(ts);
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

