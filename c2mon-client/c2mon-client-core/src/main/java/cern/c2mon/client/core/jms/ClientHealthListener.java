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
package cern.c2mon.client.core.jms;

import cern.c2mon.client.common.listener.TagUpdateListener;

/**
 * Implement this interface to be notified about problems with the
 * processing of incoming updates on the JMS topics.
 * 
 * <p>In general, these notifications indicate a serious problem with
 * possible data loss, so the client should take some appropriate
 * action on receiving these callbacks (e.g. notify the user).
 * 
 * <p>Register with the {@link ClientHealthMonitor}.
 * 
 * @author Mark Brightwell
 *
 */
public interface ClientHealthListener {

  /**
   * Called when one of the registered {@link TagUpdateListener}'s is slow.
   * 
   * @param diagnosticMessage a human-readable message for displaying
   */
  void onSlowUpdateListener(String diagnosticMessage);
  
}
