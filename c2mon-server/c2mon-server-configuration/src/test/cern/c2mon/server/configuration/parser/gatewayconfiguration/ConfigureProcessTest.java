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
import cern.c2mon.server.cache.loading.ProcessDAO;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.configuration.parser.ConfigurationParser;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.server.configuration.parser.util.ConfigurationUtil;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.process.Process;
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
public class ConfigureProcessTest {

  @Autowired
  ConfigurationParser parser;

  @Autowired
  ProcessCache processCache;

  @Autowired
  SequenceDAO sequenceDAO;

  @Autowired
  ProcessDAO processDAO;

  @Autowired
  ControlTagCache controlTagCache;

  @Autowired
  AliveTimerCache aliveTagCache;

  @Rule
  public ExpectedException tagException = ExpectedException.none();


  @Before
  public void resetMocks() {
    EasyMock.reset(processDAO, processCache, sequenceDAO, controlTagCache, aliveTagCache);
  }

  @Test
  public void createProcess() {
    // setup Configuration:
    Process process = Process.create("myProcess").build();

    List<Process> processList = new ArrayList<>();
    processList.add(process);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(processList);

    // setUp Mocks:
    EasyMock.expect(sequenceDAO.getNextProcessId()).andReturn(20L);
    EasyMock.expect(processCache.hasKey(20L)).andReturn(false);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(30L);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(31L);

    EasyMock.expect(controlTagCache.hasKey(31L)).andReturn(false).times(2);
    EasyMock.expect(aliveTagCache.hasKey(30L)).andReturn(false).times(2);

    EasyMock.replay(processCache, sequenceDAO, aliveTagCache, controlTagCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 3);
    assertEquals((long) parsed.get(0).getEntityId(), 31L);
    assertEquals((long) parsed.get(1).getEntityId(), 30L);
    assertEquals((long) parsed.get(2).getEntityId(), 20L);
    assertEquals(parsed.get(2).getAction(), ConfigConstants.Action.CREATE);
    assertEquals(parsed.get(2).getElementProperties().size(), 7);
    assertEquals(parsed.get(2).getElementProperties().get("aliveInterval"), "10000");
    assertEquals(parsed.get(2).getElementProperties().get("name"), "myProcess");
    assertEquals(parsed.get(2).getElementProperties().get("maxMessageDelay"), "1000");
    assertEquals(parsed.get(2).getElementProperties().get("description"), "No description specified");
    assertEquals(parsed.get(2).getElementProperties().get("stateTagId"), "31");
    assertEquals(parsed.get(2).getElementProperties().get("aliveTagId"), "30");
    assertEquals(parsed.get(2).getElementProperties().get("maxMessageSize"), "100");

    EasyMock.verify(processCache, sequenceDAO, aliveTagCache, controlTagCache);
  }

  @Test
  public void createProcessWithId() {
    // setup Configuration:
    Process process = Process.create("myProcess").id(21L).build();

    List<Process> processList = new ArrayList<>();
    processList.add(process);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(processList);

    // setUp Mocks:
    EasyMock.expect(processCache.hasKey(21L)).andReturn(false);

    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(30L);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(31L);

    EasyMock.expect(controlTagCache.hasKey(31L)).andReturn(false).times(2);
    EasyMock.expect(aliveTagCache.hasKey(30L)).andReturn(false).times(2);

    EasyMock.replay(processCache, sequenceDAO, controlTagCache, aliveTagCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 3);
    assertEquals((long) parsed.get(0).getEntityId(), 31L);
    assertEquals((long) parsed.get(1).getEntityId(), 30L);
    assertEquals((long) parsed.get(2).getEntityId(), 21L);
    assertEquals(parsed.get(2).getElementProperties().size(), 7);
    assertEquals(parsed.get(2).getElementProperties().get("aliveInterval"), "10000");
    assertEquals(parsed.get(2).getElementProperties().get("name"), "myProcess");
    assertEquals(parsed.get(2).getElementProperties().get("maxMessageDelay"), "1000");
    assertEquals(parsed.get(2).getElementProperties().get("description"), "No description specified");
    assertEquals(parsed.get(2).getElementProperties().get("stateTagId"), "31");
    assertEquals(parsed.get(2).getElementProperties().get("aliveTagId"), "30");
    assertEquals(parsed.get(2).getElementProperties().get("maxMessageSize"), "100");

    EasyMock.verify(processCache, sequenceDAO, controlTagCache, aliveTagCache);
  }

