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

  public void setParentSupervisionIds(Long ruleTagId) {
    // TODO (Alex) Fill this in
	  /*
	log.trace("setParentSupervisionIds() - Setting supervision ids for rule " + ruleTag.getId() + " ...");
    //sets for this ruleTag
    HashSet<Long> processIds = new HashSet<>();
    HashSet<Long> equipmentIds = new HashSet<>();
    HashSet<Long> subEquipmentIds = new HashSet<>();
    int cnt = 0;

    log.trace(ruleTag.getId() + " Has "+ ruleTag.getRuleInputTagIds().size() + " input rule tags");
    for (Long tagKey : ruleTag.getRuleInputTagIds()) {

      cnt++;
      log.trace(ruleTag.getId() + " Trying to find rule input tag No#" + cnt + " with id=" + tagKey + " in caches.. ");
      if (dataTagCache.hasKey(tagKey)) {
        DataTag dataTag = dataTagCache.getCopy(tagKey);
        processIds.add(dataTag.getProcessId());
        equipmentIds.add(dataTag.getEquipmentId());
        if (dataTag.getSubEquipmentId() != null) {
          subEquipmentIds.add(dataTag.getSubEquipmentId());
        }
      } else if (this.hasKey(tagKey)) {
        RuleTag childRuleTag = this.getCopy(tagKey);

        //if not empty, already processed; if empty, needs processing
        if (childRuleTag.getProcessIds().isEmpty()) {
          setParentSupervisionIds(childRuleTag);
          this.putQuiet(childRuleTag);
        }

        processIds.addAll(childRuleTag.getProcessIds());
        equipmentIds.addAll(childRuleTag.getEquipmentIds());
        subEquipmentIds.addAll(childRuleTag.getSubEquipmentIds());
      } else {
        throw new RuntimeException("Unable to set rule parent process & equipment ids for rule " + ruleTag.getId()
          + ": unable to locate tag " + tagKey + " in either RuleTag or DataTag cache (Control tags not supported in rules)");
      }

    }
    log.debug("setParentSupervisionIds() - Setting parent ids for rule " + ruleTag.getId() + "; process ids: " + processIds + "; equipment ids: " + equipmentIds
        + "; subequipmnet ids: " + subEquipmentIds);
    ruleTag.setProcessIds(processIds);
    ruleTag.setEquipmentIds(equipmentIds);
    ruleTag.setSubEquipmentIds(subEquipmentIds);
	   */
  }
}
