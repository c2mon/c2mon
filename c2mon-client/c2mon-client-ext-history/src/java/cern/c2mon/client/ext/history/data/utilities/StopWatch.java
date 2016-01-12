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
package cern.c2mon.client.ext.history.data.utilities;

/**
 * Used to measure how long time something takes.
 * 
 * @author vdeila
 */
public final class StopWatch {

  /**
   * 
   * @return A new started <code>StopWatch</code>
   */
  public static StopWatch start() {
    return new StopWatch();
  }

  /** The time the watch were started */
  private long startTime;

  /**
   * Constructor. Starts the time
   */
  private StopWatch() {
    this.resetWatch();
  }

  /**
   * Resets the watch
   */
  public void resetWatch() {
    this.startTime = System.currentTimeMillis();
  }

  /**
   * This function can be called several times. It will not reset the watch.
   * 
   * @return The elapsed time since the watch started in milliseconds.
   */
  public long stop() {
    return System.currentTimeMillis() - this.startTime;
  }

}
