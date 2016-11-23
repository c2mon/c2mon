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
package cern.c2mon.server.cache.cluster;

import java.io.Serializable;

import javax.annotation.PostConstruct;

import cern.c2mon.server.cache.config.CacheProperties;
import net.sf.ehcache.Ehcache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.common.DefaultCacheImpl;

@Service("clusterCache")
@ManagedResource(objectName="cern.c2mon:type=cache,name=clusterCache")
public class ClusterCacheImpl extends DefaultCacheImpl<String, Serializable> implements ClusterCache {

  private final String clusterInitializedKey = "c2mon.cache.cluster.initialized";

  @Autowired
  public ClusterCacheImpl(@Qualifier("clusterEhcache") final Ehcache ehcache, final CacheProperties properties) {
    super(ehcache, properties);
  }

  /**
   * Initializes C2MON server core distributed parameters.
   */
  @PostConstruct
  public void init() {
    //lock cluster
    cache.acquireWriteLockOnKey(clusterInitializedKey);
    try {
      // empty this cache in single server mode!
      if (!properties.isSkipPreloading() && properties.getMode().equalsIgnoreCase("single")) {
        cache.removeAll();
      }

      if (cache.get(clusterInitializedKey) == null) {
        this.put(clusterInitializedKey, Boolean.TRUE);
      }
    } finally {
      cache.releaseWriteLockOnKey(clusterInitializedKey);
    }
  }
}
