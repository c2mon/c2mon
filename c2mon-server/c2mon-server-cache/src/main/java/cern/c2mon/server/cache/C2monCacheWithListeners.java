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
package cern.c2mon.server.cache;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.shared.common.Cacheable;

public interface C2monCacheWithListeners<K, T extends Cacheable> extends C2monCache<K, T> {

  /**
   * Notifies all registered listeners of the current status
   * of this cache object. Used for instance to fix inconsistent
   * state after crash. Must also be called within a write lock, to
   * ensure against race conditions for synchronous listeners (could
   * be removed once timestamp use is generalized).
   *
   * @param cacheable the object to process (copy of cache object)
   * @param time the object was retrieved in the cache
   */
  void notifyListenerStatusConfirmation(T cacheable, long timestamp);

  /**
   * For calling the listener on the same thread as the cache update.
   * TODO switch to this listener for rule listener and client/alarm listener for guaranteed no data loss!
   * (if performance permits... may need to increase the number of JMS listeners)
   * @param cacheListener
   */
  void registerSynchronousListener(C2monCacheListener<? super T> cacheListener);

  /**
   * Registers the C2monCacheListener as listener to this cache. The listener
   * is notified when an element is updated in the cache. The notification
   * takes place on a single separate thread (asynchronous listener). A copy
   * of the cache object is passed and can be accessed to get details of the
   * object. However, the object should not be modified as it is passed to
   * many listeners. The listener should make a clone of the object if it
   * needs to be modified.
   *
   * @param cacheListener the listener to register
   * @return a Lifecycle object to start and stop the listener thread; the
   * start should be called once the registered C2monCacheListener is ready;
   * the stop should be called at the start of the listener shutdown
   */
  Lifecycle registerListener(C2monCacheListener<? super T> cacheListener);

  /**
   * Registers the C2monCompareCacheListener as listener to this cache.
   * The listener is notified when an element is updated in the cache. A copy
   * of the new cache object and the former cache object is passed and can be
   * accessed to get details of the object. However, the objects should not be
   * modified as they might be passed to many listeners. The listener should
   * make a clone of the objects if it needs to be modified.
   *
   * @param cacheListener the listener to register
   *
   */
  void registerComparableListener(ComparableCacheListener<? super T> cacheListener);

  /**
   * Register to be notified with collections of cache objects, rather than
   * single objects.
   * @param bufferedCacheListener the listener that should be notified
   * @param frequency the frequency (in ms) at which the buffer should be emptied
   * @return a Lifecycle object to start and stop the listener thread; the
   * start should be called once the registered C2monCacheListener is ready;
   * the stop should be called at the start of the listener shutdown
   */
  Lifecycle registerBufferedListener(C2monBufferedCacheListener<? super T> bufferedCacheListener, int frequency);

  /**
   * Register to receive the Ids of cache objects that have been updated.
   * @param bufferedCacheListener the listener that should be notified
   * @param frequency the frequency (in ms) at which the buffer should be emptied
   * @return a Lifecycle object to start and stop the listener thread; the
   * start should be called once the registered C2monCacheListener is ready;
   * the stop should be called at the start of the listener shutdown
   */
  Lifecycle registerKeyBufferedListener(C2monBufferedCacheListener<Long> bufferedCacheListener, int frequency);

  /**
   * Registers a listener to be notified of updates on multiple threads.
   *
   * <p><b>Note</b> the listener is assumed to be thread-safe!
   *
   * @param cacheListener the listener to register
   * @param queueCapacity the capacity of the queue of notification
   *        events (once full the server thread will be forced to wait)
   * @param threadPoolSize the number of threads on which the listener
   *        will be invoked (should be > 0)
   * @return a Lifecycle object to start and stop the listener thread; the start
   * should be called once the registered C2monCacheListener is ready;
   * the stop should be called at the start of the listener shutdown
   */
  Lifecycle registerThreadedListener(C2monCacheListener< ? super T> cacheListener, int queueCapacity, int threadPoolSize);

  /**
   * Identical to the {@link #notifyListenersOfUpdate(Cacheable)} method but
   * fetches first for you the Cacheable object and makes a copy.
   * @param id the id of the object in cache that was updated
   * @see #notifyListenersOfUpdate(Cacheable)
   */
  void notifyListenersOfUpdate(K id);

  /**
   * Call this method to inform cache listeners that an update has
   * been performed for the given object of type T (is not automatic if the
   * cache object is not "put" back in the cache).
   *
   * <p>(update = existing cache value is modified)
   *
   * <p>This method should be called within a lock on the cache object or
   * with a copy so that no modification to this object is made until
   * the object has been cloned and passed to the listeners. This is taken care
   * of by the {@link DataTagFacade} bean, which should preferably be used
   * for making updates to the cache.
   *
   * @param cacheable the cache object that has been updated
   * (a copy is passed; should not be modified as shared across listeners)
   */
  void notifyListenersOfUpdate(T cacheable);

  /**
   * Loads the cache element from the DB into the cache. Any existing cache
   * element will be overwritten.
   *
   * <p>Never returns null.
   *
   * @param id id of element to load
   * @return the cache element put in the cache
   * @throws CacheElementNotFoundException if unable to locate in the DB
   * (and if DB unavailable)
   */
  T loadFromDb(K id);

}
