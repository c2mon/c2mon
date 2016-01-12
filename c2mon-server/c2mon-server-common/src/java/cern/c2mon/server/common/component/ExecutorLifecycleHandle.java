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
package cern.c2mon.server.common.component;

import java.util.concurrent.ExecutorService;

/**
 * Allows executor shutdown via the C2MON {@link Lifecycle} interface.
 * Used in particular for listeners to be able to shutdown notification
 * threads running as ExecutorService's.
 * 
 * <p>Only attempts smooth shutdown. Stop() will not return if unsuccessful.
 * 
 * @author Mark Brightwell
 *
 */
public class ExecutorLifecycleHandle implements Lifecycle {

  /**
   * Service to manage.
   */
  private ExecutorService executor;
  
  /**
   * Constructor
   * @param executor service whose lifecycle is to be managed
   */
  public ExecutorLifecycleHandle(final ExecutorService executor) {
    super();
    this.executor = executor;
  }

  @Override
  public boolean isRunning() {
    return !executor.isShutdown();
  }

  @Override
  public void start() {
    //do nothing
  }

  @Override
  public void stop() {
    executor.shutdown();
  }

}
