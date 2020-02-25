package cern.c2mon.cache.api.listener;

import cern.c2mon.shared.common.Cacheable;

import java.util.EventListener;
import java.util.Set;

/**
 * An alternative cache listener, that collects all incoming updates to a list,
 * then periodically runs an event handler on that list
 *
 * Eliminates duplicates based on object equality (two objects with the same id
 * could both exist in the resultset: for example, an {@code Alarm} with id 1
 * could be present twice in the results if it has different {@code Alarm#triggerTimestamp}).
 *
 * If between updates it collects a large amount of items (more than {@code BufferedCacheListenerImpl.DEFAULT_MAX_SIZE}),
 * it will create a task to handle those items proactively.
 */
public interface BufferedCacheListener<CACHEABLE extends Cacheable> extends EventListener {

  /**
   * @param cacheables the set of items sent to the listener so far, after
   *                   whatever event happened that has triggered this action
   */
  void apply(Set<CACHEABLE> cacheables);
}
