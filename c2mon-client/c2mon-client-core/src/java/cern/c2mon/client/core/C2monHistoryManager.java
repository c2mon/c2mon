/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.client.core;

import java.util.Collection;

import cern.c2mon.client.common.history.HistoryLoadingManager;
import cern.c2mon.client.common.history.HistoryPlayer;
import cern.c2mon.client.common.history.HistoryPlayerEvents;
import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.HistoryProviderAvailability;
import cern.c2mon.client.common.history.HistoryProviderType;
import cern.c2mon.client.common.history.Timespan;
import cern.c2mon.client.common.history.event.HistoryPlayerListener;
import cern.c2mon.client.common.history.exception.HistoryPlayerNotActiveException;
import cern.c2mon.client.common.history.exception.NoHistoryProviderException;

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
   * 
   * @param type
   *          the type of provider to get
   * @return a history provider of the given type
   * @throws NoHistoryProviderException
   *           is thrown if something goes wrong in fetching the
   *           {@link HistoryProvider}. For example if no url, username and
   *           password is given.
   */
  HistoryProvider getHistoryProvider(HistoryProviderType type) throws NoHistoryProviderException;

  /**
   * 
   * @return an interface that provides methods for checking if any of the
   *         history providers is available
   */
  HistoryProviderAvailability getHistoryProviderAvailability();
  
  /**
   * Creates a history loading manager which uses the history provider provided
   * to load the data.
   * 
   * @param historyProvider
   *          the history provider to be used
   * @param tagIds
   *          the tag ids which will be added to the manager
   * @return a history loading manager to load data from a history provider
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
}
