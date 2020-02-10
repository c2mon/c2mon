package cern.c2mon.server.rule;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.rule.RuleTag;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import static cern.c2mon.shared.common.CacheEvent.INSERTED;
import static cern.c2mon.shared.common.CacheEvent.UPDATE_ACCEPTED;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou Koufidis
 */
@Slf4j
@Named
public class RuleTagService extends AbstractCacheServiceImpl<RuleTag> {

  private final RuleEvaluator ruleEvaluator;

  @Inject
  public RuleTagService(final C2monCache<RuleTag> ruleCacheRef, final RuleEvaluator ruleEvaluator) {
    super(ruleCacheRef, new DefaultCacheFlow<>());
    this.ruleEvaluator = ruleEvaluator;
  }

  @PostConstruct
  public void init() {
    getCache().getCacheListenerManager().registerListener(ruleTag -> ruleEvaluator.evaluateRule(ruleTag.getId()),
      INSERTED, UPDATE_ACCEPTED);
  }

  public void setParentSupervisionIds(Long parentRuleId) {
    // TODO (Alex) Fill this in
  }
}
