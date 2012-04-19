package cern.c2mon.server.configuration;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.configuraton.helper.ObjectEqualityComparison;
import cern.tim.server.cache.AlarmCache;
import cern.tim.server.cache.AliveTimerCache;
import cern.tim.server.cache.CommFaultTagCache;
import cern.tim.server.cache.CommandTagCache;
import cern.tim.server.cache.ControlTagCache;
import cern.tim.server.cache.DataTagCache;
import cern.tim.server.cache.EquipmentCache;
import cern.tim.server.cache.ProcessCache;
import cern.tim.server.cache.ProcessFacade;
import cern.tim.server.cache.RuleTagCache;
import cern.tim.server.cache.SubEquipmentCache;
import cern.tim.server.cache.TagLocationService;
import cern.tim.server.cache.dbaccess.AlarmMapper;
import cern.tim.server.cache.dbaccess.CommandTagMapper;
import cern.tim.server.cache.dbaccess.ControlTagMapper;
import cern.tim.server.cache.dbaccess.DataTagMapper;
import cern.tim.server.cache.dbaccess.EquipmentMapper;
import cern.tim.server.cache.dbaccess.ProcessMapper;
import cern.tim.server.cache.dbaccess.RuleTagMapper;
import cern.tim.server.cache.dbaccess.SubEquipmentMapper;
import cern.tim.server.common.alarm.Alarm;
import cern.tim.server.common.alarm.AlarmCacheObject;
import cern.tim.server.common.command.CommandTagCacheObject;
import cern.tim.server.common.control.ControlTagCacheObject;
import cern.tim.server.common.datatag.DataTagCacheObject;
import cern.tim.server.common.equipment.Equipment;
import cern.tim.server.common.equipment.EquipmentCacheObject;
import cern.tim.server.common.process.Process;
import cern.tim.server.common.process.ProcessCacheObject;
import cern.tim.server.common.rule.RuleTagCacheObject;
import cern.tim.server.common.subequipment.SubEquipment;
import cern.tim.server.common.subequipment.SubEquipmentCacheObject;
import cern.tim.server.common.tag.Tag;
import cern.tim.server.daqcommunication.out.ProcessCommunicationManager;
import cern.tim.server.test.TestDataInserter;
import cern.tim.shared.client.command.RbacAuthorizationDetails;
import cern.tim.shared.client.configuration.ConfigConstants;
import cern.tim.shared.client.configuration.ConfigurationElementReport;
import cern.tim.shared.client.configuration.ConfigurationReport;
import cern.tim.shared.client.configuration.ConfigConstants.Entity;
import cern.tim.shared.client.configuration.ConfigConstants.Status;
import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.common.NoSimpleValueParseException;
import cern.tim.shared.common.datatag.DataTagAddress;
import cern.tim.shared.common.datatag.DataTagConstants;
import cern.tim.shared.common.datatag.DataTagQualityImpl;
import cern.tim.shared.common.datatag.DataTagValueDictionary;
import cern.tim.shared.common.datatag.address.HardwareAddressFactory;
import cern.tim.shared.daq.config.Change;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.config.ConfigurationChangeEventReport;
import cern.tim.shared.daq.config.ChangeReport.CHANGE_STATE;
import ch.cern.tim.shared.alarm.AlarmCondition;
import ch.cern.tim.shared.datatag.address.impl.OPCHardwareAddressImpl;

