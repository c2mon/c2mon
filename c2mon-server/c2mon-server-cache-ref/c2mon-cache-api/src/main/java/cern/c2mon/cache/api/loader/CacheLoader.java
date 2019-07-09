package cern.c2mon.cache.api.loader;

import cern.c2mon.shared.common.Cacheable;

/**
 * Interface that must be implemented by all C2MON
 * cache loading mechanisms.
 *
 * @author Szymon Halastra
 */
public interface CacheLoader<V extends Cacheable> {

  /**
   * At server start-up, loads the cache from the DB into memory.
   * In distributed set-up, this is not performed once the
   * cache has already been loaded once (only performed if the disk
   * store is cleaned).
   */
  void preload();
}
