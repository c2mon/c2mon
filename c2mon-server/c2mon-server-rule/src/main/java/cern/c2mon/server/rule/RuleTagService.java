package cern.c2mon.server.rule;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import java.util.HashSet;
import java.util.Set;

import static cern.c2mon.shared.common.CacheEvent.INSERTED;
import static cern.c2mon.shared.common.CacheEvent.UPDATE_ACCEPTED;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou Koufidis
 */
@Slf4j
@Named
public class RuleTagService extends AbstractCacheServiceImpl<RuleTag> {

  private final RuleEvaluator ruleEvaluator;

  @Autowired
  private C2monCache<DataTag> dataTagCache;

  @Autowired
  private C2monCache<RuleTag> ruleTagCache;

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

  public void setParentSupervisionIds(Long ruleTagId) {
	  log.trace("setParentSupervisionIds() - Setting supervision ids for rule {}...", ruleTagId);
    RuleTag ruleTag = ruleTagCache.get(ruleTagId);

    Set<Long> processIds = new HashSet<>();
    Set<Long> equipmentIds = new HashSet<>();
    Set<Long> subEquipmentIds = new HashSet<>();
    int cnt = 0;

    log.trace("Rule {} has {} input rule tags", ruleTag.getId(), ruleTag.getRuleInputTagIds().size());
    for (Long inputTagId : ruleTag.getRuleInputTagIds()) {
      cnt++;
      log.trace("For rule {}, trying to find rule input tag number {} with id {} in caches...", ruleTag.getId(), cnt, inputTagId);
      if (dataTagCache.containsKey(inputTagId)) {
        DataTag dataTag = dataTagCache.get(inputTagId);
        processIds.add(dataTag.getProcessId());
        equipmentIds.add(dataTag.getEquipmentId());
        if (dataTag.getSubEquipmentId() != null) {
          subEquipmentIds.add(dataTag.getSubEquipmentId());
        }
      } else if (ruleTagCache.containsKey(inputTagId)) {
        RuleTag childRuleTag = ruleTagCache.get(inputTagId);

        // if not empty, already processed; if empty, needs processing
        if (childRuleTag.getProcessIds().isEmpty()) {
          setParentSupervisionIds(childRuleTag.getId());
        }

        childRuleTag = ruleTagCache.get(inputTagId);
        processIds.addAll(childRuleTag.getProcessIds());
        equipmentIds.addAll(childRuleTag.getEquipmentIds());
        subEquipmentIds.addAll(childRuleTag.getSubEquipmentIds());
      } else {
        throw new RuntimeException("Unable to set rule parent process & equipment ids for rule " + ruleTag.getId()
          + ": unable to locate tag " + inputTagId + " in either RuleTag or DataTag cache (ControlTags not supported in rules)");
      }
    }

    log.debug("setParentSupervisionIds() - Setting parent ids for rule {}; process ids: {}; equipment ids: {}; sub-equipment ids: {}",
      ruleTag.getId(), processIds, equipmentIds, subEquipmentIds);
    ruleTag.setProcessIds(processIds);
    ruleTag.setEquipmentIds(equipmentIds);
    ruleTag.setSubEquipmentIds(subEquipmentIds);
    ruleTagCache.putQuiet(ruleTagId, ruleTag);
  }
}
