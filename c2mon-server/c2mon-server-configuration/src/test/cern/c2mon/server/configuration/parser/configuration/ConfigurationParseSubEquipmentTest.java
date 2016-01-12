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

import static cern.c2mon.server.configuration.parser.util.ConfigurationEquipmentUtil.buildUpdateEquipmentNewControlTag;
import static cern.c2mon.server.configuration.parser.util.ConfigurationSubEquipmentUtil.*;
import static cern.c2mon.server.configuration.parser.util.ConfigurationUtil.getConfBuilderEquipment;
import static cern.c2mon.server.configuration.parser.util.ConfigurationUtil.getConfBuilderSubEquipment;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import cern.c2mon.server.cache.*;
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

import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.server.configuration.parser.impl.ConfigurationParserImpl;
import cern.c2mon.server.configuration.parser.util.Pair;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:cern/c2mon/server/configuration/config/server-configuration-parser-test.xml" })
public class ConfigurationParseSubEquipmentTest {

  @Autowired
  ConfigurationParserImpl configurationParser;

  @Autowired
  ProcessCache processCache;

  @Autowired
  EquipmentCache equipmentCache;

  @Autowired
  SubEquipmentCache subEquipmentCache;

  @Autowired
  ControlTagCache statusTagCache;

  @Autowired
  AliveTimerCache aliveTagCache;

  @Autowired
  CommFaultTagCache commFaultTagCache;

  @Rule
  public ExpectedException subEquipmentUpdate = ExpectedException.none();

  @Rule
  public ExpectedException subEquipmentCreate = ExpectedException.none();

  @Rule
  public ExpectedException subEquipmentDelete = ExpectedException.none();

  @Before
  public void resetMocks(){
    EasyMock.reset(subEquipmentCache,processCache, equipmentCache, statusTagCache, commFaultTagCache, aliveTagCache);
  }

