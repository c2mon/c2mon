/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
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
 *****************************************************************************/
package cern.c2mon.client.history.playback;

import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.Timespan;
import cern.c2mon.client.history.playback.exceptions.NoHistoryProviderAvailableException;

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

  /** The maximum amount the history can be extended to */
  private Timespan timespanLimit;

  /** The history provider which will be used to retrieve the data */
  private HistoryProvider historyProvider;

  /**
   * 
   * @param historyProvider
   *          The history provider which will be used to retrieve the data
   * @param timespan
   *          The timespan of the history
   * @param timespanLimit
   *          The maximum amount the history can be extended to
   */
  public HistoryConfiguration(final HistoryProvider historyProvider, final Timespan timespan, final Timespan timespanLimit) {
    this.historyProvider = historyProvider;
    this.timespan = timespan;
    this.timespanLimit = timespanLimit;
  }

  /**
   * 
   * @param historyProvider
   *          The history provider which will be used to retrieve the data
   * @param timespan
   *          The timespan of the history
   */
  public HistoryConfiguration(final HistoryProvider historyProvider, final Timespan timespan) {
    this(historyProvider, timespan, null);
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
   * @return The maximum amount the history can be extended to
   */
  public Timespan getTimespanLimit() {
    return timespanLimit;
  }

  /**
   * @param timespanLimit
   *          The maximum amount the history can be extended to
   */
  public void setTimespanLimit(final Timespan timespanLimit) {
    this.timespanLimit = timespanLimit;
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
