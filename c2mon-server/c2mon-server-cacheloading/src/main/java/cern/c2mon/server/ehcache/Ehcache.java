/**
 * Copyright (c) 2021 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.server.ehcache;

import java.util.List;

public interface Ehcache<K> {

    /**
     * @param id
     * @return
     */
    boolean isKeyInCache(K id);

    /**
     * @param id
     * @return
     */
    Element get(K id) throws CacheException;

    /**
     * @return
     */
    List<K> getKeys();

    /**
     * @param element
     */
    void put(Element element);

    /**
     * @param id
     * @return
     */
    boolean remove(K id);

    /**
     * @return
     */
    String getName();

    /**
     * @param id
     */
    void acquireReadLockOnKey(K id);

    /**
     * @param id
     */
    void releaseReadLockOnKey(K id);

    /**
     * @param id
     */
    void acquireWriteLockOnKey(K id);

    /**
     * @param id
     */
    void releaseWriteLockOnKey(K id);

    /**
     * @param id
     * @return
     */
    boolean isWriteLockedByCurrentThread(K id);

    /**
     * @param id
     * @param timeout
     * @return
     */
    boolean tryReadLockOnKey(K id, Long timeout) throws InterruptedException;

    /**
     * @param id
     * @param timeout
     * @return
     */
    boolean tryWriteLockOnKey(K id, Long timeout)throws InterruptedException;

    /**
     * @param id
     * @return
     */
    boolean isReadLockedByCurrentThread(K id);

    /**
     * @param element
     */
    void putQuiet(Element element);

}
