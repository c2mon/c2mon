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
package cern.c2mon.server.daqcommunication.out;

/**
 * Service for requesting refreshed data from the 
 * Data aquisition layer (synchronisation should 
 * usually be maintained, but provided as backup
 * refresh).
 * 
 * <p>In general, notice values only change in the cache
 * if the refresh detects fresh data.
 * 
 * @author Mark Brightwell
 *
 */
public interface DataRefreshManager {

  /**
   * Refreshes the values in the cache for a given DAQ Process (from DAQ cache).
   * @param id the id of the Process
   */
  void refreshValuesForProcess(Long id);

  /**
   * Refreshes the values of all DataTags in the system from 
   * the DAQ caches.
   */
  void refreshTagsForAllProcess();
  
}
