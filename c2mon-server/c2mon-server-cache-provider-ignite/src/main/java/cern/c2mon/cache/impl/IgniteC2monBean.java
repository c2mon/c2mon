package cern.c2mon.cache.impl;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import javax.cache.CacheException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.ignite.DataRegionMetrics;
import org.apache.ignite.DataStorageMetrics;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicLong;
import org.apache.ignite.IgniteAtomicReference;
import org.apache.ignite.IgniteAtomicSequence;
import org.apache.ignite.IgniteAtomicStamped;
import org.apache.ignite.IgniteBinary;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.IgniteCountDownLatch;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.IgniteEvents;
import org.apache.ignite.IgniteException;
import org.apache.ignite.IgniteFileSystem;
import org.apache.ignite.IgniteLock;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.IgniteMessaging;
import org.apache.ignite.IgniteQueue;
import org.apache.ignite.IgniteScheduler;
import org.apache.ignite.IgniteSemaphore;
import org.apache.ignite.IgniteServices;
import org.apache.ignite.IgniteSet;
import org.apache.ignite.IgniteTransactions;
import org.apache.ignite.Ignition;
import org.apache.ignite.MemoryMetrics;
import org.apache.ignite.PersistenceMetrics;
import org.apache.ignite.cache.affinity.Affinity;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.configuration.AtomicConfiguration;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.CollectionConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.NearCacheConfiguration;
import org.apache.ignite.lang.IgniteProductVersion;
import org.apache.ignite.plugin.IgnitePlugin;
import org.apache.ignite.plugin.PluginNotFoundException;
import org.springframework.beans.factory.DisposableBean;

import lombok.extern.slf4j.Slf4j;

/**
 * Made as a copy of {@link org.apache.ignite.IgniteSpringBean}, to work around the limitation it imposes:
 * Having to wait until the {@link org.springframework.context.event.ContextRefreshedEvent} for the bean to be ready
 *
 * This class should not be initialized twice, unless it's been destroyed properly!
 * See {@code IgniteTest} for some of the expected behaviors of the {@code Ignite} class
 * For example, it can't be started twice!
 *
 * @see <a href=https://ignite.apache.org/releases/latest/javadoc/org/apache/ignite/IgniteSpringBean.html>IgniteSpringBean doc</a>
 *
 * @author Alexandros Papageorgiou Koufidis
 */
@Slf4j
@Named
@Singleton
public class IgniteC2monBean implements Ignite, DisposableBean {

  // Delegate is lombok experimental - https://projectlombok.org/features/experimental/Delegate
  // Skips us a lot of ugly code though
  //@Delegate(types = Ignite.class)
  private Ignite igniteInstance;

