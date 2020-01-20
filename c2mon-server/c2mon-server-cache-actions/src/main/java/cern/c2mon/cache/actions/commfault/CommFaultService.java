package cern.c2mon.cache.actions.commfault;

import cern.c2mon.cache.actions.AbstractBooleanControlTagService;
import cern.c2mon.cache.actions.supervision.SupervisedCacheService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.server.common.thread.Event;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;



/**
 * @author Szymon Halastra, Alexandros Papageorgiou
 */
@Slf4j
@Service
public class CommFaultService extends AbstractBooleanControlTagService<CommFaultTag> implements SupervisedCacheService<CommFaultTag> {

  @Inject
  public CommFaultService(final C2monCache<CommFaultTag> commFaultTagCacheRef) {
    super(commFaultTagCacheRef, new CommFaultCacheFlow());
  }

  public boolean isRegisteredCommFaultTag(Long id) {
    return cache.containsKey(id);
  }

  public void updateBasedOnAliveTimer(AliveTag aliveTimer) {
    cache.compute(aliveTimer.getCommFaultTagId(), commFaultTag -> {
      if (aliveTimer.getTimestamp().getTime() >= commFaultTag.getTimestamp().getTime()) {
        commFaultTag.setValue(aliveTimer.getValue());
        commFaultTag.setTimeStampsFrom(aliveTimer);
      }
    });
  }

  public CommFaultTag generateFromEquipment(AbstractEquipment abstractEquipment) {
    return new CommFaultTag(abstractEquipment.getCommFaultTagId(), abstractEquipment.getId(),
      abstractEquipment.getName(), SupervisionEntity.EQUIPMENT.toString(),
      abstractEquipment.getStateTagId(), abstractEquipment.getAliveTagId());
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
