/**
 * Copyright (c) 2021 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.server.ehcache;

import java.io.Serializable;

/**
 * A Cache Element, consisting of a key, value and attributes.
 */
public class Element implements Serializable, Cloneable {

    private final Object key;

    private final Object value;

    /**
     * @param key
     * @param value
     */
    public Element(final Object key, final Object value) {
       this.key = key;
       this.value = value;
    }

    public Object getKey() {
        return key;
    }

    public Object getObjectValue() {
        return value;
    }

}
