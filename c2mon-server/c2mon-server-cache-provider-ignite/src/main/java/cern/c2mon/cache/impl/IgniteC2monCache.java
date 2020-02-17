package cern.c2mon.cache.impl;

import cern.c2mon.cache.api.impl.AbstractCache;
import cern.c2mon.cache.api.spi.CacheQuery;
import cern.c2mon.shared.common.Cacheable;
import lombok.Getter;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionDeadlockException;
import org.apache.ignite.transactions.TransactionTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.cache.CacheException;
import java.util.Collection;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * The Ignite implementation of C2monCache
 *
 * @param <V> the {@link Cacheable} to be stored in the cache. Key is always long
 * @author Alexandros Papageorgiou Koufidis
 */
public class IgniteC2monCache<V extends Cacheable> extends AbstractCache<V> {
  private static final Logger LOG = LoggerFactory.getLogger(IgniteC2monCache.class);
  protected final String cacheName;
  private final Ignite igniteInstance;

  protected CacheConfiguration<Long, V> cacheCfg;

  @Getter
  protected IgniteCache<Long, V> cache;

  /**
   * It is important to remember the cache is not yet initialized, even after the constructor
   * is finished! You need to explicitly call the {@link cern.c2mon.cache.api.C2monCache#init()} method
   * to "activate" the ignite cache and preload with data
   *
   * @param cacheName the name of the cache
   * @param cacheCfg ignite-specific cache configuration
   * @param igniteInstance a working ignite reference
   */
  public IgniteC2monCache(String cacheName, CacheConfiguration<Long, V> cacheCfg, Ignite igniteInstance) {
    // Cache is not yet ready!
    super(cacheName);
    this.cacheName = cacheName;
    this.cacheCfg = cacheCfg;
    this.igniteInstance = igniteInstance;
  }

  @PostConstruct
  @Override
  public void init() {
    igniteInstance.addCacheConfiguration(cacheCfg);
    cache = igniteInstance.getOrCreateCache(cacheName);

    if (cacheLoader != null) {
      cacheLoader.preload();
    }
  }

  /**
   * Custom implementation to get slightly increased performance and less memory on this query
   */
  @Override
  public Set<Long> getKeys() {
    return StreamSupport.stream(Spliterators
      .spliteratorUnknownSize(cache.query(
        // Convert the provided query into Ignite query, keep only the keys to save on memory
        // Null passed to ScanQuery gives us all results back
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
  public <S> S executeTransaction(Supplier<S> callable) {
    try (Transaction tx = (igniteInstance.transactions().tx() != null ?
      // If there is an existing transaction, use it
      igniteInstance.transactions().tx() :
      // Otherwise start a new one
      igniteInstance.transactions().txStart())) {

      S returnValue = callable.get();

      tx.commit();

      return returnValue;
    } catch (CacheException e) {
      if (e.getCause() instanceof TransactionTimeoutException &&
        e.getCause().getCause() instanceof TransactionDeadlockException) {
        LOG.error("DeadLock occurred: {}", e.getCause().getCause().getMessage());
      }
      throw e;
    }
  }
}
