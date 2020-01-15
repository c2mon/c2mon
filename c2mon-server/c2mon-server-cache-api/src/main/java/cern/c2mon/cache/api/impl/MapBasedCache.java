package cern.c2mon.cache.api.impl;

import cern.c2mon.shared.common.Cacheable;
import lombok.Getter;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a very simple cache implementation, used for testing.
 */
public class MapBasedCache<V extends Cacheable> implements Cache<Long, V> {

  @Getter
  private Map<Long, V> map = new ConcurrentHashMap<>(100);

  @Override
  public V get(Long key) {
    return map.get(key);
  }

  @Override
  public Map<Long, V> getAll(Set<? extends Long> keys) {
    Map<Long, V> resultMap = new HashMap<>();

    keys.forEach((key) -> {
      if (map.containsKey(key)) {
        resultMap.put(key, map.get(key));
      }
    });
    return resultMap;
  }

  @Override
  public boolean containsKey(Long key) {
    return map.containsKey(key);
  }

  @Override
  public void loadAll(Set<? extends Long> keys, boolean replaceExistingValues, CompletionListener completionListener) {

  }

  @Override
  @SuppressWarnings("unchecked")
  public void put(Long key, V value) {
    map.put(key, (V) value.clone());
  }

  @Override
  public V getAndPut(Long key, V value) {
    return null;
  }

  @Override
  public void putAll(Map<? extends Long, ? extends V> map) {
    this.map.putAll(map);
  }

  @Override
  public boolean putIfAbsent(Long key, V value) {
    return map.putIfAbsent(key, value) != null;
  }

  @Override
  public boolean remove(Long key) {
    return map.remove(key) != null;
  }

  @Override
  public boolean remove(Long key, V oldValue) {
    return map.remove(key, oldValue);
  }

  @Override
  public V getAndRemove(Long key) {
    if (map.containsKey(key)) {
      V oldValue = map.get(key);
      map.remove(key);
      return oldValue;
    } else {
      return null;
    }
  }

  @Override
  public boolean replace(Long key, V oldValue, V newValue) {
    return false;
  }

  @Override
  public boolean replace(Long key, V value) {
    return false;
  }

  @Override
  public V getAndReplace(Long key, V value) {
    return null;
  }

  @Override
  public void removeAll(Set<? extends Long> keys) {
    keys.forEach(map::remove);
  }

  @Override
  public void removeAll() {
    map.keySet().forEach(map::remove);
  }

  @Override
  public void clear() {
    map.clear();
  }

  @Override
  public <C extends Configuration<Long, V>> C getConfiguration(Class<C> clazz) {
    return null;
  }

  @Override
  public <T> T invoke(Long key, EntryProcessor<Long, V, T> entryProcessor, Object... arguments) throws EntryProcessorException {
    return null;
  }

  @Override
  public <T> Map<Long, EntryProcessorResult<T>> invokeAll(Set<? extends Long> keys, EntryProcessor<Long, V, T> entryProcessor, Object... arguments) {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public CacheManager getCacheManager() {
    return null;
  }

  @Override
  public void close() {

  }

  @Override
  public boolean isClosed() {
    return false;
  }

  @Override
  public <T> T unwrap(Class<T> clazz) {
    return null;
  }

  @Override
  public void registerCacheEntryListener(CacheEntryListenerConfiguration<Long, V> cacheEntryListenerConfiguration) {

  }

  @Override
  public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<Long, V> cacheEntryListenerConfiguration) {

  }

  @Override
  public Iterator<Entry<Long, V>> iterator() {
    return null;
  }
}
