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
package cern.c2mon.client.history.playback.data.utilities;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * This class is used to check the memory consumption. Is used by the
 * {@link HistoryLoader} before loading a new bunch of history.
 * 
 * @see MemoryConsumptionAdviserListener
 * 
 * @author vdeila
 * 
 */
public class MemoryConsumptionAdviser {

  /** Log4j logger for this class */
  private static final Logger LOG = Logger.getLogger(MemoryConsumptionAdviser.class);

  /** The default value of {@link #maximumMemoryConsumption} */
  private static final double DEFAULT_MAXIMUM_MEMORY_CONSUMPTION_PERCENT = 0.75;

  /**
   * The maximum memory consumption in percent of the total memory available
   * that can be used. A number between 0.0 - 1.0
   */
  private double maximumMemoryConsumption;

  /** The listeners of events */
  private final List<MemoryConsumptionAdviserListener> listeners;

  /**
   * Constructor
   */
  public MemoryConsumptionAdviser() {
    this.maximumMemoryConsumption = DEFAULT_MAXIMUM_MEMORY_CONSUMPTION_PERCENT;
    this.listeners = new ArrayList<MemoryConsumptionAdviserListener>();
  }

  /**
   * 
   * @return <code>true</code> if more data can be loaded. <code>false</code> if
   *         there is not space in memory for more data
   */
  public boolean haveEnoughMemory() {
    return haveEnoughMemory(false);
  }

  /**
   * 
   * @param doCleanUp
   *          <code>true</code> if cleanup should be done before checking the
   *          memory
   * @return <code>true</code> if more data can be loaded. <code>false</code> if
   *         there is not space in memory for more data
   */
  private boolean haveEnoughMemory(final boolean doCleanUp) {
    final Runtime runtime = Runtime.getRuntime();

    if (doCleanUp) {
      // Notifies the listeners
      for (final MemoryConsumptionAdviserListener listener : getMemoryConsumptionAdviserListeners()) {
        listener.cleanUpObjects();
      }
      runtime.gc();
    }

    final long maximumMemoryConsumption = (long) (runtime.maxMemory() * this.maximumMemoryConsumption);

    final long freeMemory = runtime.freeMemory();
    final long currentMemoryConsuption = runtime.totalMemory() - runtime.freeMemory();
    if (currentMemoryConsuption > maximumMemoryConsumption) {
      if (doCleanUp) {
        LOG.warn(String.format("Low on memory (%d MB free)", freeMemory / 1000000));
        return false;
      }
      else {
        LOG.debug("Running the JVM garbage collector because of low memory");
        return haveEnoughMemory(true);
      }
    }
    else {
      return true;
    }
  }

  /**
   * @return the maximum memory consumption in percent of the total memory
   *         available that can be used. A number between 0.0 - 1.0
   */
  public double getMaximumMemoryConsumption() {
    return maximumMemoryConsumption;
  }

  /**
   * @param maximumMemoryConsumption
   *          The maximum memory consumption in percent of the total memory
   *          available that can be used. A number between 0.0 - 1.0
   */
  public void setMaximumMemoryConsumption(final double maximumMemoryConsumption) {
    this.maximumMemoryConsumption = maximumMemoryConsumption;
  }

  /**
   * 
   * @param listener
   *          The listener to add
   */
  public synchronized void addMemoryConsumptionAdviserListener(final MemoryConsumptionAdviserListener listener) {
    this.listeners.add(listener);
  }

  /**
   * 
   * @param listener
   *          The listener to remove
   */
  public synchronized void removeMemoryConsumptionAdviserListener(final MemoryConsumptionAdviserListener listener) {
    this.listeners.remove(listener);
  }

  /**
   * 
   * @return a copy of the list of listeners
   */
  private synchronized MemoryConsumptionAdviserListener[] getMemoryConsumptionAdviserListeners() {
    return this.listeners.toArray(new MemoryConsumptionAdviserListener[0]);
  }

}
