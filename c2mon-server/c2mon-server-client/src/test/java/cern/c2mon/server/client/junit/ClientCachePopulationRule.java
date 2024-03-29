package cern.c2mon.server.client.junit;

import cern.c2mon.server.cache.alarm.impl.AlarmCacheImpl;
import cern.c2mon.server.cache.alive.AliveTimerCacheImpl;
import cern.c2mon.server.cache.cluster.ClusterCacheImpl;
import cern.c2mon.server.cache.command.CommandTagCacheImpl;
import cern.c2mon.server.cache.commfault.CommFaultTagCacheImpl;
import cern.c2mon.server.cache.control.ControlTagCacheImpl;
import cern.c2mon.server.cache.datatag.DataTagCacheImpl;
import cern.c2mon.server.cache.device.DeviceCacheImpl;
import cern.c2mon.server.cache.device.DeviceClassCacheImpl;
import cern.c2mon.server.cache.equipment.EquipmentCacheImpl;
import cern.c2mon.server.cache.process.ProcessCacheImpl;
import cern.c2mon.server.cache.rule.RuleTagCacheImpl;
import cern.c2mon.server.cache.subequipment.SubEquipmentCacheImpl;
import cern.c2mon.server.test.DatabasePopulationRule;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Using this rule in a JUnit test will ensure that all caches are preloaded
 * cleanly with test data at the start of each test.
 *
 * @author Justin Lewis Salmon
 */
@Service
public class ClientCachePopulationRule extends DatabasePopulationRule {

  @Autowired
  private ProcessCacheImpl processCache;

  @Autowired
  private EquipmentCacheImpl equipmentCache;

  @Autowired
  private SubEquipmentCacheImpl subEquipmentCache;

  @Autowired
  private DataTagCacheImpl dataTagCache;

  @Autowired
  private AlarmCacheImpl alarmCache;

  @Autowired
  private RuleTagCacheImpl ruleTagCache;

  @Autowired
  private CommandTagCacheImpl commandTagCache;

  @Autowired
  private AliveTimerCacheImpl aliveTimerCache;

  @Autowired
  private CommFaultTagCacheImpl commFaultTagCache;

  @Autowired
  private ControlTagCacheImpl controlTagCache;

  @Autowired
  private DeviceClassCacheImpl deviceClassCache;

  @Autowired
  private DeviceCacheImpl deviceCache;

  @Autowired
  private ClusterCacheImpl clusterCache;

  @Override
  protected void before() throws SQLException {
    super.before();
//    //CacheManager.getInstance().clearAll();
    clusterCache.removeAll();
    controlTagCache.removeAll();
    processCache.removeAll();
    dataTagCache.removeAll();
    equipmentCache.removeAll();
    aliveTimerCache.removeAll();
    commFaultTagCache.removeAll();
    subEquipmentCache.removeAll();
    alarmCache.removeAll();
    ruleTagCache.removeAll();
    commandTagCache.removeAll();
    deviceClassCache.removeAll();
    deviceCache.removeAll();

    clusterCache.init();
    controlTagCache.init();
    processCache.init();
    dataTagCache.init();
    equipmentCache.init();
    aliveTimerCache.init();
    commFaultTagCache.init();
    subEquipmentCache.init();
    alarmCache.init();
    ruleTagCache.init();
    commandTagCache.init();
    deviceClassCache.init();
    deviceCache.init();
  }
}
