/******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.shared.util.buffer;

import java.util.EventListener;

/**
 *  SynchroBuffer listener interface.
 *  @param <T> The object that shall be buffered
 * @author F.Calderini
 */
public interface SynchroBufferListener<T> extends EventListener {

  /** Callback method. Called with respect to the window size management properties. The callback is executed
   * within a single thread of execution.
   * @param event the pulled objects event
   * @throws PullException if the pull action failed
   */
  public void pull(PullEvent<T> event) throws PullException;
}
