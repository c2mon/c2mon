package cern.c2mon.cache.api;

import cern.c2mon.shared.common.Cacheable;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This interface simply delegates all the {@code Cache} methods to the {@code CacheDelegator#getCache()} method.
 * <p>
 * None of the operations do anything other than delegate. There are no side effects. This class serves so
 * that you can override only the methods you actually need in an implementation.
 * <p>
 * Do NOT add any custom logic here. Add it to implementation of this class. If you would like to override a lot of
 * functionality, make a child class.
 *
 * @param <V> the type of {@link Cacheable} objects the cache contains
 */
public interface CacheDelegator<V extends Cacheable> extends Cache<Long, V> {

  Cache<Long, V> getCache();

  @Override
  default V get(Long key) {
    return getCache().get(key);
  }

  @Override
  default Map<Long, V> getAll(Set<? extends Long> keys) {
    return getCache().getAll(keys);
  }

  @Override
  default boolean containsKey(Long key) {
    return getCache().containsKey(key);
  }

  @Override
  default void loadAll(Set<? extends Long> keys, boolean replaceExistingValues, CompletionListener completionListener) {
    getCache().loadAll(keys, replaceExistingValues, completionListener);
  }

  @Override
  default void put(Long key, V value) {
    getCache().put(key, value);
  }

  @Override
  default V getAndPut(Long key, V value) {
    return getCache().getAndPut(key, value);
  }

  @Override
  default void putAll(Map<? extends Long, ? extends V> map) {
    getCache().putAll(map);
  }

  @Override
  default boolean putIfAbsent(Long key, V value) {
    return getCache().putIfAbsent(key, value);
  }

  @Override
  default boolean remove(Long key) {
    return getCache().remove(key);
  }

  @Override
  default boolean remove(Long key, V oldValue) {
    return getCache().remove(key, oldValue);
  }

  @Override
  default V getAndRemove(Long key) {
    return getCache().getAndRemove(key);
  }

  @Override
  default boolean replace(Long key, V oldValue, V newValue) {
    return getCache().replace(key, oldValue, newValue);
  }

  @Override
  default boolean replace(Long key, V value) {
    return getCache().replace(key, value);
  }

  @Override
  default V getAndReplace(Long key, V value) {
    return getCache().getAndReplace(key, value);
  }

  @Override
  default void removeAll(Set<? extends Long> keys) {
    getCache().removeAll(keys);
  }

  @Override
  default void removeAll() {
    getCache().removeAll();
  }

  @Override
  default void clear() {
    getCache().clear();
  }

  @Override
  default <C extends Configuration<Long, V>> C getConfiguration(Class<C> clazz) {
    return getCache().getConfiguration(clazz);
  }

  @Override
  default <T> T invoke(Long key, EntryProcessor<Long, V, T> entryProcessor, Object... arguments) throws EntryProcessorException {
    return getCache().invoke(key, entryProcessor, arguments);
  }

  @Override
  default <T> Map<Long, EntryProcessorResult<T>> invokeAll(Set<? extends Long> keys, EntryProcessor<Long, V, T> entryProcessor, Object... arguments) {
    return getCache().invokeAll(keys, entryProcessor, arguments);
  }

  @Override
  default String getName() {
    return getCache().getName();
  }

  @Override
  default CacheManager getCacheManager() {
    return getCache().getCacheManager();
  }

  @Override
  default void close() {
    getCache().close();
  }

  @Override
  default boolean isClosed() {
    return getCache().isClosed();
  }

  @Override
  default <T> T unwrap(Class<T> clazz) {
    return getCache().unwrap(clazz);
  }

  @Override
  default void registerCacheEntryListener(CacheEntryListenerConfiguration<Long, V> cacheEntryListenerConfiguration) {
    getCache().registerCacheEntryListener(cacheEntryListenerConfiguration);
  }

  @Override
  default void deregisterCacheEntryListener(CacheEntryListenerConfiguration<Long, V> cacheEntryListenerConfiguration) {
    getCache().deregisterCacheEntryListener(cacheEntryListenerConfiguration);
  }

  @Override
  default Iterator<Entry<Long, V>> iterator() {
    return getCache().iterator();
  }


}
