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
package cern.c2mon.server.configuration;

import cern.c2mon.server.cache.*;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.config.CacheModule;
import cern.c2mon.server.cache.dbaccess.*;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModule;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmCondition;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.device.*;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.configuration.config.ConfigurationModule;
import cern.c2mon.server.configuration.config.ConfigurationProperties;
import cern.c2mon.server.configuration.config.ProcessCommunicationManagerMock;
import cern.c2mon.server.configuration.junit.ConfigurationCachePopulationRule;
import cern.c2mon.server.configuration.junit.ConfigurationDatabasePopulationRule;
import cern.c2mon.server.configuration.helper.ObjectEqualityComparison;
import cern.c2mon.server.daq.JmsContainerManager;
import cern.c2mon.server.daq.config.DaqModule;
import cern.c2mon.server.daq.update.JmsContainerManagerImpl;
import cern.c2mon.server.daq.out.ProcessCommunicationManager;
import cern.c2mon.server.rule.config.RuleModule;
import cern.c2mon.server.supervision.config.SupervisionModule;
import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigConstants.Status;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.ConfigurationReportHeader;
import cern.c2mon.shared.client.configuration.converter.ProcessListConverter;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.NoSimpleValueParseException;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.address.HardwareAddressFactory;
import cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.c2mon.shared.daq.config.ConfigurationChangeEventReport;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Component/integration tests of the configuration module (integrates the cache
 * modules, but mocks the daq module).
 *
 * @author Mark Brightwell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    CommonModule.class,
    CacheModule.class,
    CacheDbAccessModule.class,
    CacheLoadingModule.class,
    SupervisionModule.class,
    ConfigurationModule.class,
    DaqModule.class,
    RuleModule.class,
    ProcessCommunicationManagerMock.class
})
public class ConfigurationLoaderTest {

  @Rule
  @Autowired
  public ConfigurationDatabasePopulationRule populationRule;

  @Rule
  @Autowired
  public ConfigurationCachePopulationRule configurationCachePopulationRule;

  @Autowired
  private ConfigurationProperties properties;
  
  /**
   * Mocked daqcommunication-out module.
   */
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
  private CommandTagCache commandTagCache;

  @Autowired
  private CommandTagMapper commandTagMapper;

  @Autowired
  private RuleTagCache ruleTagCache;

  @Autowired
  private RuleTagMapper ruleTagMapper;

  @Autowired
  private EquipmentCache equipmentCache;

  @Autowired
  private EquipmentMapper equipmentMapper;

  @Autowired
  private EquipmentFacade equipmentFacade;

  @Autowired
  private SubEquipmentCache subEquipmentCache;

  @Autowired
  private SubEquipmentMapper subEquipmentMapper;

  @Autowired
  private SubEquipmentFacade subEquipmentFacade;

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
  private TagLocationService tagLocationService;

  @Autowired
  private ProcessFacade processFacade;

  @Autowired
  private DeviceClassCache deviceClassCache;

  @Autowired
  private DeviceClassMapper deviceClassMapper;

  @Autowired
  private DeviceCache deviceCache;

  @Autowired
  private DeviceMapper deviceMapper;

  @Autowired
  private ClusterCache clusterCache;

  @Autowired
  private JmsContainerManagerImpl jmsContainerManager;

  /**
   * Clears DB of failed previous tests and resets the mock before each test.
   *
   * @throws IOException
   */
  @Before
  public void beforeTest() throws IOException {
    // make sure Process is "running" (o.w. nothing is sent to DAQ)
    processFacade.start(50L, "hostname", new Timestamp(System.currentTimeMillis()));

    // reset mock
    reset(mockManager);
  }

  @After
  public void cleanUp() {
    // Make sure the JmsContainerManager is stopped, otherwise the
    // DefaultMessageListenerContainers inside will keep trying to connect to
    // a JMS broker (which will not be running)
    jmsContainerManager.stop();
  }

  @Test
  public void testCreateUpdateRemoveControlTag() {
    // create
    ConfigurationReport report = configurationLoader.applyConfiguration(2);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
    assertEquals(Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty()); // empty because no
                                                         // process/equipment
                                                         // points to this
                                                         // control tag

    ControlTagCacheObject cacheObject = (ControlTagCacheObject) controlTagCache.get(500L);

    // corresponds to data inserted using SQL file
    ControlTagCacheObject expectedObject = new ControlTagCacheObject();
    expectedObject.setId(new Long(500)); // must be non null in DB
    expectedObject.setName("Process status"); // non null
    expectedObject.setMode(DataTagConstants.MODE_TEST); // non null
    expectedObject.setDataType("Integer"); // non null
    expectedObject.setDescription("test");
    expectedObject.setMinValue(new Integer(12));
    expectedObject.setMaxValue(new Integer(22));
    expectedObject.setLogged(false); // null allowed

    expectedObject.setDataTagQuality(new DataTagQualityImpl());

    ObjectEqualityComparison.assertDataTagConfigEquals(expectedObject, cacheObject);
    // test update of control tag
    report = configurationLoader.applyConfiguration(6);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
    assertEquals(Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    // ControlTagCacheObject updatedCacheObject = (ControlTagCacheObject)
    // controlTagCache.get(500L);
    expectedObject.setDescription("modified description");
    expectedObject.setMinValue(null); // check can reset min & max to null using
                                      // update
    expectedObject.setMaxValue(null);
    cacheObject = (ControlTagCacheObject) controlTagCache.get(500L);
    ObjectEqualityComparison.assertDataTagConfigEquals(expectedObject, cacheObject);

  }

  @Test
  public void testRemoveControlTag() {
    // check as expected before test
    assertTrue(controlTagCache.hasKey(1250L));
    assertNotNull(controlTagMapper.getItem(1250L));

    // run test
    ConfigurationReport report = configurationLoader.applyConfiguration(8);

    // check outcome

    assertEquals(Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty()); // empty because no
                                                         // process/equipment
                                                         // points to this
                                                         // control tag
    assertFalse(controlTagCache.hasKey(1250L));
    assertNull(controlTagMapper.getItem(1250L));
  }

  @Test
  public void testCreateAndUpdateCommandTag() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException,
      NoSuchFieldException, NoSimpleValueParseException {
    // the mocked ProcessCommmunicationManager can return an empty report
    // (expect 3 calls)
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    ConfigurationReport report = configurationLoader.applyConfiguration(3);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
    CommandTagCacheObject cacheObject = (CommandTagCacheObject) commandTagCache.get(10000L);

    CommandTagCacheObject expectedObject = new CommandTagCacheObject(10000L, "Test CommandTag", "test description", "String", DataTagConstants.MODE_TEST);
    // expectedObject.setAuthorizedHostsPattern("*");
    expectedObject.setEquipmentId(150L);
    expectedObject.setClientTimeout(30000);
    expectedObject.setExecTimeout(6000);
    expectedObject.setSourceRetries(2);
    expectedObject.setSourceTimeout(200);
    RbacAuthorizationDetails details = new RbacAuthorizationDetails();
    details.setRbacClass("RBAC class");
    details.setRbacDevice("RBAC device");
    details.setRbacProperty("RBAC property");
    expectedObject.setAuthorizationDetails(details);
    expectedObject
        .setHardwareAddress(HardwareAddressFactory
            .getInstance()
            .fromConfigXML(
                "<HardwareAddress class=\"cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl\"><opc-item-name>PLC_B_CMD_ACQ_DEF_5A6</opc-item-name><command-pulse-length>100</command-pulse-length></HardwareAddress>"));
    ObjectEqualityComparison.assertCommandTagEquals(expectedObject, cacheObject);

    // test update
    report = configurationLoader.applyConfiguration(5);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));

