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
package cern.c2mon.server.cache;

/**
 * Allows retrieving the references to the different caches.
 * 
 * @author Matthias Braeger
 */
public interface CacheProvider {

  /**
   * @return Reference to the alarm cache
   */
  AlarmCache getAlarmCache();
  
  /**
   * @return Reference to the alive timer cache
   */
  AliveTimerCache getAliveTimerCache();
  
  /**
   * @return Reference to the cluster cache
   */
  ClusterCache getClusterCache();
  
  /**
   * @return Reference to the command tag cache
   */
  CommandTagCache getCommandTagCache();
  
  /**
   * @return Reference to the communication fault cache
   */
  CommFaultTagCache getCommFaultTagCache();
  
  /**
   * @return Reference to the control tag cache
   */
  ControlTagCache getControlTagCache();
  
  /**
   * @return Reference to the data tag cache
   */
  DataTagCache getDataTagCache();
  
  /**
   * @return Reference to the equipment cache
   */
  EquipmentCache getEquipmentCache();
  
  /**
   * @return Reference to the process cache
   */
  ProcessCache getProcessCache();
  
  /**
   * @return Reference to the rule tag cache
   */
  RuleTagCache getRuleTagCache();
  
  /**
   * @return Reference to the sub-equipment cache
   */
  SubEquipmentCache getSubEquipmentCache();
}
