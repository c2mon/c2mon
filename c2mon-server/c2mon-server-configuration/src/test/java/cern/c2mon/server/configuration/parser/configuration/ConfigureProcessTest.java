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
//import cern.c2mon.server.cache.AliveTimerCache;
//import cern.c2mon.server.cache.ControlTagCache;
//import cern.c2mon.server.cache.ProcessCache;
//import cern.c2mon.server.cache.TagFacadeGateway;
//import cern.c2mon.server.cache.loading.ProcessDAO;
//import cern.c2mon.server.cache.loading.SequenceDAO;
//import cern.c2mon.server.configuration.parser.ConfigurationParser;
//import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
//import cern.c2mon.shared.client.configuration.ConfigConstants;
//import cern.c2mon.shared.client.configuration.ConfigurationElement;
//import cern.c2mon.shared.client.configuration.api.Configuration;
//import cern.c2mon.shared.client.configuration.api.process.Process;
//
//import static cern.c2mon.server.configuration.parser.util.ConfigurationProcessUtil.*;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
///**
// * @author Franz Ritter
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = ConfigurationParserTestMocks.class)
//public class ConfigureProcessTest {
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
//  ProcessDAO processDAO;
//
//  @Autowired
//  ControlTagCache controlTagCache;
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
//    EasyMock.reset(processDAO, processCache, sequenceDAO, tagFacadeGateway, controlTagCache);
//  }
//
//  @Test
//  public void createProcess() {
//    // setup Configuration:
//
//    Properties expectedProps = new Properties();
//    Process process = buildCreateBasicProcess(expectedProps);
//
//    List<Process> processList = Arrays.asList(process);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(processList);
//
//    // setUp Mocks:
//    EasyMock.expect(sequenceDAO.getNextProcessId()).andReturn(1L);
//    EasyMock.expect(processDAO.getIdByName("P_TEST")).andReturn(null);
//    EasyMock.expect(processCache.hasKey(1L)).andReturn(false);
//    EasyMock.expect(controlTagCache.get("P_TEST:ALIVE")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(101L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(101L);
//    EasyMock.expect(controlTagCache.get("P_TEST:STATUS")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(100L);
//
//    EasyMock.replay(processCache, sequenceDAO, tagFacadeGateway, controlTagCache);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals(parsed.size(), 3);
//
//    assertEquals((long) parsed.get(0).getEntityId(), 101L);
//    assertTrue(parsed.get(0).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(0).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(0).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(1).getEntityId(), 100L);
//    assertTrue(parsed.get(1).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(1).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(1).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(2).getEntityId(), 1L);
//    assertTrue(parsed.get(2).getEntity().equals(ConfigConstants.Entity.PROCESS));
//    assertTrue(parsed.get(2).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(2).getElementProperties(), expectedProps);
//
//    EasyMock.verify(processCache, sequenceDAO, tagFacadeGateway, controlTagCache);
//  }
//
//  @Test
//  public void createProcessWithAllFields() {
//    Properties expectedProps = new Properties();
//    Process process = buildCreateAllFieldsProcess(1L, expectedProps);
//
//    List<Process> processList = Arrays.asList(process);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(processList);
//
//    // setUp Mocks:
//    EasyMock.expect(processDAO.getIdByName("P_TEST1")).andReturn(null);
//    EasyMock.expect(processCache.hasKey(1L)).andReturn(false);
//    EasyMock.expect(controlTagCache.get("P_TEST1:ALIVE")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(101L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(101L);
//    EasyMock.expect(controlTagCache.get("P_TEST1:STATUS")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(100L);
//
//    EasyMock.replay(processCache, sequenceDAO, tagFacadeGateway, controlTagCache);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals(parsed.size(), 3);
//
//    assertEquals((long) parsed.get(0).getEntityId(), 101L);
//    assertTrue(parsed.get(0).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(0).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(0).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(1).getEntityId(), 100L);
//    assertTrue(parsed.get(1).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(1).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(1).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(2).getEntityId(), 1L);
//    assertTrue(parsed.get(2).getEntity().equals(ConfigConstants.Entity.PROCESS));
//    assertTrue(parsed.get(2).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(2).getElementProperties(), expectedProps);
//
//    EasyMock.verify(processCache, sequenceDAO, tagFacadeGateway, controlTagCache);
//  }
//
//  @Test
//  public void createMultipleProcessWithAllFields() {
//    Properties expectedProps1 = new Properties();
//    Properties expectedProps2 = new Properties();
//    Properties expectedProps3 = new Properties();
//
//    Process process1 = buildCreateAllFieldsProcess(1L, expectedProps1);
//    Process process2 = buildCreateAllFieldsProcess(2L, expectedProps2);
//    Process process3 = buildCreateAllFieldsProcess(3L, expectedProps3);
//
//    List<Process> processList = Arrays.asList(process1, process2, process3);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(processList);
//
//    // setUp Mocks:
//    EasyMock.expect(processDAO.getIdByName("P_TEST1")).andReturn(null);
//    EasyMock.expect(processCache.hasKey(1L)).andReturn(false);
//    EasyMock.expect(controlTagCache.get("P_TEST1:ALIVE")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(101L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(101L);
//    EasyMock.expect(controlTagCache.get("P_TEST1:STATUS")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(100L);
//
//    EasyMock.expect(processDAO.getIdByName("P_TEST2")).andReturn(null);
//    EasyMock.expect(processCache.hasKey(2L)).andReturn(false);
//    EasyMock.expect(controlTagCache.get("P_TEST2:ALIVE")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(101L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(101L);
//    EasyMock.expect(controlTagCache.get("P_TEST2:STATUS")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(100L);
//
//    EasyMock.expect(processDAO.getIdByName("P_TEST3")).andReturn(null);
//    EasyMock.expect(processCache.hasKey(3L)).andReturn(false);
//    EasyMock.expect(controlTagCache.get("P_TEST3:ALIVE")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(101L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(101L);
//    EasyMock.expect(controlTagCache.get("P_TEST3:STATUS")).andReturn(null);
//    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(false);
//    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(100L);
//
//    EasyMock.replay(processCache, sequenceDAO, tagFacadeGateway,controlTagCache, processDAO);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals(parsed.size(), 9);
//
//    assertEquals((long) parsed.get(0).getEntityId(), 101L);
//    assertTrue(parsed.get(0).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(0).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(0).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(1).getEntityId(), 100L);
//    assertTrue(parsed.get(1).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(1).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(1).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(2).getEntityId(), 1L);
//    assertTrue(parsed.get(2).getEntity().equals(ConfigConstants.Entity.PROCESS));
//    assertTrue(parsed.get(2).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(2).getElementProperties(), expectedProps1);
//
//    assertEquals((long) parsed.get(3).getEntityId(), 101L);
//    assertTrue(parsed.get(3).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(3).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(3).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(4).getEntityId(), 100L);
//    assertTrue(parsed.get(4).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(4).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(4).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(5).getEntityId(), 2L);
//    assertTrue(parsed.get(5).getEntity().equals(ConfigConstants.Entity.PROCESS));
//    assertTrue(parsed.get(5).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(5).getElementProperties(), expectedProps2);
//
//    assertEquals((long) parsed.get(6).getEntityId(), 101L);
//    assertTrue(parsed.get(6).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(6).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(6).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(7).getEntityId(), 100L);
//    assertTrue(parsed.get(7).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
//    assertTrue(parsed.get(7).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(7).getElementProperties().size(), 6);
//
//    assertEquals((long) parsed.get(8).getEntityId(), 3L);
//    assertTrue(parsed.get(8).getEntity().equals(ConfigConstants.Entity.PROCESS));
//    assertTrue(parsed.get(8).getAction().equals(ConfigConstants.Action.CREATE));
//    assertEquals(parsed.get(8).getElementProperties(), expectedProps3);
//
//    EasyMock.verify(processCache, sequenceDAO, tagFacadeGateway, controlTagCache, processDAO);
//  }
//
//  @Test
//  public void createProcessWithNoFields() {
//    // Setup Exception
//    tagException.expect(IllegalArgumentException.class);
//    tagException.expectMessage("Process name is required!");
//
//    // Setup Configuration Instance
//    Process process = Process.create(null).build();
//
//    List<Process> processList = Arrays.asList(process);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(processList);
//
//    // Setup Mock
//    EasyMock.expect(sequenceDAO.getNextProcessId()).andReturn(1L);
//    EasyMock.expect(processCache.hasKey(1L)).andReturn(false);
//
//    // Switch to replay mode
//    EasyMock.replay(sequenceDAO, processCache);
//
//    // Run the code to be tested
//    parser.parse(config);
//
//    // Verify mock methods were called
//    EasyMock.verify(sequenceDAO, processCache);
//  }
//
//  @Test
//  public void createExistingProcess() {
//    // Setup Exception
//    tagException.expect(ConfigurationParseException.class);
//
//    Process process = Process.create("P_TEST").id(1L).build();
//
//    List<Process> processList = Arrays.asList(process);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(processList);
//
//    // setUp Mocks:
//    EasyMock.expect(processDAO.getIdByName("P_TEST")).andReturn(1l);
//
//    // run test
//    EasyMock.replay(processDAO);
//    parser.parse(config);
//    EasyMock.verify(processDAO);
//  }
//
//  @Test
//  public void updateProcessWithName() {
//    // setup Configuration:
//    Properties expectedProps = new Properties();
//    Process process = buildUpdateProcessWithSomeFields("myProcess", expectedProps);
//
//    List<Process> processUpdateList = Arrays.asList(process);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(processUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(processDAO.getIdByName("myProcess")).andReturn(1L);
//    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
//
//    EasyMock.replay(processCache, processDAO);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals((long) parsed.get(0).getEntityId(), 1L);
//    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.PROCESS);
//    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
//    assertEquals(parsed.get(0).getElementProperties(), expectedProps);
//
//    EasyMock.verify(processCache, processDAO);
//  }
//
//  @Test
//  public void updateProcessWithId() {
//    // setup Configuration:
//    Properties expectedProps = new Properties();
//    Process process = buildUpdateProcessWithSomeFields(1L, expectedProps);
//
//    List<Process> processUpdateList = Arrays.asList(process);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(processUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
//
//    EasyMock.replay(processCache);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals((long) parsed.get(0).getEntityId(), 1L);
//    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.PROCESS);
//    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
//    assertEquals(parsed.get(0).getElementProperties(), expectedProps);
//
//    EasyMock.verify(processCache);
//  }
//
//  @Test
//  public void updateProcessWithAllFields() {
//    // setup Configuration:
//    Properties expectedProps = new Properties();
//    Process process = buildUpdateProcessWithAllFields(1L, expectedProps);
//
//    List<Process> processUpdateList = Arrays.asList(process);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(processUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
//
//    EasyMock.replay(processCache);
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals((long) parsed.get(0).getEntityId(), 1L);
//    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.PROCESS);
//    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
//    assertEquals(parsed.get(0).getElementProperties(), expectedProps);
//
//    EasyMock.verify(processCache);
//  }
//
//  @Test
//  public void updateNonExistentProcess() {
//    // setup Configuration:
//    Process process = Process.update(1L).description("The description").build();
//
//    List<Process> processUpdateList = Arrays.asList(process);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(processUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(processCache.hasKey(1L)).andReturn(false);
//
//    // run test
//    EasyMock.replay(processCache);
//
//    List<ConfigurationElement> result = parser.parse(config);
//
//    assertEquals(1, result.size());
//    assertEquals(ConfigConstants.Entity.MISSING, result.get(0).getEntity());
//    assertEquals(ConfigConstants.Status.WARNING, result.get(0).getStatus());
//
//    EasyMock.verify(processCache);
//  }
//
//  @Test
//  public void deleteProcess() {
//    // setup Configuration:
//    Process process = buildDeleteProcess(1L);
//
//    List<Process> processUpdateList = Arrays.asList(process);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(processUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(processCache.hasKey(1L)).andReturn(true);
//
//    EasyMock.replay(processCache);
//
//
//    List<ConfigurationElement> parsed = parser.parse(config);
//
//    assertEquals(parsed.size(), 1);
//    assertEquals((long) parsed.get(0).getEntityId(), 1L);
//    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.REMOVE);
//    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.PROCESS);
//    assertTrue(parsed.get(0).getElementProperties().isEmpty());
//
//    EasyMock.verify(processCache);
//  }
//
//  @Test
//  public void deleteNonExistentProcess() {
//    // setup Configuration:
//    Process process = buildDeleteProcess(1L);
//
//    List<Process> processUpdateList = Arrays.asList(process);
//
//    Configuration config = new Configuration(1L);
//    config.setEntities(processUpdateList);
//
//    // setUp Mocks:
//    EasyMock.expect(processCache.hasKey(1L)).andReturn(false);
//    EasyMock.replay(processCache);
//
//    assertEquals(0, parser.parse(config).size());
//
//    EasyMock.verify(processCache);
//  }
//}
