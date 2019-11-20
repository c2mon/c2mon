package cern.c2mon.cache.api.listener;

import cern.c2mon.shared.common.Cacheable;

import java.util.EventListener;


public interface CacheListener<V extends Cacheable> extends EventListener {

  /**
   * Any update operations should be done by reinserting the item in cache (and retriggering listeners along the way)
   *
   * @param cacheable the item, after whatever event happened that has triggered this action
   */
  void apply(V cacheable);
}
