package cern.c2mon.server.test;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.shared.common.command.CommandTag;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

/**
 * Using this rule in a JUnit test will ensure that all caches are preloaded
 * cleanly with test data at the start of each test.
 *
 * @author Justin Lewis Salmon
 */
@Service
public class CachePopulationRule extends DatabasePopulationRule {

  private C2monCache<Alarm> alarmCache;
  private C2monCache<AliveTag> aliveTimerCache;
  private C2monCache<CommandTag> commandTagCache;
  private C2monCache<CommFaultTag> commFaultTagCache;
  private C2monCache<DataTag> dataTagCache;
  private C2monCache<Device> deviceCache;
  private C2monCache<DeviceClass> deviceClassCache;
  private C2monCache<Equipment> equipmentCache;
  private C2monCache<Process> processCache;
  private C2monCache<RuleTag> ruleTagCache;
  private C2monCache<SubEquipment> subEquipmentCache;

  public CachePopulationRule(C2monCache<Alarm> alarmCache, C2monCache<AliveTag> aliveTimerCache,
                             C2monCache<CommandTag> commandTagCache, C2monCache<CommFaultTag> commFaultTagCache,
                             C2monCache<DataTag> dataTagCache, C2monCache<Device> deviceCache,
                             C2monCache<DeviceClass> deviceClassCache, C2monCache<Equipment> equipmentCache,
                             C2monCache<Process> processCache, C2monCache<RuleTag> ruleTagCache,
                             C2monCache<SubEquipment> subEquipmentCache) {
    this.alarmCache = alarmCache;
    this.aliveTimerCache = aliveTimerCache;
    this.commandTagCache = commandTagCache;
    this.commFaultTagCache = commFaultTagCache;
    this.dataTagCache = dataTagCache;
    this.deviceCache = deviceCache;
    this.deviceClassCache = deviceClassCache;
    this.equipmentCache = equipmentCache;
    this.processCache = processCache;
    this.ruleTagCache = ruleTagCache;
    this.subEquipmentCache = subEquipmentCache;
  }


  @Override
  protected void before() throws SQLException {
    super.before();
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
