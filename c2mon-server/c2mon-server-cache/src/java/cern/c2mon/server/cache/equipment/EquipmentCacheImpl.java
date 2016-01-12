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
package cern.c2mon.server.cache.equipment;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.shared.common.ConfigurationException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.loader.CacheLoader;

/**
 * Implementation of the Equipment cache.
 * 
 * <p>
 * Contains initialization logic.
 * 
 * @author Mark Brightwell
 *
 */
@Service("equipmentCache")
@ManagedResource(objectName = "cern.c2mon:type=cache,name=equipmentCache")
public class EquipmentCacheImpl extends AbstractCache<Long, Equipment>implements EquipmentCache {

  /**
   * Static class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(EquipmentCacheImpl.class);

  /** Used to post configure the associated control tags */
  private final ControlTagCache controlCache;

  @Autowired
  public EquipmentCacheImpl(final ClusterCache clusterCache, 
                            @Qualifier("equipmentEhcache") final Ehcache ehcache,
                            @Qualifier("equipmentEhcacheLoader") final CacheLoader cacheLoader, 
                            @Qualifier("equipmentCacheLoader") final C2monCacheLoader c2monCacheLoader,
                            @Qualifier("equipmentDAO") final SimpleCacheLoaderDAO<Equipment> cacheLoaderDAO, 
                            final ControlTagCache controlCache) {

    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO);
    this.controlCache = controlCache;
  }

  /**
   * Init method called on bean creation.
   */
  @PostConstruct
  public void init() {
    LOGGER.info("Initializing Equipment cache...");
    // common initialization (other than preload, which needs synch below)
    commonInit();
    doPostConfigurationOfEquipmentControlTags();
    LOGGER.info("Equipment cache initialization complete.");
  }

  /**
   * Ensures that the Alive-, Status- and CommFault Tags have the Equipment id
   * set.
   */
  private void doPostConfigurationOfEquipmentControlTags() {
    for (Long key : getKeys()) {
      doPostDbLoading(get(key));
    }
  }

  /**
   * Ensures that the Alive-, Status- and CommFault Tags have appropriately the
   * equipment id set.
   */
  @Override
  protected void doPostDbLoading(Equipment equipment) {
    Long processId = equipment.getProcessId();
    Long equipmentId = equipment.getId();

    Long aliveTagId = equipment.getAliveTagId();
    if (aliveTagId != null) {
      ControlTag aliveTagCopy = controlCache.getCopy(aliveTagId);
      if (aliveTagCopy != null) {
        setEquipmentId((ControlTagCacheObject) aliveTagCopy, equipmentId, processId);
      }
      else {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
            String.format("No Alive tag (%s) found for Equipment %s (#%d).", aliveTagId, equipment.getName(), equipment.getId()));
      }
    } // alive tag is not mandatory for an Equipment

    Long commFaultTagId = equipment.getCommFaultTagId();
    if (commFaultTagId != null) {
      ControlTag commFaultTagCopy = controlCache.getCopy(commFaultTagId);
      if (commFaultTagCopy != null) {
        setEquipmentId((ControlTagCacheObject) commFaultTagCopy, equipmentId, processId);
      }
      else {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
            String.format("No CommFault tag (%s) found for Equipment %s (#%d).", commFaultTagId, equipment.getName(), equipment.getId()));
      }
    }
    else {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
          String.format("No CommFault tag for Equipment %s (#%d) defined.", equipment.getName(), equipment.getId()));
    }

    Long statusTag = equipment.getStateTagId();
    if (statusTag != null) {
      ControlTag statusTagCopy = controlCache.getCopy(statusTag);
      if (statusTagCopy != null) {
        setEquipmentId((ControlTagCacheObject) statusTagCopy, equipmentId, processId);
      }
      else {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
            String.format("No Status tag (%s) found for Equipment %s (#%d).", statusTag, equipment.getName(), equipment.getId()));
      }
    }
    else {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
          String.format("No Status tag for Equipment %s (#%d) defined.", equipment.getName(), equipment.getId()));
    }
  }

  @Override
  protected C2monCacheName getCacheName() {
    return C2monCacheName.EQUIPMENT;
  }

  @Override
  protected String getCacheInitializedKey() {
    return cacheInitializedKey;
  }

  private void setEquipmentId(ControlTagCacheObject copy, Long equipmentId, Long processId) {
    String logMsg = String.format("Adding equipment id #%s to control tag #%s", equipmentId, copy.getId());
    LOGGER.trace(logMsg);
    copy.setEquipmentId(equipmentId);
    copy.setProcessId(processId);
    controlCache.putQuiet(copy);
  }

}
