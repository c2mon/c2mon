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
package cern.c2mon.client.history.dbaccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import cern.c2mon.client.common.history.HistoryProvider;
import cern.c2mon.client.common.history.event.HistoryProviderListener;

/**
 * Implements the listener functions which is common with the history providers
 * 
 * @author vdeila
 * 
 */
abstract class HistoryProviderAbs implements HistoryProvider {

  private static final Double PROGRESS_FINISH = 1.0; 
  
  /** A list of the registered listeners */
  private final List<HistoryProviderListener> listeners;

  /** Lock for the listeners */
  private final ReentrantReadWriteLock listenersLock;
  
  /** Keeps track of the queries and their progress */
  private final Map<Object, Double> queries;
  
  /** Lock for {@link #queries} */
  private final ReentrantReadWriteLock queriesLock = new ReentrantReadWriteLock();

  /** A counter for creating unique identifiers */
  private volatile long uniqueIdentifier = 1L;
  
  /**
   * <code>true</code> if the {@link HistoryProvider#disableProvider()} have
   * been called
   */
  private boolean disableProvider = false;
  
  /**
   * Constructor
   */
  public HistoryProviderAbs() {
    this.listeners = new ArrayList<HistoryProviderListener>();
    this.listenersLock = new ReentrantReadWriteLock();
    this.queries = new HashMap<Object, Double>();
  }

  @Override
  public void addHistoryProviderListener(final HistoryProviderListener listener) {
    try {
      this.listenersLock.writeLock().lock();
      this.listeners.add(listener);
    }
    finally {
      this.listenersLock.writeLock().unlock();
    }
  }

  @Override
  public void removeHistoryProviderListener(final HistoryProviderListener listener) {
    try {
      this.listenersLock.writeLock().lock();
      this.listeners.remove(listener);
    }
    finally {
      this.listenersLock.writeLock().unlock();
    }
  }

  /**
   * 
   * @return The listeners registered
   */
  protected HistoryProviderListener[] getHistoryProviderListeners() {
    try {
      this.listenersLock.readLock().lock();
      return this.listeners.toArray(new HistoryProviderListener[0]);
    }
    finally {
      this.listenersLock.readLock().unlock();
    }
  }

  /**
   * <code>true</code> if the {@link #fireQueryStarting()} have been fired
   * without {@link #fireQueryFinished(Object)} have been called
   */
  private AtomicBoolean queryStartedNotified = new AtomicBoolean(false);
  
  /**
   * Fires the queryStarting() method on all the listeners
   *
   * @return the identifier used for the rest of the calls
   */
  protected Object fireQueryStarting() {
    final Object queryId = Long.valueOf(uniqueIdentifier++);
    queriesLock.writeLock().lock();
    try {
      this.queries.put(queryId, 0.0);
    }
    finally {
      queriesLock.writeLock().unlock();
    }
    
    if (queryStartedNotified.compareAndSet(false, true)) {
      for (HistoryProviderListener listener : getHistoryProviderListeners()) {
        listener.queryStarting();
      }
    }
    
    fireQueryProgressChanged();
    return queryId;
  }

  /**
   * Fires the queryFinished() method on all the listeners
   * 
   * @param queryId
   *          an identifier for the query
   */
  protected void fireQueryFinished(final Object queryId) {
    queriesLock.writeLock().lock();
    try {
      if (this.queries.remove(queryId) != null) {
        this.queries.put(queryId, PROGRESS_FINISH);
      }
    }
    finally {
      queriesLock.writeLock().unlock();
    }
    fireQueryProgressChanged();
  }

  /**
   * Fires the queryProgressChanged(percent) method on all the listeners
   * 
   * @param percent
   *          The percentage which is currently loaded. Between 0.0 and 1.0
   * @param queryId
   *          an identifier for the query
   */
  protected void fireQueryProgressChanged(final Object queryId, final double percent) {
    queriesLock.writeLock().lock();
    try {
      this.queries.put(queryId, percent);
    }
    finally {
      queriesLock.writeLock().unlock();
    }
    fireQueryProgressChanged();
  }
  
  /**
   * Fires the queryProgressChanged(percent) method on all the listeners with
   * the combined percentage of all the concurrent queries.
   */
  private void fireQueryProgressChanged() {
    boolean allIsFinish = true;
    
    // Gets the percent
    double percent = 0.0;
    queriesLock.readLock().lock();
    try {
      final int numberOfQueries = this.queries.size();
      for (Double singlePercent : this.queries.values()) {
        percent += singlePercent / (double) numberOfQueries;
        if (!singlePercent.equals(PROGRESS_FINISH)) {
          allIsFinish = false;
        }
      }
    }
    finally {
      queriesLock.readLock().unlock();
    }
    
    if (percent > 1.0) {
      percent = 1.0;
    }
    
    for (HistoryProviderListener listener : getHistoryProviderListeners()) {
      listener.queryProgressChanged(percent);
    }
    
    // Notifies listeners if all queries are finish
    if (allIsFinish && queryStartedNotified.compareAndSet(true, false)) {
      for (HistoryProviderListener listener : getHistoryProviderListeners()) {
        listener.queryFinished();
      }
    }
  }
  
  /**
   * 
   * @return <code>true</code> if the {@link HistoryProvider#disableProvider()}
   *         have been called
   */
  protected synchronized boolean isProviderDisabled() {
    return disableProvider;
  }
  
  @Override
  public synchronized void disableProvider() {
    disableProvider = true;
  }

  @Override
  public synchronized void enableProvider() {
    disableProvider = false;
  }

  @Override
  public int getExecutingJobsCount() {
    int numberOfJobs = 0;
    final Collection<Double> percents;
    queriesLock.readLock().lock();
    try {
      percents = new ArrayList<Double>(this.queries.values());
    }
    finally {
      queriesLock.readLock().unlock();
    }
    for (Double percent : percents) {
      if (percent < 1.0) {
        numberOfJobs++;
      }
    }
    return numberOfJobs;
  }

  @Override
  public void resetProgress() {
    queriesLock.readLock().lock();
    try {
      final Iterator<Entry<Object, Double>> iterator = this.queries.entrySet().iterator();
      while (iterator.hasNext()) {
        final Entry<Object, Double> entry = iterator.next();
        if (entry.getValue().equals(PROGRESS_FINISH)) {
          iterator.remove();
        }
      }
    }
    finally {
      queriesLock.readLock().unlock();
    }
    fireQueryProgressChanged();
  }
  
  
}
 