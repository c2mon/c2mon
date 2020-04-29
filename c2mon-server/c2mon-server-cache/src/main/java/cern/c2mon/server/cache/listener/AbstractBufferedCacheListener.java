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
package cern.c2mon.server.cache.listener;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import cern.c2mon.server.cache.C2monBufferedCacheListener;
import cern.c2mon.server.cache.C2monCacheListener;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.daq.lifecycle.Lifecycle;

/**
 * Abstract listener implementation that batches the notifications before
 * calling the BufferedListener
 *
 * @author Mark Brightwell
 * @param <T> the type of cache object received
 * @param <S> the type of object passed to the listener
 *
 */
@Slf4j
public abstract class AbstractBufferedCacheListener<T extends Cacheable, S> implements C2monCacheListener<T>, Lifecycle {

  /**
   * Max number of objects passed to the listener.
   */
  private static final int MAX_TO_LISTENER = 20000;

  private static final long CHECK_FREQUENCY = 500L;

  /**
   * Max sleep time between pulls (could be longer if previous task is longer)
   */
  private int frequency;

  /**
   * Queues keeping the keys for supported methods.
   */
  private final LinkedBlockingQueue<S> onUpdateQueue = new LinkedBlockingQueue<>();
  private final LinkedBlockingQueue<S> statusConfirmationQueue = new LinkedBlockingQueue<>();

  /**
   * Wrapped listener.
   */
  private C2monBufferedCacheListener<S> bufferedCacheListener;

  /**
   * Indicates if the listener is enabled (if not, notifications are ignored and exception is thrown).
   */
  @Getter
  private volatile boolean enabled;

  /**
   * Notice that duplicate policy is ignored if finite capacity is set (duplicate.OK is set) - TODO rewrite/adapt SynchroBuffer for this...

   * @param bufferedCacheListener listener expecting collections of cache objects
   * @param frequency the frequency (in ms) at which the buffer should be emptied
   */
  public AbstractBufferedCacheListener(C2monBufferedCacheListener<S> bufferedCacheListener, int frequency) {
    this.bufferedCacheListener = bufferedCacheListener;
    this.frequency = frequency;
    enabled = false;
  }

  abstract S getDerivedObject(T cacheable);

  @Override
  public void confirmStatus(T cacheable) {
    if (enabled) {
      try {
        statusConfirmationQueue.put(getDerivedObject(cacheable));
      } catch (InterruptedException e) {
        log.error("Interrupted while waiting to insert into queue", e);
      }
    } else {
      String errorMessage = "Updated notification received with listener disabled";
      log.warn(errorMessage);
      throw new IllegalStateException(errorMessage);
    }
  }

  @Override
  public void notifyElementUpdated(T cacheable) {
    if (enabled) {
      try {
        onUpdateQueue.put(getDerivedObject(cacheable));
      } catch (InterruptedException e) {
        log.error("Interrupted while waiting to insert key into queue", e);
      }
    } else {
      String errorMessage = "Update notification received with listener disabled for " + bufferedCacheListener.getThreadName();
      log.warn(errorMessage);
      throw new IllegalStateException(errorMessage);
    }
  }

  /**
   * A simple wrapper method around {@link AbstractBufferedCacheListener#notifyElementUpdated(Cacheable)}
   * @param cacheableList A list of {@link Cacheable} objects
   */
  public void notifyElementsUpdated(Collection<T> cacheableList) {
    for (T cacheable : cacheableList) {
      notifyElementUpdated(cacheable);
    }
  }

  @Override
  public synchronized boolean isRunning() {
    return enabled;
  }

  /**
   * Synchronized to prevent simultaneous start/stop.
   */
  @Override
  public synchronized void start() {
    if (!enabled) {
      log.info("Starting BufferedCacheListener for {}", bufferedCacheListener.getThreadName());

      Runnable task = () -> {
        while (enabled || !onUpdateQueue.isEmpty() || !statusConfirmationQueue.isEmpty()) {
          long millisStart = System.currentTimeMillis();
          flush();
          while (System.currentTimeMillis() - millisStart < frequency) {
            try {
              Thread.sleep(CHECK_FREQUENCY);
            } catch (InterruptedException e) {
              log.error("Sleep interrupted in BufferedCacheListener thread");
            }
          }
        }
      };

      new Thread(task, bufferedCacheListener.getThreadName()).start();
      enabled = true;
    }
  }

  @Override
  public synchronized void stop() {
    if (enabled) {
      log.info("Shutting down BufferedKeyCacheListener for {}", bufferedCacheListener.getThreadName());
      enabled = false;
      flush();
    }
  }

  private synchronized void flush() {
    if (!onUpdateQueue.isEmpty()) {
      LinkedList<S> updateKeys = new LinkedList<>();
      onUpdateQueue.drainTo(updateKeys, MAX_TO_LISTENER);
      if (!updateKeys.isEmpty()) {
        try {
          bufferedCacheListener.notifyElementUpdated(updateKeys);
        } catch (Exception e) {
          log.error("Uncaught exception occured in {} whilst notifying for update of {} elements!",  bufferedCacheListener.getThreadName(), updateKeys.size(), e);
        }
      }
    }

    if (!statusConfirmationQueue.isEmpty()) {
      LinkedList<S> confirmationKeys = new LinkedList<>();
      statusConfirmationQueue.drainTo(confirmationKeys, MAX_TO_LISTENER);
      if (!confirmationKeys.isEmpty()) {
        try {
          bufferedCacheListener.confirmStatus(confirmationKeys);
        } catch (Exception e) {
          log.error("Uncaught exception occured in {} whilst confirming status of {} cache objects!", bufferedCacheListener.getThreadName(), confirmationKeys.size(), e);
        }
      }
    }
  }


}
