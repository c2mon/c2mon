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

import cern.c2mon.server.cache.*;
import cern.c2mon.server.cache.loading.EquipmentDAO;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.configuration.parser.ConfigurationParser;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.server.configuration.parser.util.ConfigurationUtil;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
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
public class ConfigureEquipmentTest {

  @Autowired
  ConfigurationParser parser;

  @Autowired
  ProcessCache processCache;

  @Autowired
  SequenceDAO sequenceDAO;

  @Autowired
  EquipmentCache equipmentCache;

  @Autowired
  EquipmentDAO equipmentDAO;

  @Autowired
  ControlTagCache controlTagCache;

  @Autowired
  CommFaultTagCache commFaultTagCache;

  @Rule
  public ExpectedException tagException = ExpectedException.none();


  @Before
  public void resetMocks() {
    EasyMock.reset(equipmentDAO, processCache, sequenceDAO, equipmentCache,  controlTagCache, commFaultTagCache);
  }

  @Test
  public void createEquipment() {
    // setup Configuration:
    Equipment equipment = Equipment.create("myEquipment", "HandlerClassName").build();
    equipment.setParentProcessId(10L);

    List<Equipment> equipmentList = new ArrayList<>();
    equipmentList.add(equipment);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(equipmentList);

    // setUp Mocks:
    EasyMock.expect(processCache.hasKey(10L)).andReturn(true);
    EasyMock.expect(sequenceDAO.getNextEquipmentId()).andReturn(20L);
    EasyMock.expect(equipmentCache.hasKey(20L)).andReturn(false);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(30L);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(31L);

    EasyMock.expect(controlTagCache.hasKey(31L)).andReturn(false).times(2);
    EasyMock.expect(commFaultTagCache.hasKey(30L)).andReturn(false).times(2);

    EasyMock.replay(processCache, sequenceDAO, equipmentCache, commFaultTagCache, controlTagCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 3);
    assertEquals((long) parsed.get(0).getEntityId(), 31L);
    assertEquals((long) parsed.get(1).getEntityId(), 30L);
    assertEquals((long) parsed.get(2).getEntityId(), 20L);
    assertEquals(parsed.get(2).getElementProperties().size(), 7);
    assertEquals(parsed.get(2).getElementProperties().get("aliveInterval"), "60000");
    assertEquals(parsed.get(2).getElementProperties().get("name"), "myEquipment");
    assertEquals(parsed.get(2).getElementProperties().get("handlerClass"), "HandlerClassName");
    assertEquals(parsed.get(2).getElementProperties().get("description"), "<no description provided>");
    assertEquals(parsed.get(2).getElementProperties().get("stateTagId"), "31");
    assertEquals(parsed.get(2).getElementProperties().get("commFaultTagId"), "30");
    assertEquals(parsed.get(2).getElementProperties().get("processId"), "10");

    EasyMock.verify(processCache, sequenceDAO, equipmentCache, commFaultTagCache, controlTagCache);
  }

  @Test
  public void createEquipmentWithId() {
    // setup Configuration:
    Equipment equipment = Equipment.create("myEquipment", "HandlerClassName").id(21L).build();
    equipment.setParentProcessId(10L);

    List<Equipment> equipmentList = new ArrayList<>();
    equipmentList.add(equipment);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(equipmentList);

    // setUp Mocks:
    EasyMock.expect(equipmentCache.hasKey(21L)).andReturn(false);
    EasyMock.expect(processCache.hasKey(10L)).andReturn(true);

    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(30L);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(31L);

    EasyMock.expect(controlTagCache.hasKey(31L)).andReturn(false).times(2);
    EasyMock.expect(commFaultTagCache.hasKey(30L)).andReturn(false).times(2);

    EasyMock.replay(processCache, sequenceDAO, equipmentCache, controlTagCache, commFaultTagCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 3);
    assertEquals((long) parsed.get(0).getEntityId(), 31L);
    assertEquals((long) parsed.get(1).getEntityId(), 30L);
    assertEquals((long) parsed.get(2).getEntityId(), 21L);
    assertEquals(parsed.get(2).getElementProperties().size(), 7);
    assertEquals(parsed.get(2).getElementProperties().get("aliveInterval"), "60000");
    assertEquals(parsed.get(2).getElementProperties().get("name"), "myEquipment");
    assertEquals(parsed.get(2).getElementProperties().get("handlerClass"), "HandlerClassName");
    assertEquals(parsed.get(2).getElementProperties().get("description"), "<no description provided>");
    assertEquals(parsed.get(2).getElementProperties().get("stateTagId"), "31");
    assertEquals(parsed.get(2).getElementProperties().get("commFaultTagId"), "30");
    assertEquals(parsed.get(2).getElementProperties().get("processId"), "10");

    EasyMock.verify(processCache, sequenceDAO, equipmentCache, controlTagCache, commFaultTagCache);
  }

  @Test
  public void createEquipmentWithNotExistingProcess() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Creating of a new Equipment (id = 21) failed: No Process with the id 10 found");

