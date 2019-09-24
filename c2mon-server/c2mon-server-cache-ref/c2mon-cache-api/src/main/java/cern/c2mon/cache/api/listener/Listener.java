package cern.c2mon.cache.api.listener;

import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.shared.common.Cacheable;

/**
 * @author Szymon Halastra
 */
public interface Listener<V extends Cacheable> {

  void notifyListenersOfUpdate(V cacheable);

  /**
   * Calls all listeners notified for supervision invalidation messages. These are
   * passed the tag as supplied to this method (supervision status needs to have
   * been applied beforehand.
   *
   * @param tag the tag affected by the supervision change, *with* the supervision
   *            status applied
   */
  void notifyListenersOfSupervisionChange(V tag);

  void notifyListenerStatusConfirmation(V cacheable, long timestamp);

  void registerSynchronousListener(CacheListener<? super V> cacheListener);

  Lifecycle registerListener(CacheListener<? super V> cacheListener);

  /**
   * Register a listener to be notified of supervision invalidations/validation callbacks
   * due to Supervision changes for each affected Tag (including rules).
   *
   * <p>Notice the cache get(Long) method does not append this information
   * to the Tag (no change is made in the cache).
   *
   * <p>Listeners get notified of tag supervision status changes, but the Tag timestamps remain UNCHANGED.
   * Consequently, tags arriving with identical timetamps should be accepted by the listener, at least if
   * they have a Supervision invalidation set. The listener can publish the event with a new timestamp
   * if necessary (although it will then need to decide how to deal with an older incoming valid value).
   *
   * <p>However, these listeners SHOULD FILTER OUT UPDATES WITH OLDER SERVER TIMESTAMPS (by checking
   * the current value in the cache for instance), never accepting older updates. This must be done as
   * there is no guarantee that a "revalidation" supervision event will not be overtaken by a newer valid
   * update, which will have a more recent server timestamp (since the revalidation notification is
   * using the timestamp of the previous value).
   *
   * <p>Notice that if the supervision status changes twice in close succession, there is no.
   * TODO Change this to a lifecycle?
   * @param cacheSupervisionListener the listener to register
   */
  void registerListenerWithSupervision(CacheSupervisionListener<? super V> cacheSupervisionListener);

  Lifecycle registerThreadedListener(CacheListener<? super V> cacheListener, int queueCapacity, int threadPoolSize);

  Lifecycle registerBufferedListener(BufferedCacheListener<Cacheable> bufferedCacheListener, int frequency);

  Lifecycle registerKeyBufferedListener(BufferedCacheListener<Long> bufferedCacheListener, int frequency);
}
