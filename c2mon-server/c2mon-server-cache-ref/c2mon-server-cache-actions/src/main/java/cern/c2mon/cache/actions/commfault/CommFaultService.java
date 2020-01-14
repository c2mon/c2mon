package cern.c2mon.cache.actions.commfault;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.exception.TooManyQueryResultsException;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.server.common.thread.Event;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Objects;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou
 */
@Slf4j
@Service
public class CommFaultService extends AbstractCacheServiceImpl<CommFaultTag> {

  @Inject
  public CommFaultService(final C2monCache<CommFaultTag> commFaultTagCacheRef) {
    super(commFaultTagCacheRef, new CommFaultCacheFlow());
  }

  @PostConstruct
  public void init() {
    cache.getCacheListenerManager().registerListener(commFaultTag -> {
      // TODO (Alex) This should propagate to supervision status
    }, CacheEvent.UPDATE_ACCEPTED);
  }

  public boolean isRegisteredCommFaultTag(Long id) {
    return cache.containsKey(id);
  }

  public CommFaultTag generateFromEquipment(AbstractEquipment abstractEquipment) {
    return new CommFaultTag(abstractEquipment.getCommFaultTagId(), abstractEquipment.getId(),
            abstractEquipment.getName(), SupervisionEntity.EQUIPMENT.toString(),
      abstractEquipment.getAliveTagId(), abstractEquipment.getStateTagId());
    // TODO This used to also put, so remember to do that when calling!
  }

  public void bringDownBasedOnAliveTimer(AliveTag aliveTimer) {
    final Collection<CommFaultTag> commFaultTags =
      cache.query(tag -> Objects.equals(tag.getAliveTagId(), aliveTimer.getId()));

    if (commFaultTags.isEmpty())
      throw new CacheElementNotFoundException();

    if (commFaultTags.size() > 1)
      throw new TooManyQueryResultsException();

    long commFaultTagId = commFaultTags.stream().findFirst().get().getId();

    // TODO What's going on here? Looks like this logic is flawed? How do you set a CommFaultTag to down?
    cache.compute(commFaultTagId, commFaultTag -> {
      if (aliveTimer.getLastUpdate() >= commFaultTag.getSourceTimestamp().getTime()) {
//        ((CommFaultTag) commFaultTag).set
      }
    });
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
