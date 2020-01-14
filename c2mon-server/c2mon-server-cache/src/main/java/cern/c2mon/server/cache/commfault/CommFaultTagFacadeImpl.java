/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.cache.commfault;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.CommFaultTagCache;
import cern.c2mon.server.cache.CommFaultTagFacade;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
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
    CommFaultTag commFaultTag = new CommFaultTag(abstractEquipment.getCommFaultTagId(), abstractEquipment.getId(),
                                                        abstractEquipment.getName(), abstractEquipment.getAliveTagId(), abstractEquipment.getStateTagId());
    commFaultTagCache.put(commFaultTag.getId(), commFaultTag);
    
  }




}
