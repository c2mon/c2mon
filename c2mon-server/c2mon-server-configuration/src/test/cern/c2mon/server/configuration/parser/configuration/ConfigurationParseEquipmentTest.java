package cern.c2mon.server.configuration.parser.configuration;

import static cern.c2mon.server.configuration.parser.util.ConfigurationEquipmentUtil.*;
import static cern.c2mon.server.configuration.parser.util.ConfigurationProcessUtil.buildUpdateProcessNewControlTag;
import static cern.c2mon.server.configuration.parser.util.ConfigurationUtil.getConfBuilderEquipment;
import static cern.c2mon.server.configuration.parser.util.ConfigurationUtil.getConfBuilderProcess;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import cern.c2mon.server.cache.*;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.api.process.*;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.configuration.parser.impl.ConfigurationParserImpl;
import cern.c2mon.server.configuration.parser.util.Pair;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/configuration/config/server-configuration-parser-test.xml"})
public class ConfigurationParseEquipmentTest {

  @Autowired
  ConfigurationParserImpl configurationParser;

  @Autowired
  ProcessCache processCache;

  @Autowired
  EquipmentCache equipmentCache;

  @Autowired
  ControlTagCache statusTagCache;

  @Autowired
  AliveTimerCache aliveTagCache;

  @Autowired
  CommFaultTagCache commFaultTagCache;

  @Rule
  public ExpectedException equipmentUpdate = ExpectedException.none();

  @Rule
  public ExpectedException equipmentCreate = ExpectedException.none();

  @Rule
  public ExpectedException equipmentDelete = ExpectedException.none();

  @Before
  public void resetMocks() {
    EasyMock.reset(processCache, equipmentCache, statusTagCache, commFaultTagCache, aliveTagCache);
  }

  @Test
  public void equipmentUpdate_withNoFields() {
    // Setup Configuration Instance
    Configuration configuration = getConfBuilderEquipment(buildEquipmentWtihId(1L)._1);

//    DataTag.builder().address(new DataTagAddress(new PLCHardwareAddressImpl(0, 0, 0, 0, 0, 0.0f, "")));

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);


    // Switch to replay mode
    EasyMock.replay(processCache);
    EasyMock.replay(equipmentCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertFalse(elements.size() > 0);

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
  }

  @Test
  public void equipmentUpdate_notExistingInstance() {
    // Setup Exception
    equipmentUpdate.expect(ConfigurationParseException.class);
    equipmentUpdate.expectMessage("Creating Equipment 1 failed. Not enough arguments.");

    // Setup Configuration Instance
    Pair<Equipment, Properties> pair = buildUpdateEquipmentWithSomeFields(1l);
    Configuration configuration = getConfBuilderEquipment(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache);
    EasyMock.replay(equipmentCache);

    // Run the code to be tested
    configurationParser.parse(configuration);

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
  }

