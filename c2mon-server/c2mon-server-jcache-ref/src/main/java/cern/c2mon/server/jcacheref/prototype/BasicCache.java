package cern.c2mon.server.jcacheref.prototype;

import cern.c2mon.server.common.config.C2monCacheName;

/**
 * @author Szymon Halastra
 */
public interface BasicCache<K, V> {

  C2monCacheName getName();
}