/**
 * Component/integration tests of the configuration module (integrates
 * the cache modules, but mocks the daqcommunication-out module).
 *  
 * <p>These tests assume the test data is present before the test is run.
 * The data is removed and inserted after every test, ready to run the next
 * one. If a test is interrupted, will need to run twice to correct this.
 * 
 * <p>(Notice the data must be in the DB *before* the context is loaded!)
 *  
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/configuration/config/server-configuration-oracle-test.xml" })
//@TransactionConfiguration(transactionManager = "cacheTransactionManager", defaultRollback = true)
public class ConfigurationLoaderTest implements ApplicationContextAware {

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
  private TagLocationService tagLocationService;
  
  @Autowired
  private ProcessFacade processFacade;
  
  /**
   * Needs explicitly starting.
   */
  private ApplicationContext context;
  
  @Autowired
  private TestDataInserter testDataInserter;
  
  /**
   * Clears DB of failed previous tests and resets the
   * mock before each test.
   * @throws IOException 
   */
  @Before  
  public void beforeTest() throws IOException {
    ((AbstractApplicationContext) context).start();
    //clean DB
    dataTagMapper.deleteDataTag(5000000L);
    controlTagMapper.deleteControlTag(500L);
    commandTagMapper.deleteCommandTag(10000L);
    ruleTagMapper.deleteRuleTag(50100L);
    equipmentMapper.deleteEquipment(110L);
    controlTagMapper.deleteControlTag(501L);
    processMapper.deleteProcess(2L);
    subEquipmentMapper.deleteSubEquipment(200L);
    alarmMapper.deleteAlarm(300000L);
    
    //make sure Process is "running" (o.w. nothing is sent to DAQ)
    processFacade.start(processCache.get(50L), "hostname", new Timestamp(System.currentTimeMillis()));
    
    //reset mock
    reset(mockManager);    
  }
  
  @After
  public void afterTest() throws IOException {    
    dataTagMapper.deleteDataTag(5000000L);
    controlTagMapper.deleteControlTag(500L);
    commandTagMapper.deleteCommandTag(10000L);
    ruleTagMapper.deleteRuleTag(50100L);
    equipmentMapper.deleteEquipment(110L);
    controlTagMapper.deleteControlTag(501L);
    processMapper.deleteProcess(2L);
    subEquipmentMapper.deleteSubEquipment(200L);
    alarmMapper.deleteAlarm(300000L);
    testDataInserter.removeTestData();
    testDataInserter.insertTestData();
  }
  
  @Test 
  @DirtiesContext //TODO why?  
  public void testCreateUpdateRemoveControlTag() {
    //create
    ConfigurationReport report = configurationLoader.applyConfiguration(2);
    System.out.println(report.toXML());
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty()); //empty because no process/equipment points to this control tag
      
    ControlTagCacheObject cacheObject = (ControlTagCacheObject) controlTagCache.get(500L);
    
    //corresponds to data inserted using SQL file
    ControlTagCacheObject expectedObject = new ControlTagCacheObject();
    expectedObject.setId(new Long(500));  //must be non null in DB
    expectedObject.setName("Process status"); //non null    
    expectedObject.setMode(DataTagConstants.MODE_TEST); //non null
    expectedObject.setDataType("Integer"); // non null
    expectedObject.setDescription("test");
    expectedObject.setMinValue(new Integer(12));
    expectedObject.setMaxValue(new Integer(22));

    expectedObject.setLogged(false); //null allowed

    expectedObject.setDataTagQuality(new DataTagQualityImpl());
    
    ObjectEqualityComparison.assertDataTagConfigEquals(expectedObject, cacheObject); //object correctly loaded to cache
    
    //test update of control tag
    report = configurationLoader.applyConfiguration(6);
    System.out.println(report.toXML());
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    //ControlTagCacheObject updatedCacheObject = (ControlTagCacheObject) controlTagCache.get(500L);
    expectedObject.setDescription("modified description");
    ObjectEqualityComparison.assertDataTagConfigEquals(expectedObject, cacheObject);
    
  }
  
  @Test
  @DirtiesContext
  public void testRemoveControlTag() {
    //check as expected before test
    assertTrue(controlTagCache.hasKey(1250L));
    assertNotNull(controlTagMapper.getItem(1250L));
    
    //run test
    ConfigurationReport report = configurationLoader.applyConfiguration(8);
    
    //check outcome
    System.out.println(report.toXML());
    assertEquals(Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty()); //empty because no process/equipment points to this control tag
    assertFalse(controlTagCache.hasKey(1250L));
    assertNull(controlTagMapper.getItem(1250L));   
  }
  
  @Test
  @DirtiesContext
  public void testCreateAndUpdateCommandTag() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException, NoSuchFieldException, NoSimpleValueParseException {
    //the mocked ProcessCommmunicationManager can return an empty report (expect 3 calls)
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);
   
    ConfigurationReport report = configurationLoader.applyConfiguration(3);
    System.out.println(report.toXML());
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    CommandTagCacheObject cacheObject = (CommandTagCacheObject) commandTagCache.get(10000L);
    
    CommandTagCacheObject expectedObject = new CommandTagCacheObject(10000L, "Test CommandTag", "test description", "String", DataTagConstants.MODE_TEST);
    //expectedObject.setAuthorizedHostsPattern("*");    
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
    expectedObject.setHardwareAddress(HardwareAddressFactory.getInstance().fromConfigXML("<HardwareAddress class=\"ch.cern.tim.shared.datatag.address.impl.OPCHardwareAddressImpl\"><opc-item-name>PLC_B_CMD_ACQ_DEF_5A6</opc-item-name><command-pulse-length>100</command-pulse-length></HardwareAddress>"));   
    ObjectEqualityComparison.assertCommandTagEquals(expectedObject, cacheObject);
   
    //test update
    report = configurationLoader.applyConfiguration(5);
    System.out.println(report.toXML());
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    
    CommandTagCacheObject cacheObjectUpdated = (CommandTagCacheObject) commandTagCache.get(10000L);
        
    expectedObject.setName("Test CommandTag Updated");
    expectedObject.getAuthorizationDetails().setRbacClass("new RBAC class");
    expectedObject.getAuthorizationDetails().setRbacDevice("new RBAC device");
    expectedObject.setHardwareAddress(HardwareAddressFactory.getInstance().fromConfigXML("<HardwareAddress class=\"ch.cern.tim.shared.datatag.address.impl.OPCHardwareAddressImpl\"><opc-item-name>PLC_B_CMD_ACQ_DEF_5A6</opc-item-name><command-pulse-length>150</command-pulse-length></HardwareAddress>"));
    ObjectEqualityComparison.assertCommandTagEquals(expectedObject, cacheObjectUpdated);
  }
  
  @Test
  @DirtiesContext
  public void testRemoveCommand() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException, NoSuchFieldException, NoSimpleValueParseException {  
    //check as expected
    assertTrue(commandTagCache.hasKey(11000L));
    assertNotNull(commandTagMapper.getItem(11000L));
    EasyMock.expect(mockManager.sendConfiguration(EasyMock.isA(Long.class), EasyMock.isA(List.class))).andReturn(new ConfigurationChangeEventReport());
        
    //rung test
    replay(mockManager);
    ConfigurationReport report = configurationLoader.applyConfiguration(9);
    
    //check successful
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertFalse(commandTagCache.hasKey(11000L));
    assertNull(commandTagMapper.getItem(11000L));
    verify(mockManager);
  }
  
  @Test
  @DirtiesContext
  public void testCreateAndUpdateDataTag() throws ConfigurationException, InterruptedException, ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException, NoSuchFieldException, NoSimpleValueParseException {
    //the mocked ProcessCommmunicationManager can return an empty report (expect 3 calls for create, update and remove)    
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);
    
    ConfigurationReport report = configurationLoader.applyConfiguration(1);
    System.out.println(report.toXML());
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    
    DataTagCacheObject cacheObject = (DataTagCacheObject) dataTagCache.get(new Long(5000000));
    
    //corresponds to data inserted using SQL file
    DataTagCacheObject expectedObject = new DataTagCacheObject();
    expectedObject.setId(new Long(5000000));  //must be non null in DB
    expectedObject.setName("Config_test_datatag"); //non null
    expectedObject.setDescription("test description config datatag");
    expectedObject.setMode(DataTagConstants.MODE_TEST); //non null
    expectedObject.setDataType("Float"); // non null
    //cacheObject.setTopic("tim.testdatatag.XADDRESS");
    expectedObject.setLogged(false); //null allowed
    expectedObject.setUnit("config unit m/sec");
    expectedObject.setDipAddress("testConfigDIPaddress");
    expectedObject.setJapcAddress("testConfigJAPCaddress");
    //expectedObject.setValue(Boolean.TRUE);
    //expectedObject.setValueDescription("test config value description");
    expectedObject.setSimulated(false); //null allowed
    expectedObject.setEquipmentId(new Long(150)); //need test equipment inserted 
    expectedObject.setProcessId(50L);
    expectedObject.setMinValue(new Float(12.2));
    expectedObject.setMaxValue(new Float(23.3));
    expectedObject.setValueDictionary(new DataTagValueDictionary());
    expectedObject.setAddress(new DataTagAddress(new OPCHardwareAddressImpl("CW_TEMP_IN_COND3")));
    expectedObject.setDataTagQuality(new DataTagQualityImpl());
    //expectedObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis())); //should be set to creation time, so not null
    //expectedObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    //expectedObject.setRuleIdsString("1234,3456"); //NO: never loaded at reconfiguration of datatag, but only when a new rule is added
    
    ObjectEqualityComparison.assertDataTagConfigEquals(expectedObject, cacheObject);
    
    Equipment equipment = equipmentCache.get(cacheObject.getEquipmentId());
    equipment.getWriteLock().lock();
    //check equipment now has datatag in list
    assertTrue(equipmentCache.get(cacheObject.getEquipmentId()).getDataTagIds().contains(5000000L));
    equipment.getWriteLock().unlock();
    
    //test update of this datatag
    report = configurationLoader.applyConfiguration(4);
    System.out.println(report.toXML());
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    DataTagCacheObject updatedCacheObject = (DataTagCacheObject) dataTagCache.get(5000000L);
       
    expectedObject.setJapcAddress("testConfigJAPCaddress2");    
    expectedObject.setMaxValue(new Float(26));   
    expectedObject.setAddress(new DataTagAddress(new OPCHardwareAddressImpl("CW_TEMP_IN_COND4")));
    
    ObjectEqualityComparison.assertDataTagConfigEquals(expectedObject, updatedCacheObject);
    equipment = equipmentCache.get(cacheObject.getEquipmentId());
    
    equipment.getWriteLock().lock();
    System.out.println(equipment.getDataTagIds().toString());
    System.out.println(equipmentCache.get(cacheObject.getEquipmentId()).getDataTagIds().toString());    
    equipment.getWriteLock().unlock(); 
  }
  
  @Test
  @DirtiesContext
  public void testRemoveDataTag() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException, NoSuchFieldException, NoSimpleValueParseException {     
    //check data as expected
    Long tagId = 200001L;
    DataTagCacheObject cacheObject = (DataTagCacheObject) dataTagCache.get(200001L);    
    assertNotNull(cacheObject);
    assertNotNull(dataTagMapper.getItem(tagId));
    
    EasyMock.expect(mockManager.sendConfiguration(EasyMock.isA(Long.class), EasyMock.isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    
    replay(mockManager);
    //run test    
    ConfigurationReport report = configurationLoader.applyConfiguration(7);  

    //check successful
    System.out.println(report.toXML());
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());   
    assertFalse(dataTagCache.hasKey(tagId));
    assertNull(dataTagMapper.getItem(tagId));
    //tag id is no longer in equipment    
    assertFalse(equipmentCache.get(cacheObject.getEquipmentId()).getDataTagIds().contains(tagId));
    
    verify(mockManager);
  }
  
  /**
   * No communication should take place with the DAQs during rule configuration.
   * @throws InterruptedException
   * @throws NoSimpleValueParseException 
   * @throws NoSuchFieldException 
   * @throws TransformerException 
   * @throws InstantiationException 
   * @throws IllegalAccessException 
   * @throws ParserConfigurationException 
   */
  @Test
  @DirtiesContext
  public void testCreateUpdateRemoveRuleTag() throws InterruptedException, ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException, NoSuchFieldException, NoSimpleValueParseException {
    //the mocked ProcessCommmunicationManager will be called once when creating the datatag to base the rule on
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);
    
    //insert datatag to base rule on
    configurationLoader.applyConfiguration(1);
    ConfigurationReport report = configurationLoader.applyConfiguration(10);
    System.out.println(report.toXML());
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    RuleTagCacheObject cacheObject = (RuleTagCacheObject) ruleTagCache.get(50100L);
    
    RuleTagCacheObject expectedObject = new RuleTagCacheObject(50100L);    
    expectedObject.setName("test ruletag"); //non null
    expectedObject.setDescription("test ruletag description");
    expectedObject.setMode(DataTagConstants.MODE_MAINTENANCE); //non null
    expectedObject.setDataType("Float"); // non null
    expectedObject.setLogged(true); //null allowed
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
    
    //update ruletag
    expectedObject.setJapcAddress("newTestConfigJAPCaddress");
    expectedObject.setRuleText("true[0]");
    expectedObject.setProcessIds(Collections.EMPTY_SET);
    expectedObject.setEquipmentIds(Collections.EMPTY_SET);
    report = configurationLoader.applyConfiguration(11);
    System.out.println(report.toXML());
    RuleTagCacheObject updatedCacheObject = (RuleTagCacheObject) ruleTagCache.get(50100L);
    ObjectEqualityComparison.assertRuleTagConfigEquals(expectedObject, updatedCacheObject);
    
    verify(mockManager);
  }
  
  @Test
  @DirtiesContext
  public void testRemoveRuleTag() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException, NoSuchFieldException, NoSimpleValueParseException {
    
    replay(mockManager);
    
    //remove ruletag
    ConfigurationReport report = configurationLoader.applyConfiguration(12);
    System.out.println(report.toXML());
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertFalse(ruleTagCache.hasKey(60007L));
    assertNull(ruleTagMapper.getItem(60007L));
    
    //dependent rules removed, e.g.
    assertFalse(ruleTagCache.hasKey(60009L));
    assertNull(ruleTagMapper.getItem(60009L));
    
    verify(mockManager);
  }
  
  /**
   * Tests a dependent rule is removed when a tag is.
   */
  @DirtiesContext
  @Test
  public void testRuleRemovedOnTagRemoval() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException, NoSuchFieldException, NoSimpleValueParseException {
    Long tagId = 200001L;
    Long ruleId1 = 60000L; //two of the rules that should be removed
    Long ruleId2 = 59999L;
    assertTrue(ruleTagCache.hasKey(ruleId1));
    assertNotNull(ruleTagMapper.getItem(ruleId1));
    assertTrue(ruleTagCache.hasKey(ruleId2));
    assertNotNull(ruleTagMapper.getItem(ruleId2));
    assertTrue(dataTagCache.hasKey(tagId));
    assertNotNull(dataTagMapper.getItem(tagId));
    
    //for tag removal
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());

    replay(mockManager);
    
    //test removal of tag 20004L removes the rule also
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
   * Tests that a tag removal does indeed remove an associated alarm.
   * @throws NoSimpleValueParseException 
   * @throws NoSuchFieldException 
   * @throws TransformerException 
   * @throws InstantiationException 
   * @throws IllegalAccessException 
   * @throws ParserConfigurationException 
   */
  @Test
  @DirtiesContext
  public void testAlarmRemovedOnTagRemoval() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException, NoSuchFieldException, NoSimpleValueParseException {   
    replay(mockManager);
    
    //test removal of (rule)tag 60000 removes the alarm also
    configurationLoader.applyConfiguration(27); 
    assertFalse(alarmCache.hasKey(350000L));
    assertNull(alarmMapper.getItem(350000L));
    assertFalse(ruleTagCache.hasKey(60000L));
    assertNull(ruleTagMapper.getItem(60000L));
    verify(mockManager);
  }
  
  /**
   * Test the creation, update and removal of equipment.
   * @throws NoSimpleValueParseException 
   * @throws NoSuchFieldException 
   * @throws TransformerException 
   * @throws InstantiationException 
   * @throws IllegalAccessException 
   * @throws ParserConfigurationException 
   */
  @Test
  @DirtiesContext
  public void testCreateUpdateEquipment() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException, NoSuchFieldException, NoSimpleValueParseException {
    //called once when updating the equipment; 
    //mock returns a list with the correct number of SUCCESS ChangeReports
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
    }).times(2); //twice: once for create, another for update
        
    replay(mockManager);
    
    ConfigurationReport report = configurationLoader.applyConfiguration(13);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(Status.OK, report.getStatus()); //ok as DAQ handles Equipment creation
    assertFalse(report.getProcessesToReboot().contains("P_TESTHANDLER03"));
    System.out.println(report.toXML());
    
    EquipmentCacheObject cacheObject = (EquipmentCacheObject) equipmentCache.get(110L);
    EquipmentCacheObject expectedObject = new EquipmentCacheObject(110L);
    expectedObject.setName("E_CONFIG_TEST");
    expectedObject.setAddress("serverHostName=VGTCVENTTEST");
    expectedObject.setStateTagId(1250L);
    expectedObject.setCommFaultTagId(1252L);    
    expectedObject.setHandlerClassName("ch.cern.tim.driver.");
    expectedObject.setProcessId(50L);
    expectedObject.setDescription("test description");
    
    ObjectEqualityComparison.assertEquipmentEquals(expectedObject, cacheObject);
    
    //also check that the process, commfault and alive cache were updated
    Process process = processCache.get(expectedObject.getProcessId());
    //check process is running
    ((ProcessCacheObject) process).setRequiresReboot(false);
    assertFalse(process.getRequiresReboot());
    assertTrue(process.getEquipmentIds().contains(expectedObject.getId()));
    //the alivetimer and commfault have overriden those already in the cache (check reference to the equipment has changed)   
    assertNotNull(commFaultTagCache.get(expectedObject.getCommFaultTagId()));
    assertEquals(expectedObject.getId(), commFaultTagCache.get(cacheObject.getCommFaultTagId()).getEquipmentId());
    
    //update (creates controltag and updates equipment) - should succeed 
    report = configurationLoader.applyConfiguration(25);
    System.out.println(report.toXML());
    //expect 2 top elements (control and equipment, with control first)
    //  equipment report should have 2 sub-reports, control report none
    List<ConfigurationElementReport> topList = report.getElementReports();
    assertEquals(2, topList.size());
    assertEquals(topList.get(0).getEntity(), Entity.CONTROLTAG);
    assertEquals(topList.get(1).getEntity(), Entity.EQUIPMENT);
    assertEquals(0, topList.get(0).getSubreports().size());
    assertEquals(2, topList.get(1).getSubreports().size());   
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    //should contain both a controltag update and equipment update
    //  (note the DAQ response is generated automatically by the mock)   
    assertTrue(report.toXML().contains("CONTROLTAG"));
    assertTrue(report.toXML().contains("EQUIPMENT"));
    
    //checks restart status is correctly set (DAQ call is mocked as success for equipment update)
    assertEquals(Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    
    cacheObject = (EquipmentCacheObject) equipmentCache.get(110L);
    expectedObject.setDescription("updated description");
    expectedObject.setAddress("serverHostName=VGTCVENTTEST;test");
    expectedObject.setAliveTagId(501L);
    
    ObjectEqualityComparison.assertEquipmentEquals(expectedObject, cacheObject);
    
    //check alive timer reference is updated in DB
    assertEquals(new Long(501), equipmentMapper.getItem(110L).getAliveTagId());
    //also expect alivetimercache to have element 501:
    assertNotNull(aliveTimerCache.get(501L));
    
    verify(mockManager);
  }
  
  /**
   * Test equipment and control tags are removed correctly.
   * @throws NoSimpleValueParseException 
   * @throws NoSuchFieldException 
   * @throws TransformerException 
   * @throws InstantiationException 
   * @throws IllegalAccessException 
   * @throws ParserConfigurationException 
   */
  @Test
  public void testRemoveEquipement() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException, NoSuchFieldException, NoSimpleValueParseException {
    //check as expected
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
    //remove equipment
    //remove completes successfully; both Equipment and ControlTags are removed
    ConfigurationReport report = configurationLoader.applyConfiguration(15);
    System.out.println(report.toXML());
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));    
    assertEquals(Status.OK, report.getStatus()); //DAQ deals with Equipment removal
    assertFalse(report.getProcessesToReboot().contains("P_TESTHANDLER03"));
    assertFalse(equipmentCache.hasKey(150L));
    assertNull(equipmentMapper.getItem(150L));
    //commfault and alive should no longer be in cache 
    assertFalse(aliveTimerCache.hasKey(equipment.getAliveTagId())); 
    assertFalse(commFaultTagCache.hasKey(equipment.getCommFaultTagId()));
    verify(mockManager);
  }
  
  @Test
  @DirtiesContext
  public void testCreateUpdateRemoveProcess() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException, NoSuchFieldException, NoSimpleValueParseException {
    //currently no configuration required on DAQ layer for Process configuration options
    replay(mockManager);
    
    ConfigurationReport report = configurationLoader.applyConfiguration(16);
    System.out.println(report.toXML());
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    
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
    
    //update    
    report = configurationLoader.applyConfiguration(17);
    System.out.println(report.toXML());
    
    cacheObject = (ProcessCacheObject) processCache.get(2L);
    expectedObject.setDescription("updated description");
    expectedObject.setMaxMessageDelay(4000);
    
    ObjectEqualityComparison.assertProcessEquals(expectedObject, cacheObject);
       
    verify(mockManager);
  }
  
  /**
   * Tests the removal of a process succeeds, with dependent rules and alarms.
   * Relies on permanent test data in test account and must be rolled back.
   * No changes should be sent to the DAQ layer.
   */
  @Test
  @DirtiesContext
  public void testRemoveProcess() {
    //stop DAQ else remove not allowed
    processFacade.stop(processCache.get(50L), new Timestamp(System.currentTimeMillis()));
    
    replay(mockManager);
    
    ConfigurationReport report = configurationLoader.applyConfiguration(28);
    System.out.println(report.toXML());
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    verify(mockManager);
    //check process, tag, rules and alarms are gone
    assertFalse(processCache.hasKey(50L));
    assertNull(processMapper.getItem(50L));
    assertFalse(equipmentCache.hasKey(150L));
    assertNull(equipmentMapper.getItem(150L));
    //check couple of rules
    assertFalse(ruleTagCache.hasKey(60010L));
    assertNull(ruleTagMapper.getItem(60010L));
    assertFalse(ruleTagCache.hasKey(60002L));
    assertNull(ruleTagMapper.getItem(60002L));
    //tags
    assertFalse(dataTagCache.hasKey(200002L));
    assertNull(dataTagMapper.getItem(200002L));
    assertFalse(dataTagCache.hasKey(200003L));
    assertNull(dataTagMapper.getItem(200003L));
    //control tags
    assertFalse(controlTagCache.hasKey(1220L));
    assertNull(controlTagMapper.getItem(1220L));
    assertFalse(controlTagCache.hasKey(1221L));
    assertNull(controlTagMapper.getItem(1221L)); 
    //equipment control tags
    assertFalse(controlTagCache.hasKey(1222L));
    assertNull(controlTagMapper.getItem(1222L));
    assertFalse(controlTagCache.hasKey(1223L));
    assertNull(controlTagMapper.getItem(1223L)); 
    //equipment commfault
    assertFalse(commFaultTagCache.hasKey(1223L));
    //process alive
    assertFalse(aliveTimerCache.hasKey(1221L));
    //alarms
    assertFalse(alarmCache.hasKey(350000L));
    assertNull(alarmMapper.getItem(350000L));
    assertFalse(alarmCache.hasKey(350001L));
    assertNull(alarmMapper.getItem(350001L));
    verify(mockManager);
  }
  
  
  /**
   * Tests the removal of a process succeeds, with dependent rules and alarms.
   * Relies on permanent test data in test account and must be rolled back.
   * No changes should be sent to the DAQ layer.
   * @throws NoSimpleValueParseException 
   * @throws NoSuchFieldException 
   * @throws TransformerException 
   * @throws InstantiationException 
   * @throws IllegalAccessException 
   * @throws ParserConfigurationException 
   */
  @Test
  @DirtiesContext
  public void testRemoveEquipmentDependentObjects() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException, NoSuchFieldException, NoSimpleValueParseException {
    
    //expect equipment remove message to DAQ
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
    //check equipment, tag, rules and alarms are gone   
    assertFalse(equipmentCache.hasKey(150L));
    assertNull(equipmentMapper.getItem(150L));
    //check couple of rules
    assertFalse(ruleTagCache.hasKey(60005L));
    assertNull(ruleTagMapper.getItem(60005L));
    assertFalse(ruleTagCache.hasKey(60004L));
    assertNull(ruleTagMapper.getItem(60004L));
    //tags
    assertFalse(dataTagCache.hasKey(200001L));
    assertNull(dataTagMapper.getItem(200001L));
    assertFalse(dataTagCache.hasKey(200004L));
    assertNull(dataTagMapper.getItem(200004L));
    //control tags
    assertFalse(controlTagCache.hasKey(1222L));
    assertNull(controlTagMapper.getItem(1222L));
    assertFalse(controlTagCache.hasKey(1223L));
    assertNull(controlTagMapper.getItem(1223L)); 
    assertFalse(controlTagCache.hasKey(1224L));
    assertNull(controlTagMapper.getItem(1224L));
    //alivetimer & commfault
    assertFalse(aliveTimerCache.hasKey(1224L));    
    assertFalse(commFaultTagCache.hasKey(1223L));    
    //alarms
    assertFalse(alarmCache.hasKey(350000L));
    assertNull(alarmMapper.getItem(350000L));
    assertFalse(alarmCache.hasKey(350001L));
    assertNull(alarmMapper.getItem(350001L));
    
    verify(mockManager);
  }
  
  /**
   * Test the creation, update and removal of equipment.
   */  
  @DirtiesContext
  @Test
  public void testCreateUpdateSubEquipment() {
    //update unsuccessful so no message sent to DAQ
    replay(mockManager);
   
    ConfigurationReport report = configurationLoader.applyConfiguration(19);
    System.out.println(report.toXML());
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));    
    
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
    
    //check DB update was successful
    SubEquipmentCacheObject dbObject = (SubEquipmentCacheObject) subEquipmentMapper.getItem(200L);
    ObjectEqualityComparison.assertSubEquipmentEquals(expectedObject, dbObject);
    
    //also check that the equipment, commfault and alive cache were updated
    Equipment equipment = equipmentCache.get(expectedObject.getParentId());
    assertTrue(equipment.getSubEquipmentIds().contains(expectedObject.getId()));
    //the alivetimer and commfault caches should reflect the changes
    assertNotNull(aliveTimerCache.get(expectedObject.getAliveTagId())); 
    assertEquals(expectedObject.getId(), aliveTimerCache.get(cacheObject.getAliveTagId()).getRelatedId());
    assertNotNull(commFaultTagCache.get(expectedObject.getCommFaultTagId()));
    assertEquals(expectedObject.getId(), commFaultTagCache.get(cacheObject.getCommFaultTagId()).getEquipmentId());
    
    //update should fail as try to change parent id 
    boolean failed = false;
    try {
      report = configurationLoader.applyConfiguration(20);     
    } catch (cern.tim.shared.client.configuration.ConfigurationException e) {
      System.out.println(e.getConfigurationReport());
      failed = true;
    }      
    assertFalse(failed);   
       
    verify(mockManager);
  }
  
  @Test
  @DirtiesContext
  public void testRemoveSubEquipment() {
    SubEquipment subEquipment = subEquipmentCache.get(250L);
    assertNotNull(subEquipment);
    assertTrue(aliveTimerCache.hasKey(subEquipment.getAliveTagId()));
    assertTrue(commFaultTagCache.hasKey(subEquipment.getCommFaultTagId()));
    assertTrue(controlTagCache.hasKey(subEquipment.getAliveTagId()));
    assertTrue(controlTagCache.hasKey(subEquipment.getStateTagId()));
    assertTrue(controlTagCache.hasKey(subEquipment.getCommFaultTagId()));
    
    replay(mockManager);
    
    ConfigurationReport report = configurationLoader.applyConfiguration(21);
    System.out.println(report.toXML());
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
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
    
    verify(mockManager);
  }
  
  /**
   * Test the creation, update and removal of alarm.
   */  
  @DirtiesContext
  @Test
  public void testCreateUpdateAlarm() {
    replay(mockManager);
    
    ConfigurationReport report = configurationLoader.applyConfiguration(22);
    System.out.println(report.toXML());
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));    
    
    AlarmCacheObject cacheObject = (AlarmCacheObject) alarmCache.get(300000L);
    AlarmCacheObject expectedObject = new AlarmCacheObject(300000L);
    expectedObject.setDataTagId(200003L);  
    expectedObject.setFaultFamily("fault family");
    expectedObject.setFaultMember("fault member");
    expectedObject.setFaultCode(223);
    expectedObject.setCondition(AlarmCondition.fromConfigXML("<AlarmCondition class=\"ch.cern.tim.shared.alarm.ValueAlarmCondition\"><alarm-value type=\"Boolean\">true</alarm-value></AlarmCondition>"));
    
    ObjectEqualityComparison.assertAlarmEquals(expectedObject, cacheObject);
    
    //also check that the Tag was updated
    Tag tag = tagLocationService.get(expectedObject.getTagId());    
    assertTrue(tag.getAlarmIds().contains(expectedObject.getId()));    
    
    //update should succeed
    report = configurationLoader.applyConfiguration(23);
    System.out.println(report.toXML());
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));    
    cacheObject = (AlarmCacheObject) alarmCache.get(300000L);
    expectedObject.setFaultFamily("updated fault family");
    ObjectEqualityComparison.assertAlarmEquals(expectedObject, cacheObject);
    
    verify(mockManager);
  }
  
  @Test
  @DirtiesContext
  public void testRemoveAlarm() {
    Alarm alarm = alarmCache.get(350000L);
    assertNotNull(alarm);
    assertTrue(alarmCache.hasKey(350000L));
    assertNotNull(alarmMapper.getItem(350000L));
    
    replay(mockManager);
    
    ConfigurationReport report = configurationLoader.applyConfiguration(24);
    System.out.println(report.toXML());
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertFalse(alarmCache.hasKey(350000L));
    assertNull(alarmMapper.getItem(350000L));    
    Tag tag = tagLocationService.get(alarm.getTagId());
    assertFalse(tag.getAlarmIds().contains(alarm.getId()));
    verify(mockManager);
  }

  @Override
  public void setApplicationContext(ApplicationContext arg0) throws BeansException {
    context = arg0;
  }
  
}
