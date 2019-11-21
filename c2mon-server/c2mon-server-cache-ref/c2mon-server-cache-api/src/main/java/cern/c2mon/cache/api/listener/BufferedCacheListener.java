package cern.c2mon.cache.api.listener;

import cern.c2mon.shared.common.Cacheable;

import java.util.EventListener;
import java.util.List;

/**
 * An alternative cache listener, that collects all incoming updates to a list,
 * then periodically runs an event handler on that list
 */
public interface BufferedCacheListener<CACHEABLE extends Cacheable> extends EventListener {


  /**
   * @param cacheableList the list of items sent to the listener so far, after
   *                      whatever event happened that has triggered this action
   */
  void apply(List<CACHEABLE> cacheableList);
}
