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

import cern.c2mon.server.cache.*;
import cern.c2mon.server.configuration.parser.impl.ConfigurationParserImpl;
import cern.c2mon.server.configuration.parser.util.Pair;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;
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

import static cern.c2mon.server.configuration.parser.util.ConfigurationAllTogetherUtil.buildAllMandatory;
import static cern.c2mon.server.configuration.parser.util.ConfigurationAllTogetherUtil.buildAllWithAllFields;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/configuration/config/server-configuration-parser-test.xml"})
public class ConfigurationParseAllTogetherTest {

  @Autowired
  ConfigurationParserImpl configurationParser;

  @Autowired
  ProcessCache processCache;

  @Autowired
  EquipmentCache equipmentCache;

  @Autowired
  SubEquipmentCache subEquipmentCache;

  @Autowired
  DataTagCache dataTagCache;

  @Autowired
  AliveTimerCache aliveTagCache;

  @Autowired
  AlarmCache alarmCache;

  @Autowired
  ControlTagCache statusTagCache;

  @Autowired
  CommFaultTagCache commFaultTagCache;

  @Autowired
  RuleTagCache ruleTagCache;

  @Autowired
  CommandTagCache commandtagCache;


  @Rule
  public ExpectedException processUpdate = ExpectedException.none();

  @Rule
  public ExpectedException processCreate = ExpectedException.none();

  @Rule
  public ExpectedException processDelete = ExpectedException.none();

  @Before
  public void resetMocks() {
    EasyMock.reset(ruleTagCache, processCache, equipmentCache, subEquipmentCache, alarmCache, dataTagCache, aliveTagCache, commFaultTagCache, statusTagCache, commandtagCache);
  }


