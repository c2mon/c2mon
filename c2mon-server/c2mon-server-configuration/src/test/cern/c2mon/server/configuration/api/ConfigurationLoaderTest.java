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
package cern.c2mon.server.configuration.api;

import cern.c2mon.server.cache.*;
import cern.c2mon.server.cache.dbaccess.*;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alive.AliveTimerCacheObject;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.common.commfault.CommFaultTagCacheObject;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.server.configuration.ConfigurationLoader;
import cern.c2mon.server.configuration.api.util.CacheObjectFactoryWithProperties;
import cern.c2mon.server.configuration.parser.util.ConfigurationUtil;
import cern.c2mon.server.configuration.parser.util.Pair;
import cern.c2mon.server.configuraton.helper.ObjectEqualityComparison;
import cern.c2mon.server.daqcommunication.out.ProcessCommunicationManager;
import cern.c2mon.server.test.TestDataInserter;
import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.CommandTag;
import cern.c2mon.shared.client.configuration.api.tag.AliveTag;
import cern.c2mon.shared.client.configuration.api.tag.CommFaultTag;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;
import cern.c2mon.shared.client.configuration.api.tag.RuleTag;
import cern.c2mon.shared.common.NoSimpleValueParseException;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.address.HardwareAddressFactory;
import cern.c2mon.shared.common.metadata.Metadata;
import cern.c2mon.shared.common.type.TypeConverter;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ConfigurationChangeEventReport;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static cern.c2mon.server.configuration.parser.util.ConfigurationAlarmUtil.buildDeleteAlarm;
import static cern.c2mon.server.configuration.parser.util.ConfigurationAlarmUtil.builderAlarmUpdate;
import static cern.c2mon.server.configuration.parser.util.ConfigurationAlarmUtil.builderAlarmWithAllFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationAliveTagUtil.builderAliveTagUpdate;
import static cern.c2mon.server.configuration.parser.util.ConfigurationAliveTagUtil.builderAliveTagWithAllFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationAliveTagUtil.builderAliveTagWithPrimFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationCommFaultTagUtil.builderCommFaultTagUpdate;
import static cern.c2mon.server.configuration.parser.util.ConfigurationCommFaultTagUtil.builderCommFaultTagWithAllFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationCommFaultTagUtil.builderCommFaultTagWithPrimFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationCommandTagUtil.buildDeleteCommandTag;
import static cern.c2mon.server.configuration.parser.util.ConfigurationCommandTagUtil.builderCommandTagUpdate;
import static cern.c2mon.server.configuration.parser.util.ConfigurationCommandTagUtil.builderCommandTagWithAllFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationDataTagUtil.buildDeleteDataTag;
import static cern.c2mon.server.configuration.parser.util.ConfigurationDataTagUtil.builderDataTagUpdate;
import static cern.c2mon.server.configuration.parser.util.ConfigurationDataTagUtil.builderDataTagWithAllFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationEquipmentUtil.buildDeleteEquipment;
import static cern.c2mon.server.configuration.parser.util.ConfigurationEquipmentUtil.builderEquipmentUpdate;
import static cern.c2mon.server.configuration.parser.util.ConfigurationEquipmentUtil.builderEquipmentWithAllFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationProcessUtil.*;
import static cern.c2mon.server.configuration.parser.util.ConfigurationRuleTagUtil.buildDeleteRuleTag;
import static cern.c2mon.server.configuration.parser.util.ConfigurationRuleTagUtil.builderRuleTagUpdate;
import static cern.c2mon.server.configuration.parser.util.ConfigurationRuleTagUtil.builderRuleTagWithAllFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationStatusTagUtil.builderStatusTagUpdate;
import static cern.c2mon.server.configuration.parser.util.ConfigurationStatusTagUtil.builderStatusTagWithPrimFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationSubEquipmentUtil.buildDeleteSubEquipment;
import static cern.c2mon.server.configuration.parser.util.ConfigurationSubEquipmentUtil.builderSubEquipmentUpdate;
import static cern.c2mon.server.configuration.parser.util.ConfigurationSubEquipmentUtil.builderSubEquipmentWithAllFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationUtil.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * @author Franz Ritter
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/configuration/config/server-configuration-loader-api-test.xml"})
@DirtiesContext
public class ConfigurationLoaderTest {

  @Autowired
  CacheObjectFactoryWithProperties cacheObjectFactory;

  @Autowired
  private ProcessCommunicationManager mockManager;

  @Autowired
  private ConfigurationLoader configurationLoader;

  @Autowired
  private DataTagCache dataTagCache;

  @Autowired
  private DataTagMapper dataTagMapper;

  @Autowired
  private ControlTagCache controlTagCache;

  @Autowired
  private ControlTagMapper controlTagMapper;

  @Autowired
  private CommandTagMapper commandTagMapper;

  @Autowired
  private CommandTagCache commandTagCache;

  @Autowired
  private RuleTagCache ruleTagCache;

  @Autowired
  private RuleTagMapper ruleTagMapper;

  @Autowired
  private EquipmentCache equipmentCache;

  @Autowired
  private EquipmentMapper equipmentMapper;

  @Autowired
  private SubEquipmentCache subEquipmentCache;

  @Autowired
  private SubEquipmentMapper subEquipmentMapper;

  @Autowired
  private ProcessCache processCache;

  @Autowired
  private ProcessMapper processMapper;

  @Autowired
  private AliveTimerCache aliveTimerCache;

  @Autowired
  private CommFaultTagCache commFaultTagCache;

  @Autowired
  private AlarmCache alarmCache;

  @Autowired
  private AlarmMapper alarmMapper;

  @Autowired
  private ProcessFacade processFacade;

  @Autowired
  private TestDataInserter testDataInserter;

  @Value("${c2mon.server.daqcommunication.jms.queue.trunk}")
  private String jmsDaqQueueTrunk;

  @Value("${c2mon.jms.tag.publication.topic}")
  private String tagPublicationTrunk = "c2mon.client.tag.default";

  @Before
  public void beforeTest() throws IOException {

    // reset mock
    reset(mockManager);
  }

  @After
  public void afterTest() throws IOException {
    testDataInserter.removeTestData();
  }

  @Test
  public void testCreateProcessStatusAliveTag() {
    replay(mockManager);

    // Build ConfigurationObject with expected result values
    Pair<StatusTag.StatusTagBuilder, Properties> statusTag = builderStatusTagWithPrimFields(11L, "process", 1L); // 24
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTag = builderAliveTagWithPrimFields(12l, "process", 1L); // 6
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Process build = process._1.aliveTag(aliveTag._1.build()).statusTag(statusTag._1.build()).build();
    Configuration configuration = ConfigurationUtil.getConfBuilderProcess(build);


    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result and caches
    assertTrue(report.getStatus() == ConfigConstants.Status.RESTART);
    assertTrue(processCache.hasKey(1L));
    assertNotNull(processMapper.getItem(1L));
    assertTrue(controlTagCache.hasKey(11L));
    assertNotNull(controlTagMapper.getItem(11L));
    assertTrue(controlTagCache.hasKey(12L));
    assertNotNull(controlTagMapper.getItem(12L));
    assertTrue(aliveTimerCache.hasKey(12L));

    // Check Process in the cache
    ProcessCacheObject cacheObjectProcess = (ProcessCacheObject) processCache.get(1L);
    ProcessCacheObject expectedObjectProcess = cacheObjectFactory.buildProcessCacheObject(1L, process._2);

    ObjectEqualityComparison.assertProcessEquals(expectedObjectProcess, cacheObjectProcess);

    // Check statusTag in the cache
    ControlTagCacheObject cacheObjectStatus = (ControlTagCacheObject) controlTagCache.get(11L);
    ControlTagCacheObject expectedObjectStatus = cacheObjectFactory.buildControlTagCacheObject(11L, statusTag._2, 1L, null, null);

    ObjectEqualityComparison.assertDataTagConfigEquals(expectedObjectStatus, cacheObjectStatus);

    // Check aliveTag in the cache
    AliveTimerCacheObject cacheObjectAlive = (AliveTimerCacheObject) aliveTimerCache.get(12L);
    AliveTimerCacheObject expectedObjectAlive = new AliveTimerCacheObject(12L, 1L, process._2.getProperty("name"), 11L, "PROC", Integer.parseInt(process._2.getProperty("aliveInterval")));
    ControlTagCacheObject cacheObjectAliveControlCache = (ControlTagCacheObject) controlTagCache.get(12L);
    ControlTagCacheObject expectedObjectAliveControlCache = cacheObjectFactory.buildControlTagCacheObject(12L, aliveTag._2, 1L, null, null);

    ObjectEqualityComparison.assertAliveTimerValuesEquals(expectedObjectAlive, cacheObjectAlive);
    ObjectEqualityComparison.assertDataTagConfigEquals(expectedObjectAliveControlCache, cacheObjectAliveControlCache);

    verify(mockManager);

    // remove the process from the server
    Configuration remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(controlTagCache.hasKey(12L));
    assertNull(controlTagMapper.getItem(12L));
    assertFalse(aliveTimerCache.hasKey(12L));

    verify(mockManager);
  }

  @Test
  public void testUpdateProcess() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    replay(mockManager);

