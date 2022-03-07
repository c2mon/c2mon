package cern.c2mon.server.ehcache.impl;

import cern.c2mon.server.ehcache.CacheException;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.config.IgniteCacheProperties;
import cern.c2mon.server.ehcache.event.RegisteredEventListeners;
import cern.c2mon.server.ehcache.loader.CacheLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteClientDisconnectedException;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheRebalanceMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.PartitionLossPolicy;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataPageEvictionMode;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.TransactionConfiguration;
import org.apache.ignite.internal.processors.cache.CacheStoppedException;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.spi.metric.jmx.JmxMetricExporterSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IgniteCacheImpl<T, K> implements Ehcache<T, K> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IgniteCacheImpl.class);

    public static final int maxRetryTries = 20;

    IgniteCache<T, K> cache;

    private ConcurrentHashMap<T, Lock> locks = new ConcurrentHashMap<>();

    private volatile RegisteredEventListeners registeredEventListeners;
    private volatile List<CacheLoader> registeredCacheLoaders;

    private Ignite ignite;
    private CacheConfiguration igniteCacheConfig;
    
    private final String cacheName;

    private final IgniteCacheProperties properties;

    public IgniteCacheImpl(String cacheName, IgniteCacheProperties properties, CacheConfiguration cacheCfg){
        this.cacheName = cacheName;
        this.properties = properties;

        this.registeredEventListeners = new RegisteredEventListeners(this);
        this.registeredCacheLoaders = new ArrayList<>();

        cacheCfg.setName(cacheName);
        cacheCfg.setAtomicityMode(CacheAtomicityMode.valueOf(properties.getAtomicityMode()));

        /**
         * Configurable properties
         */
        cacheCfg.setOnheapCacheEnabled(properties.isOnHeapCacheEnabled());
        cacheCfg.setCacheMode(CacheMode.valueOf(properties.getCacheMode()));
        cacheCfg.setBackups(properties.getNumberOfBackups());
        cacheCfg.setRebalanceMode(CacheRebalanceMode.valueOf(properties.getCacheRebalanceMode()));
        cacheCfg.setWriteSynchronizationMode(CacheWriteSynchronizationMode.valueOf(properties.getCacheWriteSynchronizationMode()));
        cacheCfg.setPartitionLossPolicy(PartitionLossPolicy.valueOf(properties.getPartitionLossPolicy()));
        cacheCfg.setStatisticsEnabled(properties.isStatisticsEnabled());

        igniteCacheConfig = cacheCfg;

        IgniteConfiguration igniteConfig = getIgniteConfiguration();
        igniteConfig.setCacheConfiguration(cacheCfg);

        if(properties.isEnableJmxMetrics()){
            igniteConfig.setMetricExporterSpi(new JmxMetricExporterSpi());
        }

        if(properties.getAtomicityMode().equals("TRANSACTIONAL")) {
            // Transactional mode configuration
            TransactionConfiguration txCfg = new TransactionConfiguration();
            txCfg.setTxTimeoutOnPartitionMapExchange(properties.getTxTimeoutOnPartitionMapExchange());
            igniteConfig.setTransactionConfiguration(txCfg);
        }

        ignite = Ignition.getOrStart(igniteConfig);
        cache = ignite.getOrCreateCache(cacheCfg);
    }

    private IgniteConfiguration getIgniteConfiguration(){

        IgniteConfiguration igniteConfig = new IgniteConfiguration();
        igniteConfig.setIgniteInstanceName("c2mon-ignite");

        //Client Mode Configuration
        igniteConfig.setClientMode(properties.isClientMode());

        //Metrics Log Frequency Configuration
        igniteConfig.setMetricsLogFrequency(properties.getMetricsLogFrequency());

        igniteConfig.setPeerClassLoadingEnabled(false);

        //TCP Discovery Configuration
        TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
        TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
        ipFinder.setAddresses(properties.getIpFinderAddresses());
        tcpDiscoverySpi.setIpFinder(ipFinder);
        igniteConfig.setDiscoverySpi(tcpDiscoverySpi);

        //TCP Communications SPI Configuration
        TcpCommunicationSpi tcpCommunicationSpi = new TcpCommunicationSpi();
        tcpCommunicationSpi.setMessageQueueLimit(properties.getMessageQueueLimit());
        tcpCommunicationSpi.setSlowClientQueueLimit(properties.getMessageQueueLimit() - 1);
        igniteConfig.setCommunicationSpi(tcpCommunicationSpi);

        if(!properties.isClientMode()) {

            //Data Storage Configuration
            DataStorageConfiguration storageCfg = new DataStorageConfiguration();

            DataRegionConfiguration defaultRegion = new DataRegionConfiguration();
            defaultRegion.setName(properties.getDefaultRegionName());
            defaultRegion.setInitialSize(properties.getDefaultRegionInitialSize());
            defaultRegion.setMaxSize(properties.getDefaultRegionMaxSize());
            defaultRegion.setPersistenceEnabled(properties.isDefaultRegionPersistenceEnabled());
            defaultRegion.setPageEvictionMode(DataPageEvictionMode.valueOf(properties.getDefaultRegionPageEvictionMode()));
            defaultRegion.setMetricsEnabled(properties.isDefaultRegionMetricsEnabled());

            storageCfg.setDefaultDataRegionConfiguration(defaultRegion);

            igniteConfig.setDataStorageConfiguration(storageCfg);
        }

        return igniteConfig;
    }

    @Override
    public boolean isKeyInCache(T id) {
        if (id == null) {
            return false;
        }

        int tryCount = 0;
        while(true) {
            try {
                return cache.containsKey(id);
            } catch (Exception e) {
                handleCacheException(e);
                if(++tryCount == maxRetryTries) throw e;
            }
        }
    }

    @Override
    public K get(T id) throws CacheException {
        int tryCount = 0;
        while(true) {
            try {
                if (cache.containsKey(id)) {
                    return cache.get(id);
                }else{
                    return null;
                }
            } catch (Exception e) {
                handleCacheException(e);
                if (++tryCount == maxRetryTries) throw e;
            }
        }
    }

    @Override
    public List getKeys() {
        int tryCount = 0;
        while (true) {
            try {
                List<T> keys = new ArrayList<>();
                cache.query(new ScanQuery<>(null)).forEach(entry -> keys.add((T) entry.getKey()));
                return keys;
            } catch(Exception e){
                handleCacheException(e);
                if (++tryCount == maxRetryTries) throw e;
            }
        }
    }

    @Override
    public void put(T id, K value) {
        int tryCount = 0;
        while (true) {
            try {
                lock(id);
                cache.put(id, value);
                unlock(id);
                return;
            } catch(Exception e){
                handleCacheException(e);
                if (++tryCount == maxRetryTries) throw e;
            }
        }
    }

    @Override
    public void putQuiet(T id, K value) {
        int tryCount = 0;
        while (true) {
            try {
                lock(id);
                cache.put(id, value);
                unlock(id);
                return;
            } catch(Exception e){
                handleCacheException(e);
                if (++tryCount == maxRetryTries) throw e;
            }
        }
    }


    private IgniteCache<T, K> getCache(){
        return this.cache;
    }

    @Override
    public boolean remove(T id) {
        int tryCount = 0;
        while (true) {
            try {
                if(cache.containsKey(id)){
                    lock(id);
                    boolean removed = cache.remove(id);
                    unlock(id);
                    return removed;
                }else{
                    return false;
                }
            } catch(Exception e){
                handleCacheException(e);
                if (++tryCount == maxRetryTries) throw e;
            }
        }
    }

    @Override
    public String getName() {
        return cacheName;
    }

    /**
     * In Ignite, locks are supported only for the TRANSACTIONAL atomicity mode
     */
    private void lock(T id){
        if(properties.getAtomicityMode().equals("TRANSACTIONAL")) {
            if (locks.containsKey(id)) {
                LOGGER.warn("Lock with id {} already acquired", id);
            } else {
                Lock lock = cache.lock(id);
                lock.lock();
                locks.put(id, lock);
            }
        }
    }

    /**
     * In Ignite, locks are supported only for the TRANSACTIONAL atomicity mode
     */
    private void unlock(T id){
        if(properties.getAtomicityMode().equals("TRANSACTIONAL")) {
            if (locks.containsKey(id)) {
                Lock lock = locks.get(id);
                lock.unlock();
                locks.remove(id);
            } else {
                LOGGER.warn("Lock with id {} already acquired", id);
            }
        }
    }

    public QueryCursor<List<?>> sqlQueryCache(SqlFieldsQuery query){
        int tryCount = 0;
        while (true) {
            try {
                return cache.query(query);
            } catch (Exception e) {
                handleCacheException(e);
                if (++tryCount == maxRetryTries) throw e;
            }
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
        int tryCount = 0;
        while (true) {
            try {
                cache.removeAll();
                cache.destroy();
                return;
            } catch(Exception e){
                handleCacheException(e);
                if (++tryCount == maxRetryTries) throw e;
            }
        }
    }

    @Override
    public void setNodeBulkLoadEnabled(boolean b) {

    }

    public void handleCacheException(Exception e) {
        if (e.getCause() instanceof IgniteClientDisconnectedException) {
            IgniteClientDisconnectedException ex = (IgniteClientDisconnectedException) e.getCause();

            LOGGER.error("Client lost connection to the cluster. Waiting for reconnect...");

            // Waiting until the client is reconnected.
            ex.reconnectFuture().get();

            LOGGER.warn("Client has been reconnected to the cluster.");

        } else
        if (e.getCause() instanceof CacheStoppedException) {
            LOGGER.error("Failed to perform cache operation (cache is stopped): {}. Recreating the cache and trying again", cacheName);

            cache = ignite.getOrCreateCache(igniteCacheConfig);
        } else {
            LOGGER.error("Something went wrong while performing a cache operation", e);
            //throw RuntimeException ??
        }
    }
}
