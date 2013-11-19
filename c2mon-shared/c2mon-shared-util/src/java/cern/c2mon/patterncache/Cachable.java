/**
 * Copyright (c) 2013 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.patterncache;

/**
 * This interface defines methods that need to be implemented by all classes that will be cached inside the pattern
 * cache
 * 
 * @author wbuczak
 */
public interface Cachable {
    void init(String... tokens);
}
