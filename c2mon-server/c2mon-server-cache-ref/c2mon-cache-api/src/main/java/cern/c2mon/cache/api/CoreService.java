package cern.c2mon.cache.api;

/**
 * @author Szymon Halastra
 */
public interface CoreService<K, V> {

  C2monCache<K, V> getCache();
}
