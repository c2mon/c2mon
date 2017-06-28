package cern.c2mon.server.jcacheref.various.temp;

import javax.cache.Cache;

/**
 * @author Szymon Halastra
 */
public class C2monDistCache {

  //Inject
  C2monLock lock;
  Cache<Long, Object> cache;

  public void get() {
//    lock.lock();
//    cache.get(1L);
//    //do stuff
//    lock.unlock(1);
  }
}
