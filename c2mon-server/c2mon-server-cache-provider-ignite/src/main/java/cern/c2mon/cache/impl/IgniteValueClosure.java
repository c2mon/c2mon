package cern.c2mon.cache.impl;

import cern.c2mon.shared.common.Cacheable;
import org.apache.ignite.lang.IgniteClosure;

import javax.cache.Cache;

/**
 * Custom ignite closure to get the value from an entry
 * @param <V>
 */
public class IgniteValueClosure<V extends Cacheable> implements IgniteClosure<Cache.Entry<Long, V>, V> {
    @Override
    public V apply(Cache.Entry<Long, V> e) {
        return e.getValue();
    }
}
