package cern.c2mon.server.configuration;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.collections.ControlCacheCollection;
import cern.c2mon.server.cache.dbaccess.AliveTagMapper;
import cern.c2mon.server.cache.dbaccess.CommFaultTagMapper;
import cern.c2mon.server.cache.dbaccess.EquipmentMapper;
import cern.c2mon.server.cache.dbaccess.SupervisionStateTagMapper;
import cern.c2mon.server.cache.test.CachePopulationRule;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.configuration.parser.util.ConfigurationEquipmentUtil;
import cern.c2mon.shared.client.configuration.ConfigConstants.Status;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class ConfigurationHandlerRollbackTest extends ConfigurationCacheTest {

  public static final long EQ_ID = 10L;
  public static final long ALIVE_TAG_ID = 300000L;
  public static final long COMMFAULT_TAG_ID = 300001L;
  public static final long STATE_TAG_ID = 300002L;
  public static Long PROCESS_ID = null;

  @Rule @Inject public CachePopulationRule cachePopulationRule;

  @Inject protected ConfigurationLoader configurationLoader;

  @Inject protected C2monCache<Equipment> equipmentCache;
  @Inject protected C2monCache<Process> processCache;

  @Inject private EquipmentMapper equipmentMapper;

  @Inject private ControlCacheCollection controlCacheCollection;

  @Inject private AliveTagMapper aliveTagMapper;
  @Inject private CommFaultTagMapper commFaultTagMapper;
  @Inject private SupervisionStateTagMapper stateTagMapper;

  @Test
  public void rollbackEqCreate() {
    PROCESS_ID = 5L;
    // Adding the process object to cache, it will be looked up
    // but it doesn't exist in the DB - the DB insertion will fail!
    processCache.put(PROCESS_ID, new ProcessCacheObject(PROCESS_ID));

    Configuration configuration = createEquipmentConfiguration();

    //apply the configuration to the server - exception should be gulped,
    // but the configuration fails after inserting the control tags, because
    // the process ID provided above is missing
    expectSuccess(false, configurationLoader.applyConfiguration(configuration));
  }

  @Test
  public void correctEqCreateSucceeds() {
    PROCESS_ID = 50L;
    Configuration configuration = createEquipmentConfiguration();

    //apply the configuration to the server - exception should be gulped,
    // but the configuration fails after inserting the control tags, because
    // the process ID provided above is missing
    expectSuccess(true, configurationLoader.applyConfiguration(configuration));
  }

  private Configuration createEquipmentConfiguration() {
    cern.c2mon.shared.client.configuration.api.equipment.Equipment equipment =
      ConfigurationEquipmentUtil.buildCreateAllFieldsEquipment(EQ_ID, new Properties());
    equipment.setProcessId(PROCESS_ID);
    equipment.getAliveTag().setId(ALIVE_TAG_ID);
    equipment.getCommFaultTag().setId(COMMFAULT_TAG_ID);
    equipment.getStatusTag().setId(STATE_TAG_ID);

    Configuration configuration = new Configuration();
    configuration.addEntity(equipment);
    return configuration;
  }

  private void expectSuccess(boolean success, ConfigurationReport report) {
    // Configuration [succeeded/failed]
    assertEquals(success ? Status.RESTART : Status.FAILURE, report.getStatus());

    // Equipment was [or not] created
    assertEquals(success, equipmentCache.containsKey(EQ_ID));
    assertEquals(success, equipmentMapper.isInDb(EQ_ID));

    // Process was [or not] updated
    assertEquals(success, processCache.get(PROCESS_ID).getEquipmentIds().contains(EQ_ID));

    // Control tags were [or not] rolled back
    assertEquals(success, controlCacheCollection.containsKey(ALIVE_TAG_ID));
    assertEquals(success, aliveTagMapper.isInDb(ALIVE_TAG_ID));
    assertEquals(success, controlCacheCollection.containsKey(COMMFAULT_TAG_ID));
    assertEquals(success, commFaultTagMapper.isInDb(COMMFAULT_TAG_ID));
    assertEquals(success, controlCacheCollection.containsKey(STATE_TAG_ID));
    assertEquals(success, stateTagMapper.isInDb(STATE_TAG_ID));
  }
}