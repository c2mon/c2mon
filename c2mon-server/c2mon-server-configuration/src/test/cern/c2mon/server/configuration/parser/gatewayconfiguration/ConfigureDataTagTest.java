/*
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
 */

package cern.c2mon.server.configuration.parser.gatewayconfiguration;

import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.configuration.parser.ConfigurationParser;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.server.configuration.parser.util.ConfigurationUtil;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Franz Ritter
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/configuration/config/server-configuration-parser-test.xml"})
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
  DataTagCache dataTagCache;

  @Rule
  public ExpectedException tagException = ExpectedException.none();


  @Before
  public void resetMocks() {
    EasyMock.reset(equipmentCache, sequenceDAO, dataTagCache, subEquipmentCache);
  }

  @Test
  public void createDataTag() {
    // setup Configuration:

    DataTag tag = DataTag.create("myDataTag", Integer.class, new DataTagAddress()).build();
    tag.setParentId(10L);

    List<DataTag> dataTagList = new ArrayList<>();
    dataTagList.add(tag);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(dataTagList);

    // setUp Mocks:
    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(20L);
    EasyMock.expect(dataTagCache.hasKey(20L)).andReturn(false);

    EasyMock.replay(equipmentCache, sequenceDAO, dataTagCache);

    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals((long) parsed.get(0).getEntityId(), 20L);
    assertEquals(parsed.get(0).getElementProperties().size(), 7);
    assertEquals(parsed.get(0).getElementProperties().get("dataType"), Integer.class.getName());
    assertEquals(parsed.get(0).getElementProperties().get("isLogged"), "true");
    assertEquals(parsed.get(0).getElementProperties().get("equipmentId"), "10");
    assertEquals(parsed.get(0).getElementProperties().get("name"), "myDataTag");
    assertEquals(parsed.get(0).getElementProperties().get("description"), "No description specified");
    assertEquals(parsed.get(0).getElementProperties().get("address"), new DataTagAddress().toConfigXML());
    assertEquals(parsed.get(0).getElementProperties().get("mode"), "1");

    EasyMock.verify(equipmentCache, sequenceDAO, dataTagCache);
  }

  @Test
  public void createDataTagWithId() {
    DataTag tag = DataTag.create("myDataTag", Integer.class, new DataTagAddress()).id(21L).build();
    tag.setParentId(10L);

    List<DataTag> dataTagList = new ArrayList<>();
    dataTagList.add(tag);


    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(dataTagList);

    // setUp Mocks:
    EasyMock.expect(dataTagCache.hasKey(21L)).andReturn(false);
    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);

    EasyMock.replay(equipmentCache, sequenceDAO, dataTagCache);

    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals((long) parsed.get(0).getEntityId(), 21L);
    assertEquals(parsed.get(0).getElementProperties().size(), 7);
    assertEquals(parsed.get(0).getElementProperties().get("dataType"), Integer.class.getName());
    assertEquals(parsed.get(0).getElementProperties().get("isLogged"), "true");
    assertEquals(parsed.get(0).getElementProperties().get("equipmentId"), "10");
    assertEquals(parsed.get(0).getElementProperties().get("name"), "myDataTag");
    assertEquals(parsed.get(0).getElementProperties().get("description"), "No description specified");
    assertEquals(parsed.get(0).getElementProperties().get("address"), new DataTagAddress().toConfigXML());
    assertEquals(parsed.get(0).getElementProperties().get("mode"), "1");

    EasyMock.verify(equipmentCache, sequenceDAO, dataTagCache);
  }

  @Test
  public void createDataTagWithNotExistingEquipment() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Creating of a new DataTag (id = 21) failed: No Equipment or SubEquipment with the id 10 found");

    DataTag tag = DataTag.create("myDataTag", Integer.class, new DataTagAddress()).id(21L).build();
    tag.setParentId(10L);

    List<DataTag> dataTagList = new ArrayList<>();
    dataTagList.add(tag);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(dataTagList);

    // setUp Mocks:
    EasyMock.expect(dataTagCache.hasKey(21L)).andReturn(false);
    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(false);
    EasyMock.expect(subEquipmentCache.hasKey(10L)).andReturn(false);

    // run test
    EasyMock.replay(equipmentCache, sequenceDAO, dataTagCache, subEquipmentCache);
    parser.parse(config);
    EasyMock.verify(equipmentCache, sequenceDAO, dataTagCache, subEquipmentCache);
  }

  @Test
  public void createExistingDataTag() {
    // Setup Exception
    tagException.expect(IllegalArgumentException.class);
    tagException.expectMessage("Error while parsing a 'create' Configuration: Id 21 of the class DataTag already known to the server");

    DataTag tag = DataTag.create("myDataTag", Integer.class, new DataTagAddress()).id(21L).build();
    tag.setParentId(10L);

    List<DataTag> dataTagList = new ArrayList<>();
    dataTagList.add(tag);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(dataTagList);

    // setUp Mocks:
    EasyMock.expect(dataTagCache.hasKey(21L)).andReturn(true);

    // run test
    EasyMock.replay(equipmentCache, sequenceDAO, dataTagCache);
    parser.parse(config);
    EasyMock.verify(equipmentCache, sequenceDAO, dataTagCache);
  }

  @Test
  public void updateDataTagWithName() {
    // setup Configuration:
    DataTag dataTag = DataTag.update("myDataTag").description("The description").build();

    List<Tag> tagUpdateList = new ArrayList<>();
    tagUpdateList.add(dataTag);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(tagUpdateList);

    // setUp Mocks:
    EasyMock.expect(dataTagCache.get("myDataTag")).andReturn(new DataTagCacheObject(20L));
    EasyMock.expect(dataTagCache.hasKey(20L)).andReturn(true);

    EasyMock.replay(equipmentCache, sequenceDAO, dataTagCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals((long) parsed.get(0).getEntityId(), 20L);
    assertEquals(parsed.get(0).getElementProperties().get("description"), "The description");

    EasyMock.verify(equipmentCache, sequenceDAO, dataTagCache);
  }

  @Test
  public void updateDataTagWithId() {
    // setup Configuration:
    DataTag dataTag = DataTag.update(20L).description("The description").build();

    List<Tag> tagUpdateList = new ArrayList<>();
    tagUpdateList.add(dataTag);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(tagUpdateList);

    // setUp Mocks:
    EasyMock.expect(dataTagCache.hasKey(20L)).andReturn(true);

    EasyMock.replay(equipmentCache, sequenceDAO, dataTagCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals((long) parsed.get(0).getEntityId(), 20L);
    assertEquals(parsed.get(0).getElementProperties().get("description"), "The description");

    EasyMock.verify(equipmentCache, sequenceDAO, dataTagCache);
  }

  @Test
  public void updateNonExistingDataTag() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Updating of DataTag (id = 20) failed: The object is unknown to the sever.");

    // setup Configuration:
    DataTag dataTag = DataTag.update(20L).description("The description").build();

    List<Tag> tagUpdateList = new ArrayList<>();
    tagUpdateList.add(dataTag);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(tagUpdateList);

    // setUp Mocks:
    EasyMock.expect(dataTagCache.hasKey(20L)).andReturn(false);

    // run test
    EasyMock.replay(equipmentCache, sequenceDAO, dataTagCache);
    parser.parse(config);
    EasyMock.verify(equipmentCache, sequenceDAO, dataTagCache);
  }

  @Test
  public void deleteDataTag() {
    // setup Configuration:
    DataTag dataTag = DataTag.builder().id(20L).deleted(true).build();

    List<Tag> tagUpdateList = new ArrayList<>();
    tagUpdateList.add(dataTag);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(tagUpdateList);

    // setUp Mocks:
    EasyMock.expect(dataTagCache.hasKey(20L)).andReturn(true);

    EasyMock.replay(equipmentCache, sequenceDAO, dataTagCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.REMOVE);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.DATATAG);
    assertTrue(parsed.get(0).getElementProperties().isEmpty());

    EasyMock.verify(equipmentCache, sequenceDAO, dataTagCache);
  }

  @Test
  public void deleteNotExistingDataTag() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Deleting of DataTag (id = 20) failed: The object is unknown to the sever.");

    // setup Configuration:
    DataTag dataTag = DataTag.builder().id(20L).deleted(true).build();

    List<Tag> tagUpdateList = new ArrayList<>();
    tagUpdateList.add(dataTag);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(tagUpdateList);

    // setUp Mocks:
    EasyMock.expect(dataTagCache.hasKey(20L)).andReturn(false);
    EasyMock.replay(equipmentCache, sequenceDAO, dataTagCache);

    parser.parse(config);


    EasyMock.verify(equipmentCache, sequenceDAO, dataTagCache);
  }

}
