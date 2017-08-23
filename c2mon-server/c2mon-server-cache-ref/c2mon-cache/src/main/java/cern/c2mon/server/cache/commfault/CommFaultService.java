package cern.c2mon.server.cache.commfault;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.CoreService;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.commfault.CommFaultTagCacheObject;
import cern.c2mon.server.common.equipment.AbstractEquipment;

/**
 * @author Szymon Halastra
 */
@Slf4j
@Service
public class CommFaultService implements CoreService {

  private final C2monCache<Long, CommFaultTag> commFaultTagCache;

  @Autowired
  public CommFaultService(final C2monCache<Long, CommFaultTag> commFaultTagCache) {
    this.commFaultTagCache = commFaultTagCache;
  }

  @Override
  public C2monCache getCache() {
    return commFaultTagCache;
  }

  public void generateFromEquipment(AbstractEquipment abstractEquipment) {
    CommFaultTag commFaultTag = new CommFaultTagCacheObject(abstractEquipment.getCommFaultTagId(), abstractEquipment.getId(),
            abstractEquipment.getName(), abstractEquipment.getAliveTagId(), abstractEquipment.getStateTagId());
    commFaultTagCache.put(commFaultTag.getId(), commFaultTag);
  }
}
