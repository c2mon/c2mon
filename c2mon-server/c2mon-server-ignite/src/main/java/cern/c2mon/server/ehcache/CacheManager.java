/**
 * Copyright (c) 2021 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.server.ehcache;

import cern.c2mon.server.ehcache.cluster.CacheCluster;

/**
 * A container for {@link Ehcache}s that maintain all aspects of their lifecycle.
 * <p/>
 * CacheManager may be either be a singleton if created with factory methods, or multiple instances may exist, in which case resources
 * required by each must be unique.
 * <p/>
 * A CacheManager holds references to Caches and Ehcaches and manages their creation and lifecycle.
 */
public class CacheManager {

    private static volatile CacheManager singleton;

    // TODO need to implement
    public static CacheManager getInstance() {
        return null;
    }

    /**
     * 
     */
    public void clearAll() {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param terracotta
     * @return
     */
    public CacheCluster getCluster(String terracotta) {
        // TODO Auto-generated method stub
        return null;
    }

    public static CacheManager create() throws CacheException {
        if (singleton != null) {
            return singleton;
        }
        synchronized (CacheManager.class) {
            if (singleton == null) {
                singleton = new CacheManager();
            }
            return singleton;
        }
    }
}
