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
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.cache.loading.SubEquipmentDAO;
import cern.c2mon.server.configuration.parser.ConfigurationParser;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.server.configuration.parser.util.ConfigurationUtil;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
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
public class ConfigureSubEquipmentTest {

  @Autowired
  ConfigurationParser parser;

  @Autowired
  SequenceDAO sequenceDAO;

  @Autowired
  EquipmentCache equipmentCache;

  @Autowired
  SubEquipmentCache subEquipmentCache;

  @Autowired
  SubEquipmentDAO subEquipmentDAO;

  @Autowired
  ControlTagCache controlTagCache;

  @Autowired
  CommFaultTagCache commFaultTagCache;

  @Rule
  public ExpectedException tagException = ExpectedException.none();


  @Before
  public void resetMocks() {
    EasyMock.reset(subEquipmentDAO, sequenceDAO, equipmentCache, subEquipmentCache, controlTagCache, commFaultTagCache);
  }

  @Test
  public void createSubEquipment() {
    // setup Configuration:
    SubEquipment equipment = SubEquipment.create("myEquipment", "HandlerClassName").build();
    equipment.setParentEquipmentId(10L);

    List<SubEquipment> equipmentList = new ArrayList<>();
    equipmentList.add(equipment);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(equipmentList);

    // setUp Mocks:
    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
    EasyMock.expect(sequenceDAO.getNextEquipmentId()).andReturn(20L);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(30L);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(31L);

    EasyMock.expect(controlTagCache.hasKey(31L)).andReturn(false).times(2);
    EasyMock.expect(commFaultTagCache.hasKey(30L)).andReturn(false).times(2);

    EasyMock.replay(sequenceDAO, equipmentCache, commFaultTagCache, controlTagCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 3);
    assertEquals((long) parsed.get(0).getEntityId(), 31L);
    assertEquals((long) parsed.get(1).getEntityId(), 30L);
    assertEquals((long) parsed.get(2).getEntityId(), 20L);
    assertEquals(parsed.get(2).getElementProperties().size(), 7);
    assertEquals(parsed.get(2).getElementProperties().get("aliveInterval"), "60000");
    assertEquals(parsed.get(2).getElementProperties().get("name"), "myEquipment");
    assertEquals(parsed.get(2).getElementProperties().get("handlerClass"), "HandlerClassName");
    assertEquals(parsed.get(2).getElementProperties().get("description"), "No description specified");
    assertEquals(parsed.get(2).getElementProperties().get("stateTagId"), "31");
    assertEquals(parsed.get(2).getElementProperties().get("commFaultTagId"), "30");
    assertEquals(parsed.get(2).getElementProperties().get("equipmentId"), "10");

    EasyMock.verify(sequenceDAO, equipmentCache, commFaultTagCache, controlTagCache);
  }

  @Test
  public void createEquipmentWithId() {
    // setup Configuration:
    SubEquipment equipment = SubEquipment.create("myEquipment", "HandlerClassName").id(21L).build();
    equipment.setParentEquipmentId(10L);

    List<SubEquipment> equipmentList = new ArrayList<>();
    equipmentList.add(equipment);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(equipmentList);

    // setUp Mocks:
    EasyMock.expect(subEquipmentCache.hasKey(21L)).andReturn(false);
    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);

    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(30L);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(31L);

    EasyMock.expect(controlTagCache.hasKey(31L)).andReturn(false).times(2);
    EasyMock.expect(commFaultTagCache.hasKey(30L)).andReturn(false).times(2);

    EasyMock.replay(subEquipmentCache, sequenceDAO, equipmentCache, controlTagCache, commFaultTagCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 3);
    assertEquals((long) parsed.get(0).getEntityId(), 31L);
    assertEquals((long) parsed.get(1).getEntityId(), 30L);
    assertEquals((long) parsed.get(2).getEntityId(), 21L);
    assertEquals(parsed.get(2).getElementProperties().size(), 7);
    assertEquals(parsed.get(2).getElementProperties().get("aliveInterval"), "60000");
    assertEquals(parsed.get(2).getElementProperties().get("name"), "myEquipment");
    assertEquals(parsed.get(2).getElementProperties().get("handlerClass"), "HandlerClassName");
    assertEquals(parsed.get(2).getElementProperties().get("description"), "No description specified");
    assertEquals(parsed.get(2).getElementProperties().get("stateTagId"), "31");
    assertEquals(parsed.get(2).getElementProperties().get("commFaultTagId"), "30");
    assertEquals(parsed.get(2).getElementProperties().get("equipmentId"), "10");

