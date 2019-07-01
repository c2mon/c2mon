package cern.c2mon.server.cache;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.shared.common.Cacheable;

public abstract class C2monCacheFactory extends AbstractCacheFactory {

  public abstract <V extends Cacheable> C2monCache<V> createCache(String name, Class<V> valueType);
}
