package cern.c2mon.server.configuration.parser.configuration;

import static cern.c2mon.server.configuration.parser.util.ConfigurationAlarmUtil.buildAlarmWithId;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.server.configuration.parser.impl.ConfigurationParserImpl;
import cern.c2mon.server.configuration.parser.util.Pair;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Properties;

import static cern.c2mon.server.configuration.parser.util.ConfigurationAlarmUtil.*;
import static cern.c2mon.server.configuration.parser.util.ConfigurationUtil.getConfBuilderAlarm;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/configuration/config/server-configuration-parser-test.xml"})
public class ConfigurationParseAlarmTest {

  @Autowired
  ConfigurationParserImpl configurationParser;

  @Autowired
  ProcessCache processCache;

  @Autowired
  EquipmentCache equipmentCache;

  @Autowired
  DataTagCache dataTagCache;

  @Autowired
  AlarmCache alarmCache;

  @Rule
  public ExpectedException alarmUpdate = ExpectedException.none();

  @Rule
  public ExpectedException alarmCreate = ExpectedException.none();

  @Rule
  public ExpectedException alarmDelete = ExpectedException.none();

  @Before
  public void resetMocks() {
    EasyMock.reset(processCache, equipmentCache, dataTagCache, alarmCache);
  }

  @Test
  public void alarmUpdate_withNoFields() {
    // Setup Configuration Instance
    Configuration configuration = getConfBuilderAlarm(buildAlarmWithId(1L)._1);

    // Alarm.builder().address(new AlarmAddress(new
    // PLCHardwareAddressImpl(0, 0, 0, 0, 0, 0.0f, "")));

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(alarmCache.hasKey(1L)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, dataTagCache, alarmCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertFalse(elements.size() > 0);

    // Verify mock methods were called
    EasyMock.verify(processCache, equipmentCache, dataTagCache, alarmCache);
  }

  @Test
  public void alarmUpdate_notExistingInstance() {
    // Setup Exception
    alarmUpdate.expect(ConfigurationParseException.class);
    alarmUpdate.expectMessage("Creating Alarm (id = 1) failed. Not enough arguments.");

    // Setup Configuration Instance
    Pair<Alarm, Properties> pair = buildUpdateAlarmWithSomeFields(1l);
    Configuration configuration = getConfBuilderAlarm(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(alarmCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, dataTagCache, alarmCache);

    // Run the code to be tested
    configurationParser.parse(configuration);

    // Verify mock methods were called
    EasyMock.verify(processCache, equipmentCache, dataTagCache, alarmCache);
  }

  @Test
  public void alarmUpdate_withSomeFields() {
    // Setup Configuration Instance
    Pair<Alarm, Properties> pair = buildUpdateAlarmWithSomeFields(1l);
    Configuration configuration = getConfBuilderAlarm(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1l)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(alarmCache.hasKey(1l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, dataTagCache, alarmCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.ALARM));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache, equipmentCache, dataTagCache, alarmCache);
  }

  @Test
  public void alarmUpdate_withAllFields() {
    // Setup Configuration Instance
    Pair<Alarm, Properties> pair = buildUpdateAlarmWithAllFields(1l);
    Configuration configuration = getConfBuilderAlarm(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(alarmCache.hasKey(1l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, dataTagCache, alarmCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.ALARM));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache, equipmentCache, dataTagCache, alarmCache);
  }

  @Test
  public void alarmUpdate_multipleInstances_withAllFields() {
    // Setup Configuration Instance
    Pair<Alarm, Properties> pair1 = buildUpdateAlarmWithAllFields(1l);
    Pair<Alarm, Properties> pair2 = buildUpdateAlarmWithAllFields(2l);
    Pair<Alarm, Properties> pair3 = buildUpdateAlarmWithAllFields(3l);
    Pair<Alarm, Properties> pair4 = buildUpdateAlarmWithAllFields(4l);
    Pair<Alarm, Properties> pair5 = buildUpdateAlarmWithAllFields(5l);

    Configuration configuration = getConfBuilderAlarm(pair1._1, pair2._1, pair3._1, pair4._1, pair5._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(1L)).andReturn(true);

    EasyMock.expect(alarmCache.hasKey(1l)).andReturn(true);
    EasyMock.expect(alarmCache.hasKey(2l)).andReturn(true);
    EasyMock.expect(alarmCache.hasKey(3l)).andReturn(true);
    EasyMock.expect(alarmCache.hasKey(4l)).andReturn(true);
    EasyMock.expect(alarmCache.hasKey(5l)).andReturn(true);

    // Switch to replay mode

//    EasyMock.replay(processCache, equipmentCache, alarmCache);
    EasyMock.replay(processCache, equipmentCache, dataTagCache, alarmCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 5);

    assertTrue(elements.get(0).getElementProperties().equals(pair1._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.ALARM));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(1).getElementProperties().equals(pair2._2));
    assertTrue(elements.get(1).getEntity().equals(Entity.ALARM));
    assertTrue(elements.get(1).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(2).getElementProperties().equals(pair3._2));
    assertTrue(elements.get(2).getEntity().equals(Entity.ALARM));
    assertTrue(elements.get(2).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(3).getElementProperties().equals(pair4._2));
    assertTrue(elements.get(3).getEntity().equals(Entity.ALARM));
    assertTrue(elements.get(3).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(4).getElementProperties().equals(pair5._2));
    assertTrue(elements.get(4).getEntity().equals(Entity.ALARM));
    assertTrue(elements.get(4).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache, equipmentCache, dataTagCache, alarmCache);
  }

