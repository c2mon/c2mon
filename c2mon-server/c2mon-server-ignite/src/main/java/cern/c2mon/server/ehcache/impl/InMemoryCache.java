package cern.c2mon.server.ehcache.impl;

import cern.c2mon.server.ehcache.CacheException;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.event.RegisteredEventListeners;
import cern.c2mon.server.ehcache.loader.CacheLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryCache<T, K> implements Ehcache<T, K> {

    private ConcurrentHashMap<T, K> cache = new ConcurrentHashMap<>();

    private volatile RegisteredEventListeners registeredEventListeners;
    private volatile List<CacheLoader> registeredCacheLoaders;

    private final String cacheName;

    public InMemoryCache(String name){
        this.cacheName = name;
        this.registeredEventListeners = new RegisteredEventListeners(this);
        this.registeredCacheLoaders = new ArrayList<>();
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
        return cache.keySet().stream().collect(Collectors.toList());
    }

    @Override
    public void put(T key, K value) {
        cache.put(key, value);
    }

    @Override
    public void putQuiet(T key, K value) {
        cache.put(key, value);
    }

    @Override
    public boolean remove(T id) {
        if(cache.containsKey(id)){
            cache.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return cacheName;
    }

    public ConcurrentHashMap<T, K> getCache() {
        return cache;
    }

    public Collection<K> getValues(){
        return cache.values();
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

    @Override
    public void acquireReadLockOnKey(T id) {
        //TODO do nothing for now
    }

    @Override
    public void releaseReadLockOnKey(T id) {
        //TODO do nothing for now
    }

    @Override
    public void acquireWriteLockOnKey(T id) {
        //TODO do nothing for now
    }

    @Override
    public void releaseWriteLockOnKey(T id) {
        //TODO do nothing for now
    }

    @Override
    public boolean isWriteLockedByCurrentThread(T id) {
        return false;
    }

    @Override
    public boolean tryReadLockOnKey(T id, Long timeout) throws InterruptedException {
        return true;
    }

    @Override
    public boolean tryWriteLockOnKey(T id, Long timeout) throws InterruptedException {
        return true;
    }

    @Override
    public boolean isReadLockedByCurrentThread(T id) {
        return false;
    }
}
