package cern.c2mon.server.jcacheref.various.temp;

import javax.cache.Cache;

/**
 * @author Szymon Halastra
 */
public interface C2monLock {

  void lock(Cache cache, Object key);
  void unlock(Cache cache, Object key);
}