    // SETUP:
    // First add a process to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L); // 24
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L); // 6
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // Build configuration to add the test equipment
    Pair<Process.ProcessBuilder, Properties> processUpdate = builderProcessUpdate(1L);
    Configuration configuration = ConfigurationUtil.getConfBuilderProcess(processUpdate._1.build());

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.RESTART, report.getStatus());
    assertTrue(report.getElementReports().size() == 1);
//
    // get cacheObject from the cache and compare to the an expected cacheObject
    ProcessCacheObject cacheObjectProcess = (ProcessCacheObject) processCache.get(1L);
    ProcessCacheObject expectedCacheObjectProcess = cacheObjectFactory.buildProcessCacheObject(1L, process._2);
    expectedCacheObjectProcess.setDescription(processUpdate._2.getProperty("description"));
    expectedCacheObjectProcess.setMaxMessageSize(Integer.parseInt(processUpdate._2.getProperty("maxMessageSize")));
    expectedCacheObjectProcess.setJmsDaqCommandQueue(cacheObjectProcess.getJmsDaqCommandQueue());

    ObjectEqualityComparison.assertProcessEquals(expectedCacheObjectProcess, cacheObjectProcess);

    verify(mockManager);

    // remove the process and equipments from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    Configuration remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    verify(mockManager);
  }

  @Test
  public void testCreateEquipmentStatusAliveTag() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(1L), isA(List.class))).andAnswer(new IAnswer<ConfigurationChangeEventReport>() {

      @Override
      public ConfigurationChangeEventReport answer() throws Throwable {
        List<Change> changeList = (List<Change>) EasyMock.getCurrentArguments()[1];
        ConfigurationChangeEventReport report = new ConfigurationChangeEventReport();
        for (Change change : changeList) {
          ChangeReport changeReport = new ChangeReport(change);
          changeReport.setState(ChangeReport.CHANGE_STATE.SUCCESS);
          report.appendChangeReport(changeReport);
        }
        return report;
      }
    });

    replay(mockManager);

    // SETUP:First add a process to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L); // 24
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L); // 6
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build()).build();
    Configuration instert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(instert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:Build configuration to add the test equipment
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L); // 24
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L); // 24
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L); // 6
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);

    Equipment buildE = equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).build();
    Configuration configuration = ConfigurationUtil.getConfBuilderEquipment(buildE);

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check Report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getElementReports().size() == 4);

    // check Equipment in the cache
    EquipmentCacheObject cacheObject = (EquipmentCacheObject) equipmentCache.get(2L);
    EquipmentCacheObject expectedObject = cacheObjectFactory.buildEquipmentCacheObject(2L, equipment._2);

    ObjectEqualityComparison.assertEquipmentEquals(expectedObject, cacheObject);

    // Check StatusTag in the cache
    ControlTagCacheObject cacheObjectStatus = (ControlTagCacheObject) controlTagCache.get(21L);
    ControlTagCacheObject expectedObjectStatus = cacheObjectFactory.buildControlTagCacheObject(21L, statusTagE._2, 1L, 2L, null);

    ObjectEqualityComparison.assertDataTagConfigEquals(expectedObjectStatus, cacheObjectStatus);

    // Check commFaultTag in the cache
    CommFaultTagCacheObject cacheObjectCommFault = (CommFaultTagCacheObject) commFaultTagCache.get(22L);
    CommFaultTagCacheObject expectedCacheObjectCommFault = new CommFaultTagCacheObject(22L, 2L, equipment._2.getProperty("name"),23L, 21L);
    ControlTagCacheObject cacheObjectCommFaultControl = (ControlTagCacheObject) controlTagCache.get(22L);
    ControlTagCacheObject expectedObjectCommFaultControl = cacheObjectFactory.buildControlTagCacheObject(22L, commFaultTagE._2, 1L, 2L, null);

    ObjectEqualityComparison.assertCommFaultTagValuesEquals(expectedCacheObjectCommFault, cacheObjectCommFault);
    ObjectEqualityComparison.assertDataTagConfigEquals(expectedObjectCommFaultControl, cacheObjectCommFaultControl);

    // Check aliveTag in the cache
    AliveTimerCacheObject cacheObjectAlive = (AliveTimerCacheObject) aliveTimerCache.get(23L);
    AliveTimerCacheObject expectedObjectAlive = new AliveTimerCacheObject(23L, 2L, equipment._2.getProperty("name"), 21L, "EQ", Integer.parseInt(equipment._2.getProperty("aliveInterval")));
    ControlTagCacheObject cacheObjectAliveControlCache = (ControlTagCacheObject) controlTagCache.get(23L);
    ControlTagCacheObject expectedObjectAliveControlCache = cacheObjectFactory.buildControlTagCacheObject(23L, aliveTagE._2, 1L, 2L, null);

    ObjectEqualityComparison.assertAliveTimerValuesEquals(expectedObjectAlive, cacheObjectAlive);
    ObjectEqualityComparison.assertDataTagConfigEquals(expectedObjectAliveControlCache, cacheObjectAliveControlCache);

    // Check if all caches are updated
    cern.c2mon.server.common.process.Process processObj = processCache.get(expectedObject.getProcessId());
    assertTrue(processObj.getEquipmentIds().contains(expectedObject.getId()));
    assertNotNull(commFaultTagCache.get(expectedObject.getCommFaultTagId()));
    assertEquals(expectedObject.getId(), commFaultTagCache.get(cacheObject.getCommFaultTagId()).getEquipmentId());
    assertNotNull(equipmentMapper.getItem(2L));
    assertNotNull(controlTagMapper.getItem(21L));
    assertNotNull(controlTagMapper.getItem(23L));

    verify(mockManager);

    // remove the process and equipments from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    Configuration remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(controlTagCache.hasKey(23L));
    assertNull(controlTagMapper.getItem(23L));
    assertFalse(aliveTimerCache.hasKey(23L));

    verify(mockManager);
  }

  @Test
  public void testUpdateEquipment() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(1L), isA(List.class))).andAnswer(new IAnswer<ConfigurationChangeEventReport>() {

      @Override
      public ConfigurationChangeEventReport answer() throws Throwable {
        List<Change> changeList = (List<Change>) EasyMock.getCurrentArguments()[1];
        ConfigurationChangeEventReport report = new ConfigurationChangeEventReport();
        for (Change change : changeList) {
          ChangeReport changeReport = new ChangeReport(change);
          changeReport.setState(ChangeReport.CHANGE_STATE.SUCCESS);
          report.appendChangeReport(changeReport);
        }
        return report;
      }
    });

    replay(mockManager);

    // SETUP:
    // First add a process to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L); // 24
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L); // 6
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // Build configuration to add the test equipment
    Pair<Equipment.EquipmentBuilder, Properties> equipmentUpdate = builderEquipmentUpdate(2L);
    Configuration configuration = ConfigurationUtil.getConfBuilderEquipment(equipmentUpdate._1.build());

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertTrue(report.getElementReports().size() == 1);
//
    // get cacheObject from the cache and compare to the an expected cacheObject
    EquipmentCacheObject cacheObjectEquipment = (EquipmentCacheObject) equipmentCache.get(2L);
    EquipmentCacheObject expectedCacheObjectEquipment = cacheObjectFactory.buildEquipmentCacheObject(2L, equipment._2);
    expectedCacheObjectEquipment.setDescription(equipmentUpdate._2.getProperty("description"));
    expectedCacheObjectEquipment.setAddress(equipmentUpdate._2.getProperty("address"));

    ObjectEqualityComparison.assertEquipmentEquals(expectedCacheObjectEquipment, cacheObjectEquipment);

    verify(mockManager);

    // remove the process and equipments from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    Configuration remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(controlTagCache.hasKey(23L));
    assertNull(controlTagMapper.getItem(23L));
    assertFalse(aliveTimerCache.hasKey(23L));

    verify(mockManager);
  }

  @Test
  public void testRemoveEquipment() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(1L), isA(List.class))).andAnswer(new IAnswer<ConfigurationChangeEventReport>() {

      @Override
      public ConfigurationChangeEventReport answer() throws Throwable {
        List<Change> changeList = (List<Change>) EasyMock.getCurrentArguments()[1];
        ConfigurationChangeEventReport report = new ConfigurationChangeEventReport();
        for (Change change : changeList) {
          ChangeReport changeReport = new ChangeReport(change);
          changeReport.setState(ChangeReport.CHANGE_STATE.SUCCESS);
          report.appendChangeReport(changeReport);
        }
        return report;
      }
    });

    replay(mockManager);

    // SETUP:
    // First add a process to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L); // 24
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L); // 6
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // check if the equipment is in the cache
    assertTrue(equipmentCache.hasKey(2L));
    assertNotNull(equipmentMapper.getItem(2L));

    // Build configuration to remove the test Equipment
    Configuration remove = getConfBuilderEquipment(buildDeleteEquipment(2L));
    ConfigurationReport report = configurationLoader.applyConfiguration(remove);

    //check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);
    assertTrue(report.getElementReports().size() == 1);

    // Check if all caches are updated
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));

    verify(mockManager);

    // remove the process and equipments from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(controlTagCache.hasKey(23L));
    assertNull(controlTagMapper.getItem(23L));
    assertFalse(aliveTimerCache.hasKey(23L));

    verify(mockManager);
  }

  @Test
  public void testCreateSubEquipment() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(1L), isA(List.class))).andAnswer(new IAnswer<ConfigurationChangeEventReport>() {

      @Override
      public ConfigurationChangeEventReport answer() throws Throwable {
        List<Change> changeList = (List<Change>) EasyMock.getCurrentArguments()[1];
        ConfigurationChangeEventReport report = new ConfigurationChangeEventReport();
        for (Change change : changeList) {
          ChangeReport changeReport = new ChangeReport(change);
          changeReport.setState(ChangeReport.CHANGE_STATE.SUCCESS);
          report.appendChangeReport(changeReport);
        }
        return report;
      }
    });
    replay(mockManager);

    // SETUP:First add a process and equipment to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:Build configuration to add the test equipment
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagS = builderStatusTagWithPrimFields(31L, "subEquipment", 3L); // 24
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagS = builderCommFaultTagWithPrimFields(32L, "subEquipment", 3L); // 24
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagS = builderAliveTagWithAllFields(33l, "subEquipment", 3L); // 6
    Pair<SubEquipment.SubEquipmentBuilder, Properties> subEquipment = builderSubEquipmentWithAllFields(3L, 2L, 31L, 32L, 33L);

    SubEquipment buildS = subEquipment._1.aliveTag(aliveTagS._1.build()).statusTag(statusTagS._1.build()).commFaultTag(commFaultTagS._1.build()).build();
    Configuration configuration = ConfigurationUtil.getConfBuilderSubEquipment(2L, 1L, buildS);

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getElementReports().size() == 4);

    // get cacheObject from the cache and compare to the an expected cacheObject
    SubEquipmentCacheObject cacheObjectSubEq = (SubEquipmentCacheObject) subEquipmentCache.get(3L);
    SubEquipmentCacheObject expectedObjectSubEq = cacheObjectFactory.buildSubEquipmentCacheObject(3L, subEquipment._2);

    ObjectEqualityComparison.assertSubEquipmentEquals(expectedObjectSubEq, cacheObjectSubEq);

    // Check statusTag in the cache
    ControlTagCacheObject cacheObjectStatus = (ControlTagCacheObject) controlTagCache.get(31L);
    ControlTagCacheObject expectedObjectStatus = cacheObjectFactory.buildControlTagCacheObject(31L, statusTagS._2, 1L, null, 3L);

    ObjectEqualityComparison.assertDataTagConfigEquals(expectedObjectStatus, cacheObjectStatus);

    // Check commFaultTag in the cache
    CommFaultTagCacheObject cacheObjectCommFault = (CommFaultTagCacheObject) commFaultTagCache.get(32L);
    CommFaultTagCacheObject expectedCacheObjectCommFault = new CommFaultTagCacheObject(32L, 3L, subEquipment._2.getProperty("name"),33L, 31L);
    ControlTagCacheObject cacheObjectCommFaultControl = (ControlTagCacheObject) controlTagCache.get(32L);
    ControlTagCacheObject expectedObjectCommFaultControl = cacheObjectFactory.buildControlTagCacheObject(32L, commFaultTagS._2, 1L, null, 3L);

    ObjectEqualityComparison.assertCommFaultTagValuesEquals(expectedCacheObjectCommFault, cacheObjectCommFault);
    ObjectEqualityComparison.assertDataTagConfigEquals(expectedObjectCommFaultControl, cacheObjectCommFaultControl);

    // Check aliveTag in the cache
    AliveTimerCacheObject cacheObjectAlive = (AliveTimerCacheObject) aliveTimerCache.get(33L);
    AliveTimerCacheObject expectedObjectAlive = new AliveTimerCacheObject(33L, 3L, subEquipment._2.getProperty("name"), 31L, "SUBEQ", Integer.parseInt(subEquipment._2.getProperty("aliveInterval")));
    ControlTagCacheObject cacheObjectAliveControlCache = (ControlTagCacheObject) controlTagCache.get(33L);
    ControlTagCacheObject expectedObjectAliveControlCache = cacheObjectFactory.buildControlTagCacheObject(33L, aliveTagS._2, 1L, null, 3L);

    ObjectEqualityComparison.assertAliveTimerValuesEquals(expectedObjectAlive, cacheObjectAlive);
    ObjectEqualityComparison.assertDataTagConfigEquals(expectedObjectAliveControlCache, cacheObjectAliveControlCache);

    // Check if all caches are updated
    cern.c2mon.server.common.equipment.Equipment equip = equipmentCache.get(expectedObjectSubEq.getParentId());
    assertTrue(equip.getSubEquipmentIds().contains(expectedObjectSubEq.getId()));
    assertNotNull(commFaultTagCache.get(expectedObjectSubEq.getCommFaultTagId()));
    assertEquals(expectedObjectSubEq.getId(), commFaultTagCache.get(cacheObjectSubEq.getCommFaultTagId()).getEquipmentId());
    assertNotNull(subEquipmentMapper.getItem(3L));
    assertNotNull(controlTagMapper.getItem(31L));
    assertNotNull(controlTagMapper.getItem(33L));

    verify(mockManager);

    // remove the process and equipments from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    Configuration remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(aliveTimerCache.hasKey(23L));

    assertFalse(equipmentCache.hasKey(3L));
    assertNull(equipmentMapper.getItem(3L));
    assertFalse(controlTagCache.hasKey(31L));
    assertNull(controlTagMapper.getItem(31L));
    assertFalse(commFaultTagCache.hasKey(32L));
    assertFalse(aliveTimerCache.hasKey(33L));

    verify(mockManager);
  }

  @Test
  public void testUpdateSubEquipment() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(1L), isA(List.class))).andAnswer(new IAnswer<ConfigurationChangeEventReport>() {

      @Override
      public ConfigurationChangeEventReport answer() throws Throwable {
        List<Change> changeList = (List<Change>) EasyMock.getCurrentArguments()[1];
        ConfigurationChangeEventReport report = new ConfigurationChangeEventReport();
        for (Change change : changeList) {
          ChangeReport changeReport = new ChangeReport(change);
          changeReport.setState(ChangeReport.CHANGE_STATE.SUCCESS);
          report.appendChangeReport(changeReport);
        }
        return report;
      }
    });
    replay(mockManager);

    // SETUP:
    // First add a process and equipments to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagS = builderStatusTagWithPrimFields(31L, "subEquipment", 3L); // 24
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagS = builderCommFaultTagWithPrimFields(32L, "subEquipment", 3L); // 24
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagS = builderAliveTagWithAllFields(33l, "subEquipment", 3L); // 6
    Pair<SubEquipment.SubEquipmentBuilder, Properties> subEquipment = builderSubEquipmentWithAllFields(3L, 2L, 31L, 32L, 33L);
    SubEquipment buildS = subEquipment._1.aliveTag(aliveTagS._1.build()).statusTag(statusTagS._1.build()).commFaultTag(commFaultTagS._1.build()).build();

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).subEquipment(buildS).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // Build configuration to add the test equipment
    Pair<SubEquipment.SubEquipmentBuilder, Properties> subEquipmentUpdate = builderSubEquipmentUpdate(3L);
    Configuration configuration = ConfigurationUtil.getConfBuilderSubEquipment(2L, 1L, subEquipmentUpdate._1.build());

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertTrue(report.getElementReports().size() == 1);
//
    // get cacheObject from the cache and compare to the an expected cacheObject
    SubEquipmentCacheObject cacheObjectSubEquipment = (SubEquipmentCacheObject) subEquipmentCache.get(3L);
    SubEquipmentCacheObject expectedCacheObjectSubEquipment = cacheObjectFactory.buildSubEquipmentCacheObject(3L, subEquipment._2);
    expectedCacheObjectSubEquipment.setDescription(subEquipmentUpdate._2.getProperty("description"));

    ObjectEqualityComparison.assertSubEquipmentEquals(expectedCacheObjectSubEquipment, cacheObjectSubEquipment);

    verify(mockManager);

    // remove the process and equipments from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    Configuration remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(aliveTimerCache.hasKey(23L));

    assertFalse(subEquipmentCache.hasKey(3L));
    assertNull(subEquipmentMapper.getItem(3L));
    assertFalse(controlTagCache.hasKey(31L));
    assertNull(controlTagMapper.getItem(31L));
    assertFalse(commFaultTagCache.hasKey(32L));
    assertFalse(aliveTimerCache.hasKey(33L));

    verify(mockManager);
  }

  @Test
  public void testRemoveSubEquipment() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(1L), isA(List.class))).andAnswer(new IAnswer<ConfigurationChangeEventReport>() {

      @Override
      public ConfigurationChangeEventReport answer() throws Throwable {
        List<Change> changeList = (List<Change>) EasyMock.getCurrentArguments()[1];
        ConfigurationChangeEventReport report = new ConfigurationChangeEventReport();
        for (Change change : changeList) {
          ChangeReport changeReport = new ChangeReport(change);
          changeReport.setState(ChangeReport.CHANGE_STATE.SUCCESS);
          report.appendChangeReport(changeReport);
        }
        return report;
      }
    });
    replay(mockManager);

    // SETUP:
    // First add a process and equipments to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagS = builderStatusTagWithPrimFields(31L, "subEquipment", 3L); // 24
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagS = builderCommFaultTagWithPrimFields(32L, "subEquipment", 3L); // 24
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagS = builderAliveTagWithAllFields(33l, "subEquipment", 3L); // 6
    Pair<SubEquipment.SubEquipmentBuilder, Properties> subEquipment = builderSubEquipmentWithAllFields(3L, 2L, 31L, 32L, 33L);
    SubEquipment buildS = subEquipment._1.aliveTag(aliveTagS._1.build()).statusTag(statusTagS._1.build()).commFaultTag(commFaultTagS._1.build()).build();

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).subEquipment(buildS).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // check if the subEquipment is in the cache
    assertTrue(subEquipmentCache.hasKey(3L));
    assertNotNull(subEquipmentMapper.getItem(3L));
    assertTrue(controlTagCache.hasKey(31L));
    assertNotNull(controlTagMapper.getItem(31L));
    assertTrue(commFaultTagCache.hasKey(32L));
    assertTrue(controlTagCache.hasKey(33L));
    assertNotNull(controlTagMapper.getItem(33L));
    assertTrue(aliveTimerCache.hasKey(33L));
    assertTrue(controlTagCache.hasKey(33L));
    assertNotNull(controlTagMapper.getItem(33L));

    // Build configuration to remove the test Equipment
    Configuration remove = getConfBuilderSubEquipment(2L ,1L ,buildDeleteSubEquipment(3L));
    ConfigurationReport report = configurationLoader.applyConfiguration(remove);

    //check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);
    assertTrue(report.getElementReports().size() == 1);

    // Check if all caches are updated
    assertFalse(subEquipmentCache.hasKey(3L));
    assertNull(subEquipmentMapper.getItem(3L));
    assertFalse(controlTagCache.hasKey(31L));
    assertNull(controlTagMapper.getItem(31L));
    assertFalse(commFaultTagCache.hasKey(32L));
    assertFalse(aliveTimerCache.hasKey(33L));

    verify(mockManager);

    // remove the process and equipments from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(aliveTimerCache.hasKey(23L));

    verify(mockManager);
  }


  @Test
  public void testUpdateAliveTag() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(1L), isA(List.class))).andAnswer(new IAnswer<ConfigurationChangeEventReport>() {

      @Override
      public ConfigurationChangeEventReport answer() throws Throwable {
        List<Change> changeList = (List<Change>) EasyMock.getCurrentArguments()[1];
        ConfigurationChangeEventReport report = new ConfigurationChangeEventReport();
        for (Change change : changeList) {
          ChangeReport changeReport = new ChangeReport(change);
          changeReport.setState(ChangeReport.CHANGE_STATE.SUCCESS);
          report.appendChangeReport(changeReport);
        }
        return report;
      }
    });

    replay(mockManager);

    // SETUP:
    // First add a process to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithAllFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L, 21L, 22L, 23L);
    Pair<DataTag.DataTagBuilder, Properties> dataTag = builderDataTagWithAllFields(100L, "equipment", 2L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).dataTag(dataTag._1.build()).build()).build();

    Configuration instert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(instert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // Build configuration to update the test equipment
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagEUpdate = builderAliveTagUpdate(23l); // 6
    Configuration configuration = ConfigurationUtil.getConfBuilderAliveTagEUpdate(2L, 1L, aliveTagEUpdate._1.build());

    ///apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertTrue(report.getElementReports().size() == 1);

    // check aliveTag in the cache
    AliveTimerCacheObject cacheObjectAlive = (AliveTimerCacheObject) aliveTimerCache.get(23L);
    AliveTimerCacheObject expectedObjectAlive = new AliveTimerCacheObject(23L, 2L, equipment._2.getProperty("name"), 21L, "EQ", Integer.parseInt(equipment._2.getProperty("aliveInterval")));
    ControlTagCacheObject cacheObjectAliveControlCache = (ControlTagCacheObject) controlTagCache.get(23L);
    ControlTagCacheObject expectedObjectAliveControlCache = cacheObjectFactory.buildControlTagCacheObject(23L, aliveTagE._2, 1L, 2L, null);
    expectedObjectAliveControlCache.setDescription(aliveTagEUpdate._2.getProperty("description"));

    ObjectEqualityComparison.assertAliveTimerValuesEquals(expectedObjectAlive, cacheObjectAlive);
    ObjectEqualityComparison.assertDataTagConfigEquals(expectedObjectAliveControlCache, cacheObjectAliveControlCache);

    verify(mockManager);

    // remove the process and equipments from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    Configuration remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(controlTagCache.hasKey(23L));
    assertNull(controlTagMapper.getItem(23L));
    assertFalse(aliveTimerCache.hasKey(23L));

    verify(mockManager);
  }


  // TODO run the Test after fixing update-remove controlTag issue
  @Test
  public void testUpdateStatusTag() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    replay(mockManager);

    // SETUP:
    // First add a process to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L, 21L, 22L, 23L);
    Pair<DataTag.DataTagBuilder, Properties> dataTag = builderDataTagWithAllFields(100L, "equipment", 2L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).dataTag(dataTag._1.build()).build()).build();

    Configuration instert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(instert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // Build configuration to update the test equipment
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagEUpdate = builderStatusTagUpdate(21l); // 6
    Configuration configuration = ConfigurationUtil.getConfBuilderStatusTagEUpdate(2L, 1L, statusTagEUpdate._1.build());

    ///apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertTrue(report.getElementReports().size() == 1);

    // check aliveTag in the cache
    ControlTagCacheObject cacheObjectStatusControlCache = (ControlTagCacheObject) controlTagCache.get(21L);
    ControlTagCacheObject expectedObjectStatusControlCache = cacheObjectFactory.buildControlTagCacheObject(21L, statusTagE._2, 1L, 2L, null);
    expectedObjectStatusControlCache.setDescription(statusTagEUpdate._2.getProperty("description"));

    ObjectEqualityComparison.assertDataTagConfigEquals(expectedObjectStatusControlCache, cacheObjectStatusControlCache);

    verify(mockManager);

    // remove the process and equipments from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    Configuration remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(controlTagCache.hasKey(23L));
    assertNull(controlTagMapper.getItem(23L));
    assertFalse(aliveTimerCache.hasKey(23L));

    verify(mockManager);
  }

  // TODO run the Test after fixing update-remove controlTag issue
  @Test
  public void testUpdateCommFaultTag() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(1L), isA(List.class))).andAnswer(new IAnswer<ConfigurationChangeEventReport>() {

      @Override
      public ConfigurationChangeEventReport answer() throws Throwable {
        List<Change> changeList = (List<Change>) EasyMock.getCurrentArguments()[1];
        ConfigurationChangeEventReport report = new ConfigurationChangeEventReport();
        for (Change change : changeList) {
          ChangeReport changeReport = new ChangeReport(change);
          changeReport.setState(ChangeReport.CHANGE_STATE.SUCCESS);
          report.appendChangeReport(changeReport);
        }
        return report;
      }
    });

    replay(mockManager);

    // SETUP:
    // First add a process to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithAllFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L, 21L, 22L, 23L);
    Pair<DataTag.DataTagBuilder, Properties> dataTag = builderDataTagWithAllFields(100L, "equipment", 2L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).dataTag(dataTag._1.build()).build()).build();

    Configuration instert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(instert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // Build configuration to update the test equipment
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagEUpdate = builderCommFaultTagUpdate(22l); // 6
    Configuration configuration = ConfigurationUtil.getConfBuilderCommFaultTagEUpdate(2L, 1L, commFaultTagEUpdate._1.build());

    ///apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertTrue(report.getElementReports().size() == 1);

    // check aliveTag in the cache
    CommFaultTagCacheObject cacheObjectCommFault = (CommFaultTagCacheObject) commFaultTagCache.get(22L);
    CommFaultTagCacheObject expectedCacheObjectCommFault = new CommFaultTagCacheObject(22L, 2L, equipment._2.getProperty("name"),23L, 21L);
    ControlTagCacheObject cacheObjectCommFaultControl = (ControlTagCacheObject) controlTagCache.get(22L);
    ControlTagCacheObject expectedObjectCommFaultControl = cacheObjectFactory.buildControlTagCacheObject(22L, commFaultTagE._2, 1L, 2L, null);
    expectedObjectCommFaultControl.setDescription(commFaultTagEUpdate._2.getProperty("description"));

    ObjectEqualityComparison.assertCommFaultTagValuesEquals(expectedCacheObjectCommFault, cacheObjectCommFault);
    ObjectEqualityComparison.assertDataTagConfigEquals(expectedObjectCommFaultControl, cacheObjectCommFaultControl);

    verify(mockManager);

    // remove the process and equipments from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    Configuration remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(controlTagCache.hasKey(23L));
    assertNull(controlTagMapper.getItem(23L));
    assertFalse(aliveTimerCache.hasKey(23L));

    verify(mockManager);
  }

  @Test
  public void testCreateEquipmentDataTag() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(1L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    // SETUP:
    // First add a process and equipment to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // Build configuration to add the test DataTag

    Pair<DataTag.DataTagBuilder, Properties> dataTag = builderDataTagWithAllFields(100L, "equipment", 2L);
    Configuration configuration = ConfigurationUtil.getConfBuilderDataTagE(2L, 1L, dataTag._1.build());

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertTrue(report.getElementReports().size() == 1);
//
    // get cacheObject from the cache and compare to the an expected cacheObject
    DataTagCacheObject cacheObjectData = (DataTagCacheObject) dataTagCache.get(100L);
    DataTagCacheObject expectedCacheObjectData = cacheObjectFactory.buildDataTagCacheObject(100L, dataTag._2, 1L);

    ObjectEqualityComparison.assertDataTagConfigEquals(expectedCacheObjectData, cacheObjectData);
//    // Check if all caches are updated
    equipmentCache.acquireWriteLockOnKey(cacheObjectData.getEquipmentId());
    cern.c2mon.server.common.equipment.Equipment equip = equipmentCache.get(cacheObjectData.getEquipmentId());
    assertTrue(equipmentCache.get(cacheObjectData.getEquipmentId()).getDataTagIds().contains(100L));
    equipmentCache.releaseWriteLockOnKey(cacheObjectData.getEquipmentId());

    verify(mockManager);

    // remove the process and equipments and dataTag from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    Configuration remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(aliveTimerCache.hasKey(23L));

    assertFalse(equipmentCache.hasKey(3L));
    assertNull(equipmentMapper.getItem(3L));
    assertFalse(controlTagCache.hasKey(31L));
    assertNull(controlTagMapper.getItem(31L));
    assertFalse(commFaultTagCache.hasKey(32L));
    assertFalse(aliveTimerCache.hasKey(33L));
    assertFalse(dataTagCache.hasKey(100L));
    assertNull(dataTagMapper.getItem(100L));

    verify(mockManager);
  }

  @Test
  public void testUpdateEquipmentDataTag() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(1L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    // SETUP:
    // First add a process and equipment to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);
    Pair<DataTag.DataTagBuilder, Properties> dataTag = builderDataTagWithAllFields(100L, "equipment", 2L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).dataTag(dataTag._1.build()).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // Build configuration to update the test DataTag
    Pair<DataTag.DataTagBuilder, Properties> dataTagUpdate = builderDataTagUpdate(100L);
    Configuration configuration = ConfigurationUtil.getConfBuilderDataTagE(2L, 1L, dataTagUpdate._1.build());

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertTrue(report.getElementReports().size() == 1);

    // get cacheObject from the cache and compare to the an expected cacheObject
    DataTagCacheObject cacheObjectData = (DataTagCacheObject) dataTagCache.get(100L);
    DataTagCacheObject expectedCacheObjectData = cacheObjectFactory.buildDataTagCacheObject(100L, dataTag._2, 1L);
    expectedCacheObjectData.setDescription(dataTagUpdate._2.getProperty("description"));
    expectedCacheObjectData.setJapcAddress(dataTagUpdate._2.getProperty("japcAddress"));
    expectedCacheObjectData.setMetadata(Metadata.builder().addMetadata("testMetadata_update",true).build());
    expectedCacheObjectData.setMaxValue((Comparable) TypeConverter.cast(dataTagUpdate._2.getProperty("maxValue"), dataTag._2.getProperty("dataType")));
    expectedCacheObjectData.setAddress(DataTagAddress.fromConfigXML(dataTagUpdate._2.getProperty("address")));

    ObjectEqualityComparison.assertDataTagConfigEquals(expectedCacheObjectData, cacheObjectData);

    verify(mockManager);

    // remove the process and equipments and dataTag from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    Configuration remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(aliveTimerCache.hasKey(23L));

    assertFalse(equipmentCache.hasKey(3L));
    assertNull(equipmentMapper.getItem(3L));
    assertFalse(controlTagCache.hasKey(31L));
    assertNull(controlTagMapper.getItem(31L));
    assertFalse(commFaultTagCache.hasKey(32L));
    assertFalse(aliveTimerCache.hasKey(33L));
    assertFalse(dataTagCache.hasKey(100L));
    assertNull(dataTagMapper.getItem(100L));

    verify(mockManager);
  }

  @Test
  public void testUpdateEquipmentDataTagDirect() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(1L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    // SETUP:
    // First add a process and equipment to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);
    Pair<DataTag.DataTagBuilder, Properties> dataTag = builderDataTagWithAllFields(100L, "equipment", 2L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).dataTag(dataTag._1.build()).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // Build configuration to update the test DataTag
    Pair<DataTag.DataTagBuilder, Properties> dataTagUpdate = builderDataTagUpdate(100L);
    Configuration configuration = Configuration.builder().confId(1L).application("configuration test - application").name("configuration test name").build();

    List<DataTag> updateList = new ArrayList<>();
    DataTag tag = dataTagUpdate._1.build();
    tag.setUpdate(true);
    updateList.add(tag);

    configuration.setConfigurationItems(updateList);

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertTrue(report.getElementReports().size() == 1);

    // get cacheObject from the cache and compare to the an expected cacheObject
    DataTagCacheObject cacheObjectData = (DataTagCacheObject) dataTagCache.get(100L);
    DataTagCacheObject expectedCacheObjectData = cacheObjectFactory.buildDataTagCacheObject(100L, dataTag._2, 1L);
    expectedCacheObjectData.setDescription(dataTagUpdate._2.getProperty("description"));
    expectedCacheObjectData.setJapcAddress(dataTagUpdate._2.getProperty("japcAddress"));
    expectedCacheObjectData.setMetadata(Metadata.builder().addMetadata("testMetadata_update",true).build());
    expectedCacheObjectData.setMaxValue((Comparable) TypeConverter.cast(dataTagUpdate._2.getProperty("maxValue"), dataTag._2.getProperty("dataType")));
    expectedCacheObjectData.setAddress(DataTagAddress.fromConfigXML(dataTagUpdate._2.getProperty("address")));

    ObjectEqualityComparison.assertDataTagConfigEquals(expectedCacheObjectData, cacheObjectData);

    verify(mockManager);

    // remove the process and equipments and dataTag from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    Configuration remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(aliveTimerCache.hasKey(23L));

    assertFalse(equipmentCache.hasKey(3L));
    assertNull(equipmentMapper.getItem(3L));
    assertFalse(controlTagCache.hasKey(31L));
    assertNull(controlTagMapper.getItem(31L));
    assertFalse(commFaultTagCache.hasKey(32L));
    assertFalse(aliveTimerCache.hasKey(33L));
    assertFalse(dataTagCache.hasKey(100L));
    assertNull(dataTagMapper.getItem(100L));

    verify(mockManager);
  }

  @Test
  public void testRemoveEquipmentDataTag() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(1L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    // SETUP:First add a process and equipment to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);
    Pair<DataTag.DataTagBuilder, Properties> dataTag = builderDataTagWithAllFields(100L, "equipment", 2L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).dataTag(dataTag._1.build()).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // check if the alarm is in the cache
    assertTrue(dataTagCache.hasKey(100L));
    assertNotNull(dataTagMapper.getItem(100L));

    // Build configuration to remove the test Alarm
    Configuration remove = getConfBuilderDataTagE(2L, 1L, buildDeleteDataTag(100L));
    ConfigurationReport report = configurationLoader.applyConfiguration(remove);

    //check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);
    assertTrue(report.getElementReports().size() == 1);

    // Check if all caches are updated
    assertFalse(dataTagCache.hasKey(100L));
    assertNull(dataTagMapper.getItem(100L));

    verify(mockManager);

    // remove the process and equipments and dataTag from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(aliveTimerCache.hasKey(23L));

    assertFalse(equipmentCache.hasKey(3L));
    assertNull(equipmentMapper.getItem(3L));
    assertFalse(controlTagCache.hasKey(31L));
    assertNull(controlTagMapper.getItem(31L));
    assertFalse(commFaultTagCache.hasKey(32L));
    assertFalse(aliveTimerCache.hasKey(33L));

    verify(mockManager);
  }

  @Test
  public void testCreateSubEquipmentDataTag() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(1L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    // SETUP:
    // First add a process and equipment to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagS = builderStatusTagWithPrimFields(31L, "subEquipment", 3L); // 24
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagS = builderCommFaultTagWithPrimFields(32L, "subEquipment", 3L); // 24
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagS = builderAliveTagWithAllFields(33l, "subEquipment", 3L); // 6
    Pair<SubEquipment.SubEquipmentBuilder, Properties> subEquipment = builderSubEquipmentWithAllFields(3L, 2L, 31L, 32L, 33L);
    SubEquipment buildS = subEquipment._1.aliveTag(aliveTagS._1.build()).statusTag(statusTagS._1.build()).commFaultTag(commFaultTagS._1.build()).build();

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).subEquipment(buildS).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // Build configuration to add the test DataTag

    Pair<DataTag.DataTagBuilder, Properties> dataTag = builderDataTagWithAllFields(100L, "subEquipment", 3L);
    Configuration configuration = ConfigurationUtil.getConfBuilderDataTagS(3L, 2L, 1L, dataTag._1.build());

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertTrue(report.getElementReports().size() == 1);
//
    // get cacheObject from the cache and compare to the an expected cacheObject
    DataTagCacheObject cacheObjectData = (DataTagCacheObject) dataTagCache.get(100L);
    DataTagCacheObject expectedCacheObjectData = cacheObjectFactory.buildDataTagCacheObject(100L, dataTag._2, 1L);

    ObjectEqualityComparison.assertDataTagConfigEquals(expectedCacheObjectData, cacheObjectData);
