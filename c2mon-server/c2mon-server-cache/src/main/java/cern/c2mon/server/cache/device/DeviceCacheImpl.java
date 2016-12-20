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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.loader.CacheLoader;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.DeviceCache;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.config.CacheProperties;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceCacheObject;

/**
 * Implementation of the Device cache.
 *
 * @author Justin Lewis Salmon
 */
@Slf4j
@Service("deviceCache")
@ManagedResource(objectName="cern.c2mon:type=cache,name=deviceCache")
public class DeviceCacheImpl extends AbstractCache<Long, Device> implements DeviceCache {

  @Autowired
  public DeviceCacheImpl(@Qualifier("clusterCache") final ClusterCache clusterCache,
                         @Qualifier("deviceEhcache") final Ehcache ehcache,
                         @Qualifier("deviceEhcacheLoader") final CacheLoader cacheLoader,
                         @Qualifier("deviceCacheLoader") final C2monCacheLoader c2monCacheLoader,
                         @Qualifier("deviceDAO") final SimpleCacheLoaderDAO<Device> cacheLoaderDAO,
                         final CacheProperties properties) {
    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO, properties);
  }

  /**
   * Run on bean initialization. Sets the Ehcache field to the appropriate cache and calls
   * the preload routine if necessary.
   */
  @PostConstruct
  public void init() {
    log.debug("Initializing Device cache...");
    commonInit();
    log.info("Device cache initialization complete");
  }

  @Override
  protected void doPostDbLoading(Device cacheObject) {
  }

  @Override
  protected C2monCacheName getCacheName() {
    return C2monCacheName.DEVICE;
  }

  @Override
  protected String getCacheInitializedKey() {
    return cacheInitializedKey;
  }

  @Override
  public List<Device> getByDeviceClassId(Long deviceClassId) {
    List<Device> deviceCacheObjects = new ArrayList<>();

    Results results = null;

    try {
      Query query = getCache().createQuery();
      Attribute<Long> id = getCache().getSearchAttribute("deviceClassId");
      results = query.includeKeys().includeValues().addCriteria(id.eq(deviceClassId)).execute();

      if (results.size() == 0) {
        throw new CacheElementNotFoundException("Failed to get device ids from cache");
      }

      results.all().forEach((result) -> deviceCacheObjects.add((DeviceCacheObject) result.getValue()));
    } finally {
      if (results != null) {
        results.discard();
      }
    }

    return deviceCacheObjects;
  }
}
