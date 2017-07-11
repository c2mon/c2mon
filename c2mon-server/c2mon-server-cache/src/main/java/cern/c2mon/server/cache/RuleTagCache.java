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
package cern.c2mon.server.cache;

import cern.c2mon.server.common.rule.RuleTag;
import java.util.Collection;

/**
 * The module public interface that should be used to access the RuleTag's
 * in the server cache. 
 * 
 * <p>It provides methods for retrieving references to the objects in the
 * cache, which may be accessed by other threads concurrently. To guarantee 
 * exclusive access the thread must synchronize on the RuleTag object in
 * the cache.
 * 
 * <p>The getCopy method is available for all Tag caches for retrieving a copy
 * of the cache object.
 * 
 * @author Mark Brightwell
 *
 */
public interface RuleTagCache extends C2monCacheWithSupervision<Long, RuleTag> {
  
  String cacheInitializedKey = "c2mon.cache.rule.initialized";

  /**
   * Sets the parent process and equipment fields for RuleTags.
   * Please notice that the caller method should first make a write lock 
   * on the RuleTag reference. The caller is also responsible for putting 
   * the change back to the cache.
   * 
   * @param ruleTag the RuleTag for which the fields should be set
   */
  void setParentSupervisionIds(RuleTag ruleTag);

  /**
   * Find all {@link RuleTag}s that reference the given tag ID.
   *
   * @param tagId
   *
   * @return a collection of {@link RuleTag}s
   */
  Collection<RuleTag> findByRuleInputTagId(long tagId);
}
