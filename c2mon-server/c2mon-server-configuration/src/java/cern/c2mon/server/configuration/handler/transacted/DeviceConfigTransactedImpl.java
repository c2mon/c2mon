/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2014 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.server.configuration.handler.transacted;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.cache.DeviceCache;
import cern.c2mon.server.cache.DeviceFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.DeviceDAO;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceCacheObject;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.common.ConfigurationException;

/**
 * Implementation of {@link DeviceConfigTransacted}.
 *
 * @author Justin Lewis Salmon
 */
@Service
public class DeviceConfigTransactedImpl implements DeviceConfigTransacted {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(DeviceConfigTransactedImpl.class);

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
   * Default constructor.
   *
   * @param deviceCache reference to the Device cache.
   * @param deviceFacade reference to he Device facade bean.
   * @param deviceDAO reference to the Device DAO bean.
   */
  @Autowired
  public DeviceConfigTransactedImpl(final DeviceCache deviceCache, final DeviceFacade deviceFacade, final DeviceDAO deviceDAO) {
    this.deviceCache = deviceCache;
    this.deviceFacade = deviceFacade;
    this.deviceDAO = deviceDAO;
  }

  @Override
  @Transactional(value = "cacheTransactionManager")
  public ProcessChange doCreateDevice(ConfigurationElement element) throws IllegalAccessException {
    deviceCache.acquireWriteLockOnKey(element.getEntityId());

    try {
      LOGGER.trace("Creating Device " + element.getEntityId());

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
        LOGGER.error("Exception caught while inserting a new Device into the DB - rolling back changes", e);
        throw new UnexpectedRollbackException("Unexpected exception while creating a Device: rolling back the change", e);
      }

      // Insert the device into the cache
      try {
        deviceCache.putQuiet(device);
        return new ProcessChange();

      } catch (Exception e) {
        deviceCache.remove(device.getId());
        LOGGER.error("Exception caught when attempting to create a Device - rolling back the DB transaction and undoing cache changes.");
        throw new UnexpectedRollbackException("Unexpected exception while creating a Device: rolling back the change", e);
      }

    } finally {
      deviceCache.releaseWriteLockOnKey(element.getEntityId());
    }
  }

  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public ProcessChange doUpdateDevice(Long id, Properties properties) {
    deviceCache.acquireWriteLockOnKey(id);

    try {
      LOGGER.trace("Updating Device " + id);

      Device device = deviceCache.get(id);
      deviceDAO.updateConfig(device);
      deviceFacade.updateConfig(device, properties);

      // No event for DAQ layer
      return new ProcessChange();

    } catch (CacheElementNotFoundException e) {
      throw e;

    } catch (Exception e) {
      LOGGER.error("Exception caught while updating a Device - rolling back DB transaction", e);
      throw new UnexpectedRollbackException("Unexpected exception caught while updating a Device configuration", e);

    } finally {
      deviceCache.releaseWriteLockOnKey(id);
    }
  }

  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public ProcessChange doRemoveDevice(Long id, ConfigurationElementReport elementReport) {
    LOGGER.trace("Removing Device " + id);

    try {
      DeviceCacheObject device = (DeviceCacheObject) deviceCache.get(id);

      // TODO: Remove all property and command values of this class (does this
      // mean remove the DataTags?)

      deviceCache.acquireWriteLockOnKey(id);
      try {
        deviceDAO.deleteItem(device.getId());
        return new ProcessChange();

      } catch (Exception e) {
        LOGGER.error("Exception caught while removing a Device.", e);
        elementReport.setFailure("Unable to remove Device with id " + id);
        throw new UnexpectedRollbackException("Unable to remove Device " + id, e);

      } finally {
        if (deviceCache.isWriteLockedByCurrentThread(id)) {
          deviceCache.releaseWriteLockOnKey(id);
        }
      }

    } catch (CacheElementNotFoundException e) {
      LOGGER.warn("Attempting to remove a non-existent Device - no action taken.");
      elementReport.setWarning("Attempting to remove a non-existent Device");
      return new ProcessChange();
    }
  }

}
