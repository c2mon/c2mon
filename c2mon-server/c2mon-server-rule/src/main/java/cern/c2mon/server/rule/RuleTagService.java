package cern.c2mon.server.rule;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.tag.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;
import java.util.function.Function;

import static cern.c2mon.shared.common.CacheEvent.INSERTED;
import static cern.c2mon.shared.common.CacheEvent.UPDATE_ACCEPTED;
import static java.util.stream.Collectors.toSet;

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

    Set<Tag> inputTags = ruleTag.getRuleInputTagIds()
      .stream()
      .map(inputTagId -> {
        log.trace("For rule {}, trying to find rule input tag {} in caches...", ruleTagId, inputTagId);
        if (dataTagCache.containsKey(inputTagId)) {
          return dataTagCache.get(inputTagId);
        } else if (ruleTagCache.containsKey(inputTagId)) {
          // if not empty, already processed; if empty, needs processing
          if (ruleTagCache.get(inputTagId).getProcessIds().isEmpty()) {
            setParentSupervisionIds(inputTagId);
          }
          return ruleTagCache.get(inputTagId);
        } else {
          throw new CacheElementNotFoundException("Unable to set rule parent process & equipment ids for rule " + ruleTag.getId()
            + ": unable to locate tag " + inputTagId + " in either RuleTag or DataTag cache (ControlTags not supported in rules)");
        }
      })
      .collect(toSet());

    ruleTag.setProcessIds(collectIds(inputTags, Tag::getProcessIds));
    ruleTag.setEquipmentIds(collectIds(inputTags, Tag::getEquipmentIds));
    ruleTag.setSubEquipmentIds(collectIds(inputTags, Tag::getSubEquipmentIds));

    log.debug("setParentSupervisionIds() - Setting parent ids for rule {}; process ids: {}; equipment ids: {}; sub-equipment ids: {}",
      ruleTag.getId(), ruleTag.getProcessIds(), ruleTag.getEquipmentIds(), ruleTag.getSubEquipmentIds());

    ruleTagCache.putQuiet(ruleTagId, ruleTag);
  }

  private static Set<Long> collectIds(Set<Tag> tags, Function<Tag, Set<Long>> idsGetter) {
    return tags.stream().flatMap(t -> idsGetter.apply(t).stream()).collect(toSet());
  }
}