  @Test
  public void subEquipmentUpdate_withNoFields() {
    // Setup Configuration Instance
    Configuration configuration = getConfBuilderSubEquipment(buildSubEquipmentWithId(1L)._1);

//    DataTag.builder().address(new DataTagAddress(new PLCHardwareAddressImpl(0, 0, 0, 0, 0, 0.0f, "")));

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(subEquipmentCache.hasKey(1L)).andReturn(true);


    // Switch to replay mode
    EasyMock.replay(processCache);
    EasyMock.replay(equipmentCache);
    EasyMock.replay(subEquipmentCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertFalse(elements.size() > 0);

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(subEquipmentCache);
  }

  @Test
  public void subEquipmentUpdate_notExistingInstance() {
    // Setup Exception
    subEquipmentUpdate.expect(ConfigurationParseException.class);
    subEquipmentUpdate.expectMessage("Creating SubEquipment (id = 1) failed. Not enough arguments.");

    // Setup Configuration Instance
    Pair<SubEquipment,Properties> pair = buildUpdateSubEquipmentWithSomeFields(1l);
    Configuration configuration = getConfBuilderSubEquipment(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(subEquipmentCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache);
    EasyMock.replay(equipmentCache);
    EasyMock.replay(subEquipmentCache);

    // Run the code to be tested
   configurationParser.parse(configuration);

    // Verify mock methods were called
   EasyMock.verify(processCache);
   EasyMock.verify(equipmentCache);
   EasyMock.verify(subEquipmentCache);
  }

  @Test
  public void subEquipmentUpdate_withSomeFields() {
    // Setup Configuration Instance
    Pair<SubEquipment,Properties> pair = buildUpdateSubEquipmentWithSomeFields(1l);
    Configuration configuration = getConfBuilderSubEquipment(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1l)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(subEquipmentCache.hasKey(1l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache);
    EasyMock.replay(equipmentCache);
    EasyMock.replay(subEquipmentCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.SUBEQUIPMENT));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(subEquipmentCache);
  }

  @Test
  public void subEquipmentUpdate_withAllFields() {
    // Setup Configuration Instance
    Pair<SubEquipment,Properties> pair = buildUpdateSubEquipmentWithAllFields(1l);
    Configuration configuration = getConfBuilderSubEquipment(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(subEquipmentCache.hasKey(1l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache);
    EasyMock.replay(equipmentCache);
    EasyMock.replay(subEquipmentCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().equals(pair._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.SUBEQUIPMENT));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(subEquipmentCache);
  }

  @Test
  public void subEquipmentUpdate_multipleInstances_withAllFields() {
    // Setup Configuration Instance
    Pair<SubEquipment,Properties> pair1 = buildUpdateSubEquipmentWithAllFields(1l);
    Pair<SubEquipment,Properties> pair2 = buildUpdateSubEquipmentWithAllFields(2l);
    Pair<SubEquipment,Properties> pair3 = buildUpdateSubEquipmentWithAllFields(3l);
    Pair<SubEquipment,Properties> pair4 = buildUpdateSubEquipmentWithAllFields(4l);
    Pair<SubEquipment,Properties> pair5 = buildUpdateSubEquipmentWithAllFields(5l);

    Configuration configuration = getConfBuilderSubEquipment(pair1._1,pair2._1, pair3._1, pair4._1, pair5._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);

    EasyMock.expect(subEquipmentCache.hasKey(1l)).andReturn(true);
    EasyMock.expect(subEquipmentCache.hasKey(2l)).andReturn(true);
    EasyMock.expect(subEquipmentCache.hasKey(3l)).andReturn(true);
    EasyMock.expect(subEquipmentCache.hasKey(4l)).andReturn(true);
    EasyMock.expect(subEquipmentCache.hasKey(5l)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache);
    EasyMock.replay(equipmentCache);
    EasyMock.replay(subEquipmentCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 5);

    assertTrue(elements.get(0).getElementProperties().equals(pair1._2));
    assertTrue(elements.get(0).getEntity().equals(Entity.SUBEQUIPMENT));
    assertTrue(elements.get(0).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(1).getElementProperties().equals(pair2._2));
    assertTrue(elements.get(1).getEntity().equals(Entity.SUBEQUIPMENT));
    assertTrue(elements.get(1).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(2).getElementProperties().equals(pair3._2));
    assertTrue(elements.get(2).getEntity().equals(Entity.SUBEQUIPMENT));
    assertTrue(elements.get(2).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(3).getElementProperties().equals(pair4._2));
    assertTrue(elements.get(3).getEntity().equals(Entity.SUBEQUIPMENT));
    assertTrue(elements.get(3).getAction().equals(Action.UPDATE));

    assertTrue(elements.get(4).getElementProperties().equals(pair5._2));
    assertTrue(elements.get(4).getEntity().equals(Entity.SUBEQUIPMENT));
    assertTrue(elements.get(4).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(subEquipmentCache);
  }

  @Test
  public void subEquipmentCreate_withNoFields() {
    // Setup Exception
    subEquipmentCreate.expect(ConfigurationParseException.class);
    subEquipmentCreate.expectMessage("Creating SubEquipment (id = 1) failed. Not enough arguments.");

    // Setup Configuration Instance
    Configuration configuration = getConfBuilderSubEquipment(buildSubEquipmentWithId(1L)._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(subEquipmentCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache);
    EasyMock.replay(equipmentCache);
    EasyMock.replay(subEquipmentCache);

    // Run the code to be tested
    configurationParser.parse(configuration);

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(subEquipmentCache);
  }

  @Test
  public void subEquipmentCreate_withMandatoryFields() {
    // Setup Configuration Instance
    Pair<SubEquipment,Properties> pair = buildSubEquipmentWtihPrimFields(1l);
    Configuration configuration = getConfBuilderSubEquipment(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(subEquipmentCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(2l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(subEquipmentCache,processCache, equipmentCache, statusTagCache, commFaultTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 3);
    assertTrue(elements.get(2).getElementProperties().equals(pair._2));
    assertTrue(elements.get(2).getEntityId().equals(1L));
    assertTrue(elements.get(2).getEntity().equals(Entity.SUBEQUIPMENT));
    assertTrue(elements.get(2).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(subEquipmentCache,processCache, equipmentCache, statusTagCache, commFaultTagCache);
  }

  @Test
  public void subEquipmentCreate_withNotExistingSupClass() {
    subEquipmentCreate.expect(ConfigurationParseException.class);
    subEquipmentCreate.expectMessage("Creating Process (id = 1) failed. Not enough arguments.");

    // Setup Configuration Instance
    Pair<SubEquipment,Properties> pair = buildSubEquipmentWtihPrimFields(1l);
    Configuration configuration = getConfBuilderSubEquipment(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache);

    // Run the code to be tested
    configurationParser.parse(configuration);

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
    EasyMock.verify(subEquipmentCache);
  }

  @Test
  public void subEquipmentCreate_withAllFields() {
    // Setup Configuration Instance
    Pair<SubEquipment,Properties> pair = buildSubEquipmentWithAllFields(1l);
    Configuration configuration = getConfBuilderSubEquipment(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(subEquipmentCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(2l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(subEquipmentCache,processCache, equipmentCache, statusTagCache, commFaultTagCache, aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 4);
    assertTrue(elements.get(3).getElementProperties().equals(pair._2));
    assertTrue(elements.get(3).getEntityId().equals(1L));
    assertTrue(elements.get(3).getEntity().equals(Entity.SUBEQUIPMENT));
    assertTrue(elements.get(3).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(subEquipmentCache,processCache, equipmentCache, statusTagCache, commFaultTagCache, aliveTagCache);
  }

  @Test
  public void subEquipmentCreate_withoutDefaultFields() {
    // Setup Configuration Instance
    Pair<SubEquipment,Properties> pair = buildSubEquipmentWithoutDefaultFields(1l);
    Configuration configuration = getConfBuilderSubEquipment(pair._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(subEquipmentCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(2l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(subEquipmentCache,processCache, equipmentCache, statusTagCache, commFaultTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 3);
    assertTrue(elements.get(2).getElementProperties().equals(pair._2));
    assertTrue(elements.get(2).getEntityId().equals(1L));
    assertTrue(elements.get(2).getEntity().equals(Entity.SUBEQUIPMENT));
    assertTrue(elements.get(2).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(subEquipmentCache,processCache, equipmentCache, statusTagCache, commFaultTagCache);
  }

  @Test
  public void subEquipmentCreate_multipleInstances_withAllFields() {
    // Setup Configuration Instance
    Pair<SubEquipment,Properties> pair1 = buildSubEquipmentWithAllFields(1l);
    Pair<SubEquipment,Properties> pair2 = buildSubEquipmentWithAllFields(2l);
    Pair<SubEquipment,Properties> pair3 = buildSubEquipmentWithAllFields(3l);
    Pair<SubEquipment,Properties> pair4 = buildSubEquipmentWithAllFields(4l);
    Pair<SubEquipment,Properties> pair5 = buildSubEquipmentWithAllFields(5l);

    Configuration configuration = getConfBuilderSubEquipment(pair1._1,pair2._1, pair3._1, pair4._1, pair5._1);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);

    EasyMock.expect(subEquipmentCache.hasKey(1l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(2l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    EasyMock.expect(subEquipmentCache.hasKey(2l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(2l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    EasyMock.expect(subEquipmentCache.hasKey(3l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(2l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    EasyMock.expect(subEquipmentCache.hasKey(4l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(2l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    EasyMock.expect(subEquipmentCache.hasKey(5l)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(2l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(subEquipmentCache,processCache, equipmentCache, statusTagCache, commFaultTagCache, aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 20);

    assertTrue(elements.get(15).getElementProperties().equals(pair1._2));
    assertTrue(elements.get(15).getEntityId().equals(1L));
    assertTrue(elements.get(15).getEntity().equals(Entity.SUBEQUIPMENT));
    assertTrue(elements.get(15).getAction().equals(Action.CREATE));

    assertTrue(elements.get(16).getElementProperties().equals(pair2._2));
    assertTrue(elements.get(16).getEntityId().equals(2L));
    assertTrue(elements.get(16).getEntity().equals(Entity.SUBEQUIPMENT));
    assertTrue(elements.get(16).getAction().equals(Action.CREATE));

    assertTrue(elements.get(17).getElementProperties().equals(pair3._2));
    assertTrue(elements.get(17).getEntityId().equals(3L));
    assertTrue(elements.get(17).getEntity().equals(Entity.SUBEQUIPMENT));
    assertTrue(elements.get(17).getAction().equals(Action.CREATE));

    assertTrue(elements.get(18).getElementProperties().equals(pair4._2));
    assertTrue(elements.get(18).getEntityId().equals(4L));
    assertTrue(elements.get(18).getEntity().equals(Entity.SUBEQUIPMENT));
    assertTrue(elements.get(18).getAction().equals(Action.CREATE));

    assertTrue(elements.get(19).getElementProperties().equals(pair5._2));
    assertTrue(elements.get(19).getEntityId().equals(5L));
    assertTrue(elements.get(19).getEntity().equals(Entity.SUBEQUIPMENT));
    assertTrue(elements.get(19).getAction().equals(Action.CREATE));

    // Verify mock methods were called
    EasyMock.verify(subEquipmentCache,processCache, equipmentCache, statusTagCache, commFaultTagCache, aliveTagCache);
  }

  @Test
  public void subEquipmentDelete_NotExistingInstance() {
    // Setup Exception
//    subEquipmentDelete.expect(ConfigurationParseException.class);
//    subEquipmentDelete.expectMessage("Deleting SubEquipment 1 failed. SubEquipment do not exist in the cache.");

    // Setup Configuration Instance
    SubEquipment subEquipment = buildDeleteSubEquipment(1l);
    Configuration configuration = getConfBuilderSubEquipment(subEquipment);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
//    EasyMock.expect(subEquipmentCache.hasKey(1L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(processCache);
    EasyMock.replay(equipmentCache);
//    EasyMock.replay(subEquipmentCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().isEmpty());
    assertTrue(elements.get(0).getEntity().equals(Entity.SUBEQUIPMENT));
    assertTrue(elements.get(0).getAction().equals(Action.REMOVE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
  }

  @Test
  public void subEquipmentCreate_withNewControlTag() {
    subEquipmentCreate.expect(ConfigurationParseException.class);
    subEquipmentCreate.expectMessage("Not possible to create a ControlTag for Process or Equipment when the parent already exists.");

    // Setup Configuration Instance
    SubEquipment subEquipment = buildUpdateSubEquipmentNewControlTag(1l);
    Configuration configuration = getConfBuilderEquipment(subEquipment);
    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1l)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(0l)).andReturn(true);
    EasyMock.expect(subEquipmentCache.hasKey(0l)).andReturn(true);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(subEquipmentCache,processCache, equipmentCache, aliveTagCache);

    // runt test
    configurationParser.parse(configuration);


    // Verify mock methods were called
    EasyMock.verify(subEquipmentCache,processCache, equipmentCache, aliveTagCache);
  }

  /**
   * At the moment, the parsers returns always a Delte ConfigurationElement.
   * Independent from the id in the cache!
   */
  @Test
  public void subEquipmentDelete_ExistingInstance() {
    // Setup Configuration Instance
    SubEquipment subEquipment = buildDeleteSubEquipment(1l);
    Configuration configuration = getConfBuilderSubEquipment(subEquipment);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
//    EasyMock.expect(subEquipmentCache.hasKey(1L)).andReturn(true);

    // Switch to replay mode
    EasyMock.replay(processCache);
    EasyMock.replay(equipmentCache);
//    EasyMock.replay(subEquipmentCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

 // Assert stuff
    assertTrue(elements.size() == 1);
    assertTrue(elements.get(0).getElementProperties().isEmpty());
    assertTrue(elements.get(0).getEntity().equals(Entity.SUBEQUIPMENT));
    assertTrue(elements.get(0).getAction().equals(Action.REMOVE));

    // Verify mock methods were called
    EasyMock.verify(processCache);
    EasyMock.verify(equipmentCache);
  }

  @Test
  public void subEquipmentAllOperations_checkOrder() {
    // Setup Configuration Instance
    Pair<SubEquipment,Properties> pairUpdate = buildUpdateSubEquipmentWithAllFields(2l);
    Pair<SubEquipment,Properties> pairCreate = buildSubEquipmentWithAllFields(3l);
    SubEquipment subEquipmentDelete = buildDeleteSubEquipment(1l);
    Configuration configuration = getConfBuilderSubEquipment(pairUpdate._1, pairCreate._1, subEquipmentDelete);

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);

    EasyMock.expect(subEquipmentCache.hasKey(2L)).andReturn(true);
    EasyMock.expect(subEquipmentCache.hasKey(3L)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(0l)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(2l)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(1l)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(subEquipmentCache,processCache, equipmentCache, statusTagCache, commFaultTagCache, aliveTagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(configuration);

    // Assert stuff
    assertTrue(elements.size() == 6);

    // Check ConfigurationElement fields and order
    assertTrue(elements.get(0).getElementProperties().isEmpty());
    assertTrue(elements.get(0).getEntity().equals(Entity.SUBEQUIPMENT));
    assertTrue(elements.get(0).getAction().equals(Action.REMOVE));

    assertTrue(elements.get(4).getElementProperties().equals(pairCreate._2));
    assertTrue(elements.get(4).getEntity().equals(Entity.SUBEQUIPMENT));
    assertTrue(elements.get(4).getAction().equals(Action.CREATE));

    assertTrue(elements.get(5).getElementProperties().equals(pairUpdate._2));
    assertTrue(elements.get(5).getEntity().equals(Entity.SUBEQUIPMENT));
    assertTrue(elements.get(5).getAction().equals(Action.UPDATE));

    // Verify mock methods were called
    EasyMock.verify(subEquipmentCache,processCache, equipmentCache, statusTagCache, commFaultTagCache, aliveTagCache);
  }
}