  @Test
  public void equipmentUpdate_withSomeFields() {
    // Setup Configuration Instance
    Pair<Equipment, Properties> pair = buildUpdateEquipmentWithSomeFields(1l);
    Configuration configuration = getConfBuilderEquipment(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1l)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache);
    EasyMock.replay(equipmentCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.EQUIPMENT));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
  }

  @Test
  public void equipmentUpdate_withAllFields() {
    // Setup Configuration Instance
    Pair<Equipment, Properties> pair = buildUpdateEquipmentWithAllFields(1l);
    Configuration configuration = getConfBuilderEquipment(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache);
    EasyMock.replay(equipmentCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.EQUIPMENT));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
  }

  @Test
  public void equipmentUpdate_multipleInstances_withAllFields() {
    // Setup Configuration Instance
    Pair<Equipment, Properties> pair1 = buildUpdateEquipmentWithAllFields(1l);
    Pair<Equipment, Properties> pair2 = buildUpdateEquipmentWithAllFields(2l);
    Pair<Equipment, Properties> pair3 = buildUpdateEquipmentWithAllFields(3l);
    Pair<Equipment, Properties> pair4 = buildUpdateEquipmentWithAllFields(4l);
    Pair<Equipment, Properties> pair5 = buildUpdateEquipmentWithAllFields(5l);

    Configuration configuration = getConfBuilderEquipment(pair1._1, pair2._1, pair3._1, pair4._1, pair5._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);

    EasyMock.expect(equipmentCache.hasKey(1l)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(2l)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(3l)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(4l)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(5l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache);
    EasyMock.replay(equipmentCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 5);

    assertTrue(elements.get(0).getElementProperties().equals(pair1._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.EQUIPMENT));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(1).getElementProperties().equals(pair2._2));
    assertTrue(elements.get(1).getEntity().equals(Entity.EQUIPMENT));
    assertTrue(elements.get(1).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(2).getElementProperties().equals(pair3._2));
    assertTrue(elements.get(2).getEntity().equals(Entity.EQUIPMENT));
    assertTrue(elements.get(2).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(3).getElementProperties().equals(pair4._2));
    assertTrue(elements.get(3).getEntity().equals(Entity.EQUIPMENT));
    assertTrue(elements.get(3).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(4).getElementProperties().equals(pair5._2));
    assertTrue(elements.get(4).getEntity().equals(Entity.EQUIPMENT));
    assertTrue(elements.get(4).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
  }

  @Test
  public void equipmentCreate_withNoFields() {
    // Setup Exception
    equipmentCreate.expect(ConfigurationParseException.class);
    equipmentCreate.expectMessage("Creating Equipment 1 failed. Not enough arguments.");

    // Setup Configuration Instance
    Configuration configuration = getConfBuilderEquipment(buildEquipmentWtihId(1L)._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache);
    EasyMock.replay(equipmentCache);

    // Run the code to be tested
    configurationParser.parse(configuration);

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
  }

  @Test
  public void equipmentCreate_withMandatoryFields() {
    // Setup Configuration Instance
    Pair<Equipment, Properties> pair = buildEquipmentWithPrimFields(1l);
    Configuration configuration = getConfBuilderEquipment(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(2l)).andReturn(false);


    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, statusTagCache, commFaultTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 3);
    assertTrue(elements.get(2).getElementProperties().equals(pair._2));
    assertTrue(elements.get(2).getEntityId().equals(1L));
    assertTrue(elements.get(2).getEntity().equals(Entity.EQUIPMENT));
    assertTrue(elements.get(2).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache, equipmentCache, statusTagCache, commFaultTagCache);
  }

  @Test
  public void equipmentCreate_withNotExistingSupClass() {
    equipmentCreate.expect(ConfigurationParseException.class);
    equipmentCreate.expectMessage("Creating Process 1 failed. Not enough arguments.");

    // Setup Configuration Instance
    Pair<Equipment, Properties> pair = buildEquipmentWithPrimFields(1l);
    Configuration configuration = getConfBuilderEquipment(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache);

    // Run the code to be tested
    configurationParser.parse(configuration);

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
  }

  @Test
  public void equipmentCreate_withAllFields() {
    // Setup Configuration Instance
    Pair<Equipment, Properties> pair = buildEquipmentWithAllFields(1l);
    Configuration configuration = getConfBuilderEquipment(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(2l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);


    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, statusTagCache, commFaultTagCache, aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 4);
    assertTrue(elements.get(3).getElementProperties().equals(pair._2));
    assertTrue(elements.get(3).getEntityId().equals(1L));
    assertTrue(elements.get(3).getEntity().equals(Entity.EQUIPMENT));
    assertTrue(elements.get(3).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache, equipmentCache, statusTagCache, commFaultTagCache, aliveTagCache);
  }

  @Test
  public void equipmentCreate_withoutDefaultFields() {
    // Setup Configuration Instance
    Pair<Equipment, Properties> pair = buildEquipmentWithoutDefaultFields(1l);
    Configuration configuration = getConfBuilderEquipment(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(2l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, statusTagCache, commFaultTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 3);
    assertTrue(elements.get(2).getElementProperties().equals(pair._2));
    assertTrue(elements.get(2).getEntityId().equals(1L));
    assertTrue(elements.get(2).getEntity().equals(Entity.EQUIPMENT));
    assertTrue(elements.get(2).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache, equipmentCache, statusTagCache, commFaultTagCache);
  }

  @Test
  public void equipmentCreate_multipleInstances_withAllFields() {
    // Setup Configuration Instance
    Pair<Equipment, Properties> pair1 = buildEquipmentWithAllFields(1l);
    Pair<Equipment, Properties> pair2 = buildEquipmentWithAllFields(2l);
    Pair<Equipment, Properties> pair3 = buildEquipmentWithAllFields(3l);
    Pair<Equipment, Properties> pair4 = buildEquipmentWithAllFields(4l);
    Pair<Equipment, Properties> pair5 = buildEquipmentWithAllFields(5l);

    Configuration configuration = getConfBuilderEquipment(pair1._1, pair2._1, pair3._1, pair4._1, pair5._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);

    EasyMock.expect(equipmentCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(2l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    EasyMock.expect(equipmentCache.hasKey(2l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(2l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    EasyMock.expect(equipmentCache.hasKey(3l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(2l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    EasyMock.expect(equipmentCache.hasKey(4l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(2l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    EasyMock.expect(equipmentCache.hasKey(5l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(2l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, statusTagCache, commFaultTagCache, aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 20);

    assertTrue(elements.get(15).getElementProperties().equals(pair1._2));
    assertTrue(elements.get(15).getEntityId().equals(1L));
    assertTrue(elements.get(15).getEntity().equals(Entity.EQUIPMENT));
    assertTrue(elements.get(15).getAction().equals(Action.CREATE));

    assertTrue(elements.get(16).getElementProperties().equals(pair2._2));
    assertTrue(elements.get(16).getEntityId().equals(2L));
    assertTrue(elements.get(16).getEntity().equals(Entity.EQUIPMENT));
    assertTrue(elements.get(16).getAction().equals(Action.CREATE));

    assertTrue(elements.get(17).getElementProperties().equals(pair3._2));
    assertTrue(elements.get(17).getEntityId().equals(3L));
    assertTrue(elements.get(17).getEntity().equals(Entity.EQUIPMENT));
    assertTrue(elements.get(17).getAction().equals(Action.CREATE));

    assertTrue(elements.get(18).getElementProperties().equals(pair4._2));
    assertTrue(elements.get(18).getEntityId().equals(4L));
    assertTrue(elements.get(18).getEntity().equals(Entity.EQUIPMENT));
    assertTrue(elements.get(18).getAction().equals(Action.CREATE));

    assertTrue(elements.get(19).getElementProperties().equals(pair5._2));
    assertTrue(elements.get(19).getEntityId().equals(5L));
    assertTrue(elements.get(19).getEntity().equals(Entity.EQUIPMENT));
    assertTrue(elements.get(19).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache, equipmentCache, statusTagCache, commFaultTagCache, aliveTagCache);
  }

  @Test
  public void processCreate_withNewControlTag() {
    equipmentCreate.expect(ConfigurationParseException.class);
    equipmentCreate.expectMessage("Not possible to create a ControlTag for Process or Equipment when the parent already exists.");

    // Setup Configuration Instance
    Equipment equipment = buildUpdateEquipmentNewControlTag(1l);
    Configuration configuration = getConfBuilderEquipment(equipment);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1l)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(0l)).andReturn(true);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, aliveTagCache);

    // runt test
    configurationParser.parse(configuration);


    // Verify mock methods were called
    EasyMock.verify(processCache, aliveTagCache, equipmentCache);
  }

  @Test
  public void equipmentDelete_NotExistingInstance() {
    // Setup Exception
//    equipmentDelete.expect(ConfigurationParseException.class);
//    equipmentDelete.expectMessage("Deleting Equipment 1 failed. Equipment do not exist in the cache.");

    // Setup Configuration Instance
    Equipment equipment = buildDeleteEquipment(1l);
    Configuration configuration = getConfBuilderEquipment(equipment);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
//    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache);
//    EasyMock.replay(equipmentCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().isEmpty());
    assertTrue(elements.get(0).getEntity().equals(Entity.EQUIPMENT));
    assertTrue(elements.get(0).getAction().equals(Action.REMOVE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
  }

  /**
   * At the moment, the parsers returns always a Delte ConfigurationElement.
   * Independent from the id in the cache!
   */
  @Test
  public void equipmentDelete_ExistingInstance() {
    // Setup Configuration Instance
    Equipment equipment = buildDeleteEquipment(1l);
    Configuration configuration = getConfBuilderEquipment(equipment);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
//    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache);
//    EasyMock.replay(equipmentCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().isEmpty());
    assertTrue(elements.get(0).getEntity().equals(Entity.EQUIPMENT));
    assertTrue(elements.get(0).getAction().equals(Action.REMOVE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
  }

  @Test
  public void equipmentAllOperations_checkOrder() {
    // Setup Configuration Instance
    Pair<Equipment, Properties> pairUpdate = buildUpdateEquipmentWithAllFields(2l);
    Pair<Equipment, Properties> pairCreate = buildEquipmentWithAllFields(3l);
    Equipment equipmentDelete = buildDeleteEquipment(1l);
    Configuration configuration = getConfBuilderEquipment(pairUpdate._1, pairCreate._1, equipmentDelete);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);

    EasyMock.expect(equipmentCache.hasKey(2L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(3L)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(2l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, statusTagCache, commFaultTagCache, aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 6);

    // Check ConfigurationElement fields and order
    assertTrue(elements.get(0).getElementProperties().isEmpty());
    assertTrue(elements.get(0).getEntity().equals(Entity.EQUIPMENT));
    assertTrue(elements.get(0).getAction().equals(Action.REMOVE));

    assertTrue(elements.get(4).getElementProperties().equals(pairCreate._2));
    assertTrue(elements.get(4).getEntity().equals(Entity.EQUIPMENT));
    assertTrue(elements.get(4).getAction().equals(Action.CREATE));

    assertTrue(elements.get(5).getElementProperties().equals(pairUpdate._2));
    assertTrue(elements.get(5).getEntity().equals(Entity.EQUIPMENT));
    assertTrue(elements.get(5).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache, equipmentCache, statusTagCache, commFaultTagCache, aliveTagCache);
  }
}
