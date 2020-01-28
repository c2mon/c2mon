package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheObjectFactory;
import cern.c2mon.server.cache.loading.ConfigurableDAO;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.Cacheable;

import java.util.List;
import java.util.function.Supplier;

public abstract class ControlTagConfigHandler<CACHEABLE extends Cacheable> extends BaseConfigHandlerImpl<CACHEABLE> {

  protected ControlTagConfigHandler(C2monCache<CACHEABLE> cache,
                                    ConfigurableDAO<CACHEABLE> cacheLoaderDAO,
                                    AbstractCacheObjectFactory<CACHEABLE> factory,
                                    Supplier<List<ProcessChange>> defaultValue) {
    super(cache, cacheLoaderDAO, factory, defaultValue);
  }

  @Override
  protected void doPostCreate(CACHEABLE cacheable) {
    cache.getCacheListenerManager().notifyListenersOf(CacheEvent.INSERTED, cacheable);
  }
}
