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

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.DeviceCache;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.config.CacheProperties;
import cern.c2mon.server.cache.device.query.DeviceQuery;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.loader.CacheLoader;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

/**
 * Implementation of the Device cache.
 *
 * @author Justin Lewis Salmon
 */
@Slf4j
@Service("deviceCache")
@ManagedResource(objectName="cern.c2mon:type=cache,name=deviceCache")
public class DeviceCacheImpl extends AbstractCache<Long, Device> implements DeviceCache {

  private DeviceQuery deviceQuery;

  @Autowired
  public DeviceCacheImpl(@Qualifier("clusterCache") final ClusterCache clusterCache,
                         @Qualifier("deviceEhcache") final Ehcache ehcache,
                         @Qualifier("deviceEhcacheLoader") final CacheLoader cacheLoader,
                         @Qualifier("deviceCacheLoader") final C2monCacheLoader c2monCacheLoader,
                         @Qualifier("deviceDAO") final SimpleCacheLoaderDAO<Device> cacheLoaderDAO,
                         final CacheProperties properties,
                         @Qualifier("deviceQuery") final DeviceQuery deviceQuery) {
    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO, properties);
    this.deviceQuery = deviceQuery;
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
    return deviceQuery.findDevicesByDeviceClassId(deviceClassId);
  }
}
