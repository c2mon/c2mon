package cern.c2mon.server.cache.test;

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
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.server.test.DatabasePopulationRule;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.command.CommandTag;

import javax.inject.Named;
import java.sql.SQLException;
import java.util.List;

import static cern.c2mon.server.common.util.Java9Collections.listOf;

/**
 * Using this rule in a JUnit test will ensure that all caches are preloaded
 * cleanly with test data at the start of each test.
 *
 * @author Alexandros Papageorgiou, Justin Lewis Salmon
 */
@Named
public class CachePopulationRule extends DatabasePopulationRule {

  private List<C2monCache<? extends Cacheable>> allCaches;

  public CachePopulationRule(C2monCache<Alarm> alarmCache, C2monCache<AliveTag> aliveTimerCache,
                             C2monCache<CommandTag> commandTagCache, C2monCache<CommFaultTag> commFaultTagCache,
                             C2monCache<DataTag> dataTagCache, C2monCache<Device> deviceCache,
                             C2monCache<DeviceClass> deviceClassCache, C2monCache<Equipment> equipmentCache,
                             C2monCache<Process> processCache, C2monCache<RuleTag> ruleTagCache,
                             C2monCache<SubEquipment> subEquipmentCache, C2monCache<SupervisionStateTag> stateTagCache) {
    allCaches = listOf(
      alarmCache, aliveTimerCache, commandTagCache, commFaultTagCache, dataTagCache, deviceCache, deviceClassCache,
      equipmentCache, processCache, ruleTagCache, subEquipmentCache, stateTagCache
    );
  }


  @Override
  protected void before() throws SQLException {
    super.before();
    allCaches.forEach(C2monCache::init);
  }

  @Override
  protected void after() {
    super.after();
    allCaches.forEach(C2monCache::clear);
  }
}
