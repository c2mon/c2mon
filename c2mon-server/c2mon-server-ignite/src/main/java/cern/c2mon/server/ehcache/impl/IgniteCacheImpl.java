package cern.c2mon.server.ehcache.impl;

import cern.c2mon.server.ehcache.CacheException;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.config.IgniteCacheProperties;
import cern.c2mon.server.ehcache.event.RegisteredEventListeners;
import cern.c2mon.server.ehcache.loader.CacheLoader;

import javax.cache.Cache;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.stream.Stream;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheRebalanceMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.PartitionLossPolicy;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.lang.IgniteClosure;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IgniteCacheImpl<T, K> implements Ehcache<T, K> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IgniteCacheImpl.class);

    IgniteCache<T, K> cache;

    private ConcurrentHashMap<T, Lock> locks = new ConcurrentHashMap<>();

    private volatile RegisteredEventListeners registeredEventListeners;
    private volatile List<CacheLoader> registeredCacheLoaders;
    
    private final String cacheName;

    private final IgniteCacheProperties properties;

    public IgniteCacheImpl(String cacheName, IgniteCacheProperties properties){
        this.cacheName = cacheName;
        this.properties = properties;

        this.registeredEventListeners = new RegisteredEventListeners(this);
        this.registeredCacheLoaders = new ArrayList<>();


        CacheConfiguration cacheCfg = getCacheConfiguration();

        IgniteConfiguration igniteConfig = getIgniteConfiguration();
        igniteConfig.setCacheConfiguration(cacheCfg);

        Ignite ignite = Ignition.getOrStart(igniteConfig);
        cache = ignite.getOrCreateCache(cacheCfg);
    }
/*
    public IgniteCacheImpl(String cacheName){
        this.registeredEventListeners = new RegisteredEventListeners(this);
        this.registeredCacheLoaders = new ArrayList<>();
        
        this.cacheName = cacheName;

        //ADD TO CONFIG CLASS
        CacheConfiguration cacheCfg = new CacheConfiguration();
        cacheCfg.setName(cacheName);
        cacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);

        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setCacheConfiguration(cacheCfg);
        cfg.setIgniteInstanceName("c2mon-ignite");

        Ignite ignite = Ignition.getOrStart(cfg);;
        cache = ignite.getOrCreateCache(cacheCfg);
    }*/

    private IgniteConfiguration getIgniteConfiguration(){

        IgniteConfiguration igniteConfig = new IgniteConfiguration();
        igniteConfig.setIgniteInstanceName("c2mon-ignite");

        if(!properties.isEmbedded()){
            igniteConfig.setClientMode(properties.isEmbedded());

            igniteConfig.setPeerClassLoadingEnabled(true);

            TcpDiscoverySpi spi = new TcpDiscoverySpi();
            TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
            ipFinder.setAddresses(properties.getIpFinderAddresses());
            spi.setIpFinder(ipFinder);

            igniteConfig.setDiscoverySpi(spi);

        }

        return igniteConfig;
    }

    private CacheConfiguration getCacheConfiguration(){
        CacheConfiguration cacheCfg = new CacheConfiguration();
        cacheCfg.setName(cacheName);
        cacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);

        /**
         * Configurable properties
         */
        cacheCfg.setOnheapCacheEnabled(properties.isOnHeapCacheEnabled());
        cacheCfg.setCacheMode(CacheMode.valueOf(properties.getCacheMode()));
        cacheCfg.setBackups(properties.getNumberOfBackups());
        cacheCfg.setRebalanceMode(CacheRebalanceMode.valueOf(properties.getCacheRebalanceMode()));
        cacheCfg.setWriteSynchronizationMode(CacheWriteSynchronizationMode.valueOf(properties.getCacheWriteSynchronizationMode()));
        cacheCfg.setPartitionLossPolicy(PartitionLossPolicy.valueOf(properties.getPartitionLossPolicy()));

        return cacheCfg;
    }


    @Override
    public boolean isKeyInCache(T id) {
        if (id == null) {
            return false;
        }
        return cache.containsKey(id);
    }

    @Override
    public K get(T id) throws CacheException {
        if(cache.containsKey(id)){
            return cache.get(id);
        }
        return null;
    }

    @Override
    public List getKeys() {
        List<T> keys = new ArrayList<>();
        cache.query(new ScanQuery<>(null)).forEach(entry -> keys.add((T) entry.getKey()));
        return keys;
    }

    @Override
    public void put(T id, K value) {
        lock(id);
        cache.put(id, value);
        unlock(id);
    }

    @Override
    public void putQuiet(T id, K value) {
        lock(id);
        cache.put(id, value);
        unlock(id);
    }

    public Stream createStream(IgniteBiPredicate<T, K> filter, int maxResults){
        return cache.query(new ScanQuery<>(filter),
        (IgniteClosure<Cache.Entry<T, K>, K>) Cache.Entry::getValue).getAll().stream().limit(maxResults);
    }

    public IgniteCache<T, K> getCache(){
        return this.cache;
    }

    @Override
    public boolean remove(T id) {
        if(locks.containsKey(id)){
           return cache.remove(id);
        }
        return false;
    }

    @Override
    public String getName() {
        return cacheName;
    }

    private void lock(T id){
        if(locks.containsKey(id)){
            LOGGER.warn("Lock with id {} already acquired", id);
        }else{
            Lock lock = cache.lock(id);
            lock.lock();
            locks.put(id, lock);
        }
    }

    private void unlock(T id){
        if(locks.containsKey(id)){
            Lock lock = locks.get(id);
            lock.unlock();
            locks.remove(id);
        }else{
            LOGGER.warn("Lock with id {} already acquired", id);
        }
    }

    @Override
    public void acquireReadLockOnKey(T id) {

    }

    @Override
    public void releaseReadLockOnKey(T id) {

    }

    @Override
    public void acquireWriteLockOnKey(T id) {

    }

    @Override
    public void releaseWriteLockOnKey(T id) {

    }

    @Override
    public boolean isWriteLockedByCurrentThread(T id) {
       /* if(!locks.containsKey(id)) {
            return false;
        }*/

        return cache.isLocalLocked(id, false);
    }

    @Override
    public boolean tryReadLockOnKey(T id, Long timeout) throws InterruptedException {
        return true;
    }

    @Override
    public boolean tryWriteLockOnKey(T id, Long timeout) throws InterruptedException {
        return !locks.containsKey(id);
    }

    @Override
    public boolean isReadLockedByCurrentThread(T id) {
        return false;
    }

    @Override
    public void registerCacheLoader(CacheLoader cacheLoader) {
        registeredCacheLoaders.add(cacheLoader);
    }

    @Override
    public RegisteredEventListeners getCacheEventNotificationService() {
        return registeredEventListeners;
    }

    @Override
    public void removeAll() throws IllegalStateException, CacheException {
        cache.clear();
    }

    @Override
    public void setNodeBulkLoadEnabled(boolean b) {

    }
}
