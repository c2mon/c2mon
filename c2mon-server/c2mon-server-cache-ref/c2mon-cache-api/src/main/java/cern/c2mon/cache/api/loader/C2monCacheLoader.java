package cern.c2mon.cache.api.loader;

import cern.c2mon.cache.api.C2monCache;

/**
 * @author Szymon Halastra
 */
public interface C2monCacheLoader {

  void loadCache(C2monCache cache);
}
