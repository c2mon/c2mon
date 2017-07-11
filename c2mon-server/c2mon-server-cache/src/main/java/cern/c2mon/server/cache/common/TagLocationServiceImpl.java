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

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.C2monCache;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.tag.Tag;

/**
 * Spring bean implementation of the {@link TagLocationService}.
 * @author mbrightw
 *
 */
@Service
public class TagLocationServiceImpl implements TagLocationService {

  
  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(TagLocationServiceImpl.class);
  
  /**
   * Reference to data tag cache.
   */
  private DataTagCache dataTagCache;
  
  /**
   * Reference to control tag cache.
   */
  private ControlTagCache controlTagCache;
  
  /**
   * Reference to rule tag cache.
   */
  private RuleTagCache ruleTagCache;
  
  /**
   * 
   * @param dataTagCache
   * @param controlTagCache
   * @param ruleTagCache
   */
  @Autowired
  public TagLocationServiceImpl(DataTagCache dataTagCache, ControlTagCache controlTagCache, RuleTagCache ruleTagCache) {
    super();
    this.dataTagCache = dataTagCache;
    this.controlTagCache = controlTagCache;
    this.ruleTagCache = ruleTagCache;
  }
  
  @SuppressWarnings("unchecked")
  private <T extends Tag> C2monCache<Long, T> getCache(final Long id) {
    if (dataTagCache.hasKey(id)) {       
      return (C2monCache<Long, T>) dataTagCache;
    } else if (ruleTagCache.hasKey(id)) {
      return (C2monCache<Long, T>) ruleTagCache;
    } else if (controlTagCache.hasKey(id)) {
      return (C2monCache<Long, T>) controlTagCache;
    } else {
      throw new CacheElementNotFoundException("TagLocationService failed to locate tag with id " + id + " in any of the rule, control or datatag caches.");
    }
  }
  
  @Override
  public Tag getCopy(final Long id) {
    return getCache(id).getCopy(id);    
  }
  
  @Override
  public Tag get(final Long id) {
    return getCache(id).get(id);
  }
  
  @Override
  public Tag get(final String tagName) {
    if (dataTagCache.hasTagWithName(tagName)) {       
      return dataTagCache.get(tagName);
    } else if (ruleTagCache.hasTagWithName(tagName)) {
      return ruleTagCache.get(tagName);
    } else if (controlTagCache.hasTagWithName(tagName)) {
      return controlTagCache.get(tagName);
    } else {
      throw new CacheElementNotFoundException("TagLocationService failed to locate tag with name " + tagName + " in any of the rule, control or datatag caches.");
    }
  }
  
  @Override
  public Collection<Tag> findByNameWildcard(String regex) {
    Collection<Tag> resultList = new ArrayList<>();
    
    resultList.addAll(dataTagCache.findByNameWildcard(regex));
    resultList.addAll(ruleTagCache.findByNameWildcard(regex));
    resultList.addAll(controlTagCache.findByNameWildcard(regex));
    
    return resultList;
  }
  
  @Override
  public void put(Tag tag) {
    getCache(tag.getId()).put(tag.getId(), tag);
  }
  
  @Override
  public void putQuiet(Tag tag) {
    getCache(tag.getId()).putQuiet(tag);
  }
  
  @Override
  public Boolean isInTagCache(Long id) {
    return ruleTagCache.hasKey(id) || controlTagCache.hasKey(id) || dataTagCache.hasKey(id); 
  }
  
  @Override
  public Boolean isInTagCache(String name) {
    return ruleTagCache.hasTagWithName(name) || controlTagCache.hasTagWithName(name) || dataTagCache.hasTagWithName(name); 
  }

  @Override
  public void remove(Long id) {
    getCache(id).remove(id);
  }

  @Override
  public void acquireReadLockOnKey(Long id) {
    getCache(id).acquireReadLockOnKey(id);
  }

  @Override
  public void acquireWriteLockOnKey(Long id) {
    getCache(id).acquireWriteLockOnKey(id);
  }

  @Override
  public void releaseReadLockOnKey(Long id) {
    getCache(id).releaseReadLockOnKey(id);
  }

  @Override
  public void releaseWriteLockOnKey(Long id) {
    getCache(id).releaseWriteLockOnKey(id);
  }

  @Override
  public Collection<RuleTag> findByRuleInputTagId(Long id) {
    return this.ruleTagCache.findByRuleInputTagId(id);
  }
}
