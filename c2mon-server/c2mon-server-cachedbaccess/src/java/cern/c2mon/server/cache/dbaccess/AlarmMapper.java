/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
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
