package cern.c2mon.server.configuration.parser.configuration;

import static cern.c2mon.server.configuration.parser.util.ConfigurationDataTagUtil.buildDataTagWithAllFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationDataTagUtil.buildDataTagWithId;
import static cern.c2mon.server.configuration.parser.util.ConfigurationDataTagUtil.buildDataTagWithPrimFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationDataTagUtil.buildDataTagWithoutDefaultFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationDataTagUtil.buildDeleteDataTag;
import static cern.c2mon.server.configuration.parser.util.ConfigurationDataTagUtil.buildUpdateDataTagWithAllFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationDataTagUtil.buildUpdateDataTagWithSomeFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationUtil.getConfBuilderDataTagE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.configuration.parser.impl.ConfigurationParserImpl;
import cern.c2mon.server.configuration.parser.util.Pair;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:cern/c2mon/server/configuration/config/server-configuration-parser-test.xml" })
public class ConfigurationParseDataTagTest {

  @Autowired
  ConfigurationParserImpl configurationParser;

  @Autowired
  ProcessCache processCache;

  @Autowired
  EquipmentCache equipmentCache;

  @Autowired
  DataTagCache dataTagCache;

  @Rule
  public ExpectedException dataTagUpdate = ExpectedException.none();

  @Rule
  public ExpectedException dataTagCreate = ExpectedException.none();

  @Rule
  public ExpectedException dataTagDelete = ExpectedException.none();

  @Before
  public void resetMocks() {
    EasyMock.reset(processCache, equipmentCache, dataTagCache);
  }

  @Test
  public void dataTagUpdate_withNoFields() {
    // Setup Configuration Instance
    Configuration configuration = getConfBuilderDataTagE(buildDataTagWithId(1L)._1);

    // DataTag.builder().address(new DataTagAddress(new
    // PLCHardwareAddressImpl(0, 0, 0, 0, 0, 0.0f, "")));

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(1L)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, dataTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertFalse(elements.size() > 0);

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(dataTagCache);
  }

  @Test
  public void dataTagUpdate_notExistingInstance() {
    // Setup Exception
    dataTagUpdate.expect(ConfigurationParseException.class);
    dataTagUpdate.expectMessage("Creating DataTag (id = 1) failed. Not enough arguments.");

    // Setup Configuration Instance
    Pair<DataTag<Number>, Properties> pair = buildUpdateDataTagWithSomeFields(1l);
    Configuration configuration = getConfBuilderDataTagE(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, dataTagCache);

    // Run the code to be tested
    configurationParser.parse(configuration);

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(dataTagCache);
  }

