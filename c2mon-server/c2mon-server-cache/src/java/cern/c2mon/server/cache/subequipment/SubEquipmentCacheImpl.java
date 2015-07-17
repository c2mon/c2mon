/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.cache.subequipment;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.loader.CacheLoader;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.SubEquipmentFacade;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.shared.common.ConfigurationException;

/**
 * Implementation of the SubEquipment cache.
 * 
 * @author Mark Brightwell
 *
 */
@Service("subEquipmentCache")
@ManagedResource(objectName="cern.c2mon:type=cache,name=subEquipmentCache")
public class SubEquipmentCacheImpl extends AbstractCache<Long, SubEquipment> implements SubEquipmentCache {
  
  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(SubEquipmentCacheImpl.class);
  
  /** Used to post configure the associated control tags */
  private final ControlTagCache controlCache;

  private final SubEquipmentFacade subEquipmentFacade;
  
  @Autowired
  public SubEquipmentCacheImpl(final ClusterCache clusterCache, 
                          @Qualifier("subEquipmentEhcache") final Ehcache ehcache,
                          @Qualifier("subEquipmentEhcacheLoader") final CacheLoader cacheLoader, 
                          @Qualifier("subEquipmentCacheLoader") final C2monCacheLoader c2monCacheLoader,
                          @Qualifier("subEquipmentDAO") final SimpleCacheLoaderDAO<SubEquipment> cacheLoaderDAO,
                          final SubEquipmentFacade subEquipmentFacade,
                          final ControlTagCache controlCache) {

    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO);    
    this.subEquipmentFacade = subEquipmentFacade;
    this.controlCache = controlCache;
  }

  /**
   * Init method called on bean creation. Calls the cache loading procedure.
   */
  @PostConstruct
  public void init() {
    LOGGER.info("Initializing SubEquipment cache...");    
    commonInit();
    LOGGER.info("... SubEquipment cache initialization complete.");
  }

  /**
   * Ensures that the Alive-, Status- and CommFault Tags have appropriately the sub-equipment id set.
   * @param subEquipment the cache object
   */
  @Override
  protected void doPostDbLoading(SubEquipment subEquipment) {
    final Long processId = subEquipmentFacade.getProcessIdForAbstractEquipment(subEquipment.getId());
    
    ControlTag aliveTagCopy = controlCache.getCopy(subEquipment.getAliveTagId());
    if (aliveTagCopy != null) {
      setSubEquipmentId((ControlTagCacheObject) aliveTagCopy, subEquipment.getId(), processId);
    } else {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, 
          String.format("No Alive tag (%s) found for sub-equipment #%d (%s).", subEquipment.getAliveTagId(), subEquipment.getId(), subEquipment.getName()));
    }
    
    
    ControlTag commFaultTagCopy = controlCache.getCopy(subEquipment.getCommFaultTagId());
    if (commFaultTagCopy != null) {
      setSubEquipmentId((ControlTagCacheObject) commFaultTagCopy, subEquipment.getId(), processId);
    } else {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, 
          String.format("No CommFault tag (%s) found for sub-equipment #%d (%s).", subEquipment.getCommFaultTagId(), subEquipment.getId(), subEquipment.getName()));
    }
    
    
    ControlTag statusTagCopy = controlCache.getCopy(subEquipment.getStateTagId());
    if (statusTagCopy != null) {
      setSubEquipmentId((ControlTagCacheObject) statusTagCopy, subEquipment.getId(), processId);
    } else {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, 
          String.format("No Status tag (%s) found for sub-equipment #%d (%s).", subEquipment.getStateTagId(), subEquipment.getId(), subEquipment.getName()));
    }
  }

  @Override
  protected C2monCacheName getCacheName() {   
    return C2monCacheName.SUBEQUIPMENT;
  }
  
  @Override
  protected String getCacheInitializedKey() {
    return cacheInitializedKey;
  }
  
  private void setSubEquipmentId(ControlTagCacheObject copy, Long subEquipmentId, Long processId) {
    String logMsg = String.format("Adding sub-equipment id #%s to control tag #%s", subEquipmentId, copy.getId()); 
    LOGGER.trace(logMsg);
    copy.setSubEquipmentId(subEquipmentId);
    copy.setProcessId(processId);
    controlCache.putQuiet(copy);
  }

}
