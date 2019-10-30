package cern.c2mon.cache.actions.commfault;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.commfault.CommFaultTagCacheObject;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Szymon Halastra
 */
@Slf4j
@Service
public class CommFaultService {

  private C2monCache<CommFaultTag> commFaultTagCacheRef;

  @Inject
  public CommFaultService(final C2monCache<CommFaultTag> commFaultTagCacheRef) {
    this.commFaultTagCacheRef = commFaultTagCacheRef;
  }

  public C2monCache<CommFaultTag> getCache() {
    return commFaultTagCacheRef;
  }

  public void generateFromEquipment(AbstractEquipment abstractEquipment) {
    CommFaultTag commFaultTag = new CommFaultTagCacheObject(abstractEquipment.getCommFaultTagId(), abstractEquipment.getId(),
            abstractEquipment.getName(), abstractEquipment.getAliveTagId(), abstractEquipment.getStateTagId());
    commFaultTagCacheRef.put(commFaultTag.getId(), commFaultTag);
  }
}
