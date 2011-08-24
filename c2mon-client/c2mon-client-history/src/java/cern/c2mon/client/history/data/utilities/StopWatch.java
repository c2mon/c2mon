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
package cern.c2mon.client.history.data.utilities;

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
