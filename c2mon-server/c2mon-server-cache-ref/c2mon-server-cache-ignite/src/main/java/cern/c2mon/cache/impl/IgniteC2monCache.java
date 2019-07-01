package cern.c2mon.cache.impl;

import cern.c2mon.cache.api.listener.Listener;
import cern.c2mon.cache.api.listener.ListenerService;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.cache.api.transactions.TransactionalCallable;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.shared.common.Cacheable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteSpringBean;
import org.apache.ignite.cache.query.Query;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.lang.IgniteClosure;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionDeadlockException;
import org.apache.ignite.transactions.TransactionTimeoutException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import java.util.*;

/**
 * @author Szymon Halastra
 */
public class IgniteC2monCache<V extends Cacheable> extends IgniteC2monCacheBase<Long,V> implements C2monCache<V> {

  private Listener<V> listenerService;

  public IgniteC2monCache(String cacheName) {
    this(cacheName, new DefaultIgniteCacheConfiguration<>(cacheName));
  }

  public IgniteC2monCache(String cacheName, CacheConfiguration<Long, V> cacheConfiguration) {
    super(cacheName, cacheConfiguration);
    this.listenerService = new ListenerService<>();
  }

  public <T, R> QueryCursor<R> query(Query<T> query, IgniteClosure<T, R> closure){
    return cache.query(query,closure);
  }

  @Override
  public Listener<V> getListenerService() {
    return listenerService;
  }


  // --- Unused methods ---
}