    EasyMock.verify(subEquipmentCache, sequenceDAO, equipmentCache, controlTagCache, commFaultTagCache);
  }

  @Test
  public void createEquipmentWithNotExistingProcess() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Creating of a new SubEquipment (id = 21) failed: No Equipment with the id 10 found");

    SubEquipment equipment = SubEquipment.create("myEquipment", "HandlerClassName").id(21L).build();
    equipment.setParentEquipmentId(10L);

    List<SubEquipment> equipmentList = new ArrayList<>();
    equipmentList.add(equipment);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(equipmentList);

    // setUp Mocks:
    EasyMock.expect(subEquipmentCache.hasKey(21L)).andReturn(false);
    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(false);

    // run test
    EasyMock.replay(subEquipmentCache, sequenceDAO, equipmentCache);
    parser.parse(config);
    EasyMock.verify(subEquipmentCache, sequenceDAO, equipmentCache);
  }

  @Test
  public void createExistingEquipment() {
    // Setup Exception
    tagException.expect(IllegalArgumentException.class);
    tagException.expectMessage("Error while parsing a 'create' Configuration: Id 21 of the class SubEquipment already known to the server");

    SubEquipment equipment = SubEquipment.create("myEquipment", "HandlerClassName").id(21L).build();
    equipment.setParentEquipmentId(10L);

    List<SubEquipment> equipmentList = new ArrayList<>();
    equipmentList.add(equipment);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(equipmentList);

    // setUp Mocks:
    EasyMock.expect(equipmentCache.hasKey(10L)).andReturn(true);
    EasyMock.expect(subEquipmentCache.hasKey(21L)).andReturn(true);

    // run test
    EasyMock.replay(subEquipmentCache, sequenceDAO, equipmentCache);
    parser.parse(config);
    EasyMock.verify(subEquipmentCache, sequenceDAO, equipmentCache);
  }

  @Test
  public void updateEquipmentWithName() {
    // setup Configuration:
    SubEquipment equipment = SubEquipment.update("myEquipment").description("The description").build();

    List<SubEquipment> equipmentUpdateList = new ArrayList<>();
    equipmentUpdateList.add(equipment);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(equipmentUpdateList);

    // setUp Mocks:
    EasyMock.expect(subEquipmentDAO.getIdByName("myEquipment")).andReturn(20L);
    EasyMock.expect(subEquipmentCache.hasKey(20L)).andReturn(true);

    EasyMock.replay(subEquipmentDAO, subEquipmentCache, sequenceDAO, equipmentCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.SUBEQUIPMENT);
    assertEquals((long) parsed.get(0).getEntityId(), 20L);
    assertEquals(parsed.get(0).getElementProperties().get("description"), "The description");

    EasyMock.verify(subEquipmentDAO, subEquipmentCache, sequenceDAO, equipmentCache);
  }

  @Test
  public void updateEquipmentWithId() {
    // setup Configuration:
    SubEquipment equipment = SubEquipment.update(20L).description("The description").build();

    List<SubEquipment> equipmentUpdateList = new ArrayList<>();
    equipmentUpdateList.add(equipment);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(equipmentUpdateList);

    // setUp Mocks:
    EasyMock.expect(subEquipmentCache.hasKey(20L)).andReturn(true);

    EasyMock.replay(subEquipmentCache, sequenceDAO, equipmentCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.SUBEQUIPMENT);
    assertEquals((long) parsed.get(0).getEntityId(), 20L);
    assertEquals(parsed.get(0).getElementProperties().get("description"), "The description");

    EasyMock.verify(subEquipmentCache, sequenceDAO, equipmentCache);
  }

  @Test
  public void updateNotExistingEquipment() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Updating of SubEquipment (id = 20) failed: The object is unknown to the sever.");

    // setup Configuration:
    SubEquipment equipment = SubEquipment.update(20L).description("The description").build();

    List<SubEquipment> equipmentUpdateList = new ArrayList<>();
    equipmentUpdateList.add(equipment);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(equipmentUpdateList);

    // setUp Mocks:
    EasyMock.expect(subEquipmentCache.hasKey(20L)).andReturn(false);

    // run test
    EasyMock.replay(subEquipmentCache, sequenceDAO, equipmentCache);
    parser.parse(config);
    EasyMock.verify(subEquipmentCache, sequenceDAO, equipmentCache);
  }

  @Test
  public void deleteEquipment() {
    // setup Configuration:
    SubEquipment equipment = SubEquipment.builder().id(20L).deleted(true).build();

    List<SubEquipment> equipmentUpdateList = new ArrayList<>();
    equipmentUpdateList.add(equipment);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(equipmentUpdateList);

    // setUp Mocks:
    EasyMock.expect(subEquipmentCache.hasKey(20L)).andReturn(true);

    EasyMock.replay(subEquipmentCache, sequenceDAO, equipmentCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.REMOVE);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.SUBEQUIPMENT);
    assertTrue(parsed.get(0).getElementProperties().isEmpty());

    EasyMock.verify(subEquipmentCache, sequenceDAO, equipmentCache);
  }

  @Test
  public void deleteNotExistingEquipment() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Deleting of SubEquipment (id = 20) failed: The object is unknown to the sever.");

    // setup Configuration:
    SubEquipment equipment = SubEquipment.builder().id(20L).deleted(true).build();

    List<SubEquipment> equipmentUpdateList = new ArrayList<>();
    equipmentUpdateList.add(equipment);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(equipmentUpdateList);

    // setUp Mocks:
    EasyMock.expect(subEquipmentCache.hasKey(20L)).andReturn(false);
    EasyMock.replay(subEquipmentCache, sequenceDAO, equipmentCache);

    parser.parse(config);

    EasyMock.verify(subEquipmentCache, sequenceDAO, equipmentCache);
  }

}