  @Test
  public void dataTagUpdate_withSomeFields() {
    // Setup Configuration Instance
    Pair<DataTag<Number>, Properties> pair = buildUpdateDataTagWithSomeFields(1l);
    Configuration configuration = getConfBuilderDataTagE(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1l)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(1l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, dataTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.DATATAG));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(dataTagCache);
  }

  @Test
  public void dataTagUpdate_withAllFields() {
    // Setup Configuration Instance
    Pair<DataTag<Number>, Properties> pair = buildUpdateDataTagWithAllFields(1l);
    Configuration configuration = getConfBuilderDataTagE(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(1l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, dataTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.DATATAG));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(dataTagCache);
  }

  @Test
  public void dataTagUpdate_multipleInstances_withAllFields() {
    // Setup Configuration Instance
    Pair<DataTag<Number>, Properties> pair1 = buildUpdateDataTagWithAllFields(1l);
    Pair<DataTag<Number>, Properties> pair2 = buildUpdateDataTagWithAllFields(2l);
    Pair<DataTag<Number>, Properties> pair3 = buildUpdateDataTagWithAllFields(3l);
    Pair<DataTag<Number>, Properties> pair4 = buildUpdateDataTagWithAllFields(4l);
    Pair<DataTag<Number>, Properties> pair5 = buildUpdateDataTagWithAllFields(5l);

    Configuration configuration = getConfBuilderDataTagE(pair1._1, pair2._1, pair3._1, pair4._1, pair5._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);

    EasyMock.expect(dataTagCache.hasKey(1l)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(2l)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(3l)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(4l)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(5l)).andReturn(true);

    // Switch to replay mode

//    EasyMock.replay(processCache, equipmentCache, dataTagCache);
    EasyMock.replay(processCache);
    EasyMock.replay(equipmentCache);
    EasyMock.replay(dataTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 5);

    assertTrue(elements.get(0).getElementProperties().equals(pair1._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.DATATAG));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(1).getElementProperties().equals(pair2._2));
    assertTrue(elements.get(1).getEntity().equals(Entity.DATATAG));
    assertTrue(elements.get(1).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(2).getElementProperties().equals(pair3._2));
    assertTrue(elements.get(2).getEntity().equals(Entity.DATATAG));
    assertTrue(elements.get(2).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(3).getElementProperties().equals(pair4._2));
    assertTrue(elements.get(3).getEntity().equals(Entity.DATATAG));
    assertTrue(elements.get(3).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(4).getElementProperties().equals(pair5._2));
    assertTrue(elements.get(4).getEntity().equals(Entity.DATATAG));
    assertTrue(elements.get(4).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(dataTagCache);
  }

  @Test
  public void dataTagCreate_withNoFields() {
    // Setup Exception
    dataTagCreate.expect(ConfigurationParseException.class);
    dataTagCreate.expectMessage("Creating DataTag (id = 1) failed. Not enough arguments.");

    // Setup Configuration Instance
    Configuration configuration = getConfBuilderDataTagE(buildDataTagWithId(1L)._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, dataTagCache);

    // Run the code to be tested
    configurationParser.parse(configuration);

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(dataTagCache);
  }

  @Test
  public void dataTagCreate_withMandatoryFields() {
    // Setup Configuration Instance
    Pair<DataTag<Number>, Properties> pair = buildDataTagWithPrimFields(1l);
    Configuration configuration = getConfBuilderDataTagE(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, dataTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntityId().equals(1L));
    assertTrue(elements.get(0).getEntity().equals(Entity.DATATAG));
    assertTrue(elements.get(0).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(dataTagCache);
  }

  @Test
  public void dataTagCreate_withNotExistingSupClass() {
    dataTagCreate.expect(ConfigurationParseException.class);
    dataTagCreate.expectMessage("Creating Process (id = 1) failed. Not enough arguments.");

    // Setup Configuration Instance
    Pair<DataTag<Number>, Properties> pair = buildDataTagWithPrimFields(1l);
    Configuration configuration = getConfBuilderDataTagE(pair._1);

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
  public void dataTagCreate_withAllFields() {
    // Setup Configuration Instance
    Pair<DataTag<Number>, Properties> pair = buildDataTagWithAllFields(1l);
    Configuration configuration = getConfBuilderDataTagE(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, dataTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntityId().equals(1L));
    assertTrue(elements.get(0).getEntity().equals(Entity.DATATAG));
    assertTrue(elements.get(0).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(dataTagCache);
  }

  @Test
  public void dataTagCreate_withoutDefaultFields() {
    // Setup Configuration Instance
    Pair<DataTag<Number>, Properties> pair = buildDataTagWithoutDefaultFields(1l);
    Configuration configuration = getConfBuilderDataTagE(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, dataTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntityId().equals(1L));
    assertTrue(elements.get(0).getEntity().equals(Entity.DATATAG));
    assertTrue(elements.get(0).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(dataTagCache);
  }

  @Test
  public void dataTagCreate_multipleInstances_withAllFields() {
    // Setup Configuration Instance
    Pair<DataTag<Number>, Properties> pair1 = buildDataTagWithAllFields(1l);
    Pair<DataTag<Number>, Properties> pair2 = buildDataTagWithAllFields(2l);
    Pair<DataTag<Number>, Properties> pair3 = buildDataTagWithAllFields(3l);
    Pair<DataTag<Number>, Properties> pair4 = buildDataTagWithAllFields(4l);
    Pair<DataTag<Number>, Properties> pair5 = buildDataTagWithAllFields(5l);

    Configuration configuration = getConfBuilderDataTagE(pair1._1, pair2._1, pair3._1, pair4._1, pair5._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);

    EasyMock.expect(dataTagCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(dataTagCache.hasKey(2l)).andReturn(false);
    EasyMock.expect(dataTagCache.hasKey(3l)).andReturn(false);
    EasyMock.expect(dataTagCache.hasKey(4l)).andReturn(false);
    EasyMock.expect(dataTagCache.hasKey(5l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, dataTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 5);

    assertTrue(elements.get(0).getElementProperties().equals(pair1._2));
    assertTrue(elements.get(0).getEntityId().equals(1L));
    assertTrue(elements.get(0).getEntity().equals(Entity.DATATAG));
    assertTrue(elements.get(0).getAction().equals(Action.CREATE));

    assertTrue(elements.get(1).getElementProperties().equals(pair2._2));
    assertTrue(elements.get(1).getEntityId().equals(2L));
    assertTrue(elements.get(1).getEntity().equals(Entity.DATATAG));
    assertTrue(elements.get(1).getAction().equals(Action.CREATE));

    assertTrue(elements.get(2).getElementProperties().equals(pair3._2));
    assertTrue(elements.get(2).getEntityId().equals(3L));
    assertTrue(elements.get(2).getEntity().equals(Entity.DATATAG));
    assertTrue(elements.get(2).getAction().equals(Action.CREATE));

    assertTrue(elements.get(3).getElementProperties().equals(pair4._2));
    assertTrue(elements.get(3).getEntityId().equals(4L));
    assertTrue(elements.get(3).getEntity().equals(Entity.DATATAG));
    assertTrue(elements.get(3).getAction().equals(Action.CREATE));

    assertTrue(elements.get(4).getElementProperties().equals(pair5._2));
    assertTrue(elements.get(4).getEntityId().equals(5L));
    assertTrue(elements.get(4).getEntity().equals(Entity.DATATAG));
    assertTrue(elements.get(4).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(dataTagCache);
  }

  @Test
  public void dataTagDelete_NotExistingInstance() {
    // Setup Exception
    // dataTagDelete.expect(ConfigurationParseException.class);
    // dataTagDelete.expectMessage("Deleting DataTag 1 failed.
    // DataTag do not exist in the cache.");

    // Setup Configuration Instance
    DataTag<Number> dataTag = buildDeleteDataTag(1l);
    Configuration configuration = getConfBuilderDataTagE(dataTag);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    // EasyMock.expect(dataTagCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache);
    // EasyMock.replay(dataTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().isEmpty());
    assertTrue(elements.get(0).getEntity().equals(Entity.DATATAG));
    assertTrue(elements.get(0).getAction().equals(Action.REMOVE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
  }

  /**
   * At the moment, the parsers returns always a Delte ConfigurationElement.
   * Independent from the id in the cache!
   */
  @Test
  public void dataTagDelete_ExistingInstance() {
    // Setup Configuration Instance
    DataTag<Number> dataTag = buildDeleteDataTag(1l);
    Configuration configuration = getConfBuilderDataTagE(dataTag);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache );
    // EasyMock.replay(dataTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().isEmpty());
    assertTrue(elements.get(0).getEntity().equals(Entity.DATATAG));
    assertTrue(elements.get(0).getAction().equals(Action.REMOVE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
  }

  @Test
  public void dataTagAllOperations_checkOrder() {
    // Setup Configuration Instance
    Pair<DataTag<Number>, Properties> pairUpdate = buildUpdateDataTagWithAllFields(2l);
    Pair<DataTag<Number>, Properties> pairCreate = buildDataTagWithAllFields(3l);
    DataTag<Number> dataTagDelete = buildDeleteDataTag(1l);
    Configuration configuration = getConfBuilderDataTagE(pairUpdate._1, pairCreate._1, dataTagDelete);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);

    EasyMock.expect(dataTagCache.hasKey(2L)).andReturn(true);
    EasyMock.expect(dataTagCache.hasKey(3L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, dataTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 3);

    // Check ConfigurationElement fields and order
    assertTrue(elements.get(0).getElementProperties().isEmpty());
    assertTrue(elements.get(0).getEntity().equals(Entity.DATATAG));
    assertTrue(elements.get(0).getAction().equals(Action.REMOVE));

    assertTrue(elements.get(1).getElementProperties().equals(pairCreate._2));
    assertTrue(elements.get(1).getEntity().equals(Entity.DATATAG));
    assertTrue(elements.get(1).getAction().equals(Action.CREATE));

    assertTrue(elements.get(2).getElementProperties().equals(pairUpdate._2));
    assertTrue(elements.get(2).getEntity().equals(Entity.DATATAG));
    assertTrue(elements.get(2).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(dataTagCache);
  }
}
