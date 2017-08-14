package cern.c2mon.cache.api;

/**
 * Interface that must be implemented by all C2MON
 * cache loading mechanisms.
 *
 * @author Szymon Halastra
 */
public interface C2monCacheLoader {

  /**
   * Lock used for synchronising all servers at start up. This takes
   * place during the alive start up: read locks are used for each of
   * the cache loading mechanisms and a write lock is acquired at the
   * alive start up.
   *
   * An associated Boolean flag in the ClusterCache is indicating if
   * the alive mechanisms was started for all the DAQs loaded into
   * the cache (performed once by a singleserver at start up).
   */
  String aliveStatusInitialized = "c2mon.cache.aliveStatusInitialized";

  /**
   * At server start-up, loads the cache from the DB into memory.
   * In distributed set-up, this is not performed once the TC
   * cache has already been loaded once (only performed if the disk
   * store is cleaned).
   */
  void preload();
}
