package cern.c2mon.server.cache.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.server.common.tag.Tag;

/**
 * @author Szymon Halastra
 */

@Slf4j
@Service
public class RuleDependencyService {

  private C2monCache<Long, Tag> tagCacheRef;

  @Autowired
  public RuleDependencyService(final C2monCache<Long, Tag> tagCacheRef) {
    this.tagCacheRef = tagCacheRef;
  }

  /**
   * Adds the rule to the list of those that need evaluating when
   * this tag is updated.
   * <p>
   * <p>Note also adjust text field of cache object.
   *
   * @param tagId
   * @param ruleTagId
   */
  public void addDependentRuleToTag(final Tag tag, final Long ruleTagId) {
    AbstractTagCacheObject cacheObject = (AbstractTagCacheObject) tag;
    cacheObject.getRuleIds().add(ruleTagId);
    StringBuilder bld = new StringBuilder();
    for (Long id : cacheObject.getRuleIds()) {
      bld.append(id).append(", ");
    }
    cacheObject.setRuleIdsString(bld.toString().substring(0, bld.length() - 2)); //remove ", "
  }

  /**
   * Removes this rule from the list of those that need evaluating when
   * this tag is updated.
   * <p>
   * <p>Note also adjusts text field of cache object.
   *
   * @param tag       the tag used in the rule (directly, not via another rule)
   * @param ruleTagId the id of the rule
   */
  public void removeDependentRuleFromTag(final Tag tag, final Long ruleTagId) {
    tagCacheRef.lockOnKey(tag.getId());
    try {
      AbstractTagCacheObject cacheObject = (AbstractTagCacheObject) tag;
      cacheObject.getRuleIds().remove(ruleTagId);
      StringBuilder bld = new StringBuilder();
      for (Long id : cacheObject.getRuleIds()) {
        bld.append(id).append(",");
      }

      cacheObject.setRuleIdsString(bld.toString());

      if (bld.length() > 0) {
        cacheObject.setRuleIdsString(bld.toString().substring(0, bld.length() - 1)); //remove ", "
      }
    } finally {
      tagCacheRef.unlockOnKey(tag.getId());
    }
  }
}