  @Test
  public void processAllOperations_mandatory() {
    // Setup Configuration Instance
    Pair<Configuration, List<Properties>> confObj = buildAllMandatory();

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(0L)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(24L)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(21L)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(6L)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(13L)).andReturn(false);

    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(25L)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(22L)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(4L)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(14L)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(7L)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(15L)).andReturn(false);
    EasyMock.expect(dataTagCache.hasKey(9L)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(16L)).andReturn(false);
    EasyMock.expect(commandtagCache.hasKey(27L)).andReturn(false);

    EasyMock.expect(subEquipmentCache.hasKey(2L)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(26L)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(23L)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(5L)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(17L)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(8L)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(18L)).andReturn(false);
    EasyMock.expect(dataTagCache.hasKey(10L)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(19L)).andReturn(false);

    EasyMock.expect(ruleTagCache.hasKey(11L)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(20L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(ruleTagCache, processCache, equipmentCache, subEquipmentCache, alarmCache, dataTagCache, aliveTagCache, commFaultTagCache, statusTagCache, commandtagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(confObj._1);

    // Assert stuff
    assertTrue(elements.size() == 26);

    // Check ConfigurationElement fields and order
    for (int i = 0; i < elements.size(); i++) {
      switch (i) {
        case 0:
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
          assertTrue(elements.get(i).getEntity().equals(Entity.CONTROLTAG));
          assertTrue(elements.get(i).getAction().equals(Action.CREATE));
          break;
        case 8:
          assertTrue(elements.get(i).getEntity().equals(Entity.PROCESS));
          assertTrue(elements.get(i).getAction().equals(Action.CREATE));
          break;
        case 9:
          assertTrue(elements.get(i).getEntity().equals(Entity.EQUIPMENT));
          assertTrue(elements.get(i).getAction().equals(Action.CREATE));
          break;
        case 10:
          assertTrue(elements.get(i).getEntity().equals(Entity.SUBEQUIPMENT));
          assertTrue(elements.get(i).getAction().equals(Action.CREATE));
          break;
        case 11:
        case 12:
          assertTrue(elements.get(i).getEntity().equals(Entity.DATATAG));
          assertTrue(elements.get(i).getAction().equals(Action.CREATE));
          break;
        case 13:
          assertTrue(elements.get(i).getEntity().equals(Entity.RULETAG));
          assertTrue(elements.get(i).getAction().equals(Action.CREATE));
          break;
        case 14:
        case 15:
        case 16:
        case 17:
        case 18:
        case 19:
        case 20:
        case 21:
        case 22:
        case 23:
        case 24:
          assertTrue(elements.get(i).getEntity().equals(Entity.ALARM));
          assertTrue(elements.get(i).getAction().equals(Action.CREATE));
          break;
        case 25:
          assertTrue(elements.get(i).getEntity().equals(Entity.COMMANDTAG));
          assertTrue(elements.get(i).getAction().equals(Action.CREATE));
      }
      assertTrue(elements.get(i).getElementProperties().equals(confObj._2.get(i)));
    }

    // Verify mock methods were called
    EasyMock.verify(ruleTagCache, processCache, equipmentCache, subEquipmentCache, alarmCache, dataTagCache, aliveTagCache, commFaultTagCache, statusTagCache, commandtagCache);
  }


  @Test
  public void processAllOperations_allFields() {
    // Setup Configuration Instance
    Pair<Configuration, List<Properties>> confObj = buildAllWithAllFields();

    // Setup Mock
    // Set expectations
    EasyMock.expect(processCache.hasKey(0L)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(24L)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(21L)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(6L)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(13L)).andReturn(false);

    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(25L)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(22L)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(4L)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(14L)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(7L)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(15L)).andReturn(false);
    EasyMock.expect(dataTagCache.hasKey(9L)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(16L)).andReturn(false);
    EasyMock.expect(commandtagCache.hasKey(27L)).andReturn(false);

    EasyMock.expect(subEquipmentCache.hasKey(2L)).andReturn(false);
    EasyMock.expect(statusTagCache.hasKey(26L)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(23L)).andReturn(false);
    EasyMock.expect(commFaultTagCache.hasKey(5L)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(17L)).andReturn(false);
    EasyMock.expect(aliveTagCache.hasKey(8L)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(18L)).andReturn(false);
    EasyMock.expect(dataTagCache.hasKey(10L)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(19L)).andReturn(false);

    EasyMock.expect(ruleTagCache.hasKey(11L)).andReturn(false);
    EasyMock.expect(alarmCache.hasKey(20L)).andReturn(false);

    // Switch to replay mode
    EasyMock.replay(ruleTagCache, processCache, equipmentCache, subEquipmentCache, alarmCache, dataTagCache, aliveTagCache, commFaultTagCache, statusTagCache, commandtagCache);

    // Run the code to be tested
    List<ConfigurationElement> elements = configurationParser.parse(confObj._1);

    // Assert stuff
    assertTrue(elements.size() == 26);

    // Check ConfigurationElement fields and order
    for (int i = 0; i < elements.size(); i++) {
      assertTrue(elements.get(i).getElementProperties().equals(confObj._2.get(i)));
      switch (i) {
        case 0:
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
          assertTrue(elements.get(i).getEntity().equals(Entity.CONTROLTAG));
          assertTrue(elements.get(i).getAction().equals(Action.CREATE));
          break;
        case 8:
          assertTrue(elements.get(i).getEntity().equals(Entity.PROCESS));
          assertTrue(elements.get(i).getAction().equals(Action.CREATE));
          break;
        case 9:
          assertTrue(elements.get(i).getEntity().equals(Entity.EQUIPMENT));
          assertTrue(elements.get(i).getAction().equals(Action.CREATE));
          break;
        case 10:
          assertTrue(elements.get(i).getEntity().equals(Entity.SUBEQUIPMENT));
          assertTrue(elements.get(i).getAction().equals(Action.CREATE));
          break;
        case 11:
        case 12:
          assertTrue(elements.get(i).getEntity().equals(Entity.DATATAG));
          assertTrue(elements.get(i).getAction().equals(Action.CREATE));
          break;
        case 13:
          assertTrue(elements.get(i).getEntity().equals(Entity.RULETAG));
          assertTrue(elements.get(i).getAction().equals(Action.CREATE));
          break;
        case 14:
        case 15:
        case 16:
        case 17:
        case 18:
        case 19:
        case 20:
        case 21:
        case 22:
        case 23:
        case 24:
          assertTrue(elements.get(i).getEntity().equals(Entity.ALARM));
          assertTrue(elements.get(i).getAction().equals(Action.CREATE));
          break;
        case 25:
          assertTrue(elements.get(i).getEntity().equals(Entity.COMMANDTAG));
          assertTrue(elements.get(i).getAction().equals(Action.CREATE));
      }
    }

    // Verify mock methods were called
    EasyMock.verify(ruleTagCache, processCache, equipmentCache, subEquipmentCache, alarmCache, dataTagCache, aliveTagCache, commFaultTagCache, statusTagCache, commandtagCache);
  }
}
