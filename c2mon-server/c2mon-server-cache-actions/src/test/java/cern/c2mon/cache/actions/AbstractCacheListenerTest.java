package cern.c2mon.cache.actions;

import cern.c2mon.cache.AbstractCacheTest;
import cern.c2mon.cache.api.listener.CacheListenerManagerImpl;
import cern.c2mon.shared.common.Cacheable;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractCacheListenerTest<V extends Cacheable> extends AbstractCacheTest<V> {

  @Before
  public void resetCacheListenerManager() {
    cache.setCacheListenerManager(new CacheListenerManagerImpl<>());
  }

  @After
  public void closeCacheListeners(){
    cache.getCacheListenerManager().close();
  }
}
