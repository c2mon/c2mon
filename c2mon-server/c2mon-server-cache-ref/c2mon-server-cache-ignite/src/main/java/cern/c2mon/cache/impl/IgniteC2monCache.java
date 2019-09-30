package cern.c2mon.cache.impl;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.listener.Listener;
import cern.c2mon.cache.api.listener.ListenerService;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.cache.api.spi.CacheQuery;
import cern.c2mon.cache.api.transactions.TransactionalCallable;
import cern.c2mon.shared.common.Cacheable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionDeadlockException;
import org.apache.ignite.transactions.TransactionTimeoutException;

import javax.annotation.PostConstruct;
import javax.cache.CacheException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class IgniteC2monCache<V extends Cacheable> implements C2monCache<V> {

  protected final String cacheName;
  private final Ignite igniteInstance;

  @Getter
  @Setter
  protected CacheLoader<V> cacheLoader;

  protected CacheConfiguration<Long, V> cacheCfg;

  @Getter
  protected IgniteCache<Long, V> cache;

  @Getter
  private Listener<V> listenerService = new ListenerService<>();


  public IgniteC2monCache(String cacheName, CacheConfiguration<Long, V> cacheCfg, Ignite igniteInstance) {
    this.cacheName = cacheName;
    this.cacheCfg = cacheCfg;
    this.igniteInstance = igniteInstance;
  }

  @Override
  @PostConstruct
  public void init() {
    igniteInstance.addCacheConfiguration(cacheCfg);
    cache = igniteInstance.getOrCreateCache(cacheName);

    if (cacheLoader != null) {
      cacheLoader.preload();
    }
  }

  /**
   * Custom implementation to get slightly increased performance and less memory on this query
   *
   * @return
   */
  @Override
  public Set<Long> getKeys() {
    return StreamSupport.stream(Spliterators
      .spliteratorUnknownSize(cache.query(
        // Convert the provided query into Ignite query, keep only the keys to save on memory
        new ScanQuery<>(null), Entry::getKey).iterator(), Spliterator.ORDERED), false)
      .limit(CacheQuery.DEFAULT_MAX_RESULTS).map(i -> (long) i).collect(Collectors.toSet());
  }

  @Override
  public Collection<V> query(CacheQuery<V> providedQuery) {
    return StreamSupport.stream(Spliterators
      .spliteratorUnknownSize(cache.query(
        // Convert the provided query into Ignite query, keep only the values
        new ScanQuery<>(new IgniteC2monPredicateWrapper<>(providedQuery)), Entry::getValue).iterator(), Spliterator.ORDERED), false)
      .limit(providedQuery.maxResults()).collect(Collectors.toList());
  }

  @Override
  public Collection<V> query(Function<V, Boolean> filter) {
    return query(new CacheQuery<>(filter));
  }

  @Override
  public <S> Optional<S> executeTransaction(TransactionalCallable<S> callable) {
    try (Transaction tx = igniteInstance.transactions().txStart()) {

      S returnValue = callable.call();

      tx.commit();

      return Optional.of(returnValue);
    } catch (CacheException e) {
      if (e.getCause() instanceof TransactionTimeoutException &&
        e.getCause().getCause() instanceof TransactionDeadlockException) {
        log.error("DeadLock occurred", e.getCause().getCause().getMessage());
      }
    }

    return Optional.empty();
  }

  @Override
  public V get(Long key) throws IllegalArgumentException {
    if (key != null) {
      return cache.get(key);
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void put(Long key, V value) {
    if (value != null && key != null) {
      cache.put(key, value);
    } else {
      throw new IllegalArgumentException();
    }
    notifyListenersOfUpdate(value);
  }
}
