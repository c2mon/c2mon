/*******************************************************************************
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
 ******************************************************************************/

package cern.c2mon.server.configuration.parser.configuration;

import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.TagFacadeGateway;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.configuration.parser.ConfigurationParser;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.client.configuration.api.tag.Tag;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static cern.c2mon.server.configuration.parser.util.ConfigurationDataTagUtil.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Franz Ritter
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:test-config/server-configuration-parser-test.xml"})
public class ConfigureDataTagTest {

  @Autowired
  ConfigurationParser parser;

  @Autowired
  EquipmentCache equipmentCache;

  @Autowired
  SubEquipmentCache subEquipmentCache;

  @Autowired
  SequenceDAO sequenceDAO;

  @Autowired
  TagFacadeGateway tagFacadeGateway;

  @Autowired
  DataTagCache dataTagCache;

  @Rule
  public ExpectedException tagException = ExpectedException.none();


  @Before
  public void resetMocks() {
    EasyMock.reset(equipmentCache, sequenceDAO, dataTagCache, subEquipmentCache,tagFacadeGateway);
  }

  @Test
  public void createDataTag() {
    // setup Configuration:

    Properties expectedProps = new Properties();
    DataTag tag = buildCreateBasicDataTag(expectedProps);

    List<DataTag> dataTagList = Arrays.asList(tag);

    Configuration config = new Configuration(1L);
    config.setEntities(dataTagList);

    // setUp Mocks:
    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(100L);
    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(false);

    EasyMock.replay(equipmentCache, sequenceDAO, tagFacadeGateway);

    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals((long) parsed.get(0).getEntityId(), 100L);
    assertTrue(parsed.get(0).getEntity().equals(ConfigConstants.Entity.DATATAG));
    assertTrue(parsed.get(0).getAction().equals(ConfigConstants.Action.CREATE));
    assertEquals(parsed.get(0).getElementProperties(), expectedProps);

    EasyMock.verify(equipmentCache, sequenceDAO, tagFacadeGateway);
  }

  @Test
  public void createDataTagWithAllFields() {
    Properties expectedProps = new Properties();
    DataTag tag = buildCreateAllFieldsDataTag(101L, expectedProps);

    List<DataTag> dataTagList = Arrays.asList(tag);

    Configuration config = new Configuration(1L);
    config.setEntities(dataTagList);

    // setUp Mocks:
    EasyMock.expect(tagFacadeGateway.isInTagCache(101L)).andReturn(false);
    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);

    EasyMock.replay(equipmentCache, sequenceDAO, tagFacadeGateway);

    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals((long) parsed.get(0).getEntityId(), 101L);
    assertEquals(parsed.get(0).getElementProperties(), expectedProps);
    assertTrue(parsed.get(0).getEntity().equals(ConfigConstants.Entity.DATATAG));
    assertTrue(parsed.get(0).getAction().equals(ConfigConstants.Action.CREATE));

