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
//import cern.c2mon.server.cache.*;
//import cern.c2mon.server.cache.loading.EquipmentDAO;
//import cern.c2mon.server.cache.loading.SequenceDAO;
//import cern.c2mon.server.configuration.parser.ConfigurationParser;
//import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
//import cern.c2mon.shared.client.configuration.ConfigConstants;
//import cern.c2mon.shared.client.configuration.ConfigurationElement;
//import cern.c2mon.shared.client.configuration.api.Configuration;
//import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
//
//import static cern.c2mon.server.configuration.parser.util.ConfigurationEquipmentUtil.*;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
///**
// * @author Franz Ritter
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = ConfigurationParserTestMocks.class)
//public class ConfigureEquipmentTest {
//
//  @Autowired
//  ConfigurationParser parser;
//
//  @Autowired
//  ProcessCache processCache;
//
//  @Autowired
//  SequenceDAO sequenceDAO;
//
//  @Autowired
//  EquipmentCache equipmentCache;
//
//  @Autowired
//  EquipmentDAO equipmentDAO;
//
//  @Autowired
//  ControlTagCache controlTagCache;
//
//  @Autowired
//  CommFaultTagCache commFaultTagCache;
//
//  @Autowired
//  AliveTimerCache aliveTagCache;
//
//  @Autowired
//  TagFacadeGateway tagFacadeGateway;
//
//  @Rule
//  public ExpectedException tagException = ExpectedException.none();
//
//
//  @Before
//  public void resetMocks() {
//    EasyMock.reset(tagFacadeGateway, equipmentDAO, processCache, sequenceDAO, equipmentCache, controlTagCache, commFaultTagCache, aliveTagCache);
//  }
//
//  @Test
//  public void createEquipment() {
//    // setup Configuration:
//    Properties expectedProps = new Properties();
//    Equipment equipment = buildCreateBasicEquipment(expectedProps);
//
//    List<Equipment> processList = Arrays.asList(equipment);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(processList);
//
//    // setUp Mocks:
//    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
//    EasyMock.expect(sequenceDAO.getNextEquipmentId()).andReturn(10L);
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(false);
//    EasyMock.expect(controlTagCache.get("E_TEST:COMM_FAULT")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(100L);
//    EasyMock.expect(controlTagCache.get("E_TEST:STATUS")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(101L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(101L);
//
//    EasyMock.replay(equipmentCache, sequenceDAO, tagFacadeGateway, processCache, controlTagCache);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals(parsed.size(), 3);
//
//    assertEquals((long) parsed.get(0).getEntityId(), 100L);
//    assertTrue(parsed.get(0).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(0).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(0).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(1).getEntityId(), 101L);
//    assertTrue(parsed.get(1).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(1).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(1).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(2).getEntityId(), 10L);
//    assertTrue(parsed.get(2).getEntity().equals(ConfigConstants.Entity.EQUIPMENT));
//    assertTrue(parsed.get(2).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(2).getElementProperties(), expectedProps);
//
//    EasyMock.verify(equipmentCache, sequenceDAO, tagFacadeGateway, processCache, controlTagCache);
//  }
//
//  @Test
//  public void createEquipmentWithAllFields() {
//    // setup Configuration:
//    Properties expectedProps = new Properties();
//    Equipment equipment = buildCreateAllFieldsEquipment(10L, expectedProps);
//
//    List<Equipment> processList = Arrays.asList(equipment);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(processList);
//
//    // setUp Mocks:
//    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(false);
//    EasyMock.expect(controlTagCache.get("E_TEST10:COMM_FAULT")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(102L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(102L);
//    EasyMock.expect(controlTagCache.get("E_TEST10:STATUS")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(101L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(101L);
//    EasyMock.expect(controlTagCache.get("E_TEST10:ALIVE")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(100L);
//
//    EasyMock.replay(equipmentCache, sequenceDAO, processCache, tagFacadeGateway, controlTagCache);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals(parsed.size(), 4);
//
//    assertEquals((long) parsed.get(0).getEntityId(), 102L);
//    assertTrue(parsed.get(0).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(0).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(0).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(1).getEntityId(), 101L);
//    assertTrue(parsed.get(1).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(1).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(1).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(2).getEntityId(), 100L);
//    assertTrue(parsed.get(2).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(2).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(2).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(3).getEntityId(), 10L);
//    assertTrue(parsed.get(3).getEntity().equals(ConfigConstants.Entity.EQUIPMENT));
//    assertTrue(parsed.get(3).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(3).getElementProperties(), expectedProps);
//
//    EasyMock.verify(equipmentCache, sequenceDAO,  processCache, tagFacadeGateway, controlTagCache);
//  }
//
//  @Test
//  public void createMultipleEquipmentWithAllFields() {
//    Properties expectedProps1 = new Properties();
//    Properties expectedProps2 = new Properties();
//    Properties expectedProps3 = new Properties();
//
//    Equipment equipment1 = buildCreateAllFieldsEquipment(10L, expectedProps1);
//    Equipment equipment2 = buildCreateAllFieldsEquipment(11L, expectedProps2);
//    Equipment equipment3 = buildCreateAllFieldsEquipment(12L, expectedProps3);
//
//    List<Equipment> equipmentList = Arrays.asList(equipment1, equipment2, equipment3);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(equipmentList);
//
//    // setUp Mocks:
//    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
//    EasyMock.expect(equipmentDAO.getIdByName("E_TEST10")).andReturn(null);
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(false);
//    EasyMock.expect(controlTagCache.get("E_TEST10:COMM_FAULT")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(102L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(102L);
//    EasyMock.expect(controlTagCache.get("E_TEST10:STATUS")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(101L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(101L);
//    EasyMock.expect(controlTagCache.get("E_TEST10:ALIVE")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(100L);
//
//    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
//    EasyMock.expect(equipmentDAO.getIdByName("E_TEST11")).andReturn(null);
//    EasyMock.expect(equipmentCache.hasKey(11L)).andReturn(false);
//    EasyMock.expect(controlTagCache.get("E_TEST11:COMM_FAULT")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(102L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(102L);
//    EasyMock.expect(controlTagCache.get("E_TEST11:STATUS")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(101L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(101L);
//    EasyMock.expect(controlTagCache.get("E_TEST11:ALIVE")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(100L);
//
//    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
//    EasyMock.expect(equipmentDAO.getIdByName("E_TEST12")).andReturn(null);
//    EasyMock.expect(equipmentCache.hasKey(12L)).andReturn(false);
//    EasyMock.expect(controlTagCache.get("E_TEST12:COMM_FAULT")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(102L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(102L);
//    EasyMock.expect(controlTagCache.get("E_TEST12:STATUS")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(101L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(101L);
//    EasyMock.expect(controlTagCache.get("E_TEST12:ALIVE")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(100L);
//
//    EasyMock.replay(equipmentCache, sequenceDAO, processCache, tagFacadeGateway, equipmentDAO, controlTagCache);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals(parsed.size(), 12);
//
//    assertEquals((long) parsed.get(0).getEntityId(), 102L);
//    assertTrue(parsed.get(0).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(0).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(0).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(1).getEntityId(), 101L);
//    assertTrue(parsed.get(1).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(1).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(1).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(2).getEntityId(), 100L);
//    assertTrue(parsed.get(2).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(2).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(2).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(3).getEntityId(), 10L);
//    assertTrue(parsed.get(3).getEntity().equals(ConfigConstants.Entity.EQUIPMENT));
//    assertTrue(parsed.get(3).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(3).getElementProperties(), expectedProps1);
//
//    assertEquals((long) parsed.get(4).getEntityId(), 102L);
//    assertTrue(parsed.get(4).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(4).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(4).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(5).getEntityId(), 101L);
//    assertTrue(parsed.get(5).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(5).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(5).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(6).getEntityId(), 100L);
//    assertTrue(parsed.get(6).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(6).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(6).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(7).getEntityId(), 11L);
//    assertTrue(parsed.get(7).getEntity().equals(ConfigConstants.Entity.EQUIPMENT));
//    assertTrue(parsed.get(7).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(7).getElementProperties(), expectedProps2);
//
//    assertEquals((long) parsed.get(8).getEntityId(), 102L);
//    assertTrue(parsed.get(8).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(8).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(8).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(9).getEntityId(), 101L);
//    assertTrue(parsed.get(9).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(9).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(9).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(10).getEntityId(), 100L);
//    assertTrue(parsed.get(10).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(10).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(10).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(11).getEntityId(), 12L);
//    assertTrue(parsed.get(11).getEntity().equals(ConfigConstants.Entity.EQUIPMENT));
//    assertTrue(parsed.get(11).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(11).getElementProperties(), expectedProps3);
//
//    EasyMock.verify(equipmentCache, sequenceDAO, processCache, tagFacadeGateway, equipmentDAO, controlTagCache);
//  }
//
//  @Test
//  public void createEquipmentWithNoFields() {
//    // Setup Exception
//    tagException.expect(IllegalArgumentException.class);
//    tagException.expectMessage("Equipment name is required!");
//
//    // Setup Configuration Instance
//    Equipment equipment = Equipment.create(null, null).build();
//    equipment.setProcessId(1L);
//
//    List<Equipment> equipmentListList = Arrays.asList(equipment);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(equipmentListList);
//
//    // Setup Mock
//    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
//    EasyMock.expect(sequenceDAO.getNextEquipmentId()).andReturn(10L);
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(false);
//
//    // Switch to replay mode
//    EasyMock.replay(processCache, sequenceDAO, equipmentCache);
//
//    // Run the code to be tested
//    parser.parse(config);
//
//    // Verify mock methods were called
//    EasyMock.verify(processCache, sequenceDAO, equipmentCache);
//  }
//
//  @Test
//  public void createEquipmentWithNonExistentProcess() {
//    // Setup Exception
//    tagException.expect(ConfigurationParseException.class);
//
//    Equipment equipment = Equipment.create("myEquipment", "handlerClass").id(10L).build();
//    equipment.setProcessId(1L);
//
//    List<Equipment> equipmentList = Arrays.asList(equipment);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(equipmentList);
//
//    // setUp Mocks:
//
//    EasyMock.expect(processCache.hasKey(1L)).andReturn(false);
//
//    // run test
//    EasyMock.replay(processCache);
//    parser.parse(config);
//    EasyMock.verify(processCache);
//  }
//
//  @Test
//  public void createExistingEquipment() {
//    // Setup Exception
//    tagException.expect(ConfigurationParseException.class);
//
//    Equipment equipment = Equipment.create("E_TEST", "hanlderClass").id(10L).build();
//    equipment.setProcessId(1L);
//
//    List<Equipment> equipmentList = Arrays.asList(equipment);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(equipmentList);
//
//    // setUp Mocks:
//    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//
//    // run test
//    EasyMock.replay(processCache, equipmentCache);
//    parser.parse(config);
//    EasyMock.verify(processCache, equipmentCache);
//  }
//
//  @Test
//  public void updateEquipmentWithName() {
//    // setup Configuration:
//    Properties expectedProps = new Properties();
//    Equipment equipment = buildUpdateEquipmentWithSomeFields("myEquipment", expectedProps);
//
//    List<Equipment> equipmentUpdateList = Arrays.asList(equipment);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(equipmentUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(equipmentDAO.getIdByName("myEquipment")).andReturn(10L);
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//
//    EasyMock.replay(equipmentDAO, equipmentCache);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals((long) parsed.get(0).getEntityId(), 10L);
//    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.EQUIPMENT);
//    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
//    assertEquals(parsed.get(0).getElementProperties(), expectedProps);
//
//    EasyMock.verify(equipmentDAO, equipmentCache);
//  }
//
//  @Test
//  public void updateEquipmentWithId() {
//    // setup Configuration:
//    Properties expectedProps = new Properties();
//    Equipment equipment = buildUpdateEquipmentWithSomeFields(10L, expectedProps);
//
//    List<Equipment> equipmentUpdateList = Arrays.asList(equipment);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(equipmentUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//
//    EasyMock.replay(equipmentCache);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals((long) parsed.get(0).getEntityId(), 10L);
//    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.EQUIPMENT);
//    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
//    assertEquals(parsed.get(0).getElementProperties(), expectedProps);
//
//    EasyMock.verify(equipmentCache);
//  }
//
//  @Test
//  public void updateEquipmentWithAllFields() {
//    // setup Configuration:
//    Properties expectedProps = new Properties();
//    Equipment equipment = buildUpdateEquipmentWithAllFields(10L, expectedProps);
//
//    List<Equipment> equipmentUpdateList = Arrays.asList(equipment);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(equipmentUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//
//    EasyMock.replay(equipmentCache);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals((long) parsed.get(0).getEntityId(), 10L);
//    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.EQUIPMENT);
//    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
//    assertEquals(parsed.get(0).getElementProperties(), expectedProps);
//
//    EasyMock.verify(equipmentCache);
//  }
//
//  @Test
//  public void updateNonExistentEquipment() {
//    // setup Configuration:
//    Equipment equipment = Equipment.update(10L).description("The description").build();
//
//    List<Equipment> equipmentUpdateList = Arrays.asList(equipment);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(equipmentUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(false);
//
//    // run test
//    EasyMock.replay(equipmentCache);
//
//    List<ConfigurationElement> result = parser.parse(config);
//
//    assertEquals(1, result.size());
//    assertEquals(ConfigConstants.Entity.MISSING, result.get(0).getEntity());
//    assertEquals(ConfigConstants.Status.WARNING, result.get(0).getStatus());
//
//    EasyMock.verify(equipmentCache);
//  }
//
//  @Test
//  public void deleteEquipment() {
//    // setup Configuration:
//    Equipment equipment = buildDeleteEquipment(10L);
//
//    List<Equipment> equipmentUpdateList = Arrays.asList(equipment);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(equipmentUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
//
//    EasyMock.replay(equipmentCache);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals(parsed.size(), 1);
//    assertEquals((long) parsed.get(0).getEntityId(), 10L);
//    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.REMOVE);
//    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.EQUIPMENT);
//    assertTrue(parsed.get(0).getElementProperties().isEmpty());
//
//    EasyMock.verify(equipmentCache);
//  }
//
//  @Test
//  public void deleteNonExistentEquipment() {
//    // setup Configuration:
//    Equipment equipment = buildDeleteEquipment(10L);
//
//    List<Equipment> equipmentRemoveList = Arrays.asList(equipment);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(equipmentRemoveList);
//
//    // setUp Mocks:
//    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(false);
//    EasyMock.replay(equipmentCache);
//
//    assertEquals(0, parser.parse(config).size());
//
//    EasyMock.verify(equipmentCache);
//  }
//}
