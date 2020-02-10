package cern.c2mon.server.configuration.loader;

import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.*;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.configuration.helper.ObjectEqualityComparison;
import cern.c2mon.server.configuration.parser.util.ConfigurationProcessUtil;
import cern.c2mon.server.configuration.util.TestConfigurationProvider;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.converter.ProcessListConverter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.Properties;
import java.util.Set;

import static cern.c2mon.server.configuration.parser.util.ConfigurationProcessUtil.buildCreateAllFieldsProcess;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.*;

public class ProcessConfigTest extends ConfigurationCacheLoaderTest<Process> {

  @Autowired
  private C2monCache<Process> processCache;

  @Autowired
  private ProcessMapper processMapper;

  @Autowired
  private C2monCache<AliveTag> aliveTimerCache;

  @Autowired
  private C2monCache<CommFaultTag> commFaultTagCache;

  @Autowired
  private C2monCache<DataTag> dataTagCache;

  @Autowired
  private C2monCache<RuleTag> ruleTagCache;

  @Autowired
  private RuleTagMapper ruleTagMapper;

  @Autowired
  private C2monCache<Equipment> equipmentCache;

  @Autowired
  private EquipmentMapper equipmentMapper;

  @Autowired
  private AlarmMapper alarmMapper;

  @Autowired
  private DataTagMapper dataTagMapper;

  @Autowired
  private C2monCache<Alarm> alarmCache;

  @Autowired
  private ProcessService processService;

  @Test
  public void createProcess() {
    Properties expectedProperties = new Properties();
    cern.c2mon.shared.client.configuration.api.process.Process process = buildCreateAllFieldsProcess(1L, expectedProperties);
    expectedProperties.setProperty("stateTagId", "300000");
    expectedProperties.setProperty("aliveTagId", "300001");

    Configuration configuration = new Configuration();
    configuration.addEntity(process);

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result and caches
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.RESTART, report.getStatus());
    assertEquals(3, report.getElementReports().size());

    assertTrue(processCache.containsKey(1L));
    assertNotNull(processMapper.getItem(1L));
    assertTrue(aliveTimerCache.containsKey(300_000L));

    // Check Process in the cache
    ProcessCacheObject cacheObjectProcess = (ProcessCacheObject) processCache.get(1L);
    ProcessCacheObject expectedObjectProcess = cacheObjectFactory.buildProcessCacheObject(1L, process);

    ObjectEqualityComparison.assertProcessEquals(expectedObjectProcess, cacheObjectProcess);

    verify(mockManager);
  }


  @Test
  public void updateProcess() {
    // SETUP:
    Configuration createProcess = TestConfigurationProvider.createProcess();
    configurationLoader.applyConfiguration(createProcess);
    processService.start(5L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // Build configuration to add the test equipment
    cern.c2mon.shared.client.configuration.api.process.Process process = ConfigurationProcessUtil.buildUpdateProcessWithAllFields(5L, null);
    Configuration configuration = new Configuration();
    configuration.addEntity(process);

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.RESTART, report.getStatus());
    assertEquals(1, report.getElementReports().size());

    // get cacheObject from the cache and compare to the an expected cacheObject
    ProcessCacheObject cacheObjectProcess = (ProcessCacheObject) processCache.get(5L);
    ProcessCacheObject expectedCacheObjectProcess = cacheObjectFactory.buildProcessUpdateCacheObject(cacheObjectProcess, process);

    ObjectEqualityComparison.assertProcessEquals(expectedCacheObjectProcess, cacheObjectProcess);

    verify(mockManager);
  }

  @Test
  public void stopAndRemove() {
    configurationLoader.applyConfiguration(TestConfigurationProvider.createProcess());
    assertTrue(processCache.containsKey(5L));

    Configuration remove = TestConfigurationProvider.deleteProcess();
    ConfigurationReport report = configurationLoader.applyConfiguration(remove);
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertSame(report.getStatus(), ConfigConstants.Status.OK);

    assertFalse(processCache.containsKey(5L));
    assertNull(processMapper.getItem(5L));
    assertFalse(aliveTimerCache.containsKey(101L));
  }

  @Test
  public void testCreateUpdateRemoveProcess() {
    ConfigurationReport report = configurationLoader.applyConfiguration(16);

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

    // update
    report = configurationLoader.applyConfiguration(17);

    cacheObject = (ProcessCacheObject) processCache.get(2L);
    expectedObject.setDescription("updated description");
    expectedObject.setMaxMessageDelay(4000);

    ObjectEqualityComparison.assertProcessEquals(expectedObject, cacheObject);
    assertTrue(report.getProcessesToReboot().contains(expectedObject.getName()));
  }

  /**
   * Tests the removal of a process succeeds, with dependent rules and alarms.
   * Relies on permanent test data in test account and must be rolled back. No
   * changes should be sent to the DAQ layer.
   */
  @Test
  public void testRemoveProcess() {
    // stop DAQ else remove not allowed
    processService.stop(50L, System.currentTimeMillis());

    ConfigurationReport report = configurationLoader.applyConfiguration(28);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    verify(mockManager);
    // check process, tag, rules and alarms are gone
    assertFalse(processCache.containsKey(50L));
    assertNull(processMapper.getItem(50L));
    assertFalse(equipmentCache.containsKey(150L));
    assertNull(equipmentMapper.getItem(150L));
    // check couple of rules
    assertFalse(ruleTagCache.containsKey(60010L));
    assertNull(ruleTagMapper.getItem(60010L));
    assertFalse(ruleTagCache.containsKey(60002L));
    assertNull(ruleTagMapper.getItem(60002L));
    // tags
    assertFalse(dataTagCache.containsKey(200002L));
    assertNull(dataTagMapper.getItem(200002L));
    assertFalse(dataTagCache.containsKey(200003L));
    assertNull(dataTagMapper.getItem(200003L));
    // control tags
//    assertNull(controlTagMapper.getItem(1220L));
//    assertNull(controlTagMapper.getItem(1221L));
    // equipment control tags
//    assertNull(controlTagMapper.getItem(1222L));
//    assertNull(controlTagMapper.getItem(1223L));
    // equipment commfault
    assertFalse(commFaultTagCache.containsKey(1223L));
    // process alive
    assertFalse(aliveTimerCache.containsKey(1221L));
    // alarms
    assertFalse(alarmCache.containsKey(350000L));
    assertNull(alarmMapper.getItem(350000L));
    assertFalse(alarmCache.containsKey(350001L));
    assertNull(alarmMapper.getItem(350001L));
    verify(mockManager);
  }

  @Test
  public void testProcessListConverter() {
    ProcessListConverter converter = new ProcessListConverter();

    String list = "[P_TEST01, P_TEST02]";
    Set<String> processList = converter.convert(list);
    assertEquals(2, processList.size());
    assertTrue(processList.contains("P_TEST01"));
    assertTrue(processList.contains("P_TEST02"));

    list = "[P_TEST01]";
    processList = converter.convert(list);
    assertEquals(1, processList.size());
    assertTrue(processList.contains("P_TEST01"));

    list = "[]";
    processList = converter.convert(list);
    assertEquals(0, processList.size());
  }
}
