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
package cern.c2mon.client.ext.history.playback.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class manages listeners in a thread safe manner
 * 
 * @author vdeila
 * 
 * @param <ListenerType> The type of the listeners
 */
public class ListenersManager<ListenerType> {

  /** The list of the listener */
  private List<ListenerType> listeners = new ArrayList<ListenerType>();

  /** The lock for <code>listeners</code> */
  private ReentrantReadWriteLock listenersLock = new ReentrantReadWriteLock();

  /**
   * 
   * @return The list of listeners
   */
  public Collection<ListenerType> getAll() {
    try {
      this.listenersLock.readLock().lock();
      return new ArrayList<ListenerType>(this.listeners);

    }
    finally {
      this.listenersLock.readLock().unlock();
    }
  }

  /**
   * 
   * @param listener
   *          The listener to add
   */
  public void add(final ListenerType listener) {
    try {
      this.listenersLock.writeLock().lock();
      this.listeners.add(listener);
    }
    finally {
      this.listenersLock.writeLock().unlock();
    }
  }

  /**
   * 
   * @param listener
   *          The listener to remove
   */
  public void remove(final ListenerType listener) {
    try {
      this.listenersLock.writeLock().lock();
      this.listeners.remove(listener);
    }
    finally {
      this.listenersLock.writeLock().unlock();
    }
  }
}
