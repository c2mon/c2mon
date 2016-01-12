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
package cern.c2mon.client.ext.history.common;

import cern.c2mon.client.ext.history.common.exception.HistoryProviderException;

/**
 * Describes the methods of a history provider factory.
 * 
 * @author vdeila
 */
public interface HistoryProviderFactory {

  /**
   * @param event
   *          the event which will be requested.
   * 
   * @return A {@link HistoryProvider} which can be used to easily get event
   *         history data
   * @throws HistoryProviderException
   *           If the history provider could not be retrieved for any reason.
   */
  HistoryProvider createSavedHistoryProvider(final SavedHistoryEvent event) throws HistoryProviderException;

  /**
   * Use this provider to get the list of saved events. The records of a
   * particular event can then be retrieved using
   * {@link #createSavedHistoryProvider(SavedHistoryEvent)}
   * 
   * @return A {@link SavedHistoryEventsProvider} which can be used to easily
   *         get the list of saved history events
   * @throws HistoryProviderException
   *           If the history provider could not be retrieved for any reason.
   */
  SavedHistoryEventsProvider createSavedHistoryEventsProvider() throws HistoryProviderException;
  
  /**
   * @return A {@link HistoryProvider} which can be used to easily get data from
   *         the last 30 days of history data
   * @throws HistoryProviderException
   *           If the history provider could not be retrieved for any reason.
   */
  HistoryProvider createHistoryProvider() throws HistoryProviderException;
}
