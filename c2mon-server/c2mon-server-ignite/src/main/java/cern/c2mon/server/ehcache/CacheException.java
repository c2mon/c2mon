/**
 * Copyright (c) 2021 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.server.ehcache;

public class CacheException extends RuntimeException {

    public CacheException(){

    }

    public CacheException(String message) {
        super(message);
    }

}
