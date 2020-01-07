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
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.Properties;
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
//import cern.c2mon.server.cache.DataTagCache;
//import cern.c2mon.server.cache.EquipmentCache;
//import cern.c2mon.server.cache.SubEquipmentCache;
//import cern.c2mon.server.cache.TagFacadeGateway;
//import cern.c2mon.server.cache.loading.SequenceDAO;
//import cern.c2mon.server.common.datatag.DataTagCacheObject;
//import cern.c2mon.server.configuration.parser.ConfigurationParser;
//import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
//import cern.c2mon.shared.client.configuration.ConfigConstants;
//import cern.c2mon.shared.client.configuration.ConfigurationElement;
//import cern.c2mon.shared.client.configuration.api.Configuration;
//import cern.c2mon.shared.client.configuration.api.tag.DataTag;
//import cern.c2mon.shared.client.configuration.api.tag.Tag;
//import cern.c2mon.shared.client.metadata.Metadata;
//import cern.c2mon.shared.client.tag.TagMode;
//import cern.c2mon.shared.common.datatag.DataTagAddress;
//import cern.c2mon.shared.common.datatag.address.impl.PLCHardwareAddressImpl;
//
//import static cern.c2mon.server.configuration.parser.util.ConfigurationDataTagUtil.buildCreateAllFieldsDataTag;
//import static cern.c2mon.server.configuration.parser.util.ConfigurationDataTagUtil.buildCreateBasicDataTag;
//import static cern.c2mon.server.configuration.parser.util.ConfigurationDataTagUtil.buildUpdateDataTagWithSomeFields;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
///**
// * @author Franz Ritter
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = ConfigurationParserTestMocks.class)
//public class ConfigureDataTagTest {
//
//  @Autowired
//  ConfigurationParser parser;
//
//  @Autowired
//  EquipmentCache equipmentCache;
//
//  @Autowired
//  SubEquipmentCache subEquipmentCache;
//
//  @Autowired
//  SequenceDAO sequenceDAO;
//
//  @Autowired
//  TagFacadeGateway tagFacadeGateway;
//
//  @Autowired
//  DataTagCache dataTagCache;
//
//  @Rule
//  public ExpectedException tagException = ExpectedException.none();
//
//
//  @Before
//  public void resetMocks() {
//    EasyMock.reset(equipmentCache, sequenceDAO, dataTagCache, subEquipmentCache, tagFacadeGateway);
//  }
//
//  @Test
//  public void createDataTag() {
//    // setup Configuration:
//
//    Properties expectedProps = new Properties();
//    DataTag tag = buildCreateBasicDataTag(expectedProps);
//
//    List<DataTag> dataTagList = Arrays.asList(tag);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(dataTagList);
//
//    // setUp Mocks:
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(100L);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(false);
//
//    EasyMock.replay(equipmentCache, sequenceDAO, tagFacadeGateway);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals(parsed.size(), 1);
//    assertEquals((long) parsed.get(0).getEntityId(), 100L);
//    assertTrue(parsed.get(0).getEntity().equals(ConfigConstants.Entity.DATATAG));
//    assertTrue(parsed.get(0).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(0).getElementProperties(), expectedProps);
//
//    EasyMock.verify(equipmentCache, sequenceDAO, tagFacadeGateway);
//  }
//
//  @Test
//  public void createDataTagWithAllFields() {
//    Properties expectedProps = new Properties();
//    DataTag tag = buildCreateAllFieldsDataTag(101L, expectedProps);
//
//    List<DataTag> dataTagList = Arrays.asList(tag);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(dataTagList);
//
//    // setUp Mocks:
//    EasyMock.expect(tagFacadeGateway.isInTagCache(101L)).andReturn(false);
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//
//    EasyMock.replay(equipmentCache, sequenceDAO, tagFacadeGateway);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals((long) parsed.get(0).getEntityId(), 101L);
//    assertEquals(parsed.get(0).getElementProperties(), expectedProps);
//    assertTrue(parsed.get(0).getEntity().equals(ConfigConstants.Entity.DATATAG));
//    assertTrue(parsed.get(0).getAction().equals(ConfigConstants.Action.CREATE));
//
//    EasyMock.verify(equipmentCache, sequenceDAO, tagFacadeGateway);
//  }
//
//  @Test
//  public void createMultipleDataTagWithAllFields() {
//    Properties expectedProps1 = new Properties();
//    Properties expectedProps2 = new Properties();
//    Properties expectedProps3 = new Properties();
//    Properties expectedProps4 = new Properties();
//    Properties expectedProps5 = new Properties();
//
//    DataTag tag1 = buildCreateAllFieldsDataTag(101L, expectedProps1);
//    DataTag tag2 = buildCreateAllFieldsDataTag(102L, expectedProps2);
//    DataTag tag3 = buildCreateAllFieldsDataTag(103L, expectedProps3);
//    DataTag tag4 = buildCreateAllFieldsDataTag(104L, expectedProps4);
//    DataTag tag5 = buildCreateAllFieldsDataTag(105L, expectedProps5);
//
//    List<DataTag> dataTagList = Arrays.asList(tag1, tag2, tag3, tag4, tag5);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(dataTagList);
//
//    // setUp Mocks:
//    EasyMock.expect(tagFacadeGateway.isInTagCache(101L)).andReturn(false);
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//    EasyMock.expect(dataTagCache.get("DataTag101")).andReturn(null);
//
//    EasyMock.expect(tagFacadeGateway.isInTagCache(102L)).andReturn(false);
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//    EasyMock.expect(dataTagCache.get("DataTag102")).andReturn(null);
//
//    EasyMock.expect(tagFacadeGateway.isInTagCache(103L)).andReturn(false);
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//    EasyMock.expect(dataTagCache.get("DataTag103")).andReturn(null);
//
//    EasyMock.expect(tagFacadeGateway.isInTagCache(104L)).andReturn(false);
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//    EasyMock.expect(dataTagCache.get("DataTag104")).andReturn(null);
//
//    EasyMock.expect(tagFacadeGateway.isInTagCache(105L)).andReturn(false);
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//    EasyMock.expect(dataTagCache.get("DataTag105")).andReturn(null);
//
//    EasyMock.replay(equipmentCache, sequenceDAO, tagFacadeGateway, dataTagCache);
//
//    // run the parsing
//    List<ConfigurationElement> elements = parser.parse(config);
//
//    // Assert stuff
//    assertTrue(elements.size() == 5);
//
//    assertEquals(elements.get(0).getElementProperties(), expectedProps1);
//    assertTrue(elements.get(0).getEntityId().equals(101L));
//    assertTrue(elements.get(0).getEntity().equals(ConfigConstants.Entity.DATATAG));
//    assertTrue(elements.get(0).getAction().equals(ConfigConstants.Action.CREATE));
//
//    assertEquals(elements.get(1).getElementProperties(), expectedProps2);
//    assertTrue(elements.get(1).getEntityId().equals(102L));
//    assertTrue(elements.get(1).getEntity().equals(ConfigConstants.Entity.DATATAG));
//    assertTrue(elements.get(1).getAction().equals(ConfigConstants.Action.CREATE));
//
//    assertEquals(elements.get(2).getElementProperties(), expectedProps3);
//    assertTrue(elements.get(2).getEntityId().equals(103L));
//    assertTrue(elements.get(2).getEntity().equals(ConfigConstants.Entity.DATATAG));
//    assertTrue(elements.get(2).getAction().equals(ConfigConstants.Action.CREATE));
//
//    assertEquals(elements.get(3).getElementProperties(), expectedProps4);
//    assertTrue(elements.get(3).getEntityId().equals(104L));
//    assertTrue(elements.get(3).getEntity().equals(ConfigConstants.Entity.DATATAG));
//    assertTrue(elements.get(3).getAction().equals(ConfigConstants.Action.CREATE));
//
//    assertEquals(elements.get(4).getElementProperties(), expectedProps5);
//    assertTrue(elements.get(4).getEntityId().equals(105L));
//    assertTrue(elements.get(4).getEntity().equals(ConfigConstants.Entity.DATATAG));
//    assertTrue(elements.get(4).getAction().equals(ConfigConstants.Action.CREATE));
//
//    EasyMock.verify(equipmentCache, sequenceDAO, tagFacadeGateway);
//  }
//
//  @Test
//  public void createDataTagWithNonExistentEquipment() {
//    // Setup Exception
//    tagException.expect(ConfigurationParseException.class);
//
//    DataTag tag = DataTag.create("myDataTag", Integer.class, new DataTagAddress()).id(21L).build();
//    tag.setEquipmentId(10L);
//
//    List<DataTag> dataTagList = Arrays.asList(tag);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(dataTagList);
//
//    // setUp Mocks:
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(false);
//
//    // run test
//    EasyMock.replay(equipmentCache);
//    parser.parse(config);
//    EasyMock.verify(equipmentCache);
//  }
//
//  @Test
//  public void createDataTagMultiMetadata() {
//    DataTag dataTag = DataTag.create("DataTag", Integer.class, new DataTagAddress())
//        .addMetadata("testMetadata1", 66)
//        .addMetadata("testMetadata1", 11)
//        .addMetadata("testMetadata2", 22)
//        .build();
//    dataTag.setEquipmentId(10L);
//
//    List<Tag> tagUpdateList = Arrays.asList(dataTag);
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagUpdateList);
//
//    Properties expectedProps = new Properties();
//    expectedProps.setProperty("name", "DataTag");
//    expectedProps.setProperty("description", "<no description provided>");
//    expectedProps.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
//    expectedProps.setProperty("dataType", Integer.class.getName());
//    expectedProps.setProperty("isLogged", String.valueOf(true));
//    expectedProps.setProperty("equipmentId", String.valueOf(10L));
//    expectedProps.setProperty("address", new DataTagAddress().toConfigXML());
//    Metadata metadata = new Metadata();
//    metadata.addMetadata("testMetadata1", 11);
//    metadata.addMetadata("testMetadata2", 22);
//    expectedProps.setProperty("metadata", Metadata.toJSON(metadata));
//
//    // setUp Mocks:
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(100L);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(false);
//    EasyMock.replay(equipmentCache, sequenceDAO, tagFacadeGateway);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals(parsed.size(), 1);
//    assertEquals((long) parsed.get(0).getEntityId(), 100L);
//    assertTrue(parsed.get(0).getEntity().equals(ConfigConstants.Entity.DATATAG));
//    assertTrue(parsed.get(0).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(expectedProps.getProperty("metadata"), parsed.get(0).getElementProperties().getProperty("metadata"));
//
//    EasyMock.verify(equipmentCache, sequenceDAO, tagFacadeGateway);
//  }
//
//  @Test
//  public void createDataTagSingleMetadata() {
//    DataTag dataTag = DataTag.create("DataTag Name", Integer.class, new DataTagAddress())
//        .addMetadata("testMetadata", 11)
//        .build();
//    dataTag.setEquipmentId(10L);
//
//    List<Tag> tagUpdateList = Arrays.asList(dataTag);
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagUpdateList);
//
//    Properties expectedProps = new Properties();
//    expectedProps.setProperty("name", "DataTag Name");
//    expectedProps.setProperty("description", "<no description provided>");
//    expectedProps.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
//    expectedProps.setProperty("dataType", Integer.class.getName());
//    expectedProps.setProperty("isLogged", String.valueOf(true));
//    expectedProps.setProperty("equipmentId", String.valueOf(10l));
//    expectedProps.setProperty("address", new DataTagAddress().toConfigXML());
//    Metadata metadata = new Metadata();
//    metadata.addMetadata("testMetadata", 11);
//    expectedProps.setProperty("metadata", Metadata.toJSON(metadata));
//
//    // setUp Mocks:
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(100L);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(false);
//    EasyMock.replay(equipmentCache, sequenceDAO, tagFacadeGateway);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals(parsed.size(), 1);
//    assertEquals((long) parsed.get(0).getEntityId(), 100L);
//    assertEquals(ConfigConstants.Entity.DATATAG, parsed.get(0).getEntity());
//    assertEquals(ConfigConstants.Action.CREATE, parsed.get(0).getAction());
//    assertEquals(expectedProps.getProperty("metadata"), parsed.get(0).getElementProperties().getProperty("metadata"));
//
//    EasyMock.verify(equipmentCache, sequenceDAO, tagFacadeGateway);
//  }
//
//  @Test
//  public void createExistingDataTag() {
//    // Setup Exception
//    tagException.expect(ConfigurationParseException.class);
//
//    DataTag tag = DataTag.create("myDataTag", Integer.class, new DataTagAddress()).id(21L).build();
//    tag.setEquipmentId(10L);
//
//    List<DataTag> dataTagList = Arrays.asList(tag);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(dataTagList);
//
//    // setUp Mocks:
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(21L)).andReturn(true);
//
//    // run test
//    EasyMock.replay(equipmentCache, tagFacadeGateway);
//    parser.parse(config);
//    EasyMock.verify(equipmentCache, tagFacadeGateway);
//  }
//
//  @Test
//  public void updateDataTagWithName() {
//    // setup Configuration:
//    Properties expectedProps = new Properties();
//    DataTag dataTag = buildUpdateDataTagWithSomeFields("myDataTag", expectedProps);
//
//    List<Tag> tagUpdateList = Arrays.asList(dataTag);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(dataTagCache.get("myDataTag")).andReturn(new DataTagCacheObject(20L)).times(2);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(20L)).andReturn(true);
//
//    EasyMock.replay(dataTagCache, tagFacadeGateway);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals((long) parsed.get(0).getEntityId(), 20L);
//    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.DATATAG);
//    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
//    assertEquals(parsed.get(0).getElementProperties(), expectedProps);
//
//    EasyMock.verify(dataTagCache, tagFacadeGateway);
//  }
//
//  @Test
//  public void updateDataTagWithId() {
//    // setup Configuration:
//    Properties expectedProps = new Properties();
//    DataTag dataTag = buildUpdateDataTagWithSomeFields(100L, expectedProps);
//
//    List<Tag> tagUpdateList = Arrays.asList(dataTag);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(true);
//
//    EasyMock.replay(tagFacadeGateway);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals((long) parsed.get(0).getEntityId(), 100L);
//    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.DATATAG);
//    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
//    assertEquals(parsed.get(0).getElementProperties(), expectedProps);
//
//    EasyMock.verify(tagFacadeGateway);
//  }
//
//  @Test
//  public void updateDataTagWithAllFields() {
//    DataTag dataTag = DataTag.update(100L)
//        .unit("updateUnit")
//        .name("updateName")
//        .description("foo_Update")
//        .mode(TagMode.OPERATIONAL)
//        .dataType(Double.class)
//        .isLogged(true)
//        .minValue(1)
//        .maxValue(11)
//        .address(new DataTagAddress(new PLCHardwareAddressImpl(2, 2, 2, 2, 2, 2.0f, "testAddress_Update")))
//        .updateMetadata("testMetadata_Update", true)
//        .build();
//
//    List<Tag> tagUpdateList = Arrays.asList(dataTag);
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagUpdateList);
//
//    Properties expectedProps = new Properties();
//    expectedProps.setProperty("name", "updateName");
//    expectedProps.setProperty("unit", "updateUnit");
//    expectedProps.setProperty("description", "foo_Update");
//    expectedProps.setProperty("mode", String.valueOf(TagMode.OPERATIONAL.ordinal()));
//    expectedProps.setProperty("dataType", Double.class.getName());
//    expectedProps.setProperty("isLogged", String.valueOf(true));
//    expectedProps.setProperty("minValue", String.valueOf(1));
//    expectedProps.setProperty("maxValue", String.valueOf(11));
//    expectedProps.setProperty("address", new DataTagAddress(new PLCHardwareAddressImpl(2, 2, 2, 2, 2, 2.0f, "testAddress_Update")).toConfigXML());
//    Metadata metadata = new Metadata();
//    metadata.addMetadata("testMetadata_Update", true);
//    metadata.setUpdate(true);
//    expectedProps.setProperty("metadata", Metadata.toJSON(metadata));
//
//    // setUp Mocks:
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(true);
//
//    EasyMock.replay(tagFacadeGateway);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals(100L, (long) parsed.get(0).getEntityId());
//    assertEquals(ConfigConstants.Entity.DATATAG, parsed.get(0).getEntity());
//    assertEquals(ConfigConstants.Action.UPDATE, parsed.get(0).getAction());
//    assertEquals(expectedProps, parsed.get(0).getElementProperties());
//
//    EasyMock.verify(tagFacadeGateway);
//  }
//
//  @Test
//  public void updateDataTagSingleMetadata() {
//    DataTag dataTag = DataTag.update(100L)
//        .updateMetadata("testMetadata", 11)
//        .build();
//    dataTag.setEquipmentId(10L);
//
//    List<Tag> tagUpdateList = Arrays.asList(dataTag);
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagUpdateList);
//
//    Properties expectedProps = new Properties();
//    expectedProps.setProperty("equipmentId", String.valueOf(10l));
//    Metadata metadata = new Metadata();
//    metadata.addMetadata("testMetadata", 11);
//    metadata.setUpdate(true);
//    expectedProps.setProperty("metadata", Metadata.toJSON(metadata));
//
//    // setUp Mocks:
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(true);
//    EasyMock.replay(tagFacadeGateway);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals(100L, (long) parsed.get(0).getEntityId());
//    assertEquals(ConfigConstants.Entity.DATATAG, parsed.get(0).getEntity());
//    assertEquals(ConfigConstants.Action.UPDATE, parsed.get(0).getAction());
//    assertEquals(expectedProps.getProperty("metadata"), parsed.get(0).getElementProperties().getProperty("metadata"));
//
//    EasyMock.verify(tagFacadeGateway);
//  }
//
//  @Test
//  public void updateDataTagMultiMetadata() {
//    DataTag dataTag = DataTag.update(100L)
//        .updateMetadata("testMetadata1", 33)
//        .updateMetadata("testMetadata2", 22)
//        .updateMetadata("testMetadata1", 11)
//        .build();
//    List<Tag> tagUpdateList = Arrays.asList(dataTag);
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagUpdateList);
//
//    // setup Configuration:
//    Properties expectedProps = new Properties();
//    Metadata metadata = new Metadata();
//    metadata.addMetadata("testMetadata1", 11);
//    metadata.addMetadata("testMetadata2", 22);
//    metadata.setUpdate(true);
//    expectedProps.setProperty("metadata", Metadata.toJSON(metadata));
//
//    // setUp Mocks:
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(true);
//    EasyMock.replay(tagFacadeGateway);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals(100L, (long) parsed.get(0).getEntityId());
//    assertEquals(ConfigConstants.Entity.DATATAG, parsed.get(0).getEntity());
//    assertEquals(ConfigConstants.Action.UPDATE, parsed.get(0).getAction());
//    assertEquals(expectedProps.getProperty("metadata"), parsed.get(0).getElementProperties().getProperty("metadata"));
//
//    EasyMock.verify(tagFacadeGateway);
//  }
//
//  @Test
//  public void removeDataTagSingleMetadata() {
//    DataTag dataTag = DataTag.update(100L)
//        .removeMetadata("testMetadata")
//        .build();
//
//    List<Tag> tagUpdateList = Collections.singletonList(dataTag);
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagUpdateList);
//
//    Properties expectedProps = new Properties();
//    Metadata metadata = new Metadata();
//    metadata.setRemoveList(Collections.singletonList("testMetadata"));
//    metadata.setUpdate(true);
//    expectedProps.setProperty("metadata", Metadata.toJSON(metadata));
//
//    // setUp Mocks:
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(true);
//    EasyMock.replay(tagFacadeGateway);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals(parsed.size(), 1);
//    assertEquals(100L, (long) parsed.get(0).getEntityId());
//    assertEquals(ConfigConstants.Entity.DATATAG, parsed.get(0).getEntity());
//    assertEquals(ConfigConstants.Action.UPDATE, parsed.get(0).getAction());
//    assertEquals(expectedProps.getProperty("metadata"), parsed.get(0).getElementProperties().getProperty("metadata"));
//
//    EasyMock.verify(tagFacadeGateway);
//  }
//
//  @Test
//  public void removeDataTagMultiMetadata() {
//    DataTag dataTag = DataTag.update(100L)
//        .removeMetadata("testMetadata1")
//        .removeMetadata("testMetadata2")
//        .build();
//
//    List<Tag> tagUpdateList = Collections.singletonList(dataTag);
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagUpdateList);
//
//    Properties expectedProps = new Properties();
//    Metadata metadata = new Metadata();
//    metadata.setRemoveList(Arrays.asList("testMetadata1", "testMetadata2"));
//    metadata.setUpdate(true);
//    expectedProps.setProperty("metadata", Metadata.toJSON(metadata));
//
//    // setUp Mocks:
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(true);
//    EasyMock.replay(tagFacadeGateway);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals(parsed.size(), 1);
//    assertEquals((long) parsed.get(0).getEntityId(), 100L);
//    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.DATATAG);
//    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
//    assertEquals(expectedProps.getProperty("metadata"), parsed.get(0).getElementProperties().getProperty("metadata"));
//
//    EasyMock.verify(tagFacadeGateway);
//  }
//
//  @Test
//  public void updateNonExistentDataTag() {
//    // setup Configuration:
//    DataTag dataTag = DataTag.update(20L).description("The description").build();
//
//    List<Tag> tagUpdateList = Collections.singletonList(dataTag);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(tagFacadeGateway.isInTagCache(20L)).andReturn(false);
//
//    // run test
//    EasyMock.replay(tagFacadeGateway);
//
//    List<ConfigurationElement> result = parser.parse(config);
//
//    assertEquals(1, result.size());
//    assertEquals(ConfigConstants.Entity.MISSING, result.get(0).getEntity());
//    assertEquals(ConfigConstants.Status.WARNING, result.get(0).getStatus());
//
//    EasyMock.verify(tagFacadeGateway);
//  }
//
//  @Test
//  public void deleteDataTag() {
//    DataTag dataTag = new DataTag();
//    dataTag.setId(20L);
//    dataTag.setDeleted(true);
//
//    List<Tag> tagUpdateList = Arrays.asList(dataTag);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(tagFacadeGateway.isInTagCache(20L)).andReturn(true);
//
//    EasyMock.replay(tagFacadeGateway);
//
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals(parsed.size(), 1);
//    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.REMOVE);
//    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.DATATAG);
//    assertTrue(parsed.get(0).getElementProperties().isEmpty());
//
//    EasyMock.verify(tagFacadeGateway);
//  }
//
//  @Test
//  public void deleteNonExistentDataTag() {
//    // setup Configuration:
//    DataTag dataTag = new DataTag();
//    dataTag.setId(20L);
//    dataTag.setDeleted(true);
//
//    List<Tag> tagUpdateList = Arrays.asList(dataTag);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(tagUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(tagFacadeGateway.isInTagCache(20L)).andReturn(false);
//    EasyMock.replay(tagFacadeGateway);
//
//    assertEquals(0, parser.parse(config).size());
//
//    EasyMock.verify(tagFacadeGateway);
//  }
//}
