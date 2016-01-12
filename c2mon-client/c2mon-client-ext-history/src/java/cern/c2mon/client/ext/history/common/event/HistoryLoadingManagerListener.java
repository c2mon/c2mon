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

/**
 * Is used by the {@link cern.c2mon.client.ext.history.common.HistoryLoadingManager}
 * to inform about events.
 * 
 * @author vdeila
 * 
 */
public interface HistoryLoadingManagerListener {

  /**
   * Invoked when the loading is starting
   */
  void onLoadingStarting();

  /**
   * Invoked when the loading have progressed
   * 
   * @param percent
   *          the percentage currently of the loading process
   */
  void onLoadingProgressed(double percent);

  /**
   * Invoked when all loading is complete.
   */
  void onLoadingComplete();

}
