package cern.c2mon.cache.actions.commfault;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.actions.state.SupervisionStateTagService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.server.common.thread.Event;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import static cern.c2mon.cache.actions.commfault.CommFaultTagEvaluator.aliveTagCanUpdateCommFault;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou
 */
@Slf4j
@Service
public class CommFaultService extends AbstractCacheServiceImpl<CommFaultTag> {

  private final SupervisionStateTagService stateTagService;

  @Inject
  public CommFaultService(final C2monCache<CommFaultTag> commFaultTagCacheRef, SupervisionStateTagService stateTagService) {
    super(commFaultTagCacheRef, new CommFaultCacheFlow());
    this.stateTagService = stateTagService;
  }

  @PostConstruct
  public void init() {
    cache.getCacheListenerManager().registerListener(this::cascadeUpdate, CacheEvent.UPDATE_ACCEPTED);
  }

  private void cascadeUpdate(@NonNull CommFaultTag commFaultTag) {
    if (commFaultTag.getStateTagId() != null)
      stateTagService.updateBasedOnControl(commFaultTag.getStateTagId(), commFaultTag);
  }

  public boolean isRegisteredCommFaultTag(Long id) {
    return cache.containsKey(id);
  }

  public void updateBasedOnAliveTimer(AliveTag aliveTimer) {
    if (!aliveTagCanUpdateCommFault(aliveTimer))
      return;

    cache.compute(aliveTimer.getCommFaultTagId(), commFaultTag -> {
      if (aliveTimer.getTimestamp().after(commFaultTag.getTimestamp())) {
        commFaultTag.setValue(aliveTimer.getValue());
        // TODO (Alex) SetServerTimestamp
      }
    });
  }

  public CommFaultTag generateFromEquipment(AbstractEquipment abstractEquipment) {
    return new CommFaultTag(abstractEquipment.getCommFaultTagId(), abstractEquipment.getId(),
      abstractEquipment.getName(), SupervisionEntity.EQUIPMENT.toString(),
      abstractEquipment.getAliveTagId(), abstractEquipment.getStateTagId());
    // TODO This used to also put, so remember to do that when calling!
  }

  /**
   * Updates the tag object if the value is not filtered out. Contains the logic on when a
   * CommFaultCacheObject should be updated with new values and when not (in particular
   * timestamp restrictions).
   *
   * <p>Also notifies the listeners if an update was performed.
   *
   * <p>Notice the tag is not put back in the cache here.
   *
   * @param sourceDataTagValue the source value received from the DAQ
   * @return true if an update was performed (i.e. the value was not filtered out)
   */
  public Event<Boolean> updateFromSource(final SourceDataTagValue sourceDataTagValue) {
    // TODO (Alex) Implement this based on the contents of sourceDataTagValue used
    return new Event<>(System.currentTimeMillis(), false);
  }
}
