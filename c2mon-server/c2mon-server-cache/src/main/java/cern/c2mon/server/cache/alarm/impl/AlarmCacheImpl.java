/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.cache.alarm.impl;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.alarm.query.AlarmQuery;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.config.CacheProperties;
import cern.c2mon.server.cache.loading.AlarmLoaderDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.loader.CacheLoader;
import cern.c2mon.shared.client.alarm.AlarmQueryFilter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

/**
 * Implementation of the TIM Alarm cache.
 *
 * @author Mark Brightwell
 *
 */
@Slf4j
@Service("alarmCache")
@ManagedResource(objectName="cern.c2mon:type=cache,name=alarmCache")
public class AlarmCacheImpl extends AbstractCache<Long, Alarm> implements AlarmCache {

  /** A special logger that can be used later to store alarm updates in a separate log file */
  private static final Logger ALARM_LOGGER = LoggerFactory.getLogger("AlarmLogger");

  private final AlarmQuery alarmQuery;

  @Autowired
  public AlarmCacheImpl(@Qualifier("clusterCache") final ClusterCache clusterCache,
                        @Qualifier("alarmEhcache") final Ehcache ehcache,
                        @Qualifier("alarmEhcacheLoader") final CacheLoader cacheLoader,
                        @Qualifier("alarmCacheLoader") final C2monCacheLoader c2monCacheLoader,
                        @Qualifier("alarmLoaderDAO") final AlarmLoaderDAO cacheLoaderDAO,
                        final CacheProperties properties,
                        @Qualifier("alarmQuery") final AlarmQuery alarmQuery) {
    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO, properties);
    this.alarmQuery = alarmQuery;
  }

  /**
   * Runs on start up.
   */
  @PostConstruct
  public void init() {
    log.debug("Initializing Alarm cache...");

    //common initialization (other than preload, which needs synch below)
    commonInit();

    log.info("Alarm cache initialization complete");
  }

  @Override
  protected void doPostDbLoading(Alarm cacheObject) {
    //do nothing
  }

  @Override
  protected C2monCacheName getCacheName() {
    return C2monCacheName.ALARM;
  }

  @Override
  protected String getCacheInitializedKey() {
    return cacheInitializedKey;
  }

  @Override
  public Collection<Long> findAlarm(AlarmQueryFilter query) {

    return alarmQuery.findAlarm(query);
  }

  @Override
  public void put(Long key, Alarm value) {
    super.put(key, value);
    if (ALARM_LOGGER.isTraceEnabled()) {
      ALARM_LOGGER.trace(value.toString(true));
    } else if (ALARM_LOGGER.isInfoEnabled()) {
      ALARM_LOGGER.info(value.toString());
    }
  }

  @Override
  public void putQuiet(Alarm value) {
    super.putQuiet(value);
    if (ALARM_LOGGER.isTraceEnabled()) {
      ALARM_LOGGER.trace(value.toString(true));
    }
  }



}
