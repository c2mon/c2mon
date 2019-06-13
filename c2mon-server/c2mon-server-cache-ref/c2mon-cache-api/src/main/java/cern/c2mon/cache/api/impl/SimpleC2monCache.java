package cern.c2mon.cache.api.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.transactions.TransactionalCallable;
import cern.c2mon.shared.common.Cacheable;

/**
 * Class used only for testing, as a simple implementation of C2monCache
 *
 * @author Szymon Halastra
 */
public class SimpleC2monCache<V extends Cacheable> extends C2monCache<Long, V> {

  private final ConcurrentMap<Long, V> cache;

  public SimpleC2monCache(String cacheName) {
    super(cacheName);
    this.cache = new ConcurrentHashMap<>(100);
  }


  @Override
  @SuppressWarnings("NotRequiredForUnitTesting")
  public void init() {

  }

  @Override
  public V get(Long key) {
    return this.cache.get(key);
  }

  @Override
  public boolean containsKey(Long key) {
    return this.cache.containsKey(key);
  }

  @Override
  public void put(Long key, V value) {
    this.cache.put(key, value);
  }

  @Override
  public boolean remove(Long key) {
    return this.cache.remove(key, this.get(key));
  }

  @Override
  public Set<Long> getKeys() {
    return this.cache.keySet();
  }

  @Override
  public void putAll(Map<? extends Long, ? extends V> map) {
    this.cache.putAll(map);
  }

  @Override
  public Map<Long, V> getAll(Set<? extends Long> keys) {
    Map<Long, V> map = new HashMap<>();

    keys.forEach((key) -> {
      if (cache.containsKey(key)) {
        map.put(key, cache.get(key));
      }
    });
    return map;
  }

  @Override
  public <T> T invoke(Long var1, EntryProcessor<Long, V, T> var2, Object... var3) throws EntryProcessorException {
    return null;
  }

  @Override
  public <T> Map<Long, EntryProcessorResult<T>> invokeAll(Set<? extends Long> var1, EntryProcessor<Long, V, T> var2, Object... var3) {
    return null;
  }

  @Override
  public <S> Optional<S> executeTransaction(TransactionalCallable<S> callable) {
    S returnValue = callable.call();

    if(returnValue == null) {
      return Optional.empty();
    }
    return Optional.of(returnValue);
  }
}
