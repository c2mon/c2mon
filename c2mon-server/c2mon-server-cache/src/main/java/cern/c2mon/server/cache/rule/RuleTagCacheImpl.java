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

import lombok.extern.slf4j.Slf4j;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.loader.CacheLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.config.CacheProperties;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.tag.AbstractTagCache;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;

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
    HashSet<Long> processIds = new HashSet<>();
    HashSet<Long> equipmentIds = new HashSet<>();
    HashSet<Long> subEquipmentIds = new HashSet<>();
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
      } else if (this.hasKey(tagKey)) {
        RuleTag childRuleTag = this.getCopy(tagKey);

        //if not empty, already processed; if empty, needs processing
        if (childRuleTag.getProcessIds().isEmpty()) {
          setParentSupervisionIds(childRuleTag);
          this.putQuiet(childRuleTag);
        }

        processIds.addAll(childRuleTag.getProcessIds());
        equipmentIds.addAll(childRuleTag.getEquipmentIds());
        subEquipmentIds.addAll(childRuleTag.getSubEquipmentIds());
      } else {
        throw new RuntimeException("Unable to set rule parent process & equipment ids for rule " + ruleTag.getId()
          + ": unable to locate tag " + tagKey + " in either RuleTag or DataTag cache (Control tags not supported in rules)");
      }

    }
    log.debug("setParentSupervisionIds() - Setting parent ids for rule " + ruleTag.getId() + "; process ids: " + processIds + "; equipment ids: " + equipmentIds
        + "; subequipmnet ids: " + subEquipmentIds);
    ruleTag.setProcessIds(processIds);
    ruleTag.setEquipmentIds(equipmentIds);
    ruleTag.setSubEquipmentIds(subEquipmentIds);
  }


}
