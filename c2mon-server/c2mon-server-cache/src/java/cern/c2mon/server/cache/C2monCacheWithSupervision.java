package cern.c2mon.server.cache;

import cern.c2mon.server.common.tag.Tag;

/**
 * Implemented by caches with listeners/modules relying on invalidation notifications due
 * to supervision changes. Currently this corresponds to Tag caches (datatags,
 * rules and controltags).
 *  
 * @author Mark Brightwell
 * @param <T> the type of the objects contained in this cache
 * @param <K> the type of the cache keys
 *
 */
public interface C2monCacheWithSupervision<K, T extends Tag> extends C2monCacheWithListeners<K, T> {

  
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
   * 
   * @param cacheSupervisionListener the listener to register
   */
  void registerListenerWithSupervision(CacheSupervisionListener< ? super T> cacheSupervisionListener);

  /**
   * Calls all listeners notified for supervision invalidation messages. These are
   * passed the tag as supplied to this method (supervision status needs to have 
   * been applied beforehand.
   * 
   * @param tag the tag affected by the supervision change, *with* the supervision
   *        status applied
   */
  void notifyListenersOfSupervisionChange(T tag);
  
}
