package cern.c2mon.cache.actions;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.shared.common.Cacheable;

public interface AbstractCacheService<T extends Cacheable> {

  C2monCache<T> getCache();
}