  @Test
  public void createExistingProcess() {
    // Setup Exception
    tagException.expect(IllegalArgumentException.class);
    tagException.expectMessage("Error while parsing a 'create' Configuration: Id 21 of the class Process already known to the server");

    Process process = Process.create("myProcess").id(21L).build();

    List<Process> processList = new ArrayList<>();
    processList.add(process);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(processList);

    // setUp Mocks:
    EasyMock.expect(processCache.hasKey(21L)).andReturn(true);

    // run test
    EasyMock.replay(processCache, sequenceDAO);
    parser.parse(config);
    EasyMock.verify(processCache, sequenceDAO);
  }

  @Test
  public void updateProcessWithName() {
    // setup Configuration:
    Process process = Process.update("myProcess").description("The description").build();

    List<Process> processUpdateList = new ArrayList<>();
    processUpdateList.add(process);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(processUpdateList);

    // setUp Mocks:
    EasyMock.expect(processDAO.getIdByName("myProcess")).andReturn(20L);
    EasyMock.expect(processCache.hasKey(20L)).andReturn(true);

    EasyMock.replay(processDAO, processCache, sequenceDAO);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.PROCESS);
    assertEquals((long) parsed.get(0).getEntityId(), 20L);
    assertEquals(parsed.get(0).getElementProperties().get("description"), "The description");

    EasyMock.verify(processDAO, processCache, sequenceDAO);
  }

  @Test
  public void updateProcessWithId() {
    // setup Configuration:
    Process process = Process.update(20L).description("The description").build();

    List<Process> processUpdateList = new ArrayList<>();
    processUpdateList.add(process);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(processUpdateList);

    // setUp Mocks:
    EasyMock.expect(processCache.hasKey(20L)).andReturn(true);

    EasyMock.replay(processCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.PROCESS);
    assertEquals((long) parsed.get(0).getEntityId(), 20L);
    assertEquals(parsed.get(0).getElementProperties().get("description"), "The description");

    EasyMock.verify(processCache);
  }

  @Test
  public void updateNotExistingProcess() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Updating of Process (id = 20) failed: The object is unknown to the sever.");

    // setup Configuration:
    Process process = Process.update(20L).description("The description").build();

    List<Process> processUpdateList = new ArrayList<>();
    processUpdateList.add(process);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(processUpdateList);

    // setUp Mocks:
    EasyMock.expect(processCache.hasKey(20L)).andReturn(false);

    // run test
    EasyMock.replay(processCache);
    parser.parse(config);
    EasyMock.verify(processCache);
  }

  @Test
  public void deleteProcess() {
    // setup Configuration:
    Process process = Process.builder().id(20L).deleted(true).build();

    List<Process> processUpdateList = new ArrayList<>();
    processUpdateList.add(process);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(processUpdateList);

    // setUp Mocks:
    EasyMock.expect(processCache.hasKey(20L)).andReturn(true);

    EasyMock.replay(processCache);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.REMOVE);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.PROCESS);
    assertTrue(parsed.get(0).getElementProperties().isEmpty());

    EasyMock.verify(processCache);
  }

  @Test
  public void deleteProcessByName() {
    // setup Configuration:
    Process process = Process.builder().name("P_TEST").deleted(true).build();

    List<Process> processUpdateList = new ArrayList<>();
    processUpdateList.add(process);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(processUpdateList);

    // setUp Mocks:
    EasyMock.expect(processDAO.getIdByName("P_TEST")).andReturn(20L);
    EasyMock.expect(processCache.hasKey(20L)).andReturn(true);

    EasyMock.replay(processCache, processDAO);


    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.REMOVE);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.PROCESS);
    assertTrue(parsed.get(0).getElementProperties().isEmpty());

    EasyMock.verify(processCache, processDAO);
  }

  @Test
  public void deleteNotExistingProcess() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Deleting of Process (id = 20) failed: The object is unknown to the sever.");

    // setup Configuration:
    Process process = Process.builder().id(20L).deleted(true).build();

    List<Process> processUpdateList = new ArrayList<>();
    processUpdateList.add(process);

    Configuration config = ConfigurationUtil.getConfBuilder().build();
    config.setConfigurationItems(processUpdateList);

    // setUp Mocks:
    EasyMock.expect(processCache.hasKey(20L)).andReturn(false);
    EasyMock.replay(processCache);

    parser.parse(config);


    EasyMock.verify(processCache);
  }

}
