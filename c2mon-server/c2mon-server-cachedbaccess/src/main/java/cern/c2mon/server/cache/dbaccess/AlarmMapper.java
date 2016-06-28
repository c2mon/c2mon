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
package cern.c2mon.server.cache.dbaccess;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;

/**
 * iBatis mapper for all cache DB access for Alarms.
 * @author Mark Brightwell
 *
 */
public interface AlarmMapper extends PersistenceMapper<Alarm>, LoaderMapper<Alarm>, 
                                                BatchLoaderMapper<Alarm>, ConfigurableMapper<Alarm> {

  /**
   * Inserts the alarm into the database.
   * 
   * @param alarmCacheObject the cache object that needs inserting
   */
  void insertAlarm(AlarmCacheObject alarmCacheObject);
  
  /**
   * Deletes the alarm from the database.
   * 
   * @param id the alarm id
   */
  void deleteAlarm(Long id);
  
}