    CommandTagCacheObject cacheObjectUpdated = (CommandTagCacheObject) commandTagCache.get(10000L);

    expectedObject.setName("Test CommandTag Updated");
    expectedObject.getAuthorizationDetails().setRbacClass("new RBAC class");
    expectedObject.getAuthorizationDetails().setRbacDevice("new RBAC device");
    expectedObject
        .setHardwareAddress(HardwareAddressFactory
            .getInstance()
            .fromConfigXML(
                "<HardwareAddress class=\"cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl\"><opc-item-name>PLC_B_CMD_ACQ_DEF_5A6</opc-item-name><command-pulse-length>150</command-pulse-length></HardwareAddress>"));
    ObjectEqualityComparison.assertCommandTagEquals(expectedObject, cacheObjectUpdated);
  }

  @Test
  public void testRemoveCommand() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException,
      NoSuchFieldException, NoSimpleValueParseException {
    // check as expected
    assertTrue(commandTagCache.hasKey(11000L));
    assertNotNull(commandTagMapper.getItem(11000L));
    EasyMock.expect(mockManager.sendConfiguration(EasyMock.isA(Long.class), EasyMock.isA(List.class))).andReturn(new ConfigurationChangeEventReport());

    // rung test
    replay(mockManager);
    ConfigurationReport report = configurationLoader.applyConfiguration(9);

    // check successful
    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
    assertFalse(commandTagCache.hasKey(11000L));
    assertNull(commandTagMapper.getItem(11000L));
    verify(mockManager);
  }

  @Test
  public void testCreateAndUpdateDataTag() throws ConfigurationException, InterruptedException, ParserConfigurationException, IllegalAccessException,
      InstantiationException, TransformerException, NoSuchFieldException, NoSimpleValueParseException {
    // the mocked ProcessCommmunicationManager can return an empty report
    // (expect 3 calls for create, update and remove)
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    ConfigurationReport report = configurationLoader.applyConfiguration(1);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
    assertEquals(Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());

    DataTagCacheObject cacheObject = (DataTagCacheObject) dataTagCache.get(new Long(5000000));

    // corresponds to data inserted using SQL file
    DataTagCacheObject expectedObject = new DataTagCacheObject();
    expectedObject.setId(new Long(5000000)); // must be non null in DB
    expectedObject.setName("Config_test_datatag"); // non null
    expectedObject.setDescription("test description config datatag");
    expectedObject.setMode(DataTagConstants.MODE_TEST); // non null
    expectedObject.setDataType("Float"); // non null
    expectedObject.setLogged(false); // null allowed
    expectedObject.setUnit("config unit m/sec");
    expectedObject.setDipAddress("testConfigDIPaddress");
    expectedObject.setJapcAddress("testConfigJAPCaddress");
    // expectedObject.setValue(Boolean.TRUE);
    // expectedObject.setValueDescription("test config value description");
    expectedObject.setSimulated(false); // null allowed
    expectedObject.setEquipmentId(new Long(150)); // need test equipment
                                                  // inserted
    expectedObject.setProcessId(50L);
    expectedObject.setMinValue(new Float(12.2));
    expectedObject.setMaxValue(new Float(23.3));
    expectedObject.setAddress(new DataTagAddress(new OPCHardwareAddressImpl("CW_TEMP_IN_COND3")));
    expectedObject.setDataTagQuality(new DataTagQualityImpl());
    // expectedObject.setCacheTimestamp(new
    // Timestamp(System.currentTimeMillis())); //should be set to creation time,
    // so not null
    // expectedObject.setSourceTimestamp(new
    // Timestamp(System.currentTimeMillis()));
    // expectedObject.setRuleIdsString("1234,3456"); //NO: never loaded at
    // reconfiguration of datatag, but only when a new rule is added

    ObjectEqualityComparison.assertDataTagConfigEquals(expectedObject, cacheObject);

    equipmentCache.acquireWriteLockOnKey(cacheObject.getEquipmentId());
    Equipment equipment = equipmentCache.get(cacheObject.getEquipmentId());
    // check equipment now has datatag in list
    assertTrue(equipmentFacade.getDataTagIds(cacheObject.getEquipmentId()).contains(5000000L));
    equipmentCache.releaseWriteLockOnKey(cacheObject.getEquipmentId());

    // test update of this datatag
    report = configurationLoader.applyConfiguration(4);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
    assertEquals(Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    DataTagCacheObject updatedCacheObject = (DataTagCacheObject) dataTagCache.get(5000000L);

    expectedObject.setJapcAddress("testConfigJAPCaddress2");
    expectedObject.setDipAddress(null); // checks can be set to null also
    expectedObject.setMaxValue(new Float(26));
    expectedObject.setAddress(new DataTagAddress(new OPCHardwareAddressImpl("CW_TEMP_IN_COND4")));

    ObjectEqualityComparison.assertDataTagConfigEquals(expectedObject, updatedCacheObject);
    equipment = equipmentCache.get(cacheObject.getEquipmentId());

    equipmentCache.acquireWriteLockOnKey(cacheObject.getEquipmentId());
    equipmentCache.releaseWriteLockOnKey(cacheObject.getEquipmentId());
  }

  @Test
  public void testRemoveDataTag() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException,
      NoSuchFieldException, NoSimpleValueParseException {
    // check data as expected
    Long tagId = 200001L;
    DataTagCacheObject cacheObject = (DataTagCacheObject) dataTagCache.get(200001L);
    assertNotNull(cacheObject);
    assertNotNull(dataTagMapper.getItem(tagId));

    EasyMock.expect(mockManager.sendConfiguration(EasyMock.isA(Long.class), EasyMock.isA(List.class))).andReturn(new ConfigurationChangeEventReport());

    replay(mockManager);
    // run test
    ConfigurationReport report = configurationLoader.applyConfiguration(7);

    // check successful

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
    assertEquals(Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertFalse(dataTagCache.hasKey(tagId));
    assertNull(dataTagMapper.getItem(tagId));
    // tag id is no longer in equipment
    assertFalse(equipmentFacade.getDataTagIds(cacheObject.getEquipmentId()).contains(tagId));

    verify(mockManager);
  }

  /**
   * No communication should take place with the DAQs during rule configuration.
   *
   * @throws InterruptedException
   * @throws NoSimpleValueParseException
   * @throws NoSuchFieldException
   * @throws TransformerException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws ParserConfigurationException
   */
  @Test
  public void testCreateUpdateRemoveRuleTag() throws InterruptedException, ParserConfigurationException, IllegalAccessException, InstantiationException,
      TransformerException, NoSuchFieldException, NoSimpleValueParseException {
    // the mocked ProcessCommmunicationManager will be called once when creating
    // the datatag to base the rule on
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    // insert datatag to base rule on
    configurationLoader.applyConfiguration(1);
    ConfigurationReport report = configurationLoader.applyConfiguration(10);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
    RuleTagCacheObject cacheObject = (RuleTagCacheObject) ruleTagCache.get(50100L);

    RuleTagCacheObject expectedObject = new RuleTagCacheObject(50100L);
    expectedObject.setName("test ruletag"); // non null
    expectedObject.setDescription("test ruletag description");
    expectedObject.setMode(DataTagConstants.MODE_MAINTENANCE); // non null
    expectedObject.setDataType("Float"); // non null
    expectedObject.setLogged(true); // null allowed
    expectedObject.setUnit("config unit m/sec");
    expectedObject.setDipAddress("testConfigDIPaddress");
    expectedObject.setJapcAddress("testConfigJAPCaddress");
    expectedObject.setRuleText("(#5000000 < 0)|(#5000000 > 200)[1],true[0]");
    Set<Long> eqIds = new HashSet<Long>();
    eqIds.add(150L);
    expectedObject.setEquipmentIds(eqIds);
    Set<Long> procIds = new HashSet<Long>();
    procIds.add(50L);
    expectedObject.setProcessIds(procIds);

    ObjectEqualityComparison.assertRuleTagConfigEquals(expectedObject, cacheObject);

    // update ruletag
    expectedObject.setJapcAddress("newTestConfigJAPCaddress");
    expectedObject.setRuleText("(2 > 1)[1],true[0]");
    expectedObject.setProcessIds(Collections.EMPTY_SET);
    expectedObject.setEquipmentIds(Collections.EMPTY_SET);
    expectedObject.getDataTagQuality().validate();
    report = configurationLoader.applyConfiguration(11);
    Thread.sleep(1000); // sleep 1s to allow for rule evaluation on separate
                        // thread

    RuleTagCacheObject updatedCacheObject = (RuleTagCacheObject) ruleTagCache.get(50100L);
    ObjectEqualityComparison.assertRuleTagConfigEquals(expectedObject, updatedCacheObject);

    verify(mockManager);
  }

  @Test
  public void testRemoveRuleTag() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException,
      NoSuchFieldException, NoSimpleValueParseException {
      
    this.properties.setDeleteRulesAfterTagDeletion(true);
    
    replay(mockManager);

    // remove ruletag
    ConfigurationReport report = configurationLoader.applyConfiguration(12);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
    assertFalse(ruleTagCache.hasKey(60007L));
    assertNull(ruleTagMapper.getItem(60007L));

    // dependent rules removed, e.g.
    assertFalse(ruleTagCache.hasKey(60009L));
    assertNull(ruleTagMapper.getItem(60009L));

    verify(mockManager);
  }

  /**
   * Tests a dependent rule is removed when a tag is.
   */
  @Test
  public void testRuleRemovedOnTagRemoval() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException,
      NoSuchFieldException, NoSimpleValueParseException {
      
    this.properties.setDeleteRulesAfterTagDeletion(true);
    
    Long tagId = 200001L;
    Long ruleId1 = 60000L; // two of the rules that should be removed
    Long ruleId2 = 59999L;
    assertTrue(ruleTagCache.hasKey(ruleId1));
    assertNotNull(ruleTagMapper.getItem(ruleId1));
    assertTrue(ruleTagCache.hasKey(ruleId2));
    assertNotNull(ruleTagMapper.getItem(ruleId2));
    assertTrue(dataTagCache.hasKey(tagId));
    assertNotNull(dataTagMapper.getItem(tagId));

    // for tag removal
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());

    replay(mockManager);

    // test removal of tag 20004L removes the rule also
    configurationLoader.applyConfiguration(7);

    assertFalse(ruleTagCache.hasKey(ruleId1));
    assertNull(ruleTagMapper.getItem(ruleId1));
    assertFalse(ruleTagCache.hasKey(ruleId2));
    assertNull(ruleTagMapper.getItem(ruleId2));
    assertFalse(dataTagCache.hasKey(tagId));
    assertNull(dataTagMapper.getItem(tagId));

    verify(mockManager);
  }
  
  /**
   * Tests a dependent rule is removed when a tag is.
   */
  @Test
  public void testRuleNotRemovedOnTagRemoval() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException,
      NoSuchFieldException, NoSimpleValueParseException {
      
    this.properties.setDeleteRulesAfterTagDeletion(false);
    
    Long tagId = 200001L;
    Long ruleId1 = 60000L; // two of the rules that should be removed
    Long ruleId2 = 59999L;
    assertTrue(ruleTagCache.hasKey(ruleId1));
    assertNotNull(ruleTagMapper.getItem(ruleId1));
    assertTrue(ruleTagCache.hasKey(ruleId2));
    assertNotNull(ruleTagMapper.getItem(ruleId2));
    assertTrue(dataTagCache.hasKey(tagId));
    assertNotNull(dataTagMapper.getItem(tagId));

    // for tag removal
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());

    replay(mockManager);

    // test removal of tag 20004L removes the rule also
    configurationLoader.applyConfiguration(7);

    assertTrue(ruleTagCache.hasKey(ruleId1));
    assertNotNull(ruleTagMapper.getItem(ruleId1));
    assertTrue(ruleTagCache.hasKey(ruleId2));
    assertNotNull(ruleTagMapper.getItem(ruleId2));
    
    assertFalse(dataTagCache.hasKey(tagId));
    assertNull(dataTagMapper.getItem(tagId));

    verify(mockManager);
  }  

  /**
   * Tests that a tag removal does indeed remove an associated alarm.
   *
   * @throws NoSimpleValueParseException
   * @throws NoSuchFieldException
   * @throws TransformerException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws ParserConfigurationException
   */
  @Test
  public void testAlarmRemovedOnTagRemoval() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException,
      NoSuchFieldException, NoSimpleValueParseException {
    replay(mockManager);

    // test removal of (rule)tag 60000 removes the alarm also
    configurationLoader.applyConfiguration(27);
    assertFalse(alarmCache.hasKey(350000L));
    assertNull(alarmMapper.getItem(350000L));
    assertFalse(ruleTagCache.hasKey(60000L));
    assertNull(ruleTagMapper.getItem(60000L));
    verify(mockManager);
  }

  /**
   * Test the creation, update and removal of equipment.
   *
   * @throws NoSimpleValueParseException
   * @throws NoSuchFieldException
   * @throws TransformerException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws ParserConfigurationException
   */
  @Test
  public void testCreateUpdateEquipment() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException,
      NoSuchFieldException, NoSimpleValueParseException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andAnswer(new IAnswer<ConfigurationChangeEventReport>() {

      @Override
      public ConfigurationChangeEventReport answer() throws Throwable {
        List<Change> changeList = (List<Change>) EasyMock.getCurrentArguments()[1];
        ConfigurationChangeEventReport report = new ConfigurationChangeEventReport();
        for (Change change : changeList) {
          ChangeReport changeReport = new ChangeReport(change);
          changeReport.setState(CHANGE_STATE.SUCCESS);
          report.appendChangeReport(changeReport);
        }
        return report;
      }
    }).times(2); // twice: once for create, another for update

    replay(mockManager);

    ConfigurationReport report = configurationLoader.applyConfiguration(13);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
    assertEquals(Status.OK, report.getStatus()); // ok as DAQ handles Equipment
                                                 // creation
    assertFalse(report.getProcessesToReboot().contains("P_TESTHANDLER03"));

    EquipmentCacheObject cacheObject = (EquipmentCacheObject) equipmentCache.get(110L);
    EquipmentCacheObject expectedObject = new EquipmentCacheObject(110L);
    expectedObject.setName("E_CONFIG_TEST");
    expectedObject.setAddress("serverHostName=VGTCVENTTEST");
    expectedObject.setAliveTagId(1251L);
    expectedObject.setStateTagId(1250L);
    expectedObject.setCommFaultTagId(1252L);
    expectedObject.setHandlerClassName("cern.c2mon.driver.");
    expectedObject.setProcessId(50L);
    expectedObject.setDescription("test description");

    ObjectEqualityComparison.assertEquipmentEquals(expectedObject, cacheObject);

    // also check that the process, commfault and alive cache were updated
    Process process = processCache.get(expectedObject.getProcessId());
    // check process is running
    ((ProcessCacheObject) process).setRequiresReboot(false);
    assertFalse(process.getRequiresReboot());
    assertTrue(process.getEquipmentIds().contains(expectedObject.getId()));
    // the alivetimer and commfault have overriden those already in the cache
    // (check reference to the equipment has changed)
    assertNotNull(commFaultTagCache.get(expectedObject.getCommFaultTagId()));
    assertEquals(expectedObject.getId(), commFaultTagCache.get(cacheObject.getCommFaultTagId()).getEquipmentId());

    // update (creates controltag and updates equipment) - should succeed
    report = configurationLoader.applyConfiguration(25);

    // expect 2 top elements (control and equipment, with control first)
    // equipment report should have 1 sub-reports from DAQ (control tag has no
    // address)
    List<ConfigurationElementReport> topList = report.getElementReports();
    assertEquals(2, topList.size());
    assertEquals(topList.get(0).getEntity(), Entity.CONTROLTAG);
    assertEquals(topList.get(1).getEntity(), Entity.EQUIPMENT);
    assertEquals(0, topList.get(0).getSubreports().size());
    // 2 sub-reports: One for Equipment alive tag creation and another for the actual equipment
    assertEquals(2, topList.get(1).getSubreports().size());
    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
    // should contain both a controltag update and equipment update
    // (note the DAQ response is generated automatically by the mock)
    assertTrue(report.toXML().contains("CONTROLTAG"));
    assertTrue(report.toXML().contains("EQUIPMENT"));

    // checks restart status is correctly set (DAQ call is mocked as success for
    // equipment update)
    assertEquals(Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());

    cacheObject = (EquipmentCacheObject) equipmentCache.get(110L);
    expectedObject.setDescription("updated description");
    expectedObject.setAddress("serverHostName=VGTCVENTTEST;test");
    expectedObject.setAliveTagId(1251L);

    ObjectEqualityComparison.assertEquipmentEquals(expectedObject, cacheObject);

    // check alive timer reference is updated in DB
    assertEquals(new Long(1251L), equipmentMapper.getItem(110L).getAliveTagId());
    // also expect alivetimercache to have element 501:
    assertNotNull(aliveTimerCache.get(1251L));

    verify(mockManager);
  }

  /**
   * Test equipment and control tags are removed correctly.
   *
   * @throws NoSimpleValueParseException
   * @throws NoSuchFieldException
   * @throws TransformerException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws ParserConfigurationException
   */
  @Test
  public void testRemoveEquipement() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException,
      NoSuchFieldException, NoSimpleValueParseException {
    // check as expected
    Equipment equipment = equipmentCache.get(150L);
    assertNotNull(equipment);
    assertNotNull(equipmentMapper.getItem(150L));
    assertTrue(aliveTimerCache.hasKey(equipment.getAliveTagId()));
    assertTrue(commFaultTagCache.hasKey(equipment.getCommFaultTagId()));

    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andAnswer(new IAnswer<ConfigurationChangeEventReport>() {

      @Override
      public ConfigurationChangeEventReport answer() throws Throwable {
        List<Change> changeList = (List<Change>) EasyMock.getCurrentArguments()[1];
        ConfigurationChangeEventReport report = new ConfigurationChangeEventReport();
        for (Change change : changeList) {
          ChangeReport changeReport = new ChangeReport(change);
          changeReport.setState(CHANGE_STATE.SUCCESS);
          report.appendChangeReport(changeReport);
        }
        return report;
      }
    });

    replay(mockManager);
    // remove equipment
    // remove completes successfully; both Equipment and ControlTags are removed
    ConfigurationReport report = configurationLoader.applyConfiguration(15);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
    assertEquals(Status.OK, report.getStatus()); // DAQ deals with Equipment
                                                 // removal
    assertFalse(report.getProcessesToReboot().contains("P_TESTHANDLER03"));
    assertFalse(equipmentCache.hasKey(150L));
    assertNull(equipmentMapper.getItem(150L));
    // commfault and alive should no longer be in cache
    assertFalse(aliveTimerCache.hasKey(equipment.getAliveTagId()));
    assertFalse(commFaultTagCache.hasKey(equipment.getCommFaultTagId()));
    verify(mockManager);
  }

  @Test
  public void testCreateUpdateRemoveProcess() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException,
      NoSuchFieldException, NoSimpleValueParseException {
    // currently no configuration required on DAQ layer for Process
    // configuration options
    replay(mockManager);

    ConfigurationReport report = configurationLoader.applyConfiguration(16);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));

    ProcessCacheObject cacheObject = (ProcessCacheObject) processCache.get(2L);

    ProcessCacheObject expectedObject = new ProcessCacheObject(2L);
    expectedObject.setName("P_TEST");
    expectedObject.setAliveInterval(60000);
    expectedObject.setAliveTagId(1221L);
    expectedObject.setStateTagId(1220L);
    expectedObject.setMaxMessageSize(200);
    expectedObject.setMaxMessageDelay(1000);
    expectedObject.setDescription("test description");

    ObjectEqualityComparison.assertProcessEquals(expectedObject, cacheObject);

    // update
    report = configurationLoader.applyConfiguration(17);


    cacheObject = (ProcessCacheObject) processCache.getCopy(2L);
    expectedObject.setDescription("updated description");
    expectedObject.setMaxMessageDelay(4000);

    ObjectEqualityComparison.assertProcessEquals(expectedObject, cacheObject);
    assertFalse(report.getProcessesToReboot().isEmpty());

    verify(mockManager);
  }

  /**
   * Tests the removal of a process succeeds, with dependent rules and alarms.
   * Relies on permanent test data in test account and must be rolled back. No
   * changes should be sent to the DAQ layer.
   */
  @Test
  public void testRemoveProcess() {
    this.properties.setDeleteRulesAfterTagDeletion(true);
    
    // stop DAQ else remove not allowed
    processFacade.stop(50L, new Timestamp(System.currentTimeMillis()));

    replay(mockManager);

    ConfigurationReport report = configurationLoader.applyConfiguration(28);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
    verify(mockManager);
    // check process, tag, rules and alarms are gone
    assertFalse(processCache.hasKey(50L));
    assertNull(processMapper.getItem(50L));
    assertFalse(equipmentCache.hasKey(150L));
    assertNull(equipmentMapper.getItem(150L));
    // check couple of rules
    assertFalse(ruleTagCache.hasKey(60010L));
    assertNull(ruleTagMapper.getItem(60010L));
    assertFalse(ruleTagCache.hasKey(60002L));
    assertNull(ruleTagMapper.getItem(60002L));            
    // tags
    assertFalse(dataTagCache.hasKey(200002L));
    assertNull(dataTagMapper.getItem(200002L));
    assertFalse(dataTagCache.hasKey(200003L));
    assertNull(dataTagMapper.getItem(200003L));
    // control tags
    assertFalse(controlTagCache.hasKey(1220L));
    assertNull(controlTagMapper.getItem(1220L));
    assertFalse(controlTagCache.hasKey(1221L));
    assertNull(controlTagMapper.getItem(1221L));
    // equipment control tags
    assertFalse(controlTagCache.hasKey(1222L));
    assertNull(controlTagMapper.getItem(1222L));
    assertFalse(controlTagCache.hasKey(1223L));
    assertNull(controlTagMapper.getItem(1223L));
    // equipment commfault
    assertFalse(commFaultTagCache.hasKey(1223L));
    // process alive
    assertFalse(aliveTimerCache.hasKey(1221L));
    // alarms
    assertFalse(alarmCache.hasKey(350000L));
    assertNull(alarmMapper.getItem(350000L));        
    assertFalse(alarmCache.hasKey(350001L));
    assertNull(alarmMapper.getItem(350001L));
    verify(mockManager);
  }
  
  /**
   * Tests the removal of a process succeeds, with dependent alarms.
   * Relies on permanent test data in test account and must be rolled back. No
   * changes should be sent to the DAQ layer.
   */
  @Test
  public void testRemoveProcessDoNotDeleteRules() {
    this.properties.setDeleteRulesAfterTagDeletion(false);
    
    // stop DAQ else remove not allowed
    processFacade.stop(50L, new Timestamp(System.currentTimeMillis()));

    replay(mockManager);

    ConfigurationReport report = configurationLoader.applyConfiguration(28);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
    verify(mockManager);
    // check process, tags and alarms are gone
    assertFalse(processCache.hasKey(50L));
    assertNull(processMapper.getItem(50L));
    assertFalse(equipmentCache.hasKey(150L));
    assertNull(equipmentMapper.getItem(150L));
    // they should still be there; since 
    // c2mon.server.configuration.deleteRulesAfterTagDeletion=true we keep rules
    // when deleting tags that they are attached to        
    assertTrue(ruleTagCache.hasKey(60010L));
    assertNotNull(ruleTagMapper.getItem(60010L));
    assertTrue(ruleTagCache.hasKey(60002L));
    assertNotNull(ruleTagMapper.getItem(60002L));            
    // tags
    assertFalse(dataTagCache.hasKey(200002L));
    assertNull(dataTagMapper.getItem(200002L));
    assertFalse(dataTagCache.hasKey(200003L));
    assertNull(dataTagMapper.getItem(200003L));
    // control tags
    assertFalse(controlTagCache.hasKey(1220L));
    assertNull(controlTagMapper.getItem(1220L));
    assertFalse(controlTagCache.hasKey(1221L));
    assertNull(controlTagMapper.getItem(1221L));
    // equipment control tags
    assertFalse(controlTagCache.hasKey(1222L));
    assertNull(controlTagMapper.getItem(1222L));
    assertFalse(controlTagCache.hasKey(1223L));
    assertNull(controlTagMapper.getItem(1223L));
    // equipment commfault
    assertFalse(commFaultTagCache.hasKey(1223L));
    // process alive
    assertFalse(aliveTimerCache.hasKey(1221L));
    // alarm - will still exist because it is attached to rule tag ID 60000
    assertTrue(alarmCache.hasKey(350000L));
    assertNotNull(alarmMapper.getItem(350000L));
    // alarm - won't exist because it is attached to data tag ID 20000
    assertFalse(alarmCache.hasKey(350003L));
    assertNull(alarmMapper.getItem(350003L));
    
    verify(mockManager);
  }  

  /**
   * Tests the removal of a process succeeds, with dependent rules and alarms.
   * Relies on permanent test data in test account and must be rolled back. No
   * changes should be sent to the DAQ layer.
   *
   * @throws NoSimpleValueParseException
   * @throws NoSuchFieldException
   * @throws TransformerException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws ParserConfigurationException
   */
  @Test
  public void testRemoveEquipmentDependentObjects() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException,
      NoSuchFieldException, NoSimpleValueParseException {
    this.properties.setDeleteRulesAfterTagDeletion(true);
    
    // expect equipment remove message to DAQ
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andAnswer(new IAnswer<ConfigurationChangeEventReport>() {

      @Override
      public ConfigurationChangeEventReport answer() throws Throwable {
        List<Change> changeList = (List<Change>) EasyMock.getCurrentArguments()[1];
        ConfigurationChangeEventReport report = new ConfigurationChangeEventReport();
        for (Change change : changeList) {
          ChangeReport changeReport = new ChangeReport(change);
          changeReport.setState(CHANGE_STATE.SUCCESS);
          report.appendChangeReport(changeReport);
        }
        return report;
      }
    });

    replay(mockManager);

    ConfigurationReport report = configurationLoader.applyConfiguration(29);
    verify(mockManager);
    // check equipment, tag, rules and alarms are gone
    assertFalse(equipmentCache.hasKey(150L));
    assertNull(equipmentMapper.getItem(150L));
    // check couple of rules
    assertFalse(ruleTagCache.hasKey(60005L));
    assertNull(ruleTagMapper.getItem(60005L));
    assertFalse(ruleTagCache.hasKey(60004L));
    assertNull(ruleTagMapper.getItem(60004L));            
    // tags
    assertFalse(dataTagCache.hasKey(200001L));
    assertNull(dataTagMapper.getItem(200001L));
    assertFalse(dataTagCache.hasKey(200004L));
    assertNull(dataTagMapper.getItem(200004L));
    // control tags
    assertFalse(controlTagCache.hasKey(1222L));
    assertNull(controlTagMapper.getItem(1222L));
    assertFalse(controlTagCache.hasKey(1223L));
    assertNull(controlTagMapper.getItem(1223L));
    assertFalse(controlTagCache.hasKey(1224L));
    assertNull(controlTagMapper.getItem(1224L));
    // alivetimer & commfault
    assertFalse(aliveTimerCache.hasKey(1224L));
    assertFalse(commFaultTagCache.hasKey(1223L));
    // alarms
    assertFalse(alarmCache.hasKey(350000L));
    assertNull(alarmMapper.getItem(350000L));
    assertFalse(alarmCache.hasKey(350001L));
    assertNull(alarmMapper.getItem(350001L));

    verify(mockManager);
  }
  
  /**
   * Tests the removal of a process succeeds, with dependent rules and alarms.
   * Relies on permanent test data in test account and must be rolled back. No
   * changes should be sent to the DAQ layer.
   *
   * @throws NoSimpleValueParseException
   * @throws NoSuchFieldException
   * @throws TransformerException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws ParserConfigurationException
   */
  @Test
  public void testRemoveEquipmentDependentObjectsDoNotDeleteRules() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException,
      NoSuchFieldException, NoSimpleValueParseException {
      
    this.properties.setDeleteRulesAfterTagDeletion(false);
    
    // expect equipment remove message to DAQ
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andAnswer(new IAnswer<ConfigurationChangeEventReport>() {

      @Override
      public ConfigurationChangeEventReport answer() throws Throwable {
        List<Change> changeList = (List<Change>) EasyMock.getCurrentArguments()[1];
        ConfigurationChangeEventReport report = new ConfigurationChangeEventReport();
        for (Change change : changeList) {
          ChangeReport changeReport = new ChangeReport(change);
          changeReport.setState(CHANGE_STATE.SUCCESS);
          report.appendChangeReport(changeReport);
        }
        return report;
      }
    });

    replay(mockManager);

    ConfigurationReport report = configurationLoader.applyConfiguration(29);
    verify(mockManager);
    // check equipment, tag, rules and alarms are gone
    assertFalse(equipmentCache.hasKey(150L));
    assertNull(equipmentMapper.getItem(150L));
    // they should still be there; since 
    // c2mon.server.configuration.deleteRulesAfterTagDeletion=true we keep rules
    // when deleting tags that they are attached to        
    assertTrue(ruleTagCache.hasKey(60005L));
    assertNotNull(ruleTagMapper.getItem(60005L));
    assertTrue(ruleTagCache.hasKey(60004L));
    assertNotNull(ruleTagMapper.getItem(60004L));        
    // tags
    assertFalse(dataTagCache.hasKey(200001L));
    assertNull(dataTagMapper.getItem(200001L));
    assertFalse(dataTagCache.hasKey(200004L));
    assertNull(dataTagMapper.getItem(200004L));
    // control tags
    assertFalse(controlTagCache.hasKey(1222L));
    assertNull(controlTagMapper.getItem(1222L));
    assertFalse(controlTagCache.hasKey(1223L));
    assertNull(controlTagMapper.getItem(1223L));
    assertFalse(controlTagCache.hasKey(1224L));
    assertNull(controlTagMapper.getItem(1224L));
    // alivetimer & commfault
    assertFalse(aliveTimerCache.hasKey(1224L));
    assertFalse(commFaultTagCache.hasKey(1223L));
    // alarm - will still exist because it is attached to rule tag ID 60000
    assertTrue(alarmCache.hasKey(350000L));
    assertNotNull(alarmMapper.getItem(350000L));
    // alarm - won't exist because it is attached to data tag ID 20000
    assertFalse(alarmCache.hasKey(350003L));
    assertNull(alarmMapper.getItem(350003L));
   
    verify(mockManager);
  }  

  /**
   * Test the creation, update and removal of equipment.
   */

  @Test
  public void testCreateUpdateSubEquipment() throws IllegalAccessException, InstantiationException, NoSuchFieldException, ParserConfigurationException,
      TransformerException, NoSimpleValueParseException {
    expect(mockManager.sendConfiguration(EasyMock.anyLong(), EasyMock.<List<Change>> anyObject())).andReturn(new ConfigurationChangeEventReport()).times(2);
    replay(mockManager);

    ConfigurationReport report = configurationLoader.applyConfiguration(19);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));

    SubEquipmentCacheObject cacheObject = (SubEquipmentCacheObject) subEquipmentCache.get(200L);
    SubEquipmentCacheObject expectedObject = new SubEquipmentCacheObject(200L);
    expectedObject.setName("SUB_E_TEST");
    expectedObject.setStateTagId(1250L);
    expectedObject.setCommFaultTagId(1252L);
    expectedObject.setAliveTagId(1251L);
    expectedObject.setAliveInterval(30000);
    expectedObject.setHandlerClassName("-");
    expectedObject.setParentId(150L);
    expectedObject.setDescription("test description");

    ObjectEqualityComparison.assertSubEquipmentEquals(expectedObject, cacheObject);

    // check DB update was successful
    SubEquipmentCacheObject dbObject = (SubEquipmentCacheObject) subEquipmentMapper.getItem(200L);
    ObjectEqualityComparison.assertSubEquipmentEquals(expectedObject, dbObject);

    // also check that the equipment, commfault and alive cache were updated
    Equipment equipment = equipmentCache.get(expectedObject.getParentId());
    assertTrue(equipment.getSubEquipmentIds().contains(expectedObject.getId()));
    // the alivetimer and commfault caches should reflect the changes
    assertNotNull(aliveTimerCache.get(expectedObject.getAliveTagId()));
    assertEquals(expectedObject.getId(), aliveTimerCache.get(cacheObject.getAliveTagId()).getRelatedId());
    assertNotNull(commFaultTagCache.get(expectedObject.getCommFaultTagId()));
    assertEquals(expectedObject.getId(), commFaultTagCache.get(cacheObject.getCommFaultTagId()).getEquipmentId());

    report = configurationLoader.applyConfiguration(20);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));

    verify(mockManager);
  }

  @Test
  public void testCreateSubEquipmentDataTag() throws IllegalAccessException, InstantiationException, NoSuchFieldException, ParserConfigurationException,
      TransformerException, NoSimpleValueParseException {

    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    // Create another DataTag attached to the SubEquipment (two already exist in
    // permanent test data)
    ConfigurationReport report = configurationLoader.applyConfiguration(99);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));

    SubEquipment subEquipment = subEquipmentCache.get(250L);
    assertNotNull(subEquipment);
    assertTrue(subEquipmentFacade.getDataTagIds(250L).size() == 3);

    DataTagCacheObject cacheObject = (DataTagCacheObject) dataTagCache.get(new Long(7000000));
    assertTrue(cacheObject.getSubEquipmentId() == 250L);
  }

  @Test
  public void testRemoveSubEquipmentDataTag() throws IllegalAccessException, InstantiationException, NoSuchFieldException, ParserConfigurationException,
      TransformerException, NoSimpleValueParseException {

    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport()).times(2);
    replay(mockManager);

    // Create another DataTag attached to the SubEquipment (two already exist in
    // permanent test data)
    ConfigurationReport report = configurationLoader.applyConfiguration(99);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));

    SubEquipment subEquipment = subEquipmentCache.get(250L);
    assertNotNull(subEquipment);
    assertTrue(aliveTimerCache.hasKey(subEquipment.getAliveTagId()));
    assertTrue(commFaultTagCache.hasKey(subEquipment.getCommFaultTagId()));
    assertTrue(controlTagCache.hasKey(subEquipment.getAliveTagId()));
    assertTrue(controlTagCache.hasKey(subEquipment.getStateTagId()));
    assertTrue(controlTagCache.hasKey(subEquipment.getCommFaultTagId()));
    assertTrue(subEquipmentFacade.getDataTagIds(250L).size() == 3);
    for (Long tagId : subEquipmentFacade.getDataTagIds(250L)) {
      assertTrue(dataTagCache.hasKey(tagId));
    }

    report = configurationLoader.applyConfiguration(21);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
    assertFalse(subEquipmentCache.hasKey(250L));
    assertNull(equipmentMapper.getItem(250L));

    assertFalse(aliveTimerCache.hasKey(subEquipment.getAliveTagId()));
    assertFalse(commFaultTagCache.hasKey(subEquipment.getCommFaultTagId()));

    assertFalse(controlTagCache.hasKey(subEquipment.getAliveTagId()));
    assertNull(controlTagMapper.getItem(subEquipment.getAliveTagId()));
    assertFalse(controlTagCache.hasKey(subEquipment.getStateTagId()));
    assertNull(controlTagMapper.getItem(subEquipment.getStateTagId()));
    assertFalse(controlTagCache.hasKey(subEquipment.getCommFaultTagId()));
    assertNull(controlTagMapper.getItem(subEquipment.getCommFaultTagId()));
    for (Long tagId : subEquipmentFacade.getDataTagIds(250L)) {
      assertFalse(dataTagCache.hasKey(tagId));
    }

    verify(mockManager);
  }

  @Test

  public void testRemoveSubEquipment() throws IllegalAccessException, InstantiationException, NoSuchFieldException, ParserConfigurationException,
      TransformerException, NoSimpleValueParseException {
    expect(mockManager.sendConfiguration(EasyMock.anyLong(), EasyMock.<List<Change>> anyObject())).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    // Create the subequipment
    ConfigurationReport report = configurationLoader.applyConfiguration(19);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));

    SubEquipment subEquipment = subEquipmentCache.get(200L);
    assertNotNull(subEquipment);
    assertTrue(aliveTimerCache.hasKey(subEquipment.getAliveTagId()));
    assertTrue(commFaultTagCache.hasKey(subEquipment.getCommFaultTagId()));
    assertTrue(controlTagCache.hasKey(subEquipment.getAliveTagId()));
    assertTrue(controlTagCache.hasKey(subEquipment.getStateTagId()));
    assertTrue(controlTagCache.hasKey(subEquipment.getCommFaultTagId()));

    reset(mockManager);
    expect(mockManager.sendConfiguration(EasyMock.anyLong(), EasyMock.<List<Change>> anyObject())).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);

    report = configurationLoader.applyConfiguration(98);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
    assertFalse(subEquipmentCache.hasKey(200L));
    assertNull(equipmentMapper.getItem(200L));

    assertFalse(aliveTimerCache.hasKey(subEquipment.getAliveTagId()));
    assertFalse(commFaultTagCache.hasKey(subEquipment.getCommFaultTagId()));

    assertFalse(controlTagCache.hasKey(subEquipment.getAliveTagId()));
    assertNull(controlTagMapper.getItem(subEquipment.getAliveTagId()));
    assertFalse(controlTagCache.hasKey(subEquipment.getStateTagId()));
    assertNull(controlTagMapper.getItem(subEquipment.getStateTagId()));
    assertFalse(controlTagCache.hasKey(subEquipment.getCommFaultTagId()));
    assertNull(controlTagMapper.getItem(subEquipment.getCommFaultTagId()));

    Equipment parentEquipment = equipmentCache.get(150L);
    for (Long id : parentEquipment.getSubEquipmentIds()) {
      assertTrue(id != subEquipment.getId());
    }

    verify(mockManager);
  }

  /**
   * Test the creation, update and removal of alarm.
   */

  @Test
  public void testCreateAlarmWithExistingDatatag() {
    replay(mockManager);

    // we  expect to send the alarm as the datatag is initialized.
    C2monCacheListener<Alarm> checker = EasyMock.createMock(C2monCacheListener.class);
    checker.notifyElementUpdated(EasyMock.isA(Alarm.class));
    EasyMock.expectLastCall().once();
    EasyMock.replay(checker);

    alarmCache.registerSynchronousListener(checker);

    DataTagCacheObject toInit = (DataTagCacheObject)dataTagCache.getCopy(200003L);
    toInit.setValue(Boolean.TRUE);
    toInit.getDataTagQuality().validate();
    dataTagCache.putQuiet(toInit);

    ConfigurationReport report = configurationLoader.applyConfiguration(22);
    verify(checker);
    ((AbstractCache) alarmCache).getCacheListeners().remove(checker);
  }


  /**
   * Test the creation, update and removal of alarm.
   */

  @Test
  public void testCreateUpdateAlarm() {
    replay(mockManager);


    // we do not expect to send the alarm as the datatag is unitialized.
    C2monCacheListener<Alarm> checker = EasyMock.createMock(C2monCacheListener.class);
    EasyMock.replay(checker);

    alarmCache.registerSynchronousListener(checker);

    ConfigurationReport report = configurationLoader.applyConfiguration(22);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));

    AlarmCacheObject cacheObject = (AlarmCacheObject) alarmCache.get(300000L);
    AlarmCacheObject expectedObject = new AlarmCacheObject(300000L);
    expectedObject.setDataTagId(200003L);
    expectedObject.setFaultFamily("fault family");
    expectedObject.setFaultMember("fault member");
    expectedObject.setFaultCode(223);
    expectedObject
        .setCondition(AlarmCondition
            .fromConfigXML("<AlarmCondition class=\"cern.c2mon.server.common.alarm.ValueAlarmCondition\"><alarm-value type=\"Boolean\">true</alarm-value></AlarmCondition>"));

    ObjectEqualityComparison.assertAlarmEquals(expectedObject, cacheObject);

    // also check that the Tag was updated
    Tag tag = tagLocationService.get(expectedObject.getTagId());
    assertTrue(tag.getAlarmIds().contains(expectedObject.getId()));

    // update should succeed
    report = configurationLoader.applyConfiguration(23);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
    cacheObject = (AlarmCacheObject) alarmCache.get(300000L);
    expectedObject.setFaultFamily("updated fault family");
    ObjectEqualityComparison.assertAlarmEquals(expectedObject, cacheObject);

    verify(mockManager);
    verify(checker);
    ((AbstractCache) alarmCache).getCacheListeners().remove(checker);
  }

  @Test
  public void testRemoveAlarm() {
    Alarm alarm = alarmCache.get(350000L);
    assertNotNull(alarm);
    assertTrue(alarmCache.hasKey(350000L));
    assertNotNull(alarmMapper.getItem(350000L));

    replay(mockManager);

    // we  expect to notify the cache listeners about a TERM alarm.
    C2monCacheListener<Alarm> checker = EasyMock.createMock(C2monCacheListener.class);
    checker.notifyElementUpdated(EasyMock.isA(Alarm.class));
    EasyMock.expectLastCall().once();
    EasyMock.replay(checker);
    alarmCache.registerSynchronousListener(checker);

    ConfigurationReport report = configurationLoader.applyConfiguration(24);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
    assertFalse(alarmCache.hasKey(350000L));
    assertNull(alarmMapper.getItem(350000L));
    Tag tag = tagLocationService.get(alarm.getTagId());
    assertFalse(tag.getAlarmIds().contains(alarm.getId()));
    verify(mockManager);
    verify(checker);
    ((AbstractCache) alarmCache).getCacheListeners().remove(checker);
  }

  @Test
  public void testCreateUpdateDeviceClass() throws ClassNotFoundException {
    replay(mockManager);

    ConfigurationReport report = configurationLoader.applyConfiguration(30);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));

    DeviceClassCacheObject expectedObject = (DeviceClassCacheObject) deviceClassMapper.getItem(10L);
    assertNotNull(expectedObject);

    DeviceClassCacheObject cacheObject = (DeviceClassCacheObject) deviceClassCache.get(10L);
    expectedObject = new DeviceClassCacheObject(10L, "TEST_DEVICE_CLASS_10", "Description of TEST_DEVICE_CLASS_10");

    List<Property> expectedProperties = new ArrayList<>();
    expectedProperties.add(new Property(10L, "cpuLoadInPercent", "The current CPU load in percent"));
    expectedProperties.add(new Property(11L, "responsiblePerson", "The person responsible for this device"));
    expectedProperties.add(new Property(12L, "someCalculations", "Some super awesome calculations"));

    List<Property> expectedFields = new ArrayList<>();
    expectedFields.add(new Property(10L, "field1", null));
    expectedFields.add(new Property(11L, "field2", null));

    expectedProperties.add(new Property(13L, "TEST_PROPERTY_WITH_FIELDS", "A property containing fields", expectedFields));

    List<Command> expectedCommands = new ArrayList<>();
    expectedCommands.add(new Command(10L, "TEST_COMMAND_1", "Description of TEST_COMMAND_1"));
    expectedCommands.add(new Command(11L, "TEST_COMMAND_2", "Description of TEST_COMMAND_2"));

    expectedObject.setProperties(expectedProperties);
    expectedObject.setCommands(expectedCommands);

    ObjectEqualityComparison.assertDeviceClassEquals(expectedObject, cacheObject);

    // Assert that the object from the DB is also the same
    DeviceClassCacheObject dbObject = (DeviceClassCacheObject) deviceClassMapper.getItem(10L);
    assertNotNull(dbObject);
    ObjectEqualityComparison.assertDeviceClassEquals(expectedObject, dbObject);

    // Update should succeed
    report = configurationLoader.applyConfiguration(31);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
    cacheObject = (DeviceClassCacheObject) deviceClassCache.get(10L);

    expectedProperties.add(new Property(14L, "numCores", "The number of CPU cores on this device"));
    expectedObject.setProperties(expectedProperties);
    ObjectEqualityComparison.assertDeviceClassEquals(expectedObject, cacheObject);

    verify(mockManager);
  }

  @Test

  public void testRemoveDeviceClass() {
    DeviceClass deviceClass = deviceClassCache.get(400L);
    assertNotNull(deviceClass);
    assertTrue(deviceClassCache.hasKey(400L));
    assertNotNull(deviceClassMapper.getItem(400L));

    replay(mockManager);

    ConfigurationReport report = configurationLoader.applyConfiguration(33);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));

    deviceClass = deviceClassCache.get(400L);
    assertTrue(((DeviceClassCacheObject) deviceClass).getDeviceIds().size() == 3);

    Device device = deviceCache.get(20L);
    assertNotNull(device);

    report = configurationLoader.applyConfiguration(32);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
    assertFalse(deviceClassCache.hasKey(400L));
    DeviceClass cacheObject = deviceClassMapper.getItem(400L);
    assertNull(cacheObject);

    verify(mockManager);
  }

  @Test

  public void testCreateUpdateDevice() throws ClassNotFoundException {
    replay(mockManager);

    ConfigurationReport report = configurationLoader.applyConfiguration(33);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));

    DeviceCacheObject cacheObject = (DeviceCacheObject) deviceCache.get(20L);
    DeviceCacheObject expectedObject = new DeviceCacheObject(20L, "TEST_DEVICE_20", 400L);

    List<DeviceProperty> expectedProperties = new ArrayList<>();
    expectedProperties.add(new DeviceProperty(1L, "cpuLoadInPercent", "987654", "tagId", null));
    expectedProperties.add(new DeviceProperty(2L, "responsiblePerson", "Mr. Administrator", "constantValue", null));
    expectedProperties.add(new DeviceProperty(3L, "someCalculations", "(#123 + #234) / 2", "clientRule", "Float"));

    List<DeviceProperty> expectedFields = new ArrayList<>();
    expectedFields.add(new DeviceProperty(1L, "field1", "987654", "tagId", null));
    expectedFields.add(new DeviceProperty(2L, "field2", "(#123 + #234) / 2", "clientRule", null));
    expectedProperties.add(new DeviceProperty(9L, "TEST_PROPERTY_WITH_FIELDS", "mappedProperty", expectedFields));

    List<DeviceCommand> expectedCommands = new ArrayList<>();
    expectedCommands.add(new DeviceCommand(1L, "TEST_COMMAND_1", "4287", "commandTagId", null));
    expectedCommands.add(new DeviceCommand(2L, "TEST_COMMAND_2", "4288", "commandTagId", null));

    expectedObject.setDeviceProperties(expectedProperties);
    expectedObject.setDeviceCommands(expectedCommands);

    ObjectEqualityComparison.assertDeviceEquals(expectedObject, cacheObject);

    // Update should succeed
    report = configurationLoader.applyConfiguration(34);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
    cacheObject = (DeviceCacheObject) deviceCache.get(20L);

    expectedProperties.add(new DeviceProperty(4L, "numCores", "4", "constantValue", "Integer"));
    expectedObject.setDeviceProperties(expectedProperties);
    ObjectEqualityComparison.assertDeviceEquals(expectedObject, cacheObject);

    verify(mockManager);
  }

  @Test
  public void testRemoveDevice() {
    Device device = deviceCache.get(300L);
    assertNotNull(device);
    assertTrue(deviceCache.hasKey(300L));
    assertNotNull(deviceMapper.getItem(300L));

    replay(mockManager);

    ConfigurationReport report = configurationLoader.applyConfiguration(35);

    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
    assertFalse(deviceCache.hasKey(300L));
    assertNull(deviceMapper.getItem(300L));

    verify(mockManager);
  }

  @Test
  @Ignore
  public void testConcurrentConfigRequestRejected() throws InterruptedException, IllegalAccessException, InstantiationException, NoSuchFieldException,
      ParserConfigurationException, TransformerException, NoSimpleValueParseException {

    final ConfigurationReport report;

    clusterCache.acquireWriteLockOnKey(JmsContainerManager.CONFIG_LOCK_KEY);
    try {
      ConcurrentConfigRequestor ccr = new ConcurrentConfigRequestor();
      Thread t = new Thread(ccr);
      t.start();
      t.join();

      report = ccr.report;

      assertTrue(report.toXML().contains(Status.FAILURE.toString()));
      assertTrue(report.toXML().contains("rejected since another configuration is still running"));

    } finally {
      clusterCache.releaseWriteLockOnKey(JmsContainerManager.CONFIG_LOCK_KEY);
    }
  }

  class ConcurrentConfigRequestor implements Runnable {
    ConfigurationReport report;

    @Override
    public void run() {
      report = configurationLoader.applyConfiguration(2);
    }
  }

  @Test
  @Ignore
  public void testGetConfigurationReportHeaders() {
    List<ConfigurationReportHeader> reports = configurationLoader.getConfigurationReports();
    assertFalse(reports.isEmpty());

    for (ConfigurationReportHeader report : reports) {
      assertNotNull(report.getName());
      assertNotNull(report.getId());
      assertNotNull(report.getStatus());
      assertNotNull(report.getStatusDescription());
    }
  }

  @Test
  @Ignore
  public void testGetConfigurationReports() {
    List<ConfigurationReport> reports = configurationLoader.getConfigurationReports(String.valueOf(1));
    assertFalse(reports.isEmpty());
    assertTrue(reports.size() > 1); // Config 1 gets run 3 times

    reports.addAll(configurationLoader.getConfigurationReports(String.valueOf(2)));
    assertTrue(reports.size() > 1); // Config 2 gets run once

    for (ConfigurationReport report : reports) {
      assertNotNull(report.getName());
      assertNotNull(report.getId());
      assertNotNull(report.getStatus());
      assertNotNull(report.getStatusDescription());
    }
  }

  @Test
  public void testProcessListConverter() {
    ProcessListConverter converter = new ProcessListConverter();

    String list = "[P_TEST01, P_TEST02]";
    Set<String> processList = converter.convert(list);
    assertTrue(processList.size() == 2);
    assertTrue(processList.contains("P_TEST01"));
    assertTrue(processList.contains("P_TEST02"));

    list = "[P_TEST01]";
    processList = converter.convert(list);
    assertTrue(processList.size() == 1);
    assertTrue(processList.contains("P_TEST01"));

    list = "[]";
    processList = converter.convert(list);
    assertTrue(processList.size() == 0);
  }
}
