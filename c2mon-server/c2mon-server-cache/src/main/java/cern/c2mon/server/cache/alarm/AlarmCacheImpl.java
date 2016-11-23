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
package cern.c2mon.server.cache.alarm;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;

import cern.c2mon.server.cache.config.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.loader.CacheLoader;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.AlarmLoaderDAO;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.shared.client.alarm.AlarmQuery;

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

  @Autowired
  public AlarmCacheImpl(@Qualifier("clusterCache") final ClusterCache clusterCache,
                        @Qualifier("alarmEhcache") final Ehcache ehcache,
                        @Qualifier("alarmEhcacheLoader") final CacheLoader cacheLoader,
                        @Qualifier("alarmCacheLoader") final C2monCacheLoader c2monCacheLoader,
                        @Qualifier("alarmLoaderDAO") final AlarmLoaderDAO cacheLoaderDAO,
                        final CacheProperties properties) {
    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO, properties);
  }

  /**
   * Runs on start up.
   */
  @PostConstruct
  public void init() {
    log.info("Initializing Alarm cache...");

    //common initialization (other than preload, which needs synch below)
    commonInit();

    log.info("... Alarm cache initialization complete.");
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
  public Collection<Long> findAlarm(AlarmQuery query) {

    Ehcache ehcache = getCache();
    ArrayList<Long> result = new ArrayList<Long>();

    Query cacheQuery = ehcache.createQuery();

    if (query.getFaultCode() != 0) {
        Attribute<Integer> fc = ehcache.getSearchAttribute("faultCode");
        cacheQuery.addCriteria(fc.eq(query.getFaultCode()));
    }
    if (query.getFaultFamily() != null && !"".equals(query.getFaultFamily())) {
        Attribute<String> ff = ehcache.getSearchAttribute("faultFamily");
        cacheQuery.addCriteria(ff.ilike(query.getFaultFamily()));
    }
    if (query.getFaultMember() != null && !"".equals(query.getFaultMember())) {
        Attribute<String> fm = ehcache.getSearchAttribute("faultMember");
        cacheQuery.addCriteria(fm.ilike(query.getFaultMember()));
    }
    if (query.getPriority() != 0) {
        Attribute<Integer> prio = ehcache.getSearchAttribute("priority");
        cacheQuery.addCriteria(prio.eq(query.getPriority()));
    }
    if (query.getActive() != null) {
      Attribute<Boolean> active = ehcache.getSearchAttribute("isActive");
      cacheQuery.addCriteria(active.eq(query.getActive()));
    }

    for (Result res: cacheQuery.maxResults(query.getMaxResultSize()).includeKeys().execute().all()) {
        result.add((Long) res.getKey());
    }

    return result;
  }

}
