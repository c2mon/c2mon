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

import java.sql.Timestamp;

import cern.c2mon.client.ext.history.common.HistoryProvider;


/**
 * Adapter for {@link HistoryPlayerListener} which enabled you to choose which
 * methods you would like to override
 * 
 * @author vdeila
 */
public abstract class HistoryPlayerAdapter implements HistoryPlayerListener {

  @Override
  public void onActivatedHistoryPlayer() { }

  @Override
  public void onDeactivatingHistoryPlayer() { }

  @Override
  public void onHistoryIsFullyLoaded() { }

  @Override
  public void onHistoryDataAvailabilityChanged(final Timestamp newTime) { }

  @Override
  public void onInitializingHistoryFinished() { }

  @Override
  public void onInitializingHistoryProgressChanged(final double percent) { }

  @Override
  public void onInitializingHistoryProgressStatusChanged(final String progressMessage) { }

  @Override
  public void onInitializingHistoryStarted() { }

  @Override
  public void onStoppedLoadingDueToOutOfMemory() { }

  @Override
  public void onHistoryProviderChanged(final HistoryProvider historyProvider) { }
  
}
