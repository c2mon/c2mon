/**
 * Copyright (c) 2021 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.server.ehcache;

import cern.c2mon.server.ehcache.event.RegisteredEventListeners;
import cern.c2mon.server.ehcache.loader.CacheLoader;

import java.util.List;

/**
 * An interface for Ehcache.
 * <p/>
 * Ehcache is the central interface. Caches have {@link Element}s and are managed
 * by the {@link CacheManager}.
 */
public interface Ehcache<T, K> {

    /**
     * @param id
     * @return
     */
    boolean isKeyInCache(T id);

    /**
     * @param id
     * @return
     */
    K get(T id) throws CacheException;

    /**
     * @return
     */
    List<T> getKeys();

    /**
     *
     * @param key
     * @param value
     */
    void put(T key, K value);

    /**
     *
     * @param key
     * @param value
     */
    void putQuiet(T key, K value);

    /**
     * @param id
     * @return
     */
    boolean remove(T id);

    /**
     * @return
     */
    String getName();

    /**
     * @param id
     */
    void acquireReadLockOnKey(T id);

    /**
     * @param id
     */
    void releaseReadLockOnKey(T id);

    /**
     * @param id
     */
    void acquireWriteLockOnKey(T id);

    /**
     * @param id
     */
    void releaseWriteLockOnKey(T id);

    /**
     * @param id
     * @return
     */
    boolean isWriteLockedByCurrentThread(T id);

    /**
     * @param id
     * @param timeout
     * @return
     */
    boolean tryReadLockOnKey(T id, Long timeout) throws InterruptedException;

    /**
     * @param id
     * @param timeout
     * @return
     */
    boolean tryWriteLockOnKey(T id, Long timeout)throws InterruptedException;

    /**
     * @param id
     * @return
     */
    boolean isReadLockedByCurrentThread(T id);

    void registerCacheLoader(CacheLoader cacheLoader);

    RegisteredEventListeners getCacheEventNotificationService();

    void removeAll() throws IllegalStateException, CacheException;

    void setNodeBulkLoadEnabled(boolean b);
}
