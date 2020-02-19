package cern.c2mon.cache.actions.commfault;

import cern.c2mon.cache.actions.state.SupervisionStateTagEvaluator;
import cern.c2mon.cache.actions.state.SupervisionStateTagService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.shared.common.CacheEvent;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Objects;

/**
 * Cascades the events received in the CommFault cache to the SupervisionState
 * cache. Applies sanitation and validation before sending the event
 * for performance reasons
 */
@Named
@Singleton
@Slf4j
public class CommFaultTagUpdateCascader implements CacheListener<CommFaultTag> {

  private final C2monCache<CommFaultTag> commFaultTagCache;
  private final SupervisionStateTagService stateTagService;

  @Inject
  public CommFaultTagUpdateCascader(C2monCache<CommFaultTag> commFaultTagCache,
                                    SupervisionStateTagService stateTagService) {
    this.commFaultTagCache = commFaultTagCache;
    this.stateTagService = stateTagService;
  }

  @PostConstruct
  public void register() {
    commFaultTagCache.getCacheListenerManager().registerListener(this, CacheEvent.UPDATE_ACCEPTED);
  }

  @Override
  public void apply(CommFaultTag commFaultTag) {
    Objects.requireNonNull(commFaultTag, "CommFault Tag received in cascading should never be null!");

    if (SupervisionStateTagEvaluator.controlTagCanUpdateState(commFaultTag.getStateTagId(), commFaultTag))
      stateTagService.updateBasedOnControl(commFaultTag.getStateTagId(), commFaultTag);
  }
}
