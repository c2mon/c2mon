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
package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.server.cache.DeviceCache;
import cern.c2mon.server.cache.DeviceClassCache;
import cern.c2mon.server.cache.DeviceFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.DeviceDAO;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceCacheObject;
import cern.c2mon.server.configuration.handler.DeviceConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.common.ConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Properties;

/**
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class DeviceConfigTransactedImpl implements DeviceConfigHandler {

  /**
   * Reference to the Device cache.
   */
  private DeviceCache deviceCache;

  /**
   * Reference to the Device facade bean.
   */
  private DeviceFacade deviceFacade;

  /**
   * Reference to the Device DAO bean.
   */
  private DeviceDAO deviceDAO;

  /**
   * Reference to the DeviceClass cache.
   */
  private DeviceClassCache deviceClassCache;

  /**
   * Default constructor.
   *
   * @param deviceCache reference to the Device cache.
   * @param deviceFacade reference to he Device facade bean.
   * @param deviceDAO reference to the Device DAO bean.
   * @param deviceClassCache reference to the DeviceClass cache.
   */
  @Autowired
  public DeviceConfigTransactedImpl(final DeviceCache deviceCache,
                                    final DeviceFacade deviceFacade,
                                    final DeviceDAO deviceDAO,
                                    final DeviceClassCache deviceClassCache) {
    this.deviceCache = deviceCache;
    this.deviceFacade = deviceFacade;
    this.deviceDAO = deviceDAO;
    this.deviceClassCache = deviceClassCache;
  }

  @Override
  @Transactional(value = "cacheTransactionManager")
  public ProcessChange create(ConfigurationElement element) throws IllegalAccessException {
    deviceCache.acquireWriteLockOnKey(element.getEntityId());

    try {
      log.trace("Creating Device " + element.getEntityId());

      // Check if the device class already exists
      if (deviceCache.hasKey(element.getEntityId())) {
        throw new ConfigurationException(ConfigurationException.ENTITY_EXISTS, "Attempting to create a Device with an already existing id: "
            + element.getEntityId());
      }

      // Create the cache object
      Device device = deviceFacade.createCacheObject(element.getEntityId(), element.getElementProperties());

      // Insert the device into the DB
      try {
        deviceDAO.insert(device);

      } catch (Exception e) {
        log.error("Exception caught while inserting a new Device into the DB - rolling back changes", e);
        throw new UnexpectedRollbackException("Unexpected exception while creating a Device: rolling back the change", e);
      }

      // Insert the device into the cache
      try {
        deviceCache.putQuiet(device);

        // Update the device class so that it knows about the new device
        deviceClassCache.updateDeviceIds(device.getDeviceClassId());

        return new ProcessChange();

      } catch (Exception e) {
        deviceCache.remove(device.getId());
        log.error("Exception caught when attempting to create a Device - rolling back the DB transaction and undoing cache changes.");
        throw new UnexpectedRollbackException("Unexpected exception while creating a Device: rolling back the change", e);
      }

    } finally {
      deviceCache.releaseWriteLockOnKey(element.getEntityId());
    }
  }

  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public ProcessChange update(Long id, Properties properties) {
    deviceCache.acquireWriteLockOnKey(id);

    try {
      log.trace("Updating Device " + id);

      Device device = deviceCache.get(id);
      deviceDAO.updateConfig(device);
      deviceFacade.updateConfig(device, properties);

      // No event for DAQ layer
      return new ProcessChange();

    } catch (CacheElementNotFoundException e) {
      throw e;

    } catch (Exception e) {
      log.error("Exception caught while updating a Device - rolling back DB transaction", e);
      throw new UnexpectedRollbackException("Unexpected exception caught while updating a Device configuration", e);

    } finally {
      deviceCache.releaseWriteLockOnKey(id);
    }
  }

  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public ProcessChange remove(Long id, ConfigurationElementReport elementReport) {
    log.trace("Removing Device " + id);

    try {
      deviceCache.acquireWriteLockOnKey(id);
      DeviceCacheObject device = (DeviceCacheObject) deviceCache.get(id);

      try {
        deviceDAO.deleteItem(device.getId());
        return new ProcessChange();

      } catch (Exception e) {
        log.error("Exception caught while removing a Device.", e);
        elementReport.setFailure("Unable to remove Device with id " + id);
        throw new UnexpectedRollbackException("Unable to remove Device " + id, e);
      }

    } catch (CacheElementNotFoundException e) {
      log.warn("Attempting to remove a non-existent Device - no action taken.");
      elementReport.setWarning("Attempting to remove a non-existent Device");
      return new ProcessChange();

    } finally {
      if (deviceCache.isWriteLockedByCurrentThread(id)) {
        deviceCache.releaseWriteLockOnKey(id);
      }
    }
  }

}