  @Test
  public void alarmCreate_withNoFields() {
    // Setup Exception
    alarmCreate.expect(ConfigurationParseException.class);
    alarmCreate.expectMessage("Creating Alarm (id = 1) failed. Not enough arguments.");

    // Setup Configuration Instance
    Configuration configuration = getConfBuilderAlarm(buildAlarmWithId(1L)._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(alarmCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, dataTagCache ,alarmCache);

    // Run the code to be tested
    configurationParser.parse(configuration);

    // Verify mock methods were called
    EasyMock.verify(processCache, equipmentCache, dataTagCache, alarmCache);
  }

  @Test
  public void alarmCreate_withMandatoryFields() {
    // Setup Configuration Instance
    Pair<Alarm, Properties> pair = buildAlarmWithPrimFields(1l);
    Configuration configuration = getConfBuilderAlarm(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(alarmCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, dataTagCache ,alarmCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntityId().equals(1L));
    assertTrue(elements.get(0).getEntity().equals(Entity.ALARM));
    assertTrue(elements.get(0).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache, equipmentCache, dataTagCache, alarmCache);
  }

  @Test
  public void alarmCreate_withNotExistingSupClass() {
    alarmCreate.expect(ConfigurationParseException.class);
    alarmCreate.expectMessage("Creating Process (id = 1) failed. Not enough arguments.");

    // Setup Configuration Instance
    Pair<Alarm, Properties> pair = buildAlarmWithPrimFields(1l);
    Configuration configuration = getConfBuilderAlarm(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache);

    // Run the code to be tested
    configurationParser.parse(configuration);

    // Verify mock methods were called
    EasyMock.verify(processCache);
  }

  @Test
  public void alarmCreate_withAllFields() {
    // Setup Configuration Instance
    Pair<Alarm, Properties> pair = buildAlarmWithAllFields(1l);
    Configuration configuration = getConfBuilderAlarm(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(alarmCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, dataTagCache, alarmCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntityId().equals(1L));
    assertTrue(elements.get(0).getEntity().equals(Entity.ALARM));
    assertTrue(elements.get(0).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache, equipmentCache, dataTagCache, alarmCache);
  }

  @Test
  public void alarmCreate_withoutDefaultFields() {
    // Setup Configuration Instance
    Pair<Alarm, Properties> pair = buildAlarmWithoutDefaultFields(1l);
    Configuration configuration = getConfBuilderAlarm(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(alarmCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, dataTagCache, alarmCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntityId().equals(1L));
    assertTrue(elements.get(0).getEntity().equals(Entity.ALARM));
    assertTrue(elements.get(0).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache, equipmentCache, dataTagCache, alarmCache);
  }

  @Test
  public void alarmCreate_multipleInstances_withAllFields() {
    // Setup Configuration Instance
    Pair<Alarm, Properties> pair1 = buildAlarmWithAllFields(1l);
    Pair<Alarm, Properties> pair2 = buildAlarmWithAllFields(2l);
    Pair<Alarm, Properties> pair3 = buildAlarmWithAllFields(3l);
    Pair<Alarm, Properties> pair4 = buildAlarmWithAllFields(4l);
    Pair<Alarm, Properties> pair5 = buildAlarmWithAllFields(5l);

    Configuration configuration = getConfBuilderAlarm(pair1._1, pair2._1, pair3._1, pair4._1, pair5._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(1L)).andReturn(true);

    EasyMock.expect(alarmCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(2l)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(3l)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(4l)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(5l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, dataTagCache ,alarmCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 5);

    assertTrue(elements.get(0).getElementProperties().equals(pair1._2));
    assertTrue(elements.get(0).getEntityId().equals(1L));
    assertTrue(elements.get(0).getEntity().equals(Entity.ALARM));
    assertTrue(elements.get(0).getAction().equals(Action.CREATE));

    assertTrue(elements.get(1).getElementProperties().equals(pair2._2));
    assertTrue(elements.get(1).getEntityId().equals(2L));
    assertTrue(elements.get(1).getEntity().equals(Entity.ALARM));
    assertTrue(elements.get(1).getAction().equals(Action.CREATE));

    assertTrue(elements.get(2).getElementProperties().equals(pair3._2));
    assertTrue(elements.get(2).getEntityId().equals(3L));
    assertTrue(elements.get(2).getEntity().equals(Entity.ALARM));
    assertTrue(elements.get(2).getAction().equals(Action.CREATE));

    assertTrue(elements.get(3).getElementProperties().equals(pair4._2));
    assertTrue(elements.get(3).getEntityId().equals(4L));
    assertTrue(elements.get(3).getEntity().equals(Entity.ALARM));
    assertTrue(elements.get(3).getAction().equals(Action.CREATE));

    assertTrue(elements.get(4).getElementProperties().equals(pair5._2));
    assertTrue(elements.get(4).getEntityId().equals(5L));
    assertTrue(elements.get(4).getEntity().equals(Entity.ALARM));
    assertTrue(elements.get(4).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache, equipmentCache, dataTagCache, alarmCache);
  }

  @Test
  public void alarmDelete_NotExistingInstance() {
    // Setup Exception
    // alarmDelete.expect(ConfigurationParseException.class);
    // alarmDelete.expectMessage("Deleting Alarm 1 failed.
    // Alarm do not exist in the cache.");

    // Setup Configuration Instance
    Alarm alarm = buildDeleteAlarm(1l);
    Configuration configuration = getConfBuilderAlarm(alarm);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(1L)).andReturn(true);
    // EasyMock.expect(alarmCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, dataTagCache);
    // EasyMock.replay(alarmCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().isEmpty());
    assertTrue(elements.get(0).getEntity().equals(Entity.ALARM));
    assertTrue(elements.get(0).getAction().equals(Action.REMOVE));

    // Verify mock methods were called
    EasyMock.verify(processCache, equipmentCache, dataTagCache);
  }

  /**
   * At the moment, the parsers returns always a Delte ConfigurationElement.
   * Independent from the id in the cache!
   */
  @Test
  public void alarmDelete_ExistingInstance() {
    // Setup Configuration Instance
    Alarm alarm = buildDeleteAlarm(1l);
    Configuration configuration = getConfBuilderAlarm(alarm);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(1L)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, dataTagCache);
    // EasyMock.replay(alarmCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().isEmpty());
    assertTrue(elements.get(0).getEntity().equals(Entity.ALARM));
    assertTrue(elements.get(0).getAction().equals(Action.REMOVE));

    // Verify mock methods were called
    EasyMock.verify(processCache, equipmentCache, dataTagCache);
  }

  @Test
  public void alarmAllOperations_checkOrder() {
    // Setup Configuration Instance
    Pair<Alarm, Properties> pairUpdate = buildUpdateAlarmWithAllFields(2l);
    Pair<Alarm, Properties> pairCreate = buildAlarmWithAllFields(3l);
    Alarm alarmDelete = buildDeleteAlarm(1l);
    Configuration configuration = getConfBuilderAlarm(pairUpdate._1, pairCreate._1, alarmDelete);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(1L)).andReturn(true);

    EasyMock.expect(alarmCache.hasKey(2L)).andReturn(true);
    EasyMock.expect(alarmCache.hasKey(3L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, dataTagCache ,alarmCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 3);

    // Check ConfigurationElement fields and order
    assertTrue(elements.get(0).getElementProperties().isEmpty());
    assertTrue(elements.get(0).getEntity().equals(Entity.ALARM));
    assertTrue(elements.get(0).getAction().equals(Action.REMOVE));

    assertTrue(elements.get(1).getElementProperties().equals(pairCreate._2));
    assertTrue(elements.get(1).getEntity().equals(Entity.ALARM));
    assertTrue(elements.get(1).getAction().equals(Action.CREATE));

    assertTrue(elements.get(2).getElementProperties().equals(pairUpdate._2));
    assertTrue(elements.get(2).getEntity().equals(Entity.ALARM));
    assertTrue(elements.get(2).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache, equipmentCache, dataTagCache, alarmCache);
  }
}
