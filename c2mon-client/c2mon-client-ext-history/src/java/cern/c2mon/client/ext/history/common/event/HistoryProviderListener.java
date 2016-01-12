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
package cern.c2mon.client.ext.history.common.event;

import cern.c2mon.client.ext.history.common.HistoryProvider;

/**
 * Used by {@link HistoryProvider} to inform about events
 * 
 * @author vdeila
 */
public interface HistoryProviderListener {

  /**
   * Invoked when a query is starting
   */
  void queryStarting();

  /**
   * Invoked when a query is finished, and is just to return.
   */
  void queryFinished();

  /**
   * Is invoked when the progress percentage changed.
   * 
   * @param percent
   *          How many percent of the query is finished
   */
  void queryProgressChanged(double percent);
}
