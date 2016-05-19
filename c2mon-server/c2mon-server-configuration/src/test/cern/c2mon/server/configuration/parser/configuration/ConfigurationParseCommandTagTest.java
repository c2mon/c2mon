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
package cern.c2mon.server.configuration.parser.configuration;

import cern.c2mon.server.cache.CommandTagCache;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.server.configuration.parser.impl.ConfigurationParserImpl;
import cern.c2mon.server.configuration.parser.util.Pair;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.tag.CommandTag;
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

import static cern.c2mon.server.configuration.parser.util.ConfigurationCommandTagUtil.*;
import static cern.c2mon.server.configuration.parser.util.ConfigurationUtil.getConfBuilderCommandTag;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:cern/c2mon/server/configuration/config/server-configuration-parser-test.xml" })
public class ConfigurationParseCommandTagTest {

  @Autowired
  ConfigurationParserImpl configurationParser;

  @Autowired
  ProcessCache processCache;

  @Autowired
  EquipmentCache equipmentCache;

  @Autowired
  CommandTagCache commandTagCache;

  @Rule
  public ExpectedException commandTagUpdate = ExpectedException.none();

  @Rule
  public ExpectedException commandTagCreate = ExpectedException.none();

  @Rule
  public ExpectedException commandTagDelete = ExpectedException.none();

  @Before
  public void resetMocks() {
    EasyMock.reset(processCache, equipmentCache, commandTagCache);
  }

