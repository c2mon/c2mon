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

import cern.c2mon.server.cache.ComparableCacheListener;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.util.threadhandler.ThreadHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Asynchronous cache listener that passes events received by the Ehcache
 * listener to the wrapped {@link ComparableCacheListener} on a single thread.
 *
 * <p>A new CacheListener is created each time a new (asynchronous) listener is
 * registered, wrapping the C2monCacheListener implemented by the module
 * listener class.
 *
 * <p>It instantiates threads for each cache notification method, and passes
 * each received object to the appropriate thread.
 *
 * @author Franz Ritter
 */
@Slf4j
public final class AsynchronousComparableCacheListener<T extends Cacheable> implements ComparableCacheListener<T> {

  /**
   * The threads dealing with updates.
   */
  public ThreadHandler notifyUpdateThreadHandler;

  /**
   * Constructor returning a CacheListener wrapping the {@link ComparableCacheListener}.
   * Instantiates and starts the required threads.
   *
   * @param cacheListener the wrapped listener object
   */
  public AsynchronousComparableCacheListener(final ComparableCacheListener<T> cacheListener) {
    try {
      this.notifyUpdateThreadHandler = new ThreadHandler(cacheListener, ComparableCacheListener.class.getMethod
          ("notifyElementUpdated", Cacheable.class, Cacheable.class));
      this.notifyUpdateThreadHandler.start();
    } catch (SecurityException e) {
      log.error("Security exception caught", e);
    } catch (NoSuchMethodException e) {
      log.error("No such method found in C2monCacheListener", e);
    }
  }

  @Override
  public void notifyElementUpdated(T original, T updated) {
    notifyUpdateThreadHandler.put(new Object[]{original, updated});

  }
}
