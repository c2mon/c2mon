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
package cern.c2mon.server.common.config;

/**
 * Enumeration of all caches used by the C2MON core.
 *
 * <p>Each class is aware of its name, which can be
 * accessed using the getCacheName() method.
 *
 * @author Mark Brightwell
 *
 */
public enum CacheName {

  /**
   * DataTag cache.
   */
  DATATAG,

  /**
   * ControlTag cache.
   */
  CONTROLTAG,

  /**
   * RuleTag cache.
   */
  RULETAG,

  /**
   * Equipment cache.
   */
  EQUIPMENT,

  /**
   * SubEquipment cache.
   */
  SUBEQUIPMENT,

  /**
   * Process cache.
   */
  PROCESS,

  /**
   * AliveTimer cache.
   */
  ALIVETIMER,

  /**
   * Alarm cache.
   */
  ALARM,

  /**
   * CommFault cache.
   */
  COMMFAULT,

  /**
   * Command cache.
   */
  COMMAND,

  /**
   * Cache containing distributed parameters.
   */
  CLUSTER,

  /**
   * DeviceClass cache.
   */
  DEVICECLASS,

  /**
   * Device cache.
   */
  DEVICE
}
