///*******************************************************************************
// * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
// *
// * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
// * C2MON is free software: you can redistribute it and/or modify it under the
// * terms of the GNU Lesser General Public License as published by the Free
// * Software Foundation, either version 3 of the license.
// *
// * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
// * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// * more details.
// *
// * You should have received a copy of the GNU Lesser General Public License
// * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
// ******************************************************************************/
//
//package cern.c2mon.server.configuration.parser.configuration;
//
//import java.util.*;
//
//import org.easymock.EasyMock;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.ExpectedException;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import cern.c2mon.server.cache.CommandTagCache;
//import cern.c2mon.server.cache.EquipmentCache;
//import cern.c2mon.server.cache.loading.SequenceDAO;
//import cern.c2mon.server.configuration.parser.ConfigurationParser;
//import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
//import cern.c2mon.shared.client.configuration.ConfigConstants;
//import cern.c2mon.shared.client.configuration.ConfigurationElement;
//import cern.c2mon.shared.client.configuration.api.Configuration;
//import cern.c2mon.shared.client.configuration.api.tag.CommandTag;
//import cern.c2mon.shared.client.configuration.api.tag.Tag;
//import cern.c2mon.shared.common.datatag.address.impl.SimpleHardwareAddressImpl;
//
//import static cern.c2mon.server.configuration.parser.util.ConfigurationCommandTagUtil.*;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
///**
// * @author Franz Ritter
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = ConfigurationParserTestMocks.class)
//public class ConfigureCommandTagTest {
//
//  @Autowired
//  ConfigurationParser parser;
//
//  @Autowired
//  EquipmentCache equipmentCache;
//
//  @Autowired
//  SequenceDAO sequenceDAO;
//
//  @Autowired
//  CommandTagCache commandTagCache;
//
//  @Rule
//  public ExpectedException tagException = ExpectedException.none();
//
//
//  @Before
//  public void resetMocks() {
//    EasyMock.reset(equipmentCache, sequenceDAO, commandTagCache);
//  }
//
//  @Test
//  public void createCommandTag() {
//    // setup Configuration:
//
//    Properties expectedProps = new Properties();
//    CommandTag tag = buildCreateBasicCommandTag(expectedProps);
//
//    List<CommandTag> tagList = Arrays.asList(tag);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagList);
//
//    // setUp Mocks:
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(100L);
//    EasyMock.expect(commandTagCache.getCommandTagId("CommandTag")).andReturn(null);
//    EasyMock.expect(commandTagCache.hasKey(100L)).andReturn(false);
//
//    EasyMock.replay(equipmentCache, sequenceDAO,  commandTagCache);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals(parsed.size(), 1);
//    assertEquals((long) parsed.get(0).getEntityId(), 100L);
//    assertTrue(parsed.get(0).getEntity().equals(ConfigConstants.Entity.COMMANDTAG));
//    assertTrue(parsed.get(0).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(0).getElementProperties(), expectedProps);
//
//    EasyMock.verify(equipmentCache, sequenceDAO,  commandTagCache);
//  }
//
//  @Test
//  public void createCommandTagWithAllFields() {
//    Properties expectedProps = new Properties();
//    CommandTag tag = buildCreateAllFieldsCommandTag(101L, expectedProps);
//
//    List<CommandTag> tagList = Arrays.asList(tag);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagList);
//
//    // setUp Mocks:
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//    EasyMock.expect(commandTagCache.getCommandTagId("CommandTag101")).andReturn(null);
//    EasyMock.expect(commandTagCache.hasKey(101L)).andReturn(false);
//
//    EasyMock.replay(equipmentCache, commandTagCache);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals((long) parsed.get(0).getEntityId(), 101L);
//    assertEquals(parsed.get(0).getElementProperties(), expectedProps);
//    assertTrue(parsed.get(0).getEntity().equals(ConfigConstants.Entity.COMMANDTAG));
//    assertTrue(parsed.get(0).getAction().equals(ConfigConstants.Action.CREATE));
//
//    EasyMock.verify(equipmentCache, commandTagCache);
//  }
//
//  @Test
//  public void createMultipleCommandTagWithAllFields() {
//    List<Properties> expectedProperties = new ArrayList<>();
//    List<CommandTag> tagList = new ArrayList<>();
//    for(int i=0; i <5;i++) {
//      expectedProperties.add(new Properties());
//      tagList.add(buildCreateAllFieldsCommandTag((long) (i + 100), expectedProperties.get(i)));
//    }
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagList);
//
//    // setUp Mocks:
//    EasyMock.expect(commandTagCache.getCommandTagId("CommandTag100")).andReturn(null);
//    EasyMock.expect(commandTagCache.hasKey(100L)).andReturn(false);
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//
//    EasyMock.expect(commandTagCache.getCommandTagId("CommandTag101")).andReturn(null);
//    EasyMock.expect(commandTagCache.hasKey(101L)).andReturn(false);
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//
//    EasyMock.expect(commandTagCache.getCommandTagId("CommandTag102")).andReturn(null);
//    EasyMock.expect(commandTagCache.hasKey(102L)).andReturn(false);
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//
//    EasyMock.expect(commandTagCache.getCommandTagId("CommandTag103")).andReturn(null);
//    EasyMock.expect(commandTagCache.hasKey(103L)).andReturn(false);
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//
//    EasyMock.expect(commandTagCache.getCommandTagId("CommandTag104")).andReturn(null);
//    EasyMock.expect(commandTagCache.hasKey(104L)).andReturn(false);
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//
//    EasyMock.replay(equipmentCache, commandTagCache);
//
//    // run the parsing
//    List<ConfigurationElement> elements = parser.parse(config);
//
//    // Assert stuff
//    assertTrue(elements.size() == 5);
//
//    Long id = 100L;
//    for (ConfigurationElement currentElement : elements) {
//      assertEquals(expectedProperties.get(elements.indexOf(currentElement)), currentElement.getElementProperties());
//      assertTrue(currentElement.getEntityId().equals(id++));
//      assertTrue(currentElement.getEntity().equals(ConfigConstants.Entity.COMMANDTAG));
//      assertTrue(currentElement.getAction().equals(ConfigConstants.Action.CREATE));
//    }
//
//    EasyMock.verify(equipmentCache, commandTagCache);
//  }
//
//  @Test
//  public void createCommandTagWithNonExistentEquipment() {
//    // Setup Exception
//    tagException.expect(ConfigurationParseException.class);
//
//    CommandTag tag = CommandTag.create("CommandTag", Integer.class, new SimpleHardwareAddressImpl("testAddress"),
//        30000, 6000, 200, 2, "RBAC class", "RBAC device", "RBAC property").equipmentId(10L).build();
//    tag.setEquipmentId(10L);
//
//    List<CommandTag> tagList = Collections.singletonList(tag);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagList);
//
//    // setUp Mocks:
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(false);
//
//    // run test
//    EasyMock.replay(equipmentCache );
//    parser.parse(config);
//    EasyMock.verify(equipmentCache);
//  }
//
//  @Test
//  public void createExistingCommandTag() {
//    // Setup Exception
//    tagException.expect(ConfigurationParseException.class);
//
//    CommandTag tag = CommandTag.create("CommandTag", Integer.class, new SimpleHardwareAddressImpl("testAddress"),
//        30000, 6000, 200, 2, "RBAC class", "RBAC device", "RBAC property").equipmentId(10L).build();
//    tag.setEquipmentId(10L);
//
//    List<CommandTag> tagList = Arrays.asList(tag);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagList);
//
//    // setUp Mocks:
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//    EasyMock.expect(commandTagCache.getCommandTagId("CommandTag")).andReturn(100L);
//
//    // run test
//    EasyMock.replay(equipmentCache, commandTagCache);
//    parser.parse(config);
//    EasyMock.verify(equipmentCache, commandTagCache);
//  }
//
//  @Test
//  public void updateCommandTagWithName() {
//    // setup Configuration:
//    Properties expectedProps = new Properties();
//    CommandTag dataTag = buildUpdateCommandTagWithSomeFields("myCommandTag", expectedProps);
//
//    List<Tag> tagUpdateList = Collections.singletonList(dataTag);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(commandTagCache.getCommandTagId("myCommandTag")).andReturn(20L).times(2);
//    EasyMock.expect(commandTagCache.hasKey(20L)).andReturn(true);
//
//    EasyMock.replay(commandTagCache);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals((long) parsed.get(0).getEntityId(), 20L);
//    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.COMMANDTAG);
//    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
//    assertEquals(parsed.get(0).getElementProperties(), expectedProps);
//
//    EasyMock.verify(commandTagCache);
//  }
//
//  @Test
//  public void updateCommandTagWithId() {
//    // setup Configuration:
//    Properties expectedProps = new Properties();
//    CommandTag tag = buildUpdateCommandTagWithSomeFields(100L, expectedProps);
//
//    List<Tag> tagUpdateList = Collections.singletonList(tag);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(commandTagCache.hasKey(100L)).andReturn(true);
//
//    EasyMock.replay(commandTagCache);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals((long) parsed.get(0).getEntityId(), 100L);
//    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.COMMANDTAG);
//    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
//    assertEquals(parsed.get(0).getElementProperties(), expectedProps);
//
//    EasyMock.verify(commandTagCache);
//  }
//
//  @Test
//  public void updateCommandTagWithAllFields() {
//    // setup Configuration:
//    Properties expectedProps = new Properties();
//    CommandTag tag = buildUpdateCommandTagWithAllFields(100L, expectedProps);
//
//    List<Tag> tagUpdateList = Collections.singletonList(tag);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(commandTagCache.hasKey(100L)).andReturn(true);
//
//    EasyMock.replay(commandTagCache);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals((long) parsed.get(0).getEntityId(), 100L);
//    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.COMMANDTAG);
//    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
//    assertEquals(parsed.get(0).getElementProperties(), expectedProps);
//
//    EasyMock.verify(commandTagCache);
//  }
//
//  @Test
//  public void updateNonExistentCommandTag() {
//    // setup Configuration:
//    CommandTag tag = CommandTag.update(20L).description("The description").build();
//
//    List<Tag> tagUpdateList = Collections.singletonList(tag);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(commandTagCache.hasKey(20L)).andReturn(false);
//
//    // run test
//    EasyMock.replay(commandTagCache);
//
//    List<ConfigurationElement> result = parser.parse(config);
//
//    assertEquals(1, result.size());
//    assertEquals(ConfigConstants.Entity.MISSING, result.get(0).getEntity());
//    assertEquals(ConfigConstants.Status.WARNING, result.get(0).getStatus());
//
//    EasyMock.verify(commandTagCache);
//  }
//
//  @Test
//  public void deleteCommandTag() {
//    // setup Configuration:
//    CommandTag tag = buildDeleteCommandTag(20L);
//
//    List<Tag> tagUpdateList = Collections.singletonList(tag);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(commandTagCache.hasKey(20L)).andReturn(true);
//
//    EasyMock.replay(commandTagCache);
//
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals(parsed.size(), 1);
//    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.REMOVE);
//    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.COMMANDTAG);
//    assertTrue(parsed.get(0).getElementProperties().isEmpty());
//
//    EasyMock.verify(commandTagCache);
//  }
//
//  @Test
//  public void deleteNonExistentCommandTag() {
//    // setup Configuration:
//    CommandTag dataTag = new CommandTag();
//    dataTag.setId(20L);
//    dataTag.setDeleted(true);
//
//    List<Tag> tagUpdateList = Arrays.asList(dataTag);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(commandTagCache.hasKey(20L)).andReturn(false);
//    EasyMock.replay(commandTagCache);
//
//    assertEquals(0, parser.parse(config).size());
//
//    EasyMock.verify(commandTagCache);
//  }
//}
