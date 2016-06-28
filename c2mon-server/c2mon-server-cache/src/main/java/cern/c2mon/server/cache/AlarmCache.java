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

import java.util.Collection;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.shared.client.alarm.AlarmQuery;

/**
 * Interface to the Alarm cache.
 * 
 * <p>Nothing specific for the Alarm cache so far, but
 * this interface is used to wire the Alarm cache into
 * other Spring beans.
 * 
 * <p>The getCopy method is available for the Alarm cache
 * to retrieve a copy of the current cache object.
 * 
 * @author Mark Brightwell
 *
 */
public interface AlarmCache extends C2monCacheWithListeners<Long, Alarm> {

  String cacheInitializedKey = "c2mon.cache.alarm.initialized";
  
  Collection<Long> findAlarm(AlarmQuery query);
  
}
