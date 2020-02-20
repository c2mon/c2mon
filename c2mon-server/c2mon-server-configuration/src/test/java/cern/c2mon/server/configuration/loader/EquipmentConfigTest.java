package cern.c2mon.server.configuration.loader;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cache.dbaccess.EquipmentMapper;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.configuration.parser.util.ConfigurationEquipmentUtil;
import cern.c2mon.server.configuration.util.TestConfigurationProvider;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import org.junit.Test;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class EquipmentConfigTest extends ConfigurationCacheLoaderTest<Equipment> {

  @Inject private C2monCache<Equipment> equipmentCache;

  @Inject private EquipmentMapper equipmentMapper;

  @Inject private C2monCache<AliveTag> aliveTimerCache;

  @Inject private C2monCache<CommFaultTag> commFaultTagCache;

  @Inject private C2monCache<Process> processCache;

  @Inject private C2monCache<DataTag> dataTagCache;

  @Inject private DataTagMapper dataTagMapper;

  /**
   * Test the creation of equipment
   */
  @Test
  public void createFromDb() {
    processService.start(50L, 0);

    ConfigurationReport report = configurationLoader.applyConfiguration(13);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus()); // ok as DAQ handles Equipment
    // creation
    assertFalse(report.getProcessesToReboot().contains("P_TESTHANDLER03"));
  }

  @Test
  public void createFromDbLoadsCache() {
    EquipmentCacheObject expectedObject = expectedObject();

    configurationLoader.applyConfiguration(13);

    assertEquals(expectedObject, equipmentCache.get(110L));
  }

  @Test
  public void createFromDbUpdatesProcess() {
    EquipmentCacheObject expectedObject = expectedObject();

    configurationLoader.applyConfiguration(13);

    // also check that the process cache was updated
    Process process = processCache.get(expectedObject.getProcessId());
    assertTrue(process.getEquipmentIds().contains(expectedObject.getId()));
  }

  @Test
  public void createFromDbCreatesControlTags() {
    EquipmentCacheObject expectedObject = expectedObject();

    configurationLoader.applyConfiguration(13);

    // the alivetimer and commfault have overriden those already in the cache
    // (check reference to the equipment has changed)
    assertNotNull(commFaultTagCache.get(expectedObject.getCommFaultTagId()));
    assertEquals(expectedObject.getId(), (long) commFaultTagCache.get(expectedObject.getCommFaultTagId()).getSupervisedId());
  }

  @Test
  public void updateFromDb(){
    processService.start(50L, 0);
    configurationLoader.applyConfiguration(13);

    ConfigurationReport report = configurationLoader.applyConfiguration(25);

    // expect 2 top elements (control and equipment, with control first)
    // equipment report should have 1 sub-reports from DAQ (control tag has no
    // address)
    List<ConfigurationElementReport> topList = report.getElementReports();
    assertEquals(2, topList.size());
    assertEquals(topList.get(0).getEntity(), ConfigConstants.Entity.CONTROLTAG);
    assertEquals(topList.get(1).getEntity(), ConfigConstants.Entity.EQUIPMENT);
    assertEquals(0, topList.get(0).getSubreports().size());
    // 2 sub-reports: One for Equipment alive tag creation and another for the actual equipment
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    // should contain both a controltag update and equipment update
    // (note the DAQ response is generated automatically by the mock)
    assertTrue(report.toXML().contains("CONTROLTAG"));
    assertTrue(report.toXML().contains("EQUIPMENT"));

    // checks restart status is correctly set (DAQ call is mocked as success for
    // equipment update)
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());

    EquipmentCacheObject expectedObject = expectedObject();
    expectedObject.setDescription("updated description");
    expectedObject.setAddress("serverHostName=VGTCVENTTEST;test");
    expectedObject.setAliveTagId(1251L);

    assertEquals(expectedObject, equipmentCache.get(110L));

    // check alive timer reference is updated in DB
    assertEquals(new Long(1251L), equipmentMapper.getItem(110L).getAliveTagId());
    // also expect alivetimercache to have element 501:
    assertNotNull(aliveTimerCache.get(1251L));
  }

  /**
   * Test equipment and control tags are removed correctly.
   */
  @Test
  public void testRemoveEquipment() {
    processService.start(50L, System.currentTimeMillis());
    // check as expected
    Equipment equipment = equipmentCache.get(150L);
    assertNotNull(equipment);
    assertNotNull(equipmentMapper.getItem(150L));
    assertTrue(aliveTimerCache.containsKey(equipment.getAliveTagId()));
    assertTrue(commFaultTagCache.containsKey(equipment.getCommFaultTagId()));

    // remove equipment
    // remove completes successfully; both Equipment and ControlTags are removed
    ConfigurationReport report = configurationLoader.applyConfiguration(15);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus()); // DAQ deals with Equipment
    // removal
    assertFalse(report.getProcessesToReboot().contains("P_TESTHANDLER03"));
    assertFalse(equipmentCache.containsKey(150L));
    assertNull(equipmentMapper.getItem(150L));
    // commfault and alive should no longer be in cache
    assertFalse(aliveTimerCache.containsKey(equipment.getAliveTagId()));
    assertFalse(commFaultTagCache.containsKey(equipment.getCommFaultTagId()));
  }

  /**
   * Tests the removal of a process succeeds, with dependent rules and alarms.
   * Relies on permanent test data in test account and must be rolled back. No
   * changes should be sent to the DAQ layer.
   */
  @Test
  public void testRemoveEquipmentDependentObjects() {
    configurationLoader.applyConfiguration(29);
    // check equipment, tag, rules and alarms are gone
    assertFalse(equipmentCache.containsKey(150L));
    assertNull(equipmentMapper.getItem(150L));
    // tags
    assertFalse(dataTagCache.containsKey(200001L));
    assertNull(dataTagMapper.getItem(200001L));
    assertFalse(dataTagCache.containsKey(200004L));
    assertNull(dataTagMapper.getItem(200004L));
    // control tags
//    assertNull(controlTagMapper.getItem(1222L));
//    assertNull(controlTagMapper.getItem(1223L));
//    assertNull(controlTagMapper.getItem(1224L));
    // alivetimer & commfault
    assertFalse(aliveTimerCache.containsKey(1224L));
    assertFalse(commFaultTagCache.containsKey(1223L));
  }

  @Test
  public void createEquipment() {
    // SETUP:First add a process to the server
    Configuration createProcess = TestConfigurationProvider.createProcess();
    configurationLoader.applyConfiguration(createProcess);
    processService.start(5L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:Build configuration to add the test equipment
    Properties expectedProperties = new Properties();
    cern.c2mon.shared.client.configuration.api.equipment.Equipment equipment = ConfigurationEquipmentUtil.buildCreateAllFieldsEquipment(10L, expectedProperties);
    equipment.setProcessId(5L);
    expectedProperties.setProperty("stateTagId", "300000");
    expectedProperties.setProperty("commFaultTagId", "300001");
    expectedProperties.setProperty("aliveTagId", "300002");

    Configuration configuration = new Configuration();
    configuration.addEntity(equipment);

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check Report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertEquals(1, report.getElementReports().size());

    // check Equipment in the cache
    EquipmentCacheObject cacheObject = (EquipmentCacheObject) equipmentCache.get(10L);
    EquipmentCacheObject expectedObject = cacheObjectFactory.buildEquipmentCacheObject(10L, equipment);
    expectedObject.setAliveTagId(300_000L);
    expectedObject.setCommFaultTagId(300_001L);
    expectedObject.setStateTagId(300_002L);

    assertEquals(expectedObject, cacheObject);

    // Check if all caches are updated
    cern.c2mon.server.common.process.Process processObj = processCache.get(expectedObject.getProcessId());
    assertTrue(processObj.getEquipmentIds().contains(expectedObject.getId()));
    assertNotNull(commFaultTagCache.get(expectedObject.getCommFaultTagId()));
    assertEquals(expectedObject.getId(), (long) commFaultTagCache.get(cacheObject.getCommFaultTagId()).getSupervisedId());
    assertNotNull(equipmentMapper.getItem(10L));
  }

  @Test
  public void updateEquipment() {
    // SETUP:
    Configuration createProcess = TestConfigurationProvider.createProcess();
    configurationLoader.applyConfiguration(createProcess);
    Configuration createEquipment = TestConfigurationProvider.createEquipment();
    configurationLoader.applyConfiguration(createEquipment);
    processService.start(5L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // Build configuration to add the test equipment
    cern.c2mon.shared.client.configuration.api.equipment.Equipment equipment = ConfigurationEquipmentUtil.buildUpdateEquipmentWithAllFields(15L, null);
    Configuration configuration = new Configuration();
    configuration.addEntity(equipment);

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertEquals(1, report.getElementReports().size());

    // get cacheObject from the cache and compare to the an expected cacheObject
    EquipmentCacheObject cacheObjectEquipment = (EquipmentCacheObject) equipmentCache.get(15L);
    EquipmentCacheObject expectedCacheObjectEquipment = cacheObjectFactory.buildEquipmentUpdateCacheObject(cacheObjectEquipment, equipment);

    assertEquals(expectedCacheObjectEquipment, cacheObjectEquipment);
  }

  private static EquipmentCacheObject expectedObject() {
    EquipmentCacheObject expectedObject = new EquipmentCacheObject(110L);
    expectedObject.setName("E_CONFIG_TEST");
    expectedObject.setAddress("serverHostName=VGTCVENTTEST");
    expectedObject.setAliveTagId(1251L);
    expectedObject.setStateTagId(1250L);
    expectedObject.setCommFaultTagId(1252L);
    expectedObject.setHandlerClassName("cern.c2mon.driver.");
    expectedObject.setProcessId(50L);
    expectedObject.setDescription("test description");
    return expectedObject;
  }
}