  public String name() {
    return igniteInstance.name();
}

public IgniteLogger log() {
    return igniteInstance.log();
}

public IgniteConfiguration configuration() {
    return igniteInstance.configuration();
}

public IgniteCluster cluster() {
    return igniteInstance.cluster();
}

public IgniteCompute compute() {
    return igniteInstance.compute();
}

public IgniteCompute compute(ClusterGroup grp) {
    return igniteInstance.compute(grp);
}

public IgniteMessaging message() {
    return igniteInstance.message();
}

public IgniteMessaging message(ClusterGroup grp) {
    return igniteInstance.message(grp);
}

public IgniteEvents events() {
    return igniteInstance.events();
}

public IgniteEvents events(ClusterGroup grp) {
    return igniteInstance.events(grp);
}

public IgniteServices services() {
    return igniteInstance.services();
}

public IgniteServices services(ClusterGroup grp) {
    return igniteInstance.services(grp);
}

public ExecutorService executorService() {
    return igniteInstance.executorService();
}

public ExecutorService executorService(ClusterGroup grp) {
    return igniteInstance.executorService(grp);
}

public IgniteProductVersion version() {
    return igniteInstance.version();
}

public IgniteScheduler scheduler() {
    return igniteInstance.scheduler();
}

public <K, V> IgniteCache<K, V> createCache(CacheConfiguration<K, V> cacheCfg) throws CacheException {
    return igniteInstance.createCache(cacheCfg);
}

public Collection<IgniteCache> createCaches(Collection<CacheConfiguration> cacheCfgs) throws CacheException {
    return igniteInstance.createCaches(cacheCfgs);
}

public <K, V> IgniteCache<K, V> createCache(String cacheName) throws CacheException {
    return igniteInstance.createCache(cacheName);
}

public <K, V> IgniteCache<K, V> getOrCreateCache(CacheConfiguration<K, V> cacheCfg) throws CacheException {
    return igniteInstance.getOrCreateCache(cacheCfg);
}

public <K, V> IgniteCache<K, V> getOrCreateCache(String cacheName) throws CacheException {
    return igniteInstance.getOrCreateCache(cacheName);
}

public Collection<IgniteCache> getOrCreateCaches(Collection<CacheConfiguration> cacheCfgs) throws CacheException {
    return igniteInstance.getOrCreateCaches(cacheCfgs);
}

public <K, V> void addCacheConfiguration(CacheConfiguration<K, V> cacheCfg) throws CacheException {
    igniteInstance.addCacheConfiguration(cacheCfg);
}

public <K, V> IgniteCache<K, V> createCache(CacheConfiguration<K, V> cacheCfg, NearCacheConfiguration<K, V> nearCfg)
        throws CacheException {
    return igniteInstance.createCache(cacheCfg, nearCfg);
}

public <K, V> IgniteCache<K, V> getOrCreateCache(CacheConfiguration<K, V> cacheCfg,
        NearCacheConfiguration<K, V> nearCfg) throws CacheException {
    return igniteInstance.getOrCreateCache(cacheCfg, nearCfg);
}

public <K, V> IgniteCache<K, V> createNearCache(String cacheName, NearCacheConfiguration<K, V> nearCfg)
        throws CacheException {
    return igniteInstance.createNearCache(cacheName, nearCfg);
}

public <K, V> IgniteCache<K, V> getOrCreateNearCache(String cacheName, NearCacheConfiguration<K, V> nearCfg)
        throws CacheException {
    return igniteInstance.getOrCreateNearCache(cacheName, nearCfg);
}

public void destroyCache(String cacheName) throws CacheException {
    igniteInstance.destroyCache(cacheName);
}

public void destroyCaches(Collection<String> cacheNames) throws CacheException {
    igniteInstance.destroyCaches(cacheNames);
}

public <K, V> IgniteCache<K, V> cache(String name) throws CacheException {
    return igniteInstance.cache(name);
}

public Collection<String> cacheNames() {
    return igniteInstance.cacheNames();
}

public IgniteTransactions transactions() {
    return igniteInstance.transactions();
}

public <K, V> IgniteDataStreamer<K, V> dataStreamer(String cacheName) throws IllegalStateException {
    return igniteInstance.dataStreamer(cacheName);
}

public IgniteFileSystem fileSystem(String name) throws IllegalArgumentException {
    return igniteInstance.fileSystem(name);
}

public Collection<IgniteFileSystem> fileSystems() {
    return igniteInstance.fileSystems();
}

public IgniteAtomicSequence atomicSequence(String name, long initVal, boolean create) throws IgniteException {
    return igniteInstance.atomicSequence(name, initVal, create);
}

public IgniteAtomicSequence atomicSequence(String name, AtomicConfiguration cfg, long initVal, boolean create)
        throws IgniteException {
    return igniteInstance.atomicSequence(name, cfg, initVal, create);
}

public IgniteAtomicLong atomicLong(String name, long initVal, boolean create) throws IgniteException {
    return igniteInstance.atomicLong(name, initVal, create);
}

public IgniteAtomicLong atomicLong(String name, AtomicConfiguration cfg, long initVal, boolean create)
        throws IgniteException {
    return igniteInstance.atomicLong(name, cfg, initVal, create);
}

public <T> IgniteAtomicReference<T> atomicReference(String name, T initVal, boolean create) throws IgniteException {
    return igniteInstance.atomicReference(name, initVal, create);
}

public <T> IgniteAtomicReference<T> atomicReference(String name, AtomicConfiguration cfg, T initVal, boolean create)
        throws IgniteException {
    return igniteInstance.atomicReference(name, cfg, initVal, create);
}

public <T, S> IgniteAtomicStamped<T, S> atomicStamped(String name, T initVal, S initStamp, boolean create)
        throws IgniteException {
    return igniteInstance.atomicStamped(name, initVal, initStamp, create);
}

public <T, S> IgniteAtomicStamped<T, S> atomicStamped(String name, AtomicConfiguration cfg, T initVal, S initStamp,
        boolean create) throws IgniteException {
    return igniteInstance.atomicStamped(name, cfg, initVal, initStamp, create);
}

public IgniteCountDownLatch countDownLatch(String name, int cnt, boolean autoDel, boolean create)
        throws IgniteException {
    return igniteInstance.countDownLatch(name, cnt, autoDel, create);
}

public IgniteSemaphore semaphore(String name, int cnt, boolean failoverSafe, boolean create) throws IgniteException {
    return igniteInstance.semaphore(name, cnt, failoverSafe, create);
}

public IgniteLock reentrantLock(String name, boolean failoverSafe, boolean fair, boolean create)
        throws IgniteException {
    return igniteInstance.reentrantLock(name, failoverSafe, fair, create);
}

public <T> IgniteQueue<T> queue(String name, int cap, CollectionConfiguration cfg) throws IgniteException {
    return igniteInstance.queue(name, cap, cfg);
}

public <T> IgniteSet<T> set(String name, CollectionConfiguration cfg) throws IgniteException {
    return igniteInstance.set(name, cfg);
}

public <T extends IgnitePlugin> T plugin(String name) throws PluginNotFoundException {
    return igniteInstance.plugin(name);
}

public IgniteBinary binary() {
    return igniteInstance.binary();
}

public void close() throws IgniteException {
    igniteInstance.close();
}

public <K> Affinity<K> affinity(String cacheName) {
    return igniteInstance.affinity(cacheName);
}

public boolean active() {
    return igniteInstance.active();
}

public void active(boolean active) {
    igniteInstance.active(active);
}

public void resetLostPartitions(Collection<String> cacheNames) {
    igniteInstance.resetLostPartitions(cacheNames);
}

public Collection<MemoryMetrics> memoryMetrics() {
    return igniteInstance.memoryMetrics();
}

public MemoryMetrics memoryMetrics(String memPlcName) {
    return igniteInstance.memoryMetrics(memPlcName);
}

public PersistenceMetrics persistentStoreMetrics() {
    return igniteInstance.persistentStoreMetrics();
}

public Collection<DataRegionMetrics> dataRegionMetrics() {
    return igniteInstance.dataRegionMetrics();
}

public DataRegionMetrics dataRegionMetrics(String memPlcName) {
    return igniteInstance.dataRegionMetrics(memPlcName);
}

public DataStorageMetrics dataStorageMetrics() {
    return igniteInstance.dataStorageMetrics();
}

@Inject
  public IgniteC2monBean(IgniteConfiguration defaultConfiguration) {
    // Not in a try because we want to fail-fast if there's a problem here
    igniteInstance = Ignition.getOrStart(defaultConfiguration);
  }

  @Override
  public void destroy() throws Exception {
    try {
      igniteInstance.close();
    } catch (IgniteException exception) {
      // This can also just log a warning and fail quietly
      log.warn("Failed to property shut down running ignite instance", exception);
    }
  }
}
