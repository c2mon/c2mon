/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2015 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.server.lifecycle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.C2monCache;
import cern.c2mon.server.cache.CommFaultTagCache;
import cern.c2mon.server.cache.CommandTagCache;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.DeviceCache;
import cern.c2mon.server.cache.DeviceClassCache;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.dbaccess.AlarmMapper;
import cern.c2mon.server.cache.dbaccess.AliveTimerMapper;
import cern.c2mon.server.cache.dbaccess.CommFaultTagMapper;
import cern.c2mon.server.cache.dbaccess.CommandTagMapper;
import cern.c2mon.server.cache.dbaccess.ControlTagMapper;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cache.dbaccess.DeviceClassMapper;
import cern.c2mon.server.cache.dbaccess.DeviceMapper;
import cern.c2mon.server.cache.dbaccess.EquipmentMapper;
import cern.c2mon.server.cache.dbaccess.ProcessMapper;
import cern.c2mon.server.cache.dbaccess.RuleTagMapper;
import cern.c2mon.server.cache.dbaccess.SimpleLoaderMapper;
import cern.c2mon.server.cache.dbaccess.SubEquipmentMapper;
import cern.c2mon.server.common.config.ServerConstants;

/**
 * This class runs at server startup and performs consistency checks to make
 * sure the number of cache items is the same as in the DB. If not, a warning
 * email is sent to the administrators.
 *
 * @see https://issues.cern.ch/browse/TIMS-985
 *
 * @author Justin Lewis Salmon
 */
@Service
public class CacheConsistencyChecker implements SmartLifecycle {

  private static final Logger LOG = Logger.getLogger(CacheConsistencyChecker.class);
  private static final Logger EMAIL_LOG = Logger.getLogger("AdminEmailLogger");

  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;

  /**
   * Map of cache beans to mapper beans.
   */
  private Map<C2monCache<?, ?>, SimpleLoaderMapper<?>> map = new HashMap<>();

  /**
   * Constructor.
   */
  @Autowired
  public CacheConsistencyChecker(final AlarmCache alarmCache,
                                 final AlarmMapper alarmMapper,
                                 final AliveTimerCache aliveTimerCache,
                                 final AliveTimerMapper aliveTimerMapper,
                                 final CommandTagCache commandTagCache,
                                 final CommandTagMapper commandTagMapper,
                                 final CommFaultTagCache commFaultTagCache,
                                 final CommFaultTagMapper commFaultTagMapper,
                                 final ControlTagCache controlTagCache,
                                 final ControlTagMapper controlTagMapper,
                                 final DataTagCache dataTagCache,
                                 final DataTagMapper dataTagMapper,
                                 final DeviceClassCache deviceClassCache,
                                 final DeviceClassMapper deviceClassMapper,
                                 final DeviceCache deviceCache,
                                 final DeviceMapper deviceMapper,
                                 final EquipmentCache equipmentCache,
                                 final EquipmentMapper equipmentMapper,
                                 final ProcessCache processCache,
                                 final ProcessMapper processMapper,
                                 final RuleTagCache ruleTagCache,
                                 final RuleTagMapper ruleTagMapper,
                                 final SubEquipmentCache subEquipmentCache,
                                 final SubEquipmentMapper subEquipmentMapper) {
    super();
    map.put(alarmCache, alarmMapper);
    map.put(aliveTimerCache, aliveTimerMapper);
    map.put(commandTagCache, commandTagMapper);
    map.put(commFaultTagCache, commFaultTagMapper);
    map.put(controlTagCache, controlTagMapper);
    map.put(dataTagCache, dataTagMapper);
    map.put(deviceClassCache, deviceClassMapper);
    map.put(deviceCache, deviceMapper);
    map.put(equipmentCache, equipmentMapper);
    map.put(processCache, processMapper);
    map.put(ruleTagCache, ruleTagMapper);
    map.put(subEquipmentCache, subEquipmentMapper);
  }

  @Override
  public void start() {
    if (!running) {
      running = true;

      LOG.info("Beginning cache consistency check.");
      List<String> messages = new ArrayList<>();

      // Compare the server cache sizes against the operational database
      for (Map.Entry<C2monCache<?, ?>, SimpleLoaderMapper<?>> entry : map.entrySet()) {
        C2monCache<?, ?> cache = entry.getKey();
        SimpleLoaderMapper<?> mapper = entry.getValue();

        int cacheSize = cache.getKeys().size();
        int dbSize = mapper.getNumberItems();

        if (cacheSize != dbSize || true) {
          messages.add(cache.getClass().getSimpleName() + " consistency check failed (cache size: " + cacheSize + ", DB size: " + dbSize + ")");
        }
      }

      // If any inconsistencies were found, log them and send a warning email.
      if (messages.size() > 0) {
        StringBuffer email = new StringBuffer("Cache inconsistency detected! Details:\n\n");

        for (String message : messages) {
          LOG.error(message);
          email.append(message).append("\n");
        }

        EMAIL_LOG.error(email);
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
    return false;
  }

  @Override
  public void stop(Runnable callback) {
    stop();
    callback.run();
  }
}
