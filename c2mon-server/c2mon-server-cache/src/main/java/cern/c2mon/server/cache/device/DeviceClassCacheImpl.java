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
package cern.c2mon.server.cache.device;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.loader.CacheLoader;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Results;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.DeviceClassCache;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.device.DeviceClass;

/**
 * Implementation of the Device class cache.
 *
 * @author Justin Lewis Salmon
 */
@Service("deviceClassCache")
@ManagedResource(objectName = "cern.c2mon:type=cache,name=deviceClassCache")
public class DeviceClassCacheImpl extends AbstractCache<Long, DeviceClass> implements DeviceClassCache {

  /**
   * Static class logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DeviceCacheImpl.class);

  @Autowired
  public DeviceClassCacheImpl(final ClusterCache clusterCache,
                              @Qualifier("deviceClassEhcache") final Ehcache ehcache,
                              @Qualifier("deviceClassEhcacheLoader") final CacheLoader cacheLoader,
                              @Qualifier("deviceClassCacheLoader") final C2monCacheLoader c2monCacheLoader,
                              @Qualifier("deviceClassDAO") final SimpleCacheLoaderDAO<DeviceClass> cacheLoaderDAO) {
    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO);
  }

  /**
   * Run on bean initialization. Sets the Ehcache field to the appropriate cache
   * and calls the preload routine if necessary.
   */
  @PostConstruct
  public void init() {
    LOG.info("Initializing Device class cache...");
    commonInit();
    LOG.info("Device class cache initialization complete.");
  }

  @Override
  protected void doPostDbLoading(DeviceClass cacheObject) {
  }

  @Override
  protected C2monCacheName getCacheName() {
    return C2monCacheName.DEVICECLASS;
  }

  @Override
  protected String getCacheInitializedKey() {
    return cacheInitializedKey;
  }

  @Override
  public DeviceClass getDeviceClassByName(String deviceClassName) {
    DeviceClass deviceClass = null;

    if (deviceClassName == null || deviceClassName.equalsIgnoreCase("")) {
      throw new IllegalArgumentException("Attempting to retrieve a DeviceClass from the cache with a NULL or empty name parameter.");
    }

    Results results = null;
    try {
      Attribute<String> className = getCache().getSearchAttribute("deviceClassName");
      Query query = getCache().createQuery();
      results = query.includeKeys().includeValues().addCriteria(className.eq(deviceClassName)).maxResults(1).execute();

      if (results.size() == 0) {
        throw new CacheElementNotFoundException("Failed to find a device class with name " + deviceClassName + " in the cache.");
      }

      deviceClass = (DeviceClass) results.all().get(0).getValue();
    } finally {
      if (results != null) {
        results.discard();
      }
    }

    return deviceClass;
  }

  @Override
  public void updateDeviceIds(Long deviceClassId) {
    // Remove/reload the item from the DB. This will cause the device IDs to be
    // updated correctly.
    remove(deviceClassId);
    loadFromDb(deviceClassId);
  }
}
