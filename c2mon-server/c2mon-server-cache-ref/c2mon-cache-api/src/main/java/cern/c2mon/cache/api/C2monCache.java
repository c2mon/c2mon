package cern.c2mon.cache.api;

import java.util.List;

/**
 * @param <K> cache key type
 * @param <V> cache element type
 *
 * @author Szymon Halastra
 */
public abstract class C2monCache<K, V> {

  public C2monCache() {

  }

  protected abstract V get(K key);

  protected abstract boolean containsKey(K key);

  protected abstract void put(K key, V value);

  protected abstract boolean remove(K key);

  protected abstract String getName();

  protected abstract List<K> getKeys();
}