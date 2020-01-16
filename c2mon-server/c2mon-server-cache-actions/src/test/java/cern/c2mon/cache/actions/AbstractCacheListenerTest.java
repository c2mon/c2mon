package cern.c2mon.cache.actions;

import cern.c2mon.cache.AbstractCacheTest;
import cern.c2mon.cache.api.listener.CacheListenerManagerImpl;
import cern.c2mon.server.common.AbstractCacheableImpl;
import cern.c2mon.shared.common.Cacheable;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractCacheListenerTest<V extends Cacheable, V_IMPL extends AbstractCacheableImpl>
  extends AbstractCacheTest<V, V_IMPL> {

  @Before
  public void resetCacheListenerManager() {
    cache.setCacheListenerManager(new CacheListenerManagerImpl<>());
  }

  @After
  public void closeCacheListeners(){
    cache.getCacheListenerManager().close();
  }
}
