package cern.c2mon.cache.actions.commfault;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.exception.TooManyQueryResultsException;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.commfault.CommFaultTagCacheObject;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.shared.common.CacheEvent;
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
      // TODO This should propagate to supervision status
    }, CacheEvent.UPDATE_ACCEPTED);
  }

  public CommFaultTag generateFromEquipment(AbstractEquipment abstractEquipment) {
    return new CommFaultTagCacheObject(abstractEquipment.getCommFaultTagId(), abstractEquipment.getId(),
            abstractEquipment.getName(), abstractEquipment.getAliveTagId(), abstractEquipment.getStateTagId());
    // TODO This used to also put, so remember to do that when calling!
  }

  public void bringDownBasedOnAliveTimer(AliveTimer aliveTimer) {
    final Collection<CommFaultTag> commFaultTags =
      cache.query(tag -> Objects.equals(tag.getAliveTagId(), aliveTimer.getId()));

    if (commFaultTags.isEmpty())
      throw new CacheElementNotFoundException();

    if (commFaultTags.size() > 1)
      throw new TooManyQueryResultsException();

    long commFaultTagId = commFaultTags.stream().findFirst().get().getId();

    // TODO What's going on here? Looks like this logic is flawed? How do you set a CommFaultTag to down?
    cache.compute(commFaultTagId, commFaultTag -> {
      if (aliveTimer.getLastUpdate() >= commFaultTag.getEventTimestamp().getTime()) {
//        ((CommFaultTagCacheObject) commFaultTag).set
      }
    });
  }
}
