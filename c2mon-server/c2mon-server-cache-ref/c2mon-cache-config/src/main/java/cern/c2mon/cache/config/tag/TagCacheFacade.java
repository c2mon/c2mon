package cern.c2mon.cache.config.tag;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.config.CacheName;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.tag.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import java.util.*;

/**
 * NOT a real cache, but an aggregation of [control,datatag,rule] caches
 * <p>
 * Doesn't support insert operations - use the specific caches instead.
 * <p>
 * Plenty of atomic operations, please use them if possible
 * TODO Document unsupported methods and make them throw aggressively
 *
 * @see TagCacheFacade#put(Long, Tag)
 */
@Component
public class TagCacheFacade implements Cache<Long, Tag> {

  private static final String UNSUPPORTED_OPERATION = "The unified tag cache is not a real cache, but only acts as a facade to the Control tag, Data tag and Rule tag. This operation is not supported";
  private C2monCache<ControlTag> controlTagCache;
  private C2monCache<DataTag> dataTagCache;
  private C2monCache<RuleTag> ruleTagCache;

  @Autowired
  public TagCacheFacade(final C2monCache<RuleTag> ruleTagCache, final C2monCache<ControlTag> controlTagCache, final C2monCache<DataTag> dataTagCache) {
    this.ruleTagCache = ruleTagCache;
    this.dataTagCache = dataTagCache;
    this.controlTagCache = controlTagCache;
  }

  private <T extends Tag> C2monCache<T> getCache(final Long id) {
    if (dataTagCache.containsKey(id)) {
      return (C2monCache<T>) dataTagCache;
    } else if (ruleTagCache.containsKey(id)) {
      return (C2monCache<T>) ruleTagCache;
    } else if (controlTagCache.containsKey(id)) {
      return (C2monCache<T>) controlTagCache;
    } else {
      throw new CacheElementNotFoundException("TagLocationService failed to locate tag with id " + id + " in any of the rule, control or datatag caches.");
    }
  }

  /**
   * Get a list of all tag IDs in the cache.
   * <p>
   * May contain duplicates in current implementation
   *
   * @return list of Tag IDs
   */
  public List<Long> getKeys() {
    List<Long> keys = new ArrayList<>();
    keys.addAll(controlTagCache.getKeys());
    keys.addAll(dataTagCache.getKeys());
    keys.addAll(ruleTagCache.getKeys());
    return keys;
  }

  /**
   * Returns the tag located if it can be located in any of the rule, control
   * or data tag cache (in that order). If it fails to locate a tag with the
   * given id in any of these, it throws an unchecked <java>CacheElementNotFound</java>
   * exception.
   * <p>
   * If unsure if a tag with the given id exists, use preferably the
   * <java>isInTagCache(Long)</java> method
   *
   * @param key the Tag id
   * @return a reference to the Tag object in the cache
   */
  @Override
  public Tag get(Long key) {
    if (ruleTagCache.containsKey(key))
      return ruleTagCache.get(key);
    else if (controlTagCache.containsKey(key))
      return controlTagCache.get(key);
    else if (dataTagCache.containsKey(key))
      return dataTagCache.get(key);
    else throw new CacheElementNotFoundException("The specified element was not found in any cache: " + key);
  }

  @Override
  public Map<Long, Tag> getAll(Set<? extends Long> set) {
    Map<Long, Tag> keys = new HashMap<>();
    keys.putAll(controlTagCache.getAll(set));
    keys.putAll(dataTagCache.getAll(set));
    keys.putAll(ruleTagCache.getAll(set));
    return keys;
  }

  @Override
  public boolean containsKey(Long key) {
    getCache(key);
    return true;
  }

  @Override
  public boolean remove(Long key) {
    return getCache(key).remove(key);
  }

  @Override
  public boolean remove(Long key, Tag tag) {
    return getCache(key).remove(key, tag);
  }

  @Override
  public Tag getAndRemove(Long key) {
    return getCache(key).getAndRemove(key);
  }

  @Override
  public boolean replace(Long key, Tag tag, Tag v1) {
    return getCache(key).replace(key, tag, v1);
  }

  @Override
  public boolean replace(Long key, Tag tag) {
    return getCache(key).replace(key, tag);
  }

  @Override
  public Tag getAndReplace(Long key, Tag tag) {
    return getCache(key).getAndReplace(key, tag);
  }

  @Override
  public void removeAll(Set<? extends Long> set) {
    ruleTagCache.removeAll(set);
    controlTagCache.removeAll(set);
    dataTagCache.removeAll(set);
  }

  @Override
  public void removeAll() {
    ruleTagCache.removeAll();
    controlTagCache.removeAll();
    dataTagCache.removeAll();
  }

  @Override
  public void clear() {
    ruleTagCache.clear();
    controlTagCache.clear();
    dataTagCache.clear();
  }

  @Override
  public String getName() {
    return CacheName.TAG.getLabel();
  }

//   ===== Unsupported operations =====

  /**
   * Replaces the current cache object tag by the passed reference and informs all cache listeners
   * about the change, but only if it can be located in any of the rule, control or data tag cache
   * (in that order). If it fails to locate the tag origin cache with the given id of the tag in
   * any of these, it throws an unchecked <java>CacheElementNotFound</java> exception.
   * <p>
   * If unsure if a tag with the given id exists, use preferably the
   * <java>isInTagCache(Long)</java> method
   *
   * @param tag the Tag object to be put back to the cache
   * @deprecated For inserts, use a reference to the specific cache. For updates, use {@link TagCacheFacade#replace(Long, Tag)}
   */
  @Override
  @Deprecated
  public void put(Long key, Tag tag) {
    throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
  }

  @Override
  public Tag getAndPut(Long key, Tag tag) {
    throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
  }

  @Override
  public void putAll(Map<? extends Long, ? extends Tag> map) {
    throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
  }

  @Override
  public void loadAll(Set<? extends Long> set, boolean b, CompletionListener completionListener) {
    throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
  }

  @Override
  public boolean putIfAbsent(Long key, Tag tag) {
    throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
  }

  @Override
  public <C extends Configuration<Long, Tag>> C getConfiguration(Class<C> aClass) {
    throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
  }

  @Override
  public <T> T invoke(Long key, EntryProcessor<Long, Tag, T> entryProcessor, Object... objects) throws EntryProcessorException {
    throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
  }

  @Override
  public <T> Map<Long, EntryProcessorResult<T>> invokeAll(Set<? extends Long> set, EntryProcessor<Long, Tag, T> entryProcessor, Object... objects) {
    throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
  }

  @Override
  public CacheManager getCacheManager() {
    throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
  }

  @Override
  public void close() {
    throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
  }

  @Override
  public boolean isClosed() {
    return false;
  }

  @Override
  public <T> T unwrap(Class<T> aClass) {
    throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
  }

  @Override
  public void registerCacheEntryListener(CacheEntryListenerConfiguration<Long, Tag> cacheEntryListenerConfiguration) {
    throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
  }

  @Override
  public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<Long, Tag> cacheEntryListenerConfiguration) {
    throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
  }

  @Override
  public Iterator<Entry<Long, Tag>> iterator() {
    throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
  }
}
