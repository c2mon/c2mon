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
package cern.c2mon.server.cache.rule;

import java.util.HashSet;

import javax.annotation.PostConstruct;

import cern.c2mon.server.cache.config.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.loader.CacheLoader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.cache.tag.AbstractTagCache;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
import java.util.ArrayList;
import java.util.Collection;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Result;
import net.sf.ehcache.search.Results;

/**
 * Implementation of the Rule cache.
 *
 * @author Mark Brightwell
 *
 */
@Slf4j
@Service("ruleTagCache")
@ManagedResource(objectName="cern.c2mon:type=cache,name=ruleTagCache")
public class RuleTagCacheImpl extends AbstractTagCache<RuleTag> implements RuleTagCache {

  /**
   * DataTagCache for rule parent id loading.
   */
  private final DataTagCache dataTagCache;

  @Autowired
  public RuleTagCacheImpl(@Qualifier("clusterCache") final ClusterCache clusterCache,
                          @Qualifier("ruleTagEhcache") final Ehcache ehcache,
                          @Qualifier("ruleTagEhcacheLoader") final CacheLoader cacheLoader,
                          @Qualifier("ruleTagCacheLoader") final C2monCacheLoader c2monCacheLoader,
                          @Qualifier("ruleTagLoaderDAO") final SimpleCacheLoaderDAO<RuleTag> cacheLoaderDAO,
                          @Qualifier("dataTagCache") final DataTagCache dataTagCache,
                          final CacheProperties properties) {
    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO, properties);
    this.dataTagCache = dataTagCache;
  }

  @PostConstruct
  public void init() {
    log.debug("Initializing RuleTag cache...");
    commonInit();
    log.info("RuleTag cache initialization complete");
  }

  @Override
  protected void doPostDbLoading(RuleTag ruleTag) {
    log.trace("doPostDbLoading() - Post processing RuleTag " + ruleTag.getId() + " ...");
    setParentSupervisionIds(ruleTag);
    log.trace("doPostDbLoading() - ... RuleTag " + ruleTag.getId() + " done!");
  }

  @Override
  protected C2monCacheName getCacheName() {
    return C2monCacheName.RULETAG;
  }

  @Override
  protected String getCacheInitializedKey() {
    return cacheInitializedKey;
  }

  /**
   * Sets the parent process and equipment fields for RuleTags.
   * Please notice that the caller method should first make a write lock
   * on the RuleTag reference.
   *
   * @param ruleTag the RuleTag for which the fields should be set
   */
  @Override
  public void setParentSupervisionIds(final RuleTag ruleTag) {
    log.trace("setParentSupervisionIds() - Setting supervision ids for rule " + ruleTag.getId() + " ...");
    //sets for this ruleTag
    HashSet<Long> processIds = new HashSet<Long>();
    HashSet<Long> equipmentIds = new HashSet<Long>();
    HashSet<Long> subEquipmentIds = new HashSet<Long>();
    int cnt = 0;

    log.trace(ruleTag.getId() + " Has "+ ruleTag.getRuleInputTagIds().size() + " input rule tags");
    for (Long tagKey : ruleTag.getRuleInputTagIds()) {

      cnt++;
      log.trace(ruleTag.getId() + " Trying to find rule input tag No#" + cnt + " with id=" + tagKey + " in caches.. ");
      if (dataTagCache.hasKey(tagKey)) {
        DataTag dataTag = dataTagCache.getCopy(tagKey);
        processIds.add(dataTag.getProcessId());
        equipmentIds.add(dataTag.getEquipmentId());
        if (dataTag.getSubEquipmentId() != null) {
          subEquipmentIds.add(dataTag.getSubEquipmentId());
        }
      } else {
          this.acquireWriteLockOnKey(tagKey);
          try {
              RuleTag childRuleTag = (RuleTag) this.get(tagKey);
              //if not empty, already processed; if empty, needs processing
              if (childRuleTag.getProcessIds().isEmpty()) {
                setParentSupervisionIds(childRuleTag);
                this.putQuiet(childRuleTag);
              }
              processIds.addAll(childRuleTag.getProcessIds());
              equipmentIds.addAll(childRuleTag.getEquipmentIds());
              subEquipmentIds.addAll(childRuleTag.getSubEquipmentIds());
          } catch(CacheElementNotFoundException cenfe) {
              throw new RuntimeException("Unable to set rule parent process & equipment ids for rule " + ruleTag.getId()
                      + ": unable to locate tag " + tagKey + " in either RuleTag or DataTag cache (Control tags not supported in rules)", cenfe);
          } finally {
            this.releaseWriteLockOnKey(tagKey);
          }
      }

    }
    log.debug("setParentSupervisionIds() - Setting parent ids for rule " + ruleTag.getId() + "; process ids: " + processIds + "; equipment ids: " + equipmentIds
        + "; subequipmnet ids: " + subEquipmentIds);
    ruleTag.setProcessIds(processIds);
    ruleTag.setEquipmentIds(equipmentIds);
    ruleTag.setSubEquipmentIds(subEquipmentIds);
  }
    
  /**
   * Find all RuleTags that reference the given tag ID.
   * 
   * @param tagId
   * 
   * @return A collection of {@link RuleTag}s
   */
    @Override
    public Collection<RuleTag> findByRuleInputTagId(long tagId) {
        Results results = null;
        Collection<RuleTag> resultList = new ArrayList<>();

        try {
            Ehcache ehcache = getCache();

            Attribute<String> tagRule = ehcache.getSearchAttribute("ruleInputTagId");

            Query query = ehcache.createQuery();

            String regex = "*#" + tagId + "#*";
            results = query.includeKeys().addCriteria(tagRule.ilike(regex)).execute();

            log.debug(String.format("findByRuleInputTagId() - Got %d results for regex \"%s\"", results.size(), regex));

            Long key;
            for (Result result : results.all()) {
                key = (Long) result.getKey();
                if (key != null) {
                    resultList.add(get(key));
                } else {
                    log.warn(String.format("findByRuleInputTagId() - Regex \"%s\" returned a null key for cache %s", regex, getCacheName()));
                }
            }
        } finally {
            if (results != null) {
                // Discard the results when done to free up cache resources.
                results.discard();
            }
        }

        return resultList;
    }
}
