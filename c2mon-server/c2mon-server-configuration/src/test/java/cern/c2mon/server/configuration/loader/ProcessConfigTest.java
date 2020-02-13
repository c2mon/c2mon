package cern.c2mon.server.configuration.loader;

import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cache.dbaccess.EquipmentMapper;
import cern.c2mon.server.cache.dbaccess.ProcessMapper;
import cern.c2mon.server.cache.dbaccess.SupervisionStateTagMapper;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.configuration.impl.ConfigurationLoaderImpl;
import cern.c2mon.server.configuration.parser.util.ConfigurationProcessUtil;
import cern.c2mon.server.configuration.util.TestConfigurationProvider;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.converter.ProcessListConverter;
import org.junit.Test;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.Set;

import static cern.c2mon.server.configuration.parser.util.ConfigurationProcessUtil.buildCreateAllFieldsProcess;
import static org.junit.Assert.*;

public class ProcessConfigTest extends ConfigurationCacheLoaderTest<Process> {

  @Inject private C2monCache<Process> processCache;

  @Inject private ProcessMapper processMapper;

  @Inject private C2monCache<AliveTag> aliveTimerCache;

  @Inject private C2monCache<CommFaultTag> commFaultTagCache;

  @Inject private C2monCache<DataTag> dataTagCache;

  @Inject private C2monCache<Equipment> equipmentCache;

  @Inject private EquipmentMapper equipmentMapper;

  @Inject private DataTagMapper dataTagMapper;

  @Inject private ProcessService processService;

  @Inject private SupervisionStateTagMapper stateTagMapper;

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
    ProcessCacheObject expectedObjectProcess = cacheObjectFactory.buildProcessCacheObject(1L, process);

    assertEquals(expectedObjectProcess, processCache.get(1L));
  }


  @Test
  public void updateProcess() {
    ((ConfigurationLoaderImpl) configurationLoader).setDaqConfigEnabled(false);
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

    assertEquals(expectedCacheObjectProcess, cacheObjectProcess);
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
  public void createProcessFromDb() {
    ConfigurationReport report = configurationLoader.applyConfiguration(16);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));

    assertEquals(expectedObject(), processCache.get(2L));
  }

  @Test
  public void updateProcessByDb() {
    ConfigurationReport report = configurationLoader.applyConfiguration(16);
    report = configurationLoader.applyConfiguration(17);

    ProcessCacheObject cacheObject = (ProcessCacheObject) processCache.get(2L);
    ProcessCacheObject expectedObject = expectedObject();
    expectedObject.setDescription("updated description");
    expectedObject.setMaxMessageDelay(4000);
    expectedObject.setRequiresReboot(true);

    assertEquals(expectedObject, cacheObject);
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
    // check process, tag, rules and alarms are gone
    assertFalse(processCache.containsKey(50L));
    assertNull(processMapper.getItem(50L));
    assertFalse(equipmentCache.containsKey(150L));
    assertNull(equipmentMapper.getItem(150L));
    // tags
    assertFalse(dataTagCache.containsKey(200002L));
    assertNull(dataTagMapper.getItem(200002L));
    assertFalse(dataTagCache.containsKey(200003L));
    assertNull(dataTagMapper.getItem(200003L));
    // equipment commfault
    assertNull(stateTagMapper.getItem(1222L));
    assertFalse(commFaultTagCache.containsKey(1223L));
    // process alive
    assertNull(stateTagMapper.getItem(1220L));
    assertFalse(aliveTimerCache.containsKey(1221L));
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

  private static ProcessCacheObject expectedObject() {
    ProcessCacheObject expectedObject = new ProcessCacheObject(2L);
    expectedObject.setName("P_TEST");
    expectedObject.setAliveInterval(60000);
    expectedObject.setAliveTagId(1221L);
    expectedObject.setStateTagId(1220L);
    expectedObject.setMaxMessageSize(200);
    expectedObject.setMaxMessageDelay(1000);
    expectedObject.setDescription("test description");
    expectedObject.setRequiresReboot(true);
    return expectedObject;
  }
}
