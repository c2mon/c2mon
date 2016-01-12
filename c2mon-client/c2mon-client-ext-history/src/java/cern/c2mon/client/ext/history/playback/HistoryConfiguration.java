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
package cern.c2mon.client.ext.history.playback;

import cern.c2mon.client.ext.history.common.HistoryProvider;
import cern.c2mon.client.ext.history.common.Timespan;
import cern.c2mon.client.ext.history.playback.exceptions.NoHistoryProviderAvailableException;

/**
 * This class is passed to the {@link HistoryPlayerImpl} when you want to configure
 * it (change the source and time of the data to show)
 * 
 * @author vdeila
 * 
 */
public class HistoryConfiguration {

  /** The timespan of the history */
  private Timespan timespan;

  /** The history provider which will be used to retrieve the data */
  private HistoryProvider historyProvider;

  /**
   * 
   * @param historyProvider
   *          The history provider which will be used to retrieve the data
   * @param timespan
   *          The timespan of the history
   */
  public HistoryConfiguration(final HistoryProvider historyProvider, final Timespan timespan) {
    this.historyProvider = historyProvider;
    this.timespan = timespan;
  }

  /**
   * @return The timespan of the history
   */
  public Timespan getTimespan() {
    return timespan;
  }

  /**
   * @param timespan
   *          The timespan of the history to set
   */
  public void setTimespan(final Timespan timespan) {
    this.timespan = timespan;
  }

  /**
   * @return The history provider which will be used to retrieve the data, never
   *         <code>null</code>
   * 
   * @throws NoHistoryProviderAvailableException
   *           If no History Provider is registered
   */
  public HistoryProvider getHistoryProvider() throws NoHistoryProviderAvailableException {
    if (this.historyProvider == null) {
      throw new NoHistoryProviderAvailableException("No History Provider is available");
    }
    return this.historyProvider;
  }

  /**
   * @param historyProvider
   *          The history provider which will be used to retrieve the data
   */
  public void setHistoryProvider(final HistoryProvider historyProvider) {
    this.historyProvider = historyProvider;
  }
}
