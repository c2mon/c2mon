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

import cern.c2mon.server.common.tag.Tag;

/**
 * Bean with helper methods for locating tags in all 3 tag caches
 * (data, control and rule tag caches).
 * 
 * Notice that keys for all these cache elements must always be
 * distinct!
 * 
 * TODO only the cache is checked so far, which is fine when all elts are
 * in cache, but should also check the DB store... need to add DAO method for this
 * 
 * @author Mark Brightwell
 *
 */
public interface TagLocationService {

  /**
   * Returns the tag located if it can be located in any of the rule, control
   * or data tag cache (in that order). If it fails to locate a tag with the
   * given id in any of these, it throws an unchecked <java>CacheElementNotFound</java>
   * exception.
   * 
   * If unsure if a tag with the given id exists, use preferably the 
   * <java>isInTagCache(Long)</java> method
   *  
   * @param id the Tag id
   * @return a copy of the Tag object in the cache 
   */
  Tag getCopy(Long id);
  
  /**
   * Returns the tag located if it can be located in any of the rule, control
   * or data tag cache (in that order). If it fails to locate a tag with the
   * given id in any of these, it throws an unchecked <java>CacheElementNotFound</java>
   * exception.
   * 
   * If unsure if a tag with the given id exists, use preferably the 
   * <java>isInTagCache(Long)</java> method
   *  
   * @param id the Tag id
   * @return a reference to the Tag object in the cache 
   */
  Tag get(Long id);
  
  /**
   * A {@link Tag} can also be retrieved with its unique name
   * that has to correspond to {@link Tag#getName()}. Please
   * note that the query is case insensitive.
   * @param name The unique name of a tag
   * @return The corresponding cache object or <code>null</code>, if
   *         the cache does not contain any tag with this name
   * @see #get(Object)
   * @see #findByNameWildcard(String)
   * @see Tag#getName()
   */
  Tag get(String name);
  
  /**
   * Searches for all {@link Tag} instances, where
   * the {@link Tag#getName()} attribute matches the given regular
   * Expression.
   * <p>
   * A regular expression matcher. '?' and '*' may be used.
   * The search is always case insensitive.
   * <p>
   * WARN: Expressions starting with a leading wildcard character are
   * potentially very expensive (ie. full scan) for indexed caches 
   * 
   * @param regex The regular expression including '?' and '*'
   * @return All tags where the tag name is matching the regular expression.
   * Please note, that the result is limited to 100'000 in order to avoid a
   * OutOfMemory exception!
   * @see net.sf.ehcache.search.expression.ILike
   * @see #get(String)
   */
  Collection<Tag> findByNameWildcard(String regex);

  /**
   * Get all the {@link RuleTag}s that reference the given tag ID.
   *
   * @param id tag ID.
   *
   * @return collection of RuleTag objects.
   */
  Collection<RuleTag> findByRuleInputTagId(Long id);
  
  /**
   * Determines whether one of the tag caches already contains
   * an element with the specified id (looks in rule, control
   * and tag cache in that order).
   * 
   * @param id the id to search for
   * @return true if the id corresponds to some tag
   */
  Boolean isInTagCache(Long id);
  
  /**
   * Determines whether one of the tag caches already contains
   * an element with the specified tag name (looks in rule, control
   * and tag cache in that order).
   * 
   * @param name the tag name to search for
   * @return true if the tag name corresponds to some tag
   */
  Boolean isInTagCache(String name);

  /**
   * Removes the Tag from the cache in which it is found.
   * @param id id of the Tag cache element
   */
  void remove(Long id);
  
  void acquireReadLockOnKey(Long id);
  
  void acquireWriteLockOnKey(Long id);
  
  void releaseReadLockOnKey(Long id);
  
  void releaseWriteLockOnKey(Long id);

  /**
   * Replaces the current cache object tag by the passed reference and informs all cache listeners
   * about the change, but only if it can be located in any of the rule, control or data tag cache
   * (in that order). If it fails to locate the tag origin cache with the given id of the tag in
   * any of these, it throws an unchecked <java>CacheElementNotFound</java> exception.
   * 
   * If unsure if a tag with the given id exists, use preferably the 
   * <java>isInTagCache(Long)</java> method
   *  
   * @param tag the Tag object to be put back to the cache 
   */
  void put(Tag tag);
  
  
  /**
   * Replaces the current cache object tag by the passed reference, without notifying the cache
   * listeners, but only if it can be located in any of the rule, control or data tag cache
   * (in that order). If it fails to locate the tag origin cache with the given id of the tag
   * in any of these, it throws an unchecked <java>CacheElementNotFound</java> exception.
   * 
   * If unsure if a tag with the given id exists, use preferably the 
   * <java>isInTagCache(Long)</java> method
   *  
   * @param tag the Tag object to be put back to the cache 
   */
  void putQuiet(Tag tag);
}
