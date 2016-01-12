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
package cern.c2mon.client.ext.history;

import java.util.Collection;

import cern.c2mon.client.ext.history.common.HistoryLoadingManager;
import cern.c2mon.client.ext.history.common.HistoryPlayer;
import cern.c2mon.client.ext.history.common.HistoryPlayerEvents;
import cern.c2mon.client.ext.history.common.HistoryProvider;
import cern.c2mon.client.ext.history.common.HistoryProviderAvailability;
import cern.c2mon.client.ext.history.common.HistoryProviderFactory;
import cern.c2mon.client.ext.history.common.Timespan;
import cern.c2mon.client.ext.history.common.event.HistoryPlayerListener;
import cern.c2mon.client.ext.history.common.exception.HistoryPlayerNotActiveException;
import cern.c2mon.client.ext.history.common.tag.HistoryTag;
import cern.c2mon.client.ext.history.common.tag.HistoryTagManager;

/**
 * This interface describes the methods which are provided by the C2MON history
 * manager singleton. The history manager handles the communication with the
 * C2MON STL database which is used for the history player and for trending.
 * <code>SessionListener</code>.
 * 
 * @author Matthias Braeger
 */
public interface C2monHistoryManager {

  /**
   * Starts the history player.
   * 
   * @param provider
   *          the provider to use to get history data
   * @param timespan
   *          the time frame to ask for
   */
  void startHistoryPlayerMode(HistoryProvider provider, Timespan timespan);

  /**
   * Stops the history player.
   */
  void stopHistoryPlayerMode();

  /**
   * 
   * @return <code>true</code> if history mode is active
   */
  boolean isHistoryModeEnabled();

  /**
   * @return a history provider factory
   */
  HistoryProviderFactory getHistoryProviderFactory();

  /**
   * Creates a history loading manager which uses the history provider provided
   * to load the data.
   * 
   * @param historyProvider
   *          the history provider to be used. Can be retrieved through 
   *          {@link HistoryProviderFactory#createHistoryProvider()}, a
   *          {@link HistoryProviderFactory} can be retrieved from
   *          {@link #getHistoryProviderFactory()}
   * @param tagIds
   *          the tag ids which will be added to the manager for loading.
   * @return a history loading manager which loads data from a history provider
   */
  HistoryLoadingManager createHistoryLoadingManager(HistoryProvider historyProvider, Collection<Long> tagIds);

  /**
   * 
   * @return the history player instance currently running.
   * @throws HistoryPlayerNotActiveException
   *           if the history player is not currently active, it is not possible
   *           to get the history player
   */
  HistoryPlayer getHistoryPlayer() throws HistoryPlayerNotActiveException;

  /**
   * With this method you get an interface which you can use to register
   * {@link HistoryPlayerListener} without that the history player needs to be
   * activated.
   * 
   * @return an interface where you can add {@link HistoryPlayerListener} to the
   *         history player without it being activated. It never returns
   *         <code>null</code>.
   */
  HistoryPlayerEvents getHistoryPlayerEvents();
  
  /**
   * @return a history tag manager that can be used for handling {@link HistoryTag}s.
   */
  HistoryTagManager getHistoryTagManager();
}