    Equipment equipment = Equipment.create("myEquipment", "HandlerClassName").id(21L).build();
    equipment.setParentProcessId(10L);

    List<Equipment> equipmentList = new ArrayList<>();
    equipmentList.add(equipment);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(equipmentList);

    // setUp Mocks:
    EasyMock.expect(equipmentCache.hasKey(21L)).andReturn(false);
    EasyMock.expect(processCache.hasKey(10L)).andReturn(false);

    // run test
    EasyMock.replay(processCache, sequenceDAO, equipmentCache);
    parser.parse(config);
    EasyMock.verify(processCache, sequenceDAO, equipmentCache);
  }

  @Test
  public void createExistingEquipment() {
    // Setup Exception
    tagException.expect(IllegalArgumentException.class);
    tagException.expectMessage("Error while parsing a 'create' Configuration: Id 21 of the class Equipment already known to the server");

    Equipment equipment = Equipment.create("myEquipment", "HandlerClassName").id(21L).build();
    equipment.setParentProcessId(10L);

    List<Equipment> equipmentList = new ArrayList<>();
    equipmentList.add(equipment);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(equipmentList);

    // setUp Mocks:
    EasyMock.expect(processCache.hasKey(10L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(21L)).andReturn(true);

    // run test
    EasyMock.replay(processCache, sequenceDAO, equipmentCache);
    parser.parse(config);
    EasyMock.verify(processCache, sequenceDAO, equipmentCache);
  }

  @Test
  public void updateEquipmentWithName() {
    // setup Configuration:
    Equipment equipment = Equipment.update("myEquipment").description("The description").build();

    List<Equipment> equipmentUpdateList = new ArrayList<>();
    equipmentUpdateList.add(equipment);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(equipmentUpdateList);

    // setUp Mocks:
    EasyMock.expect(equipmentDAO.getIdByName("myEquipment")).andReturn(20L);
    EasyMock.expect(equipmentCache.hasKey(20L)).andReturn(true);

    EasyMock.replay(equipmentDAO, processCache, sequenceDAO, equipmentCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.EQUIPMENT);
    assertEquals((long) parsed.get(0).getEntityId(), 20L);
    assertEquals(parsed.get(0).getElementProperties().get("description"), "The description");

    EasyMock.verify(equipmentDAO, processCache, sequenceDAO, equipmentCache);
  }

  @Test
  public void updateEquipmentWithId() {
    // setup Configuration:
    Equipment equipment = Equipment.update(20L).description("The description").build();

    List<Equipment> equipmentUpdateList = new ArrayList<>();
    equipmentUpdateList.add(equipment);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(equipmentUpdateList);

    // setUp Mocks:
    EasyMock.expect(equipmentCache.hasKey(20L)).andReturn(true);

    EasyMock.replay(processCache, sequenceDAO, equipmentCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.EQUIPMENT);
    assertEquals((long) parsed.get(0).getEntityId(), 20L);
    assertEquals(parsed.get(0).getElementProperties().get("description"), "The description");

    EasyMock.verify(processCache, sequenceDAO, equipmentCache);
  }

  @Test
  public void updateNotExistingEquipment() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Updating of Equipment (id = 20) failed: The object is unknown to the sever.");

    // setup Configuration:
    Equipment equipment = Equipment.update(20L).description("The description").build();

    List<Equipment> equipmentUpdateList = new ArrayList<>();
    equipmentUpdateList.add(equipment);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(equipmentUpdateList);

    // setUp Mocks:
    EasyMock.expect(equipmentCache.hasKey(20L)).andReturn(false);

    // run test
    EasyMock.replay(processCache, sequenceDAO, equipmentCache);
    parser.parse(config);
    EasyMock.verify(processCache, sequenceDAO, equipmentCache);
  }

  @Test
  public void deleteEquipment() {
    // setup Configuration:
    Equipment equipment = Equipment.builder().id(20L).deleted(true).build();

    List<Equipment> equipmentUpdateList = new ArrayList<>();
    equipmentUpdateList.add(equipment);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(equipmentUpdateList);

    // setUp Mocks:
    EasyMock.expect(equipmentCache.hasKey(20L)).andReturn(true);

    EasyMock.replay(processCache, sequenceDAO, equipmentCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.REMOVE);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.EQUIPMENT);
    assertTrue(parsed.get(0).getElementProperties().isEmpty());

    EasyMock.verify(processCache, sequenceDAO, equipmentCache);
  }

  @Test
  public void deleteNotExistingEquipment() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Deleting of Equipment (id = 20) failed: The object is unknown to the sever.");

    // setup Configuration:
    Equipment equipment = Equipment.builder().id(20L).deleted(true).build();

    List<Equipment> equipmentUpdateList = new ArrayList<>();
    equipmentUpdateList.add(equipment);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(equipmentUpdateList);

    // setUp Mocks:
    EasyMock.expect(equipmentCache.hasKey(20L)).andReturn(false);
    EasyMock.replay(processCache, sequenceDAO, equipmentCache);

    parser.parse(config);


    EasyMock.verify(processCache, sequenceDAO, equipmentCache);
  }

}