  @Test
  public void commandTagUpdate_withNoFields() {
    // Setup Configuration Instance
    Configuration configuration = getConfBuilderCommandTag(1L, 1L, buildCommandTagWithId(1L)._1);

    // CommandTag.builder().address(new CommandTagAddress(new
    // PLCHardwareAddressImpl(0, 0, 0, 0, 0, 0.0f, "")));

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(commandTagCache.hasKey(1L)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, commandTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertFalse(elements.size() > 0);

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(commandTagCache);
  }

  @Test
  public void commandTagUpdate_notExistingInstance() {
    // Setup Exception
    commandTagUpdate.expect(ConfigurationParseException.class);
    commandTagUpdate.expectMessage("Creating CommandTag (id = 1) failed: Not enough arguments.");

    // Setup Configuration Instance
    Pair<CommandTag, Properties> pair = buildUpdateCommandTagWithSomeFields(1l);
    Configuration configuration = getConfBuilderCommandTag(1L, 1L, pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(commandTagCache.hasKey(1l)).andReturn(false).times(2);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, commandTagCache);

    // Run the code to be tested
    configurationParser.parse(configuration);

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(commandTagCache);
  }

  @Test
  public void commandTagUpdate_withSomeFields() {
    // Setup Configuration Instance
    Pair<CommandTag, Properties> pair = buildUpdateCommandTagWithSomeFields(1l);
    Configuration configuration = getConfBuilderCommandTag(1L, 1L, pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1l)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(commandTagCache.hasKey(1l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, commandTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.COMMANDTAG));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(commandTagCache);
  }

  @Test
  public void commandTagUpdate_withAllFields() {
    // Setup Configuration Instance
    Pair<CommandTag, Properties> pair = buildUpdateCommandTagWithAllFields(1l);
    Configuration configuration = getConfBuilderCommandTag(1L, 1L, pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(commandTagCache.hasKey(1l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, commandTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.COMMANDTAG));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(commandTagCache);
  }

  @Test
  public void commandTagUpdate_multipleInstances_withAllFields() {
    // Setup Configuration Instance
    Pair<CommandTag, Properties> pair1 = buildUpdateCommandTagWithAllFields(1l);
    Pair<CommandTag, Properties> pair2 = buildUpdateCommandTagWithAllFields(2l);
    Pair<CommandTag, Properties> pair3 = buildUpdateCommandTagWithAllFields(3l);
    Pair<CommandTag, Properties> pair4 = buildUpdateCommandTagWithAllFields(4l);
    Pair<CommandTag, Properties> pair5 = buildUpdateCommandTagWithAllFields(5l);

    Configuration configuration = getConfBuilderCommandTag(1L, 1L, pair1._1, pair2._1, pair3._1, pair4._1, pair5._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);

    EasyMock.expect(commandTagCache.hasKey(1l)).andReturn(true);
    EasyMock.expect(commandTagCache.hasKey(2l)).andReturn(true);
    EasyMock.expect(commandTagCache.hasKey(3l)).andReturn(true);
    EasyMock.expect(commandTagCache.hasKey(4l)).andReturn(true);
    EasyMock.expect(commandTagCache.hasKey(5l)).andReturn(true);

    // Switch to replay mode

//    EasyMock.replay(processCache, equipmentCache, commandTagCache);
    EasyMock.replay(processCache);
    EasyMock.replay(equipmentCache);
    EasyMock.replay(commandTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 5);

    assertTrue(elements.get(0).getElementProperties().equals(pair1._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.COMMANDTAG));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(1).getElementProperties().equals(pair2._2));
    assertTrue(elements.get(1).getEntity().equals(Entity.COMMANDTAG));
    assertTrue(elements.get(1).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(2).getElementProperties().equals(pair3._2));
    assertTrue(elements.get(2).getEntity().equals(Entity.COMMANDTAG));
    assertTrue(elements.get(2).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(3).getElementProperties().equals(pair4._2));
    assertTrue(elements.get(3).getEntity().equals(Entity.COMMANDTAG));
    assertTrue(elements.get(3).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(4).getElementProperties().equals(pair5._2));
    assertTrue(elements.get(4).getEntity().equals(Entity.COMMANDTAG));
    assertTrue(elements.get(4).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(commandTagCache);
  }

  @Test
  public void commandTagCreate_withNoFields() {
    // Setup Exception
    commandTagCreate.expect(ConfigurationParseException.class);
    commandTagCreate.expectMessage("Creating CommandTag (id = 1) failed: Not enough arguments.");

    // Setup Configuration Instance
    Configuration configuration = getConfBuilderCommandTag(1L, 1L, buildCommandTagWithId(1L)._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(commandTagCache.hasKey(1L)).andReturn(false).times(2);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, commandTagCache);

    // Run the code to be tested
    configurationParser.parse(configuration);

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(commandTagCache);
  }

  @Test
  public void commandTagCreate_withMandatoryFields() {
    // Setup Configuration Instance
    Pair<CommandTag, Properties> pair = buildCommandTagWithPrimFields(1l);
    Configuration configuration = getConfBuilderCommandTag(1L, 1L, pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(commandTagCache.hasKey(1l)).andReturn(false).times(2);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, commandTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntityId().equals(1L));
    assertTrue(elements.get(0).getEntity().equals(Entity.COMMANDTAG));
    assertTrue(elements.get(0).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(commandTagCache);
  }

  @Test
  public void commandTagCreate_withNotExistingSupClass() {
    commandTagCreate.expect(ConfigurationParseException.class);
    commandTagCreate.expectMessage("Creating Process (id = 1) failed: Not enough arguments.");

    // Setup Configuration Instance
    Pair<CommandTag, Properties> pair = buildCommandTagWithPrimFields(1l);
    Configuration configuration = getConfBuilderCommandTag(1L, 1L, pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(false).times(2);

    // Switch to replay mode
    EasyMock.replay(processCache);

    // Run the code to be tested
    configurationParser.parse(configuration);

    // Verify mock methods were called
    EasyMock.verify(processCache);
  }

  @Test
  public void commandTagCreate_withAllFields() {
    // Setup Configuration Instance
    Pair<CommandTag, Properties> pair = buildCommandTagWithAllFields(1l);
    Configuration configuration = getConfBuilderCommandTag(1L, 1L, pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(commandTagCache.hasKey(1l)).andReturn(false).times(2);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, commandTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntityId().equals(1L));
    assertTrue(elements.get(0).getEntity().equals(Entity.COMMANDTAG));
    assertTrue(elements.get(0).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(commandTagCache);
  }

  @Test
  public void commandTagCreate_withoutDefaultFields() {
    // Setup Configuration Instance
    Pair<CommandTag, Properties> pair = buildCommandTagWithoutDefaultFields(1l);
    Configuration configuration = getConfBuilderCommandTag(1L, 1L, pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(commandTagCache.hasKey(1l)).andReturn(false).times(2);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, commandTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntityId().equals(1L));
    assertTrue(elements.get(0).getEntity().equals(Entity.COMMANDTAG));
    assertTrue(elements.get(0).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(commandTagCache);
  }

  @Test
  public void commandTagCreate_multipleInstances_withAllFields() {
    // Setup Configuration Instance
    Pair<CommandTag, Properties> pair1 = buildCommandTagWithAllFields(1l);
    Pair<CommandTag, Properties> pair2 = buildCommandTagWithAllFields(2l);
    Pair<CommandTag, Properties> pair3 = buildCommandTagWithAllFields(3l);
    Pair<CommandTag, Properties> pair4 = buildCommandTagWithAllFields(4l);
    Pair<CommandTag, Properties> pair5 = buildCommandTagWithAllFields(5l);

    Configuration configuration = getConfBuilderCommandTag(1L, 1L, pair1._1, pair2._1, pair3._1, pair4._1, pair5._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);

    EasyMock.expect(commandTagCache.hasKey(1l)).andReturn(false).times(2);
    EasyMock.expect(commandTagCache.hasKey(2l)).andReturn(false).times(2);
    EasyMock.expect(commandTagCache.hasKey(3l)).andReturn(false).times(2);
    EasyMock.expect(commandTagCache.hasKey(4l)).andReturn(false).times(2);
    EasyMock.expect(commandTagCache.hasKey(5l)).andReturn(false).times(2);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, commandTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 5);

    assertTrue(elements.get(0).getElementProperties().equals(pair1._2));
    assertTrue(elements.get(0).getEntityId().equals(1L));
    assertTrue(elements.get(0).getEntity().equals(Entity.COMMANDTAG));
    assertTrue(elements.get(0).getAction().equals(Action.CREATE));

    assertTrue(elements.get(1).getElementProperties().equals(pair2._2));
    assertTrue(elements.get(1).getEntityId().equals(2L));
    assertTrue(elements.get(1).getEntity().equals(Entity.COMMANDTAG));
    assertTrue(elements.get(1).getAction().equals(Action.CREATE));

    assertTrue(elements.get(2).getElementProperties().equals(pair3._2));
    assertTrue(elements.get(2).getEntityId().equals(3L));
    assertTrue(elements.get(2).getEntity().equals(Entity.COMMANDTAG));
    assertTrue(elements.get(2).getAction().equals(Action.CREATE));

    assertTrue(elements.get(3).getElementProperties().equals(pair4._2));
    assertTrue(elements.get(3).getEntityId().equals(4L));
    assertTrue(elements.get(3).getEntity().equals(Entity.COMMANDTAG));
    assertTrue(elements.get(3).getAction().equals(Action.CREATE));

    assertTrue(elements.get(4).getElementProperties().equals(pair5._2));
    assertTrue(elements.get(4).getEntityId().equals(5L));
    assertTrue(elements.get(4).getEntity().equals(Entity.COMMANDTAG));
    assertTrue(elements.get(4).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(commandTagCache);
  }

  @Test
  public void commandTagDelete_NotExistingInstance() {
    // Setup Exception
     commandTagDelete.expect(ConfigurationParseException.class);
     commandTagDelete.expectMessage("Deleting of CommandTag (id = 1) failed: The object is unknown to the sever.");

    // Setup Configuration Instance
    CommandTag commandTag = buildDeleteCommandTag(1l);
    Configuration configuration = getConfBuilderCommandTag(1L, 1L, commandTag);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
     EasyMock.expect(commandTagCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, commandTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().isEmpty());
    assertTrue(elements.get(0).getEntity().equals(Entity.COMMANDTAG));
    assertTrue(elements.get(0).getAction().equals(Action.REMOVE));

    // Verify mock methods were called
    EasyMock.verify(processCache, equipmentCache, commandTagCache);
  }

  /**
   * At the moment, the parsers returns always a Delte ConfigurationElement.
   * Independent from the id in the cache!
   */
  @Test
  public void commandTagDelete_ExistingInstance() {
    // Setup Configuration Instance
    CommandTag commandTag = buildDeleteCommandTag(1l);
    Configuration configuration = getConfBuilderCommandTag(1L, 1L, commandTag);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(commandTagCache.hasKey(1L)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache , commandTagCache);
    // EasyMock.replay(commandTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().isEmpty());
    assertTrue(elements.get(0).getEntity().equals(Entity.COMMANDTAG));
    assertTrue(elements.get(0).getAction().equals(Action.REMOVE));

    // Verify mock methods were called
    EasyMock.verify(processCache, equipmentCache, commandTagCache);
  }

  @Test
  public void commandTagAllOperations_checkOrder() {
    // Setup Configuration Instance
    Pair<CommandTag, Properties> pairUpdate = buildUpdateCommandTagWithAllFields(2l);
    Pair<CommandTag, Properties> pairCreate = buildCommandTagWithAllFields(3l);
    CommandTag commandTagDelete = buildDeleteCommandTag(1l);
    Configuration configuration = getConfBuilderCommandTag(1L, 1L, pairUpdate._1, pairCreate._1, commandTagDelete);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);

    EasyMock.expect(commandTagCache.hasKey(2L)).andReturn(true);
    EasyMock.expect(commandTagCache.hasKey(3L)).andReturn(false).times(2);
    EasyMock.expect(commandTagCache.hasKey(1L)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache, equipmentCache, commandTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 3);

    // Check ConfigurationElement fields and order
    assertTrue(elements.get(0).getElementProperties().isEmpty());
    assertTrue(elements.get(0).getEntity().equals(Entity.COMMANDTAG));
    assertTrue(elements.get(0).getAction().equals(Action.REMOVE));

    assertTrue(elements.get(1).getElementProperties().equals(pairCreate._2));
    assertTrue(elements.get(1).getEntity().equals(Entity.COMMANDTAG));
    assertTrue(elements.get(1).getAction().equals(Action.CREATE));

    assertTrue(elements.get(2).getElementProperties().equals(pairUpdate._2));
    assertTrue(elements.get(2).getEntity().equals(Entity.COMMANDTAG));
    assertTrue(elements.get(2).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(commandTagCache);
  }
}