    EasyMock.verify(equipmentCache, sequenceDAO, tagFacadeGateway);
  }

  @Test
  public void createMultipleDataTagWithAllFields() {
    Properties expectedProps1 = new Properties();
    Properties expectedProps2 = new Properties();
    Properties expectedProps3 = new Properties();
    Properties expectedProps4 = new Properties();
    Properties expectedProps5 = new Properties();

    DataTag tag1 = buildCreateAllFieldsDataTag(101L, expectedProps1);
    DataTag tag2 = buildCreateAllFieldsDataTag(102L, expectedProps2);
    DataTag tag3 = buildCreateAllFieldsDataTag(103L, expectedProps3);
    DataTag tag4 = buildCreateAllFieldsDataTag(104L, expectedProps4);
    DataTag tag5 = buildCreateAllFieldsDataTag(105L, expectedProps5);

    List<DataTag> dataTagList = Arrays.asList(tag1, tag2, tag3, tag4, tag5);

    Configuration config = new Configuration(1L);
    config.setEntities(dataTagList);

    // setUp Mocks:
    EasyMock.expect(tagFacadeGateway.isInTagCache(101L)).andReturn(false);
    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);

    EasyMock.expect(tagFacadeGateway.isInTagCache(102L)).andReturn(false);
    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);

    EasyMock.expect(tagFacadeGateway.isInTagCache(103L)).andReturn(false);
    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);

    EasyMock.expect(tagFacadeGateway.isInTagCache(104L)).andReturn(false);
    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);

    EasyMock.expect(tagFacadeGateway.isInTagCache(105L)).andReturn(false);
    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);

    EasyMock.replay(equipmentCache, sequenceDAO, tagFacadeGateway);

    // run the parsing
    List<ConfigurationElement> elements = parser.parse(config);

    // Assert stuff
    assertTrue(elements.size() == 5);

    assertEquals(elements.get(0).getElementProperties(), expectedProps1);
    assertTrue(elements.get(0).getEntityId().equals(101L));
    assertTrue(elements.get(0).getEntity().equals(ConfigConstants.Entity.DATATAG));
    assertTrue(elements.get(0).getAction().equals(ConfigConstants.Action.CREATE));

    assertEquals(elements.get(1).getElementProperties(), expectedProps2);
    assertTrue(elements.get(1).getEntityId().equals(102L));
    assertTrue(elements.get(1).getEntity().equals(ConfigConstants.Entity.DATATAG));
    assertTrue(elements.get(1).getAction().equals(ConfigConstants.Action.CREATE));

    assertEquals(elements.get(2).getElementProperties(), expectedProps3);
    assertTrue(elements.get(2).getEntityId().equals(103L));
    assertTrue(elements.get(2).getEntity().equals(ConfigConstants.Entity.DATATAG));
    assertTrue(elements.get(2).getAction().equals(ConfigConstants.Action.CREATE));

    assertEquals(elements.get(3).getElementProperties(), expectedProps4);
    assertTrue(elements.get(3).getEntityId().equals(104L));
    assertTrue(elements.get(3).getEntity().equals(ConfigConstants.Entity.DATATAG));
    assertTrue(elements.get(3).getAction().equals(ConfigConstants.Action.CREATE));

    assertEquals(elements.get(4).getElementProperties(), expectedProps5);
    assertTrue(elements.get(4).getEntityId().equals(105L));
    assertTrue(elements.get(4).getEntity().equals(ConfigConstants.Entity.DATATAG));
    assertTrue(elements.get(4).getAction().equals(ConfigConstants.Action.CREATE));

    EasyMock.verify(equipmentCache, sequenceDAO, tagFacadeGateway);
  }

  @Test
  public void createDataTagWithNotExistingEquipment() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Creating of a new DataTag (id = 21) failed: No Equipment with the id 10 found");

    DataTag tag = DataTag.create("myDataTag", Integer.class, new DataTagAddress()).id(21L).build();
    tag.setEquipmentId(10L);

    List<DataTag> dataTagList = Arrays.asList(tag);

    Configuration config = new Configuration(1L);
    config.setEntities(dataTagList);

    // setUp Mocks:
    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(false);

    // run test
    EasyMock.replay(equipmentCache );
    parser.parse(config);
    EasyMock.verify(equipmentCache);
  }

  @Test
  public void createExistingDataTag() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Error while parsing a 'create' Configuration: Id 21 of the class DataTag already known to the server");

    DataTag tag = DataTag.create("myDataTag", Integer.class, new DataTagAddress()).id(21L).build();
    tag.setEquipmentId(10L);

    List<DataTag> dataTagList = Arrays.asList(tag);

    Configuration config = new Configuration(1L);
    config.setEntities(dataTagList);

    // setUp Mocks:
    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
    EasyMock.expect(tagFacadeGateway.isInTagCache(21L)).andReturn(true);

    // run test
    EasyMock.replay(equipmentCache, tagFacadeGateway);
    parser.parse(config);
    EasyMock.verify(equipmentCache, tagFacadeGateway);
  }

  @Test
  public void updateDataTagWithName() {
    // setup Configuration:
    Properties expectedProps = new Properties();
    DataTag dataTag = buildUpdateDataTagWithSomeFields("myDataTag", expectedProps);

    List<Tag> tagUpdateList = Arrays.asList(dataTag);

    Configuration config = new Configuration(1L);
    config.setEntities(tagUpdateList);

    // setUp Mocks:
    EasyMock.expect(dataTagCache.get("myDataTag")).andReturn(new DataTagCacheObject(20L));
    EasyMock.expect(tagFacadeGateway.isInTagCache(20L)).andReturn(true);

    EasyMock.replay(dataTagCache, tagFacadeGateway);

    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals((long) parsed.get(0).getEntityId(), 20L);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.DATATAG);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
    assertEquals(parsed.get(0).getElementProperties(), expectedProps);

    EasyMock.verify(dataTagCache, tagFacadeGateway);
  }

  @Test
  public void updateDataTagWithId() {
    // setup Configuration:
    Properties expectedProps = new Properties();
    DataTag dataTag = buildUpdateDataTagWithSomeFields(100L, expectedProps);

    List<Tag> tagUpdateList = Arrays.asList(dataTag);

    Configuration config = new Configuration(1L);
    config.setEntities(tagUpdateList);

    // setUp Mocks:
    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(true);

    EasyMock.replay(tagFacadeGateway);

    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals((long) parsed.get(0).getEntityId(), 100L);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.DATATAG);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
    assertEquals(parsed.get(0).getElementProperties(), expectedProps);

    EasyMock.verify(tagFacadeGateway);
  }

  @Test
  public void updateDataTagWithAllFields() {
    // setup Configuration:
    Properties expectedProps = new Properties();
    DataTag dataTag = buildUpdateDataTagWithAllFields(100L, expectedProps);

    List<Tag> tagUpdateList = Arrays.asList(dataTag);

    Configuration config = new Configuration(1L);
    config.setEntities(tagUpdateList);

    // setUp Mocks:
    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(true);

    EasyMock.replay(tagFacadeGateway);

    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals((long) parsed.get(0).getEntityId(), 100L);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.DATATAG);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
    assertEquals(parsed.get(0).getElementProperties(), expectedProps);

    EasyMock.verify(tagFacadeGateway);
  }

  @Test
  public void updateNonExistingDataTag() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Updating of DataTag (id = 20) failed: The object is unknown to the sever.");

    // setup Configuration:
    DataTag dataTag = DataTag.update(20L).description("The description").build();

    List<Tag> tagUpdateList = Arrays.asList(dataTag);

    Configuration config = new Configuration(1L);
    config.setEntities(tagUpdateList);

    // setUp Mocks:
    EasyMock.expect(tagFacadeGateway.isInTagCache(20L)).andReturn(false);

    // run test
    EasyMock.replay(tagFacadeGateway);
    parser.parse(config);
    EasyMock.verify(tagFacadeGateway);
  }

  @Test
  public void deleteDataTag() {
    // setup Configuration:
    DataTag dataTag = buildDeleteDataTag(20L);

    List<Tag> tagUpdateList = Arrays.asList(dataTag);

    Configuration config = new Configuration(1L);
    config.setEntities(tagUpdateList);

    // setUp Mocks:
    EasyMock.expect(tagFacadeGateway.isInTagCache(20L)).andReturn(true);

    EasyMock.replay(tagFacadeGateway);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.REMOVE);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.DATATAG);
    assertTrue(parsed.get(0).getElementProperties().isEmpty());

    EasyMock.verify(tagFacadeGateway);
  }

  @Test
  public void deleteNotExistingDataTag() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Deleting of DataTag (id = 20) failed: The object is unknown to the sever.");

    // setup Configuration:
    DataTag dataTag = new DataTag();
    dataTag.setId(20L);
    dataTag.setDeleted(true);

    List<Tag> tagUpdateList = Arrays.asList(dataTag);

    Configuration config = new Configuration(1L);
    config.setEntities(tagUpdateList);

    // setUp Mocks:
    EasyMock.expect(tagFacadeGateway.isInTagCache(20L)).andReturn(false);
    EasyMock.replay(tagFacadeGateway);

    parser.parse(config);

    EasyMock.verify(tagFacadeGateway);
  }
}
