package cern.c2mon.cache.api.lock;

import cern.c2mon.cache.api.C2monCache;

/**
 * @author Szymon Halastra
 */
public interface C2monLock {

  void acquireLockOnKey(C2monCache cache, Object key);

  void releaseLockOnKey(C2monCache cache, Object key);
}
