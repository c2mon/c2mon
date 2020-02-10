package cern.c2mon.server.configuration.loader;

import cern.c2mon.cache.actions.datatag.DataTagService;
import cern.c2mon.cache.actions.subequipment.SubEquipmentService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.EquipmentMapper;
import cern.c2mon.server.cache.dbaccess.SubEquipmentMapper;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.server.configuration.parser.util.ConfigurationSubEquipmentUtil;
import cern.c2mon.server.configuration.util.TestConfigurationProvider;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import org.junit.Test;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.Properties;

import static org.easymock.EasyMock.verify;
import static org.junit.Assert.*;

public class SubEquipmentConfigTest extends ConfigurationCacheLoaderTest<SubEquipment> {
  
  @Inject
  private C2monCache<SubEquipment> subEquipmentCache;

  @Inject
  private SubEquipmentMapper subEquipmentMapper;

  @Inject
  private SubEquipmentService subEquipmentService;

  @Inject
  private C2monCache<Equipment> equipmentCache;

  @Inject
  private EquipmentMapper equipmentMapper;

  @Inject
  private C2monCache<AliveTag> aliveTimerCache;

  @Inject
  private C2monCache<CommFaultTag> commFaultTagCache;

  @Inject
  private C2monCache<SupervisionStateTag> stateTagCache;

  @Inject
  private C2monCache<DataTag> dataTagCache;

  @Inject
  private DataTagService dataTagService;

  /**
   * Test the creation, update and removal of equipment.
   */

  @Test
  public void testCreateUpdateSubEquipment() {
    ConfigurationReport report = configurationLoader.applyConfiguration(19);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));

    SubEquipmentCacheObject cacheObject = (SubEquipmentCacheObject) subEquipmentCache.get(200L);
    SubEquipmentCacheObject expectedObject = new SubEquipmentCacheObject(200L);
    expectedObject.setName("SUB_E_TEST");
    expectedObject.setStateTagId(1250L);
    expectedObject.setCommFaultTagId(1252L);
    expectedObject.setAliveTagId(1251L);
    expectedObject.setAliveInterval(30000);
    expectedObject.setHandlerClassName("cern.c2mon.daq.testhandler.TestMessageHandler");
    expectedObject.setParentId(150L);
    expectedObject.setDescription("test description");

    assertEquals(expectedObject, cacheObject);

    // check DB update was successful
    SubEquipmentCacheObject dbObject = (SubEquipmentCacheObject) subEquipmentMapper.getItem(200L);
    assertEquals(expectedObject, dbObject);

    // also check that the commfault and alive cache were updated
    // the alivetimer, commfault, state caches should reflect the changes
    assertNotNull(aliveTimerCache.get(expectedObject.getAliveTagId()));
    assertEquals(expectedObject.getId(), (long) aliveTimerCache.get(cacheObject.getAliveTagId()).getSupervisedId());
    assertNotNull(commFaultTagCache.get(expectedObject.getCommFaultTagId()));
    assertEquals(expectedObject.getId(), (long) commFaultTagCache.get(cacheObject.getCommFaultTagId()).getSupervisedId());
    assertNotNull(stateTagCache.get(expectedObject.getStateTagId()));
    assertEquals(expectedObject.getId(), (long) stateTagCache.get(cacheObject.getStateTagId()).getSupervisedId());

    report = configurationLoader.applyConfiguration(20);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));

    verify(mockManager);
  }

  @Test
  public void testCreateSubEquipmentDataTag() {

    // Create another DataTag attached to the SubEquipment (two already exist in
    // permanent test data)
    ConfigurationReport report = configurationLoader.applyConfiguration(99);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));

    SubEquipment subEquipment = subEquipmentCache.get(250L);
    assertNotNull(subEquipment);
    assertEquals(3, dataTagService.getDataTagIdsBySubEquipmentId(250L).size());

    DataTagCacheObject cacheObject = (DataTagCacheObject) dataTagCache.get(7000000L);
    assertEquals(250L, (long) cacheObject.getSubEquipmentId());
  }

  @Test
  public void testRemoveSubEquipmentDataTag() {
    // Create another DataTag attached to the SubEquipment (two already exist in
    // permanent test data)
    ConfigurationReport report = configurationLoader.applyConfiguration(99);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));

    SubEquipment subEquipment = subEquipmentCache.get(250L);
    assertNotNull(subEquipment);
    assertTrue(aliveTimerCache.containsKey(subEquipment.getAliveTagId()));
    assertTrue(commFaultTagCache.containsKey(subEquipment.getCommFaultTagId()));
    assertEquals(3, dataTagService.getDataTagIdsBySubEquipmentId(250L).size());
    for (Long tagId : dataTagService.getDataTagIdsBySubEquipmentId(250L)) {
      assertTrue(dataTagCache.containsKey(tagId));
    }

    report = configurationLoader.applyConfiguration(21);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertFalse(subEquipmentCache.containsKey(250L));
    assertNull(equipmentMapper.getItem(250L));

    assertFalse(aliveTimerCache.containsKey(subEquipment.getAliveTagId()));
    assertFalse(commFaultTagCache.containsKey(subEquipment.getCommFaultTagId()));

