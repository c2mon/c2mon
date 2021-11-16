/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.cache.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.annotation.PostConstruct;

import cern.c2mon.server.cache.config.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import cern.c2mon.server.ehcache.Ehcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.server.cache.C2monCache;
import cern.c2mon.server.cache.ClusterCache;

/**
 * This is a default implementation of the {@link C2monCache} interface which is based
 * on the {@link BasicCache} implementation. The intention of this class is to provide
 * other repositories the possibility create their own cache instance by simply passing
 * a {@link Ehcache} reference. This is for instance useful, if you have to share a map
 * across the cluster for which you cannot use the {@link ClusterCache}.
 * <p>
 * It is recommended to make the instantiation through Spring to avoid having a direct
 * dependency to Ehcache in the code.
 *
 * @author Matthias Braeger
 *
 * @param <K> key class type
 * @param <V> value class type
 */
@Slf4j
public class DefaultCacheImpl<K, V extends Serializable> extends BasicCache<K, V> implements C2monCache<K, V> {

  protected final CacheProperties properties;

  public DefaultCacheImpl(final Ehcache ehcache, final CacheProperties properties) {
    this.cache = ehcache;
    this.properties = properties;
  }

  @PostConstruct
  protected void initCache() {

    // if in single cache mode, clear the disk cache before starting up
    //(skipCacheLoading can be set to override this and use the disk store instead)
    if (!properties.isSkipPreloading() && properties.getMode().equalsIgnoreCase("single")) {
        cache.removeAll();
    }
  }

  @Override
  public final void putQuiet(final V value) {
    throw new UnsupportedOperationException("Not supported with this cache.");
  }

  @Override
  public V getCopy(final K id) {
    if (id != null) {
      this.acquireReadLockOnKey(id);
      try {
        return deepClone(id, get(id));
      } finally {
        this.releaseReadLockOnKey(id);
      }
    }
    else {
      log.error("getCopy() - Trying to access cache with a NULL key - throwing an exception!");
      //TODO throw runtime exception here or not?
      throw new IllegalArgumentException("Accessing cache with null key!");
    }
  }

  /**
   * Returns a deep copy through serialization
   * @param reference The reference object
   * @return Copy of the reference
   */
  @SuppressWarnings("unchecked")
  private V deepClone(final K id, final V reference) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(reference);

      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bais);
      return (V) ois.readObject();
    } catch (Exception ex) {
      log.error("deepClone() - Caught exception whilst trying to make a serialization copy of object with id " + id, ex);
      throw new RuntimeException("An error occured whilst trying to make a serialization copy of object with id " + id, ex);
    }
  }
}
