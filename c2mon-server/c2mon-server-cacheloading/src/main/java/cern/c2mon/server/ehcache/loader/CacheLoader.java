/**
 * Copyright (c) 2021 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.server.ehcache.loader;

import cern.c2mon.server.ehcache.CacheException;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.Status;

public interface CacheLoader {

    /**
     * @param arg0
     * @return
     * @throws CloneNotSupportedException
     */
    CacheLoader clone(Ehcache arg0) throws CloneNotSupportedException;

    /**
     * @throws CacheException
     */
    void dispose() throws CacheException;

    /**
     * @return
     */
    String getName();

    /**
     * @return
     */
    Status getStatus();

    /**
     * 
     */
    void init();

}