//    assertNull(controlTagMapper.getItem(subEquipment.getAliveTagId()));
//    assertNull(controlTagMapper.getItem(subEquipment.getStateTagId()));
//    assertNull(controlTagMapper.getItem(subEquipment.getCommFaultTagId()));
    for (Long tagId : dataTagService.getDataTagIdsBySubEquipmentId(250L)) {
      assertFalse(dataTagCache.containsKey(tagId));
    }

    verify(mockManager);
  }

  @Test
  public void testRemoveSubEquipment() {
    // Create the subequipment
    ConfigurationReport report = configurationLoader.applyConfiguration(19);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));

    SubEquipment subEquipment = subEquipmentCache.get(200L);
    assertNotNull(subEquipment);
    assertTrue(aliveTimerCache.containsKey(subEquipment.getAliveTagId()));
    assertTrue(commFaultTagCache.containsKey(subEquipment.getCommFaultTagId()));
    assertTrue(stateTagCache.containsKey(subEquipment.getStateTagId()));

    report = configurationLoader.applyConfiguration(98);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertFalse(subEquipmentCache.containsKey(200L));
    assertNull(equipmentMapper.getItem(200L));

    assertFalse(aliveTimerCache.containsKey(subEquipment.getAliveTagId()));
    assertFalse(commFaultTagCache.containsKey(subEquipment.getCommFaultTagId()));
    assertFalse(stateTagCache.containsKey(subEquipment.getStateTagId()));

