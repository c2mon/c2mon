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
package cern.c2mon.server.cache.subequipment;

import javax.annotation.PostConstruct;

import cern.c2mon.server.cache.config.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.loader.CacheLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.*;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.shared.common.ConfigurationException;

/**
 * Implementation of the SubEquipment cache.
 *
 * @author Mark Brightwell
 *
 */
@Slf4j
@Service("subEquipmentCache")
@ManagedResource(objectName="cern.c2mon:type=cache,name=subEquipmentCache")
public class SubEquipmentCacheImpl extends AbstractCache<Long, SubEquipment> implements SubEquipmentCache {

  /** Used to post configure the associated control tags */
  private final ControlTagCache controlCache;

  private final EquipmentCache equipmentCache;

  @Autowired
  public SubEquipmentCacheImpl(final ClusterCache clusterCache,
                               @Qualifier("subEquipmentEhcache") final Ehcache ehcache,
                               @Qualifier("subEquipmentEhcacheLoader") final CacheLoader cacheLoader,
                               @Qualifier("subEquipmentCacheLoader") final C2monCacheLoader c2monCacheLoader,
                               @Qualifier("subEquipmentDAO") final SimpleCacheLoaderDAO<SubEquipment> cacheLoaderDAO,
                               final ControlTagCache controlCache,
                               final EquipmentCache equipmentCache,
                               final CacheProperties properties) {

    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO, properties);
    this.controlCache = controlCache;
    this.equipmentCache = equipmentCache;
  }

  /**
   * Init method called on bean creation. Calls the cache loading procedure.
   */
  @PostConstruct
  public void init() {
    log.debug("Initializing SubEquipment cache...");
    commonInit();
    doPostConfigurationOfSubEquipmentControlTags();
    log.info("SubEquipment cache initialization complete");
  }

  /**
   * Ensures that the Alive-, Status- and CommFault Tags have the
   * Equipment id set.
   */
  private void doPostConfigurationOfSubEquipmentControlTags() {
    for (Long key : getKeys()) {
      doPostDbLoading(get(key));
    }
  }

  /**
   * Ensures that the Alive-, Status- and CommFault Tags have appropriately the sub-equipment id set.
   * @param subEquipment the cache object
   */
  @Override
  protected void doPostDbLoading(SubEquipment subEquipment) {
    Equipment parent = equipmentCache.get(subEquipment.getParentId());
    Long processId = parent.getProcessId();
    if (processId == null) {
      throw new NullPointerException(String.format("Equipment %s (%d) has no associated Process id - this should never happen!", parent.getName(), parent.getId()));
    }

    Long aliveTagId = subEquipment.getAliveTagId();
    if (aliveTagId != null) {
      ControlTag aliveTagCopy = controlCache.getCopy(aliveTagId);
      if (aliveTagCopy != null) {
        setSubEquipmentId((ControlTagCacheObject) aliveTagCopy, subEquipment.getId(), processId);
      } else {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
            String.format("No Alive tag (#%d) found for Sub-Equipment %s (#%d).", aliveTagId, subEquipment.getName(), subEquipment.getId()));
      }
    } // alive tag is not mandatory for a Sub-Equipment

    Long commFaultTagId = subEquipment.getCommFaultTagId();
    if (commFaultTagId != null) {

      ControlTag commFaultTagCopy = controlCache.getCopy(commFaultTagId);
      if (commFaultTagCopy != null) {
        setSubEquipmentId((ControlTagCacheObject) commFaultTagCopy, subEquipment.getId(), processId);
      } else {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
            String.format("No CommFault tag (%s) found for sub-equipment #%d (%s).", commFaultTagId, subEquipment.getId(), subEquipment.getName()));
      }

    }
    else {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, String.format("No CommFault tag for Sub-Equipment %s (#%d) defined.",
          subEquipment.getName(), subEquipment.getId()));
    }

    Long statusTagId = subEquipment.getStateTagId();
    if (statusTagId != null) {

      ControlTag statusTagCopy = controlCache.getCopy(statusTagId);
      if (statusTagCopy != null) {
        setSubEquipmentId((ControlTagCacheObject) statusTagCopy, subEquipment.getId(), processId);
      } else {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
            String.format("No Status tag (%s) found for Sub-Equipment %s (#%d).", statusTagId, subEquipment.getName(), subEquipment.getId()));
      }

    }
    else {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, String.format("No Status tag for Sub-Equipment %s (#%d) defined.",
          subEquipment.getName(), subEquipment.getId()));
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
    log.trace(logMsg);
    copy.setSubEquipmentId(subEquipmentId);
    copy.setProcessId(processId);
    controlCache.putQuiet(copy);
  }

}
