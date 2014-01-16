package cern.c2mon.server.cache;

import cern.c2mon.server.common.commfault.CommFaultTag;

/**
 * Interface to the {@link CommFaultTag} cache.
 * 
 * @author Matthias Braeger
 */
public interface CommFaultTagCache extends C2monCacheWithListeners<Long, CommFaultTag> {

  String cacheInitializedKey = "c2mon.cache.commfault.initialized";
}
