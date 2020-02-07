package cern.c2mon.server.rule;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.rule.RuleTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashSet;

import static cern.c2mon.shared.common.CacheEvent.*;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou Koufidis
 */
@Slf4j
@Service
public class RuleTagService extends AbstractCacheServiceImpl<RuleTag> {

  private final RuleEvaluator ruleEvaluator;
  private C2monCache<Alarm> alarmCache;

  @Inject
  public RuleTagService(final C2monCache<RuleTag> ruleCacheRef, final RuleEvaluator ruleEvaluator, C2monCache<Alarm> alarmCache) {
    super(ruleCacheRef, new DefaultCacheFlow<>());
    this.ruleEvaluator = ruleEvaluator;
    this.alarmCache = alarmCache;
  }

  @PostConstruct
  public void init() {
    getCache().getCacheListenerManager().registerListener(ruleTag -> {
      alarmCache.removeAll(new HashSet<>(ruleTag.getAlarmIds()));
      // TODO (Alex) How should we add event listeners to removeAll as well to ensure proper delete cascading?
      cache.removeAll(new HashSet<>(ruleTag.getRuleIds()));
    }, REMOVED);

    getCache().getCacheListenerManager().registerListener(ruleTag -> ruleEvaluator.evaluateRule(ruleTag.getId()),
      INSERTED, UPDATE_ACCEPTED);
  }

  public void setParentSupervisionIds(Long parentRuleId) {
    // TODO (Alex) Fill this in
  }
}
