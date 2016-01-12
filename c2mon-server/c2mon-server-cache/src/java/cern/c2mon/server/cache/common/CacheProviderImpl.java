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
package cern.c2mon.server.cache.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.CacheProvider;
import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.CommFaultTagCache;
import cern.c2mon.server.cache.CommandTagCache;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.SubEquipmentCache;

/**
 * Implementation of the {@link CacheProvider} interface. All private
 * fields are autowired by Spring.
 * @author Matthias Braeger
 */
@Service("cacheProvider")
public final class CacheProviderImpl implements CacheProvider {
  
  @Autowired
  @Qualifier("alarmCache")
  private AlarmCache alarmCache;
  
  @Autowired
  @Qualifier("aliveTimerCache")
  private AliveTimerCache aliveTimerCache;
  
  @Autowired
  @Qualifier("clusterCache")
  private ClusterCache clusterCache;
  
  @Autowired
  private CommandTagCache commandTagCache;
  
  @Autowired
  @Qualifier("commFaultTagCache")
  private CommFaultTagCache commFaultTagCache;
  
  @Autowired
  @Qualifier("controlTagCache")
  private ControlTagCache controlTagCache;
  
  @Autowired
  @Qualifier("dataTagCache")
  private DataTagCache dataTagCache;
  
  @Autowired
  @Qualifier("equipmentCache")
  private EquipmentCache equipmentCache;
  
  @Autowired
  @Qualifier("processCache")
  private ProcessCache processCache;
  
  @Autowired
  @Qualifier("ruleTagCache")
  private RuleTagCache ruleTagCache;
  
  @Autowired
  @Qualifier("subEquipmentCache")
  private SubEquipmentCache subEquipmentCache;

  @Override
  public AlarmCache getAlarmCache() {
    return alarmCache;
  }

  @Override
  public AliveTimerCache getAliveTimerCache() {
    return aliveTimerCache;
  }

  @Override
  public ClusterCache getClusterCache() {
    return clusterCache;
  }

  @Override
  public CommandTagCache getCommandTagCache() {
    return commandTagCache;
  }

  @Override
  public CommFaultTagCache getCommFaultTagCache() {
    return commFaultTagCache;
  }

  @Override
  public ControlTagCache getControlTagCache() {
    return controlTagCache;
  }

  @Override
  public DataTagCache getDataTagCache() {
    return dataTagCache;
  }

  @Override
  public EquipmentCache getEquipmentCache() {
    return equipmentCache;
  }

  @Override
  public ProcessCache getProcessCache() {
    return processCache;
  }

  @Override
  public RuleTagCache getRuleTagCache() {
    return ruleTagCache;
  }

  @Override
  public SubEquipmentCache getSubEquipmentCache() {
    return subEquipmentCache;
  }
}
