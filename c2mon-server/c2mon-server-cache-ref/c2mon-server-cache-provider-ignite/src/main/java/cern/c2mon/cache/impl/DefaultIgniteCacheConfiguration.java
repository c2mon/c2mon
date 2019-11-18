package cern.c2mon.cache.impl;

import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;

class DefaultIgniteCacheConfiguration<K, V> extends CacheConfiguration<K, V> {

  DefaultIgniteCacheConfiguration(String name) {
    super(name);
    setCacheMode(CacheMode.REPLICATED);
    setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
  }
}
