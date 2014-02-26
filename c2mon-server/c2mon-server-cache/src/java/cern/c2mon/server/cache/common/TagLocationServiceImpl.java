package cern.c2mon.server.cache.common;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.cache.C2monCache;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
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
  private static final Logger LOGGER = Logger.getLogger(TagLocationServiceImpl.class);
  
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
   * @param dataTagFacade
   * @param controlTagFacade
   * @param ruleTagFacade
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
}