//    // Check if all caches are updated
    subEquipmentCache.acquireWriteLockOnKey(cacheObjectData.getSubEquipmentId());
    assertTrue(subEquipmentCache.get(cacheObjectData.getSubEquipmentId()).getDataTagIds().contains(100L));
    subEquipmentCache.releaseWriteLockOnKey(cacheObjectData.getSubEquipmentId());

    verify(mockManager);

    // remove the process and equipments and dataTag from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    Configuration remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(aliveTimerCache.hasKey(23L));

    assertFalse(subEquipmentCache.hasKey(3L));
    assertNull(subEquipmentMapper.getItem(3L));
    assertFalse(controlTagCache.hasKey(31L));
    assertNull(controlTagMapper.getItem(31L));
    assertFalse(commFaultTagCache.hasKey(32L));
    assertFalse(aliveTimerCache.hasKey(33L));
    assertFalse(dataTagCache.hasKey(100L));
    assertNull(dataTagMapper.getItem(100L));

    verify(mockManager);
  }

  @Test
  public void testUpdateSubEquipmentDataTag() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(1L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    // SETUP:
    // First add a process and equipment to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagS = builderStatusTagWithPrimFields(31L, "subEquipment", 3L); // 24
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagS = builderCommFaultTagWithPrimFields(32L, "subEquipment", 3L); // 24
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagS = builderAliveTagWithAllFields(33l, "subEquipment", 3L); // 6
    Pair<SubEquipment.SubEquipmentBuilder, Properties> subEquipment = builderSubEquipmentWithAllFields(3L, 2L, 31L, 32L, 33L);
    Pair<DataTag.DataTagBuilder, Properties> dataTag = builderDataTagWithAllFields(100L, "subEquipment", 3L);
    SubEquipment buildS = subEquipment._1.aliveTag(aliveTagS._1.build()).statusTag(statusTagS._1.build()).commFaultTag(commFaultTagS._1.build()).dataTag(dataTag._1.build()).build();

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).subEquipment(buildS).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // Build configuration to update the test DataTag
    Pair<DataTag.DataTagBuilder, Properties> dataTagUpdate = builderDataTagUpdate(100L);
    Configuration configuration = ConfigurationUtil.getConfBuilderDataTagS(3L, 2L, 1L, dataTagUpdate._1.build());

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertTrue(report.getElementReports().size() == 1);
//
    // get cacheObject from the cache and compare to the an expected cacheObject
    DataTagCacheObject cacheObjectData = (DataTagCacheObject) dataTagCache.get(100L);
    DataTagCacheObject expectedCacheObjectData = cacheObjectFactory.buildDataTagCacheObject(100L, dataTag._2, 1L);
    expectedCacheObjectData.setDescription(dataTagUpdate._2.getProperty("description"));
    expectedCacheObjectData.setJapcAddress(dataTagUpdate._2.getProperty("japcAddress"));
    expectedCacheObjectData.setMetadata(Metadata.builder().addMetadata("testMetadata_update",true).build());
    expectedCacheObjectData.setMaxValue((Comparable) TypeConverter.cast(dataTagUpdate._2.getProperty("maxValue"), dataTag._2.getProperty("dataType")));
    expectedCacheObjectData.setAddress(DataTagAddress.fromConfigXML(dataTagUpdate._2.getProperty("address")));

    ObjectEqualityComparison.assertDataTagConfigEquals(expectedCacheObjectData, cacheObjectData);

    verify(mockManager);

    // remove the process and equipments and dataTag from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    Configuration remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(aliveTimerCache.hasKey(23L));

    assertFalse(equipmentCache.hasKey(3L));
    assertNull(equipmentMapper.getItem(3L));
    assertFalse(controlTagCache.hasKey(31L));
    assertNull(controlTagMapper.getItem(31L));
    assertFalse(commFaultTagCache.hasKey(32L));
    assertFalse(aliveTimerCache.hasKey(33L));
    assertFalse(dataTagCache.hasKey(100L));
    assertNull(dataTagMapper.getItem(100L));

    verify(mockManager);
  }

  @Test
  public void testRemoveSubEquipmentDataTag() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(1L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    // SETUP:
    // First add a process and equipment to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L, 21L, 22L, 23L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagS = builderStatusTagWithPrimFields(31L, "subEquipment", 3L); // 24
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagS = builderCommFaultTagWithPrimFields(32L, "subEquipment", 3L); // 24
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagS = builderAliveTagWithAllFields(33l, "subEquipment", 3L); // 6
    Pair<SubEquipment.SubEquipmentBuilder, Properties> subEquipment = builderSubEquipmentWithAllFields(3L, 2L, 31L, 32L, 33L);
    Pair<DataTag.DataTagBuilder, Properties> dataTag = builderDataTagWithAllFields(100L, "subEquipment", 3L);
    SubEquipment buildS = subEquipment._1.aliveTag(aliveTagS._1.build()).statusTag(statusTagS._1.build()).commFaultTag(commFaultTagS._1.build()).dataTag(dataTag._1.build()).build();

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).subEquipment(buildS).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // check if the alarm is in the cache
    assertTrue(dataTagCache.hasKey(100L));
    assertNotNull(dataTagMapper.getItem(100L));

    // Build configuration to remove the test Alarm
    Configuration remove = getConfBuilderDataTagS(3L, 2L, 1L, buildDeleteDataTag(100L));
    ConfigurationReport report = configurationLoader.applyConfiguration(remove);

    //check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);
    assertTrue(report.getElementReports().size() == 1);

    // Check if all caches are updated
    assertFalse(dataTagCache.hasKey(100L));
    assertNull(dataTagMapper.getItem(100L));

    verify(mockManager);

    // remove the process and equipments and dataTag from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(aliveTimerCache.hasKey(23L));

    assertFalse(equipmentCache.hasKey(3L));
    assertNull(equipmentMapper.getItem(3L));
    assertFalse(controlTagCache.hasKey(31L));
    assertNull(controlTagMapper.getItem(31L));
    assertFalse(commFaultTagCache.hasKey(32L));
    assertFalse(aliveTimerCache.hasKey(33L));

    assertFalse(dataTagCache.hasKey(100L));
    assertNull(dataTagMapper.getItem(100L));

    verify(mockManager);
  }

  @Test
  public void testCreateRuleTag() {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    replay(mockManager);

    // SETUP:First add a process and equipment to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);
    Pair<DataTag.DataTagBuilder, Properties> dataTag = builderDataTagWithAllFields(100L, "equipment", 2L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).dataTag(dataTag._1.build()).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:Build configuration to add the test RuleTag
    Pair<RuleTag.RuleTagBuilder, Properties> ruleTag = builderRuleTagWithAllFields(1000L, "(#100 < 0)|(#100 > 200)[1],true[0]");
    Configuration configuration = ConfigurationUtil.getConfBuilderRuleTag(ruleTag._1.build());

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getElementReports().size() == 1);

    // get cacheObject from the cache and compare to the an expected cacheObject
    RuleTagCacheObject cacheObjectRule = (RuleTagCacheObject) ruleTagCache.getCopy(1000L);
    cacheObjectRule.setDataTagQuality(new DataTagQualityImpl());
    RuleTagCacheObject expectedCacheObjectRule = cacheObjectFactory.buildRuleTagCacheObject(1000L, ruleTag._2, 1L, 2L);

    ObjectEqualityComparison.assertRuleTagConfigEquals(expectedCacheObjectRule, cacheObjectRule);
    // Check if all caches are updated
    assertNotNull(ruleTagMapper.getItem(1000L));

    verify(mockManager);

    // remove the process and equipments and dataTag from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    Configuration remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(aliveTimerCache.hasKey(23L));
    assertFalse(ruleTagCache.hasKey(100L));
    assertNull(ruleTagMapper.getItem(100L));

    verify(mockManager);
  }

  @Test
  public void testUpdateRuleTag() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException, InterruptedException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    replay(mockManager);

    // SETUP:First add a process and equipment to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);
    Pair<DataTag.DataTagBuilder, Properties> dataTag = builderDataTagWithAllFields(100L, "equipment", 2L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).dataTag(dataTag._1.build()).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    Pair<RuleTag.RuleTagBuilder, Properties> ruleTag = builderRuleTagWithAllFields(1000L, "(#100 < 0)|(#100 > 200)[1],true[0]");
    insert = ConfigurationUtil.getConfBuilderRuleTag(ruleTag._1.build());
    configurationLoader.applyConfiguration(insert);

    // TEST:
    // Build configuration to add the test RuleTagUpdate
    Pair<RuleTag.RuleTagBuilder, Properties> ruleTagUpdate = builderRuleTagUpdate(1000L, "(2 > 1)[1],true[0]");
    Configuration configuration = ConfigurationUtil.getConfBuilderRuleTag(ruleTagUpdate._1.build());

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getElementReports().size() == 1);

    // get cacheObject from the cache and compare to the an expected cacheObject
    RuleTagCacheObject cacheObjectRule = (RuleTagCacheObject) ruleTagCache.get(1000L);
    RuleTagCacheObject expectedCacheObjectRule = cacheObjectFactory.buildRuleTagCacheObject(1000L, ruleTag._2, 1L, 2L);
    expectedCacheObjectRule.setJapcAddress(ruleTagUpdate._2.getProperty("japcAddress"));
    expectedCacheObjectRule.setRuleText("(2 > 1)[1],true[0]");
    expectedCacheObjectRule.setProcessIds(Collections.EMPTY_SET);
    expectedCacheObjectRule.setEquipmentIds(Collections.EMPTY_SET);
    expectedCacheObjectRule.getDataTagQuality().validate();
    expectedCacheObjectRule.setTopic(tagPublicationTrunk + "." + 0L);
    Thread.sleep(1000); // sleep 1s to allow for rule evaluation on separate thread
    ObjectEqualityComparison.assertRuleTagConfigEquals(expectedCacheObjectRule, cacheObjectRule);

    verify(mockManager);

    // remove the process and equipments and dataTag from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    Configuration remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(aliveTimerCache.hasKey(23L));
    assertFalse(ruleTagCache.hasKey(100L));
    assertNull(ruleTagMapper.getItem(100L));

    verify(mockManager);
  }

  @Test
  public void testRemoveRuleTag(){
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    replay(mockManager);

    // SETUP:First add a process and equipment to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);
    Pair<DataTag.DataTagBuilder, Properties> dataTag = builderDataTagWithAllFields(100L, "equipment", 2L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).dataTag(dataTag._1.build()).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    Pair<RuleTag.RuleTagBuilder, Properties> ruleTag = builderRuleTagWithAllFields(1000L, "(#100 < 0)|(#100 > 200)[1],true[0]");
    insert = ConfigurationUtil.getConfBuilderRuleTag(ruleTag._1.build());
    configurationLoader.applyConfiguration(insert);

    // TEST:
    // check if the alarm is in the cache
    assertTrue(ruleTagCache.hasKey(1000L));
    assertNotNull(ruleTagMapper.getItem(1000L));

    // Build configuration to add the test RuleTag
    Configuration remove = getConfBuilderRuleTag(buildDeleteRuleTag(1000L));
    ConfigurationReport report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    // Check if all caches are updated
    assertFalse(ruleTagCache.hasKey(1000L));
    assertNull(ruleTagMapper.getItem(1000L));

    verify(mockManager);

    // remove the process and equipments and dataTag from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(aliveTimerCache.hasKey(23L));

    verify(mockManager);
  }

  @Test
  public void testRuleRemovedOnTagRemoval() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException, InterruptedException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(1L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    // SETUP:First add a process and equipment to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);
    Pair<DataTag.DataTagBuilder, Properties> dataTag = builderDataTagWithAllFields(100L, "equipment", 2L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).dataTag(dataTag._1.build()).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    Pair<RuleTag.RuleTagBuilder, Properties> ruleTag1 = builderRuleTagWithAllFields(1000L, "(#100 < 0)|(#100 > 200)[1],true[0]");
    Pair<RuleTag.RuleTagBuilder, Properties> ruleTag2 = builderRuleTagWithAllFields(1001L, "(#100 < 50)|(#100 > 250)[1],true[0]");
    insert = ConfigurationUtil.getConfBuilderRuleTag(ruleTag1._1.build(), ruleTag2._1.build());
    ConfigurationReport report = configurationLoader.applyConfiguration(insert);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);
    assertTrue(report.getElementReports().size() == 2);

    // TEST:
    // check if the DataTag and rules are in the cache
    assertTrue(ruleTagCache.hasKey(1000L));
    assertNotNull(ruleTagMapper.getItem(1000L));
    assertTrue(ruleTagCache.hasKey(1001L));
    assertNotNull(ruleTagMapper.getItem(1001L));
    assertTrue(dataTagCache.hasKey(100L));
    assertNotNull(dataTagMapper.getItem(100L));

    // Build configuration to remove the test Alarm
    Configuration remove = getConfBuilderDataTagE(2L, 1L, buildDeleteDataTag(100L));
    report = configurationLoader.applyConfiguration(remove);

    //check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);
    assertTrue(report.getElementReports().size() == 1);

    // Check if all caches are updated
    assertFalse(ruleTagCache.hasKey(1000L));
    assertNull(ruleTagMapper.getItem(1000L));
    assertFalse(ruleTagCache.hasKey(1001L));
    assertNull(ruleTagMapper.getItem(1001L));
    assertFalse(dataTagCache.hasKey(100L));
    assertNull(dataTagMapper.getItem(100L));

    verify(mockManager);

    // remove the process and equipments and dataTag from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(aliveTimerCache.hasKey(23L));

    verify(mockManager);
  }

  @Test
  public void testAlarmRemovedOnTagRemoval() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException, InterruptedException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(1L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    // SETUP:First add a process and equipment to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);
    Pair<DataTag.DataTagBuilder, Properties> dataTag = builderDataTagWithAllFields(100L, "equipment", 2L);
    Pair<Alarm.AlarmBuilder, Properties> alarm1 = builderAlarmWithAllFields(666L,100L);
    Pair<Alarm.AlarmBuilder, Properties> alarm2 = builderAlarmWithAllFields(667L,100L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).dataTag((DataTag) dataTag._1.alarm(alarm1._1.build()).alarm(alarm2._1.build()).build()).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // check if the DataTag and rules are in the cache
    assertTrue(alarmCache.hasKey(666L));
    assertNotNull(alarmMapper.getItem(666L));
    assertTrue(alarmCache.hasKey(667L));
    assertNotNull(alarmMapper.getItem(667L));
    assertTrue(dataTagCache.hasKey(100L));
    assertNotNull(dataTagMapper.getItem(100L));

    // Build configuration to remove the test Alarm
    Configuration remove = getConfBuilderDataTagE(2L, 1L, buildDeleteDataTag(100L));
    ConfigurationReport report = configurationLoader.applyConfiguration(remove);

    //check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);
    assertTrue(report.getElementReports().size() == 1);

    // Check if all caches are updated
    assertFalse(alarmCache.hasKey(666L));
    assertNull(alarmMapper.getItem(666L));
    assertFalse(alarmCache.hasKey(667L));
    assertNull(alarmMapper.getItem(667L));
    assertFalse(dataTagCache.hasKey(100L));
    assertNull(dataTagMapper.getItem(100L));

    verify(mockManager);

    // remove the process and equipments and dataTag from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(aliveTimerCache.hasKey(23L));

    verify(mockManager);
  }

  @Test
  public void testCreateAlarm(){
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    replay(mockManager);

    // SETUP:First add a process and equipment to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);
    Pair<DataTag.DataTagBuilder, Properties> dataTag = builderDataTagWithAllFields(100L, "equipment", 2L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).dataTag(dataTag._1.build()).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:Build configuration to add the test Alarm
    Pair<Alarm.AlarmBuilder, Properties> alarm = builderAlarmWithAllFields(666L,100L);
    Configuration configuration = ConfigurationUtil.getConfBuilderAlarm(100L, 2L, 1L, alarm._1.build());

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getElementReports().size() == 1);

    // get cacheObject from the cache and compare to the an expected cacheObject
    AlarmCacheObject cacheObjectAlarm = (AlarmCacheObject) alarmCache.get(666L);
    AlarmCacheObject expectedCacheObjectAlarm = cacheObjectFactory.buildAlarmCacheObject(666L, alarm._2);

    ObjectEqualityComparison.assertAlarmEquals(expectedCacheObjectAlarm, cacheObjectAlarm);
    // Check if all caches are updated
    assertNotNull(alarmMapper.getItem(666L));

    verify(mockManager);

    // remove the process and equipments and dataTag from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    Configuration remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(aliveTimerCache.hasKey(23L));
    assertFalse(alarmCache.hasKey(666L));
    assertNull(alarmMapper.getItem(666L));

    verify(mockManager);
  }

  @Test
  public void testUpdateAlarm(){
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    replay(mockManager);

    // SETUP:First add a process and equipment to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23L, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);
    Pair<DataTag.DataTagBuilder, Properties> dataTag = builderDataTagWithAllFields(100L, "equipment", 2L);
    Pair<Alarm.AlarmBuilder, Properties> alarm = builderAlarmWithAllFields(666L,100L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).dataTag((DataTag) dataTag._1.alarm(alarm._1.build()).build()).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:Build configuration to update the test Alarm
    Pair<Alarm.AlarmBuilder, Properties> alarmUpdate = builderAlarmUpdate(666L, 100L);
    Configuration configuration = ConfigurationUtil.getConfBuilderAlarm(100L, 2L, 1L, alarmUpdate._1.build());

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getElementReports().size() == 1);

    // get cacheObject from the cache and compare to the an expected cacheObject
    AlarmCacheObject cacheObjectAlarm = (AlarmCacheObject) alarmCache.get(666L);
    AlarmCacheObject expectedCacheObjectAlarm = cacheObjectFactory.buildAlarmCacheObject(666L, alarm._2);
    expectedCacheObjectAlarm.setFaultFamily(alarmUpdate._2.getProperty("faultFamily"));

    ObjectEqualityComparison.assertAlarmEquals(expectedCacheObjectAlarm, cacheObjectAlarm);
    // Check if all caches are updated
    assertNotNull(alarmMapper.getItem(666L));

    verify(mockManager);

    // remove the process and equipments and dataTag from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    Configuration remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(aliveTimerCache.hasKey(23L));
    assertFalse(alarmCache.hasKey(666L));
    assertNull(alarmMapper.getItem(666L));

    verify(mockManager);
  }

  @Test
  public void testUpdateAlarmDirect(){
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    replay(mockManager);

    // SETUP:First add a process and equipment to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23L, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);
    Pair<DataTag.DataTagBuilder, Properties> dataTag = builderDataTagWithAllFields(100L, "equipment", 2L);
    Pair<Alarm.AlarmBuilder, Properties> alarm = builderAlarmWithAllFields(666L,100L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).dataTag((DataTag) dataTag._1.alarm(alarm._1.build()).build()).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:Build configuration to update the test Alarm
    Pair<Alarm.AlarmBuilder, Properties> alarmUpdate = builderAlarmUpdate(666L, 100L);
    Configuration configuration = Configuration.builder().confId(1L).application("configuration test - application").name("configuration test name").build();

    List<Alarm> alarmUpdates = new ArrayList<Alarm>();
    Alarm alarmBuild = alarmUpdate._1.build();
    alarmBuild.setUpdate(true);
    alarmUpdates.add(alarmBuild);
    configuration.setConfigurationItems(alarmUpdates);

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getElementReports().size() == 1);

    // get cacheObject from the cache and compare to the an expected cacheObject
    AlarmCacheObject cacheObjectAlarm = (AlarmCacheObject) alarmCache.get(666L);
    AlarmCacheObject expectedCacheObjectAlarm = cacheObjectFactory.buildAlarmCacheObject(666L, alarm._2);
    expectedCacheObjectAlarm.setFaultFamily(alarmUpdate._2.getProperty("faultFamily"));

    ObjectEqualityComparison.assertAlarmEquals(expectedCacheObjectAlarm, cacheObjectAlarm);
    // Check if all caches are updated
    assertNotNull(alarmMapper.getItem(666L));

    verify(mockManager);

    // remove the process and equipments and dataTag from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    Configuration remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(aliveTimerCache.hasKey(23L));
    assertFalse(alarmCache.hasKey(666L));
    assertNull(alarmMapper.getItem(666L));

    verify(mockManager);
  }

  @Test
  public void testRemoveAlarm(){
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    replay(mockManager);

    // SETUP:
    // First add a process and equipment to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);
    Pair<DataTag.DataTagBuilder, Properties> dataTag = builderDataTagWithAllFields(100L, "equipment", 2L);
    Pair<Alarm.AlarmBuilder, Properties> alarm = builderAlarmWithAllFields(666L,100L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).dataTag((DataTag) dataTag._1.alarm(alarm._1.build()).build()).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // check if the alarm is in the cache
    assertTrue(alarmCache.hasKey(666L));
    assertNotNull(alarmMapper.getItem(666L));

    // Build configuration to remove the test Alarm
    Configuration remove = getConfBuilderAlarm(100L, 2L, 1L, buildDeleteAlarm(666L));
    ConfigurationReport report = configurationLoader.applyConfiguration(remove);

    //check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);
    assertTrue(report.getElementReports().size() == 1);

    // Check if all caches are updated
    assertFalse(alarmCache.hasKey(666L));
    assertNull(alarmMapper.getItem(666L));

    verify(mockManager);

    // remove the process and equipments and dataTag from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(aliveTimerCache.hasKey(23L));

    verify(mockManager);
  }

  @Test
  public void testCreateCommandTag() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(1L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    // SETUP:
    // First add a process and equipment to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // Build configuration to add the test DataTag

    Pair<CommandTag.CommandTagBuilder, Properties> commandTag = builderCommandTagWithAllFields(100_000L, 2L);
    Configuration configuration = ConfigurationUtil.getConfBuilderCommandTag(2L, 1L, commandTag._1.build());

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertTrue(report.getElementReports().size() == 1);
//
    // get cacheObject from the cache and compare to the an expected cacheObject
    CommandTagCacheObject cacheObjectCommand = (CommandTagCacheObject) commandTagCache.get(100_000L);
    CommandTagCacheObject expectedCacheObjectCommand = cacheObjectFactory.buildCommandTagCacheObject(100_000L, commandTag._2);

    ObjectEqualityComparison.assertCommandTagEquals(expectedCacheObjectCommand, cacheObjectCommand);

    verify(mockManager);

    // remove the process and equipments and dataTag from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    Configuration remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(aliveTimerCache.hasKey(23L));
    assertFalse(commandTagCache.hasKey(100_000L));
    assertNull(commandTagMapper.getItem(100_000L));

    verify(mockManager);
  }

  @Test
  public void testUpdateCommandTag() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(1L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    // SETUP:
    // First add a process and equipment to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);
    Pair<CommandTag.CommandTagBuilder, Properties> commandTag = builderCommandTagWithAllFields(100_000L, 2L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).commandTag(commandTag._1.build()).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // Build configuration to update the test CommandTag
    Pair<CommandTag.CommandTagBuilder, Properties> commandTagUpdate = builderCommandTagUpdate(100_000L);
    Configuration configuration = ConfigurationUtil.getConfBuilderCommandTag(2L, 1L, commandTagUpdate._1.build());

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertTrue(report.getElementReports().size() == 1);
//
    // get cacheObject from the cache and compare to the an expected cacheObject
    CommandTagCacheObject cacheObjectCommand = (CommandTagCacheObject) commandTagCache.get(100_000L);
    CommandTagCacheObject expectedCacheObjectCommand = cacheObjectFactory.buildCommandTagCacheObject(100_000L, commandTag._2);
    expectedCacheObjectCommand.setDescription(commandTagUpdate._2.getProperty("description"));
    expectedCacheObjectCommand.setMode(Short.parseShort(commandTagUpdate._2.getProperty("mode")));
    expectedCacheObjectCommand.setHardwareAddress(HardwareAddressFactory.getInstance().fromConfigXML(commandTagUpdate._2.getProperty("hardwareAddress")));
    expectedCacheObjectCommand.setClientTimeout(Integer.parseInt(commandTagUpdate._2.getProperty("clientTimeout")));
    expectedCacheObjectCommand.setSourceRetries(Integer.parseInt(commandTagUpdate._2.getProperty("sourceRetries")));
    RbacAuthorizationDetails details = new RbacAuthorizationDetails();
    details.setRbacClass(commandTag._2.getProperty("rbacClass"));
    details.setRbacDevice(commandTag._2.getProperty("rbacDevice"));
    details.setRbacProperty(commandTagUpdate._2.getProperty("rbacProperty"));
    expectedCacheObjectCommand.setAuthorizationDetails(details);

    ObjectEqualityComparison.assertCommandTagEquals(expectedCacheObjectCommand, cacheObjectCommand);

    verify(mockManager);

    // remove the process and equipments and dataTag from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    Configuration remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(aliveTimerCache.hasKey(23L));
    assertFalse(commandTagCache.hasKey(100_000L));
    assertNull(commandTagMapper.getItem(100_000L));

    verify(mockManager);
  }

  @Test
  public void testRemoveCommandTag() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(1L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    // SETUP:First add a process and equipment to the server
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(11L, "process", 1L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(12l, "process", 1L);
    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(1L, 11L, 12L);

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(21L, "equipment", 2L);
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(22L, "equipment", 2L);
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(23l, "equipment", 2L);
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(2L, 1L , 21L, 22L, 23L);
    Pair<CommandTag.CommandTagBuilder, Properties> commandTag = builderCommandTagWithAllFields(100_000L, 2L);

    Process buildP = process._1.aliveTag(aliveTagP._1.build()).statusTag(statusTagP._1.build())
        .equipment(equipment._1.aliveTag(aliveTagE._1.build()).statusTag(statusTagE._1.build()).commFaultTag(commFaultTagE._1.build()).commandTag(commandTag._1.build()).build()).build();
    Configuration insert = ConfigurationUtil.getConfBuilderProcess(buildP);
    configurationLoader.applyConfiguration(insert);
    processFacade.start(1L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // check if the CommandTag is in the cache
    assertTrue(commandTagCache.hasKey(100_000L));
    assertNotNull(commandTagMapper.getItem(100_000L));

    // Build configuration to remove the test Alarm
    Configuration remove = getConfBuilderCommandTag(2L, 1L, buildDeleteCommandTag(100_000L));
    ConfigurationReport report = configurationLoader.applyConfiguration(remove);

    //check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);
    assertTrue(report.getElementReports().size() == 1);

    // Check if all caches are updated
    assertFalse(commandTagCache.hasKey(100_000L));
    assertNull(commandTagMapper.getItem(100_000L));

    verify(mockManager);

    // remove the process and equipments and dataTag from the server
    processFacade.stop(1L, new Timestamp(System.currentTimeMillis()));
    remove = getConfBuilderProcess(buildDeleteProcess(1L));
    report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertTrue(report.getStatus() == ConfigConstants.Status.OK);

    assertFalse(processCache.hasKey(1L));
    assertNull(processMapper.getItem(1L));
    assertFalse(controlTagCache.hasKey(11L));
    assertNull(controlTagMapper.getItem(11L));
    assertFalse(aliveTimerCache.hasKey(12L));

    // equipment stuff
    assertFalse(equipmentCache.hasKey(2L));
    assertNull(equipmentMapper.getItem(2L));
    assertFalse(controlTagCache.hasKey(21L));
    assertNull(controlTagMapper.getItem(21L));
    assertFalse(commFaultTagCache.hasKey(22L));
    assertFalse(aliveTimerCache.hasKey(23L));

    verify(mockManager);
  }

}
