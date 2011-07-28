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
import java.util.List;
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

  /** A list of the registered listeners */
  private final List<HistoryProviderListener> listeners;

  /** Lock for the listeners */
  private final ReentrantReadWriteLock listenersLock;

  /**
   * Constructor
   */
  public HistoryProviderAbs() {
    this.listeners = new ArrayList<HistoryProviderListener>();
    this.listenersLock = new ReentrantReadWriteLock();
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
   * Fires the queryStarting() method on all the listeners
   */
  protected void fireQueryStarting() {
    for (HistoryProviderListener listener : getHistoryProviderListeners()) {
      listener.queryStarting();
    }
  }

  /**
   * Fires the queryFinished() method on all the listeners
   */
  protected void fireQueryFinished() {
    for (HistoryProviderListener listener : getHistoryProviderListeners()) {
      listener.queryFinished();
    }
  }

  /**
   * Fires the queryProgressChanged(percent) method on all the listeners
   * 
   * @param percent
   *          The percentage which is currently loaded. Between 0.0 and 1.0
   */
  protected void fireQueryProgressChanged(final double percent) {
    for (HistoryProviderListener listener : getHistoryProviderListeners()) {
      listener.queryProgressChanged(percent);
    }
  }
}
