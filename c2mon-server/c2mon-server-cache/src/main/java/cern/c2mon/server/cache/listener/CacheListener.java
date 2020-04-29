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

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.ApplicationObjectSupport;

import cern.c2mon.server.cache.C2monCacheListener;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.daq.lifecycle.Lifecycle;
import cern.c2mon.shared.util.threadhandler.ThreadHandler;

/**
 * Asynchronous cache listener that passes events received by the Ehcache listener
 * to the wrapped {@link C2monCacheListener} on a single thread.
 *
 * <p>A new CacheListener is created each time a new (asynchronous) listener is registered, wrapping
 * the C2monCacheListener implemented by the module listener class.
 *
 * <p>It instantiates threads for each cache notification method, and passes each
 * received object to the appropriate thread.
 *
 * <p><b>This class is deprecated and the {@link MultiThreadedCacheListener} should
 * preferably be used instead (with #threads = 1 for a single-threaded listener)</b>
 *
 * @author Mark Brightwell
 *
 */
@Slf4j
public final class CacheListener<T extends Cacheable> extends ApplicationObjectSupport implements C2monCacheListener<T>, Lifecycle {

  /**
   * The threads dealing with updates.
   */
  private ThreadHandler notifyUpdateThreadHandler;
  private ThreadHandler statusConfirmationHandler;

  /**
   * Indicates if the listener is enabled (if not, notifications are ignored and exception is thrown).
   */
  private volatile boolean running = false;

  /**
   * Constructor returning a CacheListener wrapping the {@link C2monCacheListener}.
   * Instantiates and starts the required threads.
   *
   * @param cacheListener the wrapped listener object
   */
  public CacheListener(final C2monCacheListener<T> cacheListener) {
    super();

    try {
      this.notifyUpdateThreadHandler = new ThreadHandler(cacheListener, C2monCacheListener.class.getMethod("notifyElementUpdated", new Class< ? >[] {Cacheable.class}));
      this.statusConfirmationHandler = new ThreadHandler(cacheListener, C2monCacheListener.class.getMethod("confirmStatus", new Class< ? >[] {Cacheable.class}));

      this.notifyUpdateThreadHandler.setName("NotifyUpdater");
      this.statusConfirmationHandler.setName("StatusConfirm");
    } catch (SecurityException e) {
      log.error("Security exception caught.", e);
    } catch (NoSuchMethodException e) {
      log.error("No such method found in C2monCacheListener.", e);
    }

  }


  @Override
  public void confirmStatus(final T cacheable) {
    statusConfirmationHandler.put(new Object[] {cacheable});
  }


  @Override
  public void notifyElementUpdated(final T cacheable) {
    notifyUpdateThreadHandler.put(new Object[] {cacheable});
  }


  /**
   * For management purposes.
   * @return the size of the task queue for onUpdate calls
   */
  public int getTaskQueueSize() {
    return notifyUpdateThreadHandler.getTaskQueueSize();
  }

  /**
   * For management purposes.
   * @return the size of the task queue for status confirmations
   */
  public int getConfirmStatusQueueSize() {
    return statusConfirmationHandler.getTaskQueueSize();
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  /**
   * Synchronized to prevent simultaneous stop.
   */
  @Override
  public void start() {
    if (!running) {
      running = true;
      notifyUpdateThreadHandler.start();
      statusConfirmationHandler.start();
    }
  }

  /**
   * Waits for all tasks to complete then shuts down the thread.
   * Should be called when the cache is closed on server shutdown.
   * Synchronized to prevent simultaneous start.
   */
  @Override
  public void stop() {
    if (running) {
      running = false;
      if (log.isDebugEnabled()) {
        log.debug("Shutting down CacheListener threads.");
      }
      notifyUpdateThreadHandler.shutdown();
      statusConfirmationHandler.shutdown();
    }
  }



}
