package cern.c2mon.cache.actions.rule;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.flow.DefaultC2monCacheFlow;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou Koufidis
 */
@Slf4j
@Service
public class RuleTagService extends AbstractCacheServiceImpl<RuleTag> {

  @Inject
  public RuleTagService(final C2monCache<RuleTag> ruleCacheRef) {
    super(ruleCacheRef, new DefaultC2monCacheFlow<>());
  }

  /**
   * Adds the rule to the list of those that need evaluating when
   * this tag is updated.
   * <p>
   * Note also adjust text field of cache object.
   */
  public void addDependentRuleToTag(final Tag tag, final Long ruleTagId) {
    cache.compute(tag.getId(), ruleTag -> {
      RuleTagCacheObject cacheRuleTag = (RuleTagCacheObject) ruleTag;
      cacheRuleTag.getRuleIds().add(ruleTagId);
      updateRuleIdsString(cacheRuleTag);
    });
  }

  /**
   * Removes this rule from the list of those that need evaluating when
   * this tag is updated.
   * <p>
   * Note also adjusts text field of cache object.
   *
   * @param tag       the tag used in the rule (directly, not via another rule)
   * @param ruleTagId the id of the rule
   */
  public void removeDependentRuleFromTag(final Tag tag, final Long ruleTagId) {
    // TODO (Alex) I think the ruleIds will be in the datatag, not the ruletag right?
    // TODO So we need the other cache here as well
    cache.compute(tag.getId(), ruleTag -> {
      RuleTagCacheObject cacheRuleTag = (RuleTagCacheObject) ruleTag;
      cacheRuleTag.getRuleIds().remove(ruleTagId);
      updateRuleIdsString(cacheRuleTag);
    });
  }

  private void updateRuleIdsString(RuleTagCacheObject cacheRuleTag){
    StringBuilder bld = new StringBuilder();
    for (Long id : cacheRuleTag.getRuleIds()) {
      bld.append(id).append(",");
    }

    cacheRuleTag.setRuleIdsString(bld.toString());

    if (bld.length() > 0) {
      cacheRuleTag.setRuleIdsString(bld.toString().substring(0, bld.length() - 1)); //remove ", "
    }
  }
}
