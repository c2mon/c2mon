package cern.c2mon.server.cache.commfault;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.CommFaultTagCache;
import cern.c2mon.server.cache.CommFaultTagFacade;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.commfault.CommFaultTagCacheObject;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.shared.daq.config.Change;

@Service
public class CommFaultTagFacadeImpl implements CommFaultTagFacade {

  private CommFaultTagCache commFaultTagCache;
  
  @Autowired
  public CommFaultTagFacadeImpl(CommFaultTagCache commFaultTagCache) {
    super();
    this.commFaultTagCache = commFaultTagCache;
  }

  @Override
  public CommFaultTag createCacheObject(Long id, Properties properties) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Change updateConfig(CommFaultTag commFaultTag, Properties properties) {
    throw new UnsupportedOperationException("CommFaultTags are not currently updated manually from the DB using properties.");
  }

  @Override
  public void generateFromEquipment(AbstractEquipment abstractEquipment) {
    CommFaultTag commFaultTag = new CommFaultTagCacheObject(abstractEquipment.getCommFaultTagId(), abstractEquipment.getId(), 
                                                        abstractEquipment.getName(), abstractEquipment.getAliveTagId(), abstractEquipment.getStateTagId());
    commFaultTagCache.put(commFaultTag.getId(), commFaultTag);
    
  }




}
