package cern.c2mon.cache.api.impl;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.listener.Listener;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.cache.api.spi.CacheQuery;
import cern.c2mon.cache.api.transactions.TransactionalCallable;
import cern.c2mon.shared.common.Cacheable;
import lombok.NonNull;

import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Class used only for testing, as a simple implementation of C2monCache
 *
 * @author Szymon Halastra
 */
public class SimpleC2monCache<V extends Cacheable> implements C2monCache<V> {

  private final ConcurrentMap<Long, V> cache;

  public SimpleC2monCache(String cacheName) {
    this.cache = new ConcurrentHashMap<>(100);
  }


  @SuppressWarnings("NotRequiredForUnitTesting")
  @Override
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
  public void loadAll(Set<? extends Long> set, boolean b, CompletionListener completionListener) {

  }

  @Override
  public void put(Long key, V value) {
    this.cache.put(key, value);
  }

  @Override
  public V getAndPut(Long aLong, V v) {
    return null;
  }

  @Override
  public boolean remove(Long key) {
    return this.cache.remove(key, this.get(key));
  }

  @Override
  public boolean remove(Long aLong, V v) {
    return false;
  }

  @Override
  public V getAndRemove(Long aLong) {
    return null;
  }

  @Override
  public boolean replace(Long aLong, V v, V v1) {
    return false;
  }

  @Override
  public boolean replace(Long aLong, V v) {
    return false;
  }

  @Override
  public V getAndReplace(Long aLong, V v) {
    return null;
  }

  @Override
  public void removeAll(Set<? extends Long> set) {

  }

  @Override
  public void removeAll() {

  }

  @Override
  public void clear() {

  }

  @Override
  public <C extends Configuration<Long, V>> C getConfiguration(Class<C> aClass) {
    return null;
  }

  @Override
  public CacheLoader<V> getCacheLoader() {
    return null;
  }

  @Override
  public void setCacheLoader(CacheLoader<V> cacheLoader) {

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
  public boolean putIfAbsent(Long aLong, V v) {
    return false;
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
  public <T> T unwrap(Class<T> aClass) {
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

  @Override
  public <S> Optional<S> executeTransaction(TransactionalCallable<S> callable) {
    S returnValue = callable.call();

    if (returnValue == null) {
      return Optional.empty();
    }
    return Optional.of(returnValue);
  }

  @Override
  public void executeTransaction(Runnable callable) {
    callable.run();
  }

  @Override
  public Collection<V> query(@NonNull Function<V, Boolean> filter) {
    return null;
  }

  @Override
  public Collection<V> query(@NonNull CacheQuery<V> providedQuery) {
    return null;
  }

  @Override
  public Listener<V> getListenerService() {
    return null;
  }
}
