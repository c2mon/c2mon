package cern.c2mon.server.cache;

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
   * Determines whether one of the tag caches already contains
   * an element with the specified id (looks in rule, control
   * and tag cache in that order).
   * 
   * @param id the id to search for
   * @return true if the id corresponds to some tag
   */
  Boolean isInTagCache(Long id);
  
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
