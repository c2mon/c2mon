package cern.c2mon.cache.api.listener;

import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.shared.common.Cacheable;

/**
 * TODO Delete this
 *
 * To be removed soon, just left here to review bizLogic
 *
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
@Deprecated
public interface DeprecatedListener<V extends Cacheable> {

  /**
   * Call this method to inform cache listeners that an update has
   * been performed for the given object of type T (is not automatic if the
   * cache object is not "put" back in the cache).
   *
   * <p>(update = existing cache value is modified)
   *
   * <p>This method should be called within a lock on the cache object or
   * with a copy so that no modification to this object is made until
   * the object has been cloned and passed to the listeners. This is taken care of
   * by the {@link DataTagFacade} bean, which should preferably be used
   * for making updates to the cache.
   *
   * @param cacheable the cache object that has been updated
   * (a copy is passed; should not be modified as shared across listeners)
   */
//  void notifyListenersOfUpdate(V cacheable);

  /**
   * Calls all listeners notified for supervision invalidation messages. These are
   * passed the tag as supplied to this method (supervision status needs to have
   * been applied beforehand.
   *
   * @param tag the tag affected by the supervision change, *with* the supervision
   *            status applied
   */
//  void notifyListenersOfSupervisionChange(V tag);

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
//  void notifyListenerStatusConfirmation(V cacheable, long timestamp);

  /**
   * For calling the listener on the same thread as the cache update.
   * TODO switch to this listener for rule listener and client/alarm listener for guaranteed no data loss!
   * (if performance permits... may need to increase the number of JMS listeners)
   * @param timCacheListener
   */
//  void registerSynchronousListener(CacheListener<? super V> cacheListener);

  /**
   * Registers the C2monCacheListener as listener to this cache. The listener
   * is notified when an element is updated in the cache. The notification
   * takes place on a single separate thread (asynchronous listener). A copy
   * of the cache object is passed and can be accessed to get details of the
   * object. However, the object should not be modified as it is passed to
   * many listeners. The listener should make a clone of the object if it
   * needs to be modified.
   *
   * @param timCacheListener the listener to register
   * @return a Lifecycle object to start and stop the listener thread; the start should be called once the
   *        registered C2monCacheListener is ready; the stop should be called at the start of the listener shutdown
   */
//  Lifecycle registerListener(CacheListener<? super V> cacheListener);

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
//  void registerListenerWithSupervision(CacheSupervisionListener<? super V> cacheSupervisionListener);

  /**
   * Registers a listener to be notified of updates on multiple threads.
   *
   * <p><b>Note</b> the listener is assumed to be thread-safe!
   *
   * @param timCacheListener the listener to register
   * @param queueCapacity the capacity of the queue of notification
   *        events (once full the server thread will be forced to wait)
   * @param threadPoolSize the number of threads on which the listener
   *        will be invoked (should be > 0)
   * @return a Lifecycle object to start and stop the listener thread; the start should be called once the
   *        registered C2monCacheListener is ready; the stop should be called at the start of the listener shutdown
   */
  Lifecycle registerThreadedListener(CacheListener<? super V> cacheListener, int queueCapacity, int threadPoolSize);

  /**
   * Register to be notified with collections of cache objects, rather than single objects.
   * @param bufferedTimCacheListener the listener that should be notified
   * @param frequency the frequency (in ms) at which the buffer should be emptied
   * @return a Lifecycle object to start and stop the listener thread; the start should be called once the
   *        registered C2monCacheListener is ready; the stop should be called at the start of the listener shutdown
   */
//  Lifecycle registerBufferedListener(BufferedCacheListener<Cacheable> bufferedCacheListener, int frequency);

  /**
   * Register to receive the Ids of cache objects that have been updated.
   * @param bufferedTimCacheListener the listener that should be notified
   * @param frequency the frequency (in ms) at which the buffer should be emptied
   * @return a Lifecycle object to start and stop the listener thread; the start should be called once the
   *  registered C2monCacheListener is ready; the stop should be called at the start of the listener shutdown
   */
//  Lifecycle registerKeyBufferedListener(BufferedCacheListener<Long> bufferedCacheListener, int frequency);
}
