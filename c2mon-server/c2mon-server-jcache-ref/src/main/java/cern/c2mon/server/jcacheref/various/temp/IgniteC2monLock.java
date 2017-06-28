package cern.c2mon.server.jcacheref.various.temp;

import javax.cache.Cache;

/**
 * @author Szymon Halastra
 */
public class IgniteC2monLock implements C2monLock {

  @Override
  public void lock(Cache cache, Object key) {
//    Lock lock = ((IgniteCache) cache).lock(key);
//
//    lock.lock();
  }

  @Override
  public void unlock(Cache cache, Object key) {
//    Lock lock = ((IgniteCache) cache).lock(key);
//    lock.unlock();
  }
}
