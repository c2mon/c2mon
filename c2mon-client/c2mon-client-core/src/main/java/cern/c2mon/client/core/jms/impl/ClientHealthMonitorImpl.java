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
package cern.c2mon.client.core.jms.impl;

import cern.c2mon.client.core.jms.ClientHealthListener;
import cern.c2mon.client.core.jms.ClientHealthMonitor;
import cern.c2mon.client.core.jms.EnqueuingEventListener;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ClientHealthMonitorImpl implements ClientHealthMonitor, SlowConsumerListener, EnqueuingEventListener {

  /**
   * Listeners.
   */
  private HashSet<ClientHealthListener> listeners  = new HashSet<>();

  /**
   * Lock to access listener set.
   */
  private ReentrantReadWriteLock listenerLock = new ReentrantReadWriteLock();

  /**
   * Will only notify listeners once.
   */
  private volatile boolean slowConsumerNotified = false;

  @Override
  public void addHealthListener(final ClientHealthListener clientHealthListener) {
    listenerLock.writeLock().lock();
    try {
      listeners.add(clientHealthListener);
    } finally {
      listenerLock.writeLock().unlock();
    }
  }

  @Override
  public void removeHealthListener(final ClientHealthListener clientHealthListener) {
    listenerLock.writeLock().lock();
    try {
      listeners.remove(clientHealthListener);
    } finally {
      listenerLock.writeLock().unlock();
    }
  }

  @Override
  public void onSlowConsumer(String details) {
    if (!slowConsumerNotified) {
      log.warn("Slow update consumer detected: {}", details);
      listenerLock.writeLock().lock();
      try {
        for (ClientHealthListener listener : listeners) {
          listener.onSlowUpdateListener(details);
        }
        slowConsumerNotified = true;
      } finally {
        listenerLock.writeLock().unlock();
      }
    }
  }

  @Override
  public void onEnqueuingEvent(String details, int percentage) {
    log.warn("Enqueuing event: {}", details);
    listenerLock.writeLock().lock();
    try {
      for (ClientHealthListener listener : listeners) {
        listener.onEnqueuingEventListener(details, percentage);
      }
    } finally {
      listenerLock.writeLock().unlock();
    }

  }
}
