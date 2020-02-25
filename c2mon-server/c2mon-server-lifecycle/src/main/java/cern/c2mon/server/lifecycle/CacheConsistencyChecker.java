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
package cern.c2mon.server.lifecycle;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.*;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.shared.common.command.CommandTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class runs at server startup and performs consistency checks to make
 * sure the number of cache items is the same as in the DB. If not, a warning
 * email is sent to the administrators.
 *
 * @see <a href=https://issues.cern.ch/browse/TIMS-985>TMS-985</a>
 *
 * @author Justin Lewis Salmon
 */
@Service
public class CacheConsistencyChecker implements SmartLifecycle {

  private static final Logger LOG = LoggerFactory.getLogger(CacheConsistencyChecker.class);
  private static final Logger EMAIL_LOG = LoggerFactory.getLogger("AdminEmailLogger");

  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;

  /**
   * Map of cache beans to mapper beans.
   */
  private Map<C2monCache<?>, SimpleLoaderMapper<?>> map = new HashMap<>();

  /**
   * Constructor.
   */
  @Autowired
  public CacheConsistencyChecker(final C2monCache<Alarm> alarmCache,
                                 final AlarmMapper alarmMapper,
                                 final C2monCache<AliveTag> aliveTimerCache,
                                 final AliveTagMapper aliveTimerMapper,
                                 final C2monCache<CommandTag> commandTagCache,
                                 final CommandTagMapper commandTagMapper,
                                 final C2monCache<CommFaultTag> commFaultTagCache,
                                 final CommFaultTagMapper commFaultTagMapper,
                                 final C2monCache<DataTag> dataTagCache,
                                 final DataTagMapper dataTagMapper,
                                 final C2monCache<DeviceClass> deviceClassCache,
                                 final DeviceClassMapper deviceClassMapper,
                                 final C2monCache<Device> deviceCache,
                                 final DeviceMapper deviceMapper,
                                 final C2monCache<Equipment> equipmentCache,
                                 final EquipmentMapper equipmentMapper,
                                 final C2monCache<Process> processCache,
                                 final ProcessMapper processMapper,
                                 final C2monCache<RuleTag> ruleTagCache,
                                 final RuleTagMapper ruleTagMapper,
                                 final C2monCache<SubEquipment> subEquipmentCache,
                                 final SubEquipmentMapper subEquipmentMapper,
                                 final C2monCache<SupervisionStateTag> supervisionStateTagCache,
                                 final SupervisionStateTagMapper stateTagMapper) {
    super();
    map.put(alarmCache, alarmMapper);
    map.put(aliveTimerCache, aliveTimerMapper);
    map.put(commandTagCache, commandTagMapper);
    map.put(commFaultTagCache, commFaultTagMapper);
    map.put(dataTagCache, dataTagMapper);
    map.put(deviceClassCache, deviceClassMapper);
    map.put(deviceCache, deviceMapper);
    map.put(equipmentCache, equipmentMapper);
    map.put(processCache, processMapper);
    map.put(ruleTagCache, ruleTagMapper);
    map.put(subEquipmentCache, subEquipmentMapper);
    map.put(supervisionStateTagCache, stateTagMapper);
  }

  @Override
  public void start() {
    if (!running) {
      running = true;

      LOG.info("Beginning cache consistency check.");
      List<String> messages = new ArrayList<>();

      // Compare the server cache sizes against the operational database
      for (Map.Entry<C2monCache<?>, SimpleLoaderMapper<?>> entry : map.entrySet()) {
        C2monCache<?> cache = entry.getKey();
        SimpleLoaderMapper<?> mapper = entry.getValue();

        int cacheSize = cache.getKeys().size();
        int dbSize = mapper.getNumberItems();

        if (cacheSize != dbSize) {
          messages.add(cache.getName() + " consistency check failed (cache size: " + cacheSize + ", DB size: " + dbSize + ")");
        }
      }

      // If any inconsistencies were found, log them and send a warning email.
      if (messages.size() > 0) {
        StringBuffer email = new StringBuffer("Cache inconsistency detected! Details:\n\n");

        for (String message : messages) {
          LOG.error(message);
          email.append(message).append("\n");
        }

        EMAIL_LOG.error(email.toString());
      }

      LOG.info("Finished cache consistency check.");
    }
  }

  @Override
  public void stop() {
    LOG.debug("Stopping cache consistency check.");
    running = false;
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_START_LAST;
  }

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void stop(Runnable callback) {
    stop();
    callback.run();
  }
}