//    assertNull(controlTagMapper.getItem(subEquipment.getAliveTagId()));
//    assertNull(controlTagMapper.getItem(subEquipment.getStateTagId()));
//    assertNull(controlTagMapper.getItem(subEquipment.getCommFaultTagId()));

    for (Long id : subEquipmentService.getSubEquipmentIdsFor(150L)) {
      assertTrue(id != subEquipment.getId());
    }

    verify(mockManager);
  }


  @Test
  public void createSubEquipment() {
    // SETUP:
    Configuration createProcess = TestConfigurationProvider.createProcess();
    configurationLoader.applyConfiguration(createProcess);
    Configuration createEquipment = TestConfigurationProvider.createEquipment();
    configurationLoader.applyConfiguration(createEquipment);
    processService.start(5L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:Build configuration to add the test equipment
    Properties expectedProperties = new Properties();
    cern.c2mon.shared.client.configuration.api.equipment.SubEquipment subEquipment = ConfigurationSubEquipmentUtil.buildCreateAllFieldsSubEquipment(20L, expectedProperties);
    subEquipment.setEquipmentId(15L);
    expectedProperties.setProperty("stateTagId", "300000");
    expectedProperties.setProperty("commFaultTagId", "300001");
    expectedProperties.setProperty("aliveTagId", "300002");

    Configuration configuration = new Configuration();
    configuration.addEntity(subEquipment);

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(4, report.getElementReports().size());

    // check Equipment in the cache
    SubEquipmentCacheObject expectedObject = buildSubEquipmentCacheObject(20L, subEquipment);
    expectedObject.setHandlerClassName("handlerClass");
    SubEquipmentCacheObject cacheObject = (SubEquipmentCacheObject) subEquipmentCache.get(20L);

    assertEquals(expectedObject, cacheObject);

    // Check if all caches are updated
    assertTrue(subEquipmentService.getSubEquipmentIdsFor(expectedObject.getParentId()).contains(expectedObject.getId()));
    assertNotNull(commFaultTagCache.get(expectedObject.getCommFaultTagId()));
    assertEquals(expectedObject.getId(), (long) commFaultTagCache.get(cacheObject.getCommFaultTagId()).getSupervisedId());
    assertNotNull(subEquipmentMapper.getItem(20L));
  }

  @Test
  public void updateSubEquipment() {
    setUp();

    // TEST:
    // Build configuration to add the test equipment
    cern.c2mon.shared.client.configuration.api.equipment.SubEquipment subEquipment = ConfigurationSubEquipmentUtil.buildUpdateSubEquipmentWithAllFields(25L, null);
    Configuration configuration = new Configuration();
    configuration.addEntity(subEquipment);

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(1, report.getElementReports().size());

    // get cacheObject from the cache and compare to the an expected cacheObject
    SubEquipmentCacheObject cacheObjectEquipment = (SubEquipmentCacheObject) subEquipmentCache.get(25L);
    SubEquipmentCacheObject expectedCacheObjectEquipment = buildSubEquipmentUpdateCacheObject(cacheObjectEquipment, subEquipment);
    expectedCacheObjectEquipment.setHandlerClassName("handlerClass");

    assertEquals(expectedCacheObjectEquipment, cacheObjectEquipment);
  }

  private SubEquipmentCacheObject buildSubEquipmentCacheObject(Long id,
    cern.c2mon.shared.client.configuration.api.equipment.SubEquipment configEquipment) {

    return setCacheSubEquipmentCacheObjectFields(new SubEquipmentCacheObject(id), configEquipment);
  }

  private SubEquipmentCacheObject buildSubEquipmentUpdateCacheObject(SubEquipmentCacheObject originalObject,
    cern.c2mon.shared.client.configuration.api.equipment.SubEquipment configObject) {

    SubEquipmentCacheObject result = originalObject.clone();
    setCacheSubEquipmentCacheObjectFields(result, configObject);

    return result;
  }

  private static SubEquipmentCacheObject setCacheSubEquipmentCacheObjectFields(SubEquipmentCacheObject cacheObject,
     cern.c2mon.shared.client.configuration.api.equipment.SubEquipment configObject) {

    if (configObject.getName() != null) {
      cacheObject.setName(configObject.getName());
    }
    if (configObject.getAliveInterval() != null) {
      cacheObject.setAliveInterval(configObject.getAliveInterval());
    }
    if (configObject.getAliveTag() != null) {
      cacheObject.setAliveTagId(configObject.getAliveTag().getId());
    }
    if (configObject.getStatusTag() != null) {
      cacheObject.setStateTagId(configObject.getStatusTag().getId());
    }
    if (configObject.getCommFaultTag() != null) {
      cacheObject.setCommFaultTagId(configObject.getCommFaultTag().getId());
    }
    if (configObject.getEquipmentId() != null) {
      cacheObject.setParentId(configObject.getEquipmentId());
    }
    if (configObject.getDescription() != null) {
      cacheObject.setDescription(configObject.getDescription());
    }
    return cacheObject;
  }
}
