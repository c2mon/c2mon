package cern.c2mon.server.configuration.loader;

import cern.c2mon.cache.actions.datatag.DataTagService;
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
import cern.c2mon.server.configuration.helper.ObjectEqualityComparison;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.common.NoSimpleValueParseException;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.ConfigurationChangeEventReport;
import org.easymock.EasyMock;
import org.junit.Test;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class SubEquipmentConfigTest extends ConfigurationCacheLoaderTest<SubEquipment> {
  
  @Inject
  private C2monCache<SubEquipment> subEquipmentCache;

  @Inject
  private SubEquipmentMapper subEquipmentMapper;

  @Inject
  private C2monCache<Equipment> equipmentCache;

  @Inject
  private EquipmentMapper equipmentMapper;

  @Inject
  private C2monCache<AliveTag> aliveTimerCache;

  @Inject
  private C2monCache<CommFaultTag> commFaultTagCache;

  @Inject
  private C2monCache<DataTag> dataTagCache;

  @Inject
  private DataTagService dataTagService;

  /**
   * Test the creation, update and removal of equipment.
   */

  @Test
  public void testCreateUpdateSubEquipment() throws IllegalAccessException, InstantiationException, NoSuchFieldException, ParserConfigurationException,
    TransformerException, NoSimpleValueParseException {
    expect(mockManager.sendConfiguration(EasyMock.anyLong(), EasyMock.<List<Change>> anyObject())).andReturn(new ConfigurationChangeEventReport()).times(2);
    replay(mockManager);

    ConfigurationReport report = configurationLoader.applyConfiguration(19);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));

    SubEquipmentCacheObject cacheObject = (SubEquipmentCacheObject) subEquipmentCache.get(200L);
    SubEquipmentCacheObject expectedObject = new SubEquipmentCacheObject(200L);
    expectedObject.setName("SUB_E_TEST");
    expectedObject.setStateTagId(1250L);
    expectedObject.setCommFaultTagId(1252L);
    expectedObject.setAliveTagId(1251L);
    expectedObject.setAliveInterval(30000);
    expectedObject.setHandlerClassName("-");
    expectedObject.setParentId(150L);
    expectedObject.setDescription("test description");

    ObjectEqualityComparison.assertSubEquipmentEquals(expectedObject, cacheObject);

    // check DB update was successful
    SubEquipmentCacheObject dbObject = (SubEquipmentCacheObject) subEquipmentMapper.getItem(200L);
    ObjectEqualityComparison.assertSubEquipmentEquals(expectedObject, dbObject);

    // also check that the equipment, commfault and alive cache were updated
    Equipment equipment = equipmentCache.get(expectedObject.getParentId());
    assertTrue(equipment.getSubEquipmentIds().contains(expectedObject.getId()));
    // the alivetimer and commfault caches should reflect the changes
    assertNotNull(aliveTimerCache.get(expectedObject.getAliveTagId()));
    assertEquals(expectedObject.getId(), (long) aliveTimerCache.get(cacheObject.getAliveTagId()).getSupervisedId());
    assertNotNull(commFaultTagCache.get(expectedObject.getCommFaultTagId()));
    assertEquals(expectedObject.getId(), (long) commFaultTagCache.get(cacheObject.getCommFaultTagId()).getSupervisedId());

    report = configurationLoader.applyConfiguration(20);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));

    verify(mockManager);
  }

  @Test
  public void testCreateSubEquipmentDataTag() throws IllegalAccessException, InstantiationException, NoSuchFieldException, ParserConfigurationException,
    TransformerException, NoSimpleValueParseException {

    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    // Create another DataTag attached to the SubEquipment (two already exist in
    // permanent test data)
    ConfigurationReport report = configurationLoader.applyConfiguration(99);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));

    SubEquipment subEquipment = subEquipmentCache.get(250L);
    assertNotNull(subEquipment);
    assertTrue(dataTagService.getDataTagIdsBySubEquipmentId(250L).size() == 3);

    DataTagCacheObject cacheObject = (DataTagCacheObject) dataTagCache.get(7000000L);
    assertTrue(cacheObject.getSubEquipmentId() == 250L);
  }

  @Test
  public void testRemoveSubEquipmentDataTag() throws IllegalAccessException, InstantiationException, NoSuchFieldException, ParserConfigurationException,
    TransformerException, NoSimpleValueParseException {

    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport()).times(2);
    replay(mockManager);

    // Create another DataTag attached to the SubEquipment (two already exist in
    // permanent test data)
    ConfigurationReport report = configurationLoader.applyConfiguration(99);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));

    SubEquipment subEquipment = subEquipmentCache.get(250L);
    assertNotNull(subEquipment);
    assertTrue(aliveTimerCache.containsKey(subEquipment.getAliveTagId()));
    assertTrue(commFaultTagCache.containsKey(subEquipment.getCommFaultTagId()));
    assertTrue(dataTagService.getDataTagIdsBySubEquipmentId(250L).size() == 3);
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

  public void testRemoveSubEquipment() throws IllegalAccessException, InstantiationException, NoSuchFieldException, ParserConfigurationException,
    TransformerException, NoSimpleValueParseException {
    expect(mockManager.sendConfiguration(EasyMock.anyLong(), EasyMock.<List<Change>> anyObject())).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    // Create the subequipment
    ConfigurationReport report = configurationLoader.applyConfiguration(19);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));

    SubEquipment subEquipment = subEquipmentCache.get(200L);
    assertNotNull(subEquipment);
    assertTrue(aliveTimerCache.containsKey(subEquipment.getAliveTagId()));
    assertTrue(commFaultTagCache.containsKey(subEquipment.getCommFaultTagId()));

    reset(mockManager);
    expect(mockManager.sendConfiguration(EasyMock.anyLong(), EasyMock.<List<Change>> anyObject())).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    report = configurationLoader.applyConfiguration(98);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertFalse(subEquipmentCache.containsKey(200L));
    assertNull(equipmentMapper.getItem(200L));

    assertFalse(aliveTimerCache.containsKey(subEquipment.getAliveTagId()));
    assertFalse(commFaultTagCache.containsKey(subEquipment.getCommFaultTagId()));

//    assertNull(controlTagMapper.getItem(subEquipment.getAliveTagId()));
//    assertNull(controlTagMapper.getItem(subEquipment.getStateTagId()));
//    assertNull(controlTagMapper.getItem(subEquipment.getCommFaultTagId()));

    Equipment parentEquipment = equipmentCache.get(150L);
    for (Long id : parentEquipment.getSubEquipmentIds()) {
      assertTrue(id != subEquipment.getId());
    }

    verify(mockManager);
  }
}
