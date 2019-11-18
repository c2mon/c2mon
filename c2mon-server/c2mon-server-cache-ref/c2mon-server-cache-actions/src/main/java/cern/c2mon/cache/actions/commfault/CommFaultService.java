package cern.c2mon.cache.actions.commfault;

import cern.c2mon.cache.actions.AbstractCacheService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.flow.DefaultC2monCacheFlow;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.commfault.CommFaultTagCacheObject;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou
 */
@Slf4j
@Service
public class CommFaultService extends AbstractCacheService<CommFaultTag> {

  @Inject
  public CommFaultService(final C2monCache<CommFaultTag> commFaultTagCacheRef) {
    super(commFaultTagCacheRef, new DefaultC2monCacheFlow<>());
  }

  public void generateFromEquipment(AbstractEquipment abstractEquipment) {
    CommFaultTag commFaultTag = new CommFaultTagCacheObject(abstractEquipment.getCommFaultTagId(), abstractEquipment.getId(),
            abstractEquipment.getName(), abstractEquipment.getAliveTagId(), abstractEquipment.getStateTagId());
    cache.put(commFaultTag.getId(), commFaultTag);
  }
}
