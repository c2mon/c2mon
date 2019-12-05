/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.elasticsearch.junit;

import java.sql.SQLException;

import net.sf.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.alarm.impl.AlarmCacheImpl;
import cern.c2mon.server.cache.alive.AliveTimerCacheImpl;
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

/**
 * Using this rule in a JUnit test will ensure that all caches are preloaded
 * cleanly with test data at the start of each test.
 *
 * @author Justin Lewis Salmon
 */
@Service
public class CachePopulationRule extends DatabasePopulationRule {

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

  @Override
  protected void before() throws SQLException {
    super.before();
    CacheManager.getInstance().clearAll();
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
