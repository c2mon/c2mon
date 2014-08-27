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

import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.cache.DeviceClassCache;
import cern.c2mon.server.cache.DeviceClassFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.DeviceClassDAO;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.common.device.DeviceClassCacheObject;
import cern.c2mon.server.configuration.handler.DeviceConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.common.ConfigurationException;

/**
 * Implementation of {@link DeviceClassConfigTransacted}.
 *
 * @author Justin Lewis Salmon
 */
@Service
public class DeviceClassConfigTransactedImpl implements DeviceClassConfigTransacted {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(DeviceClassConfigTransactedImpl.class);

  /**
   * Reference to the DeviceClass cache.
   */
  private DeviceClassCache deviceClassCache;

  /**
   * Reference to the DeviceClass facade bean.
   */
  private DeviceClassFacade deviceClassFacade;

  /**
   * Reference to the DeviceClass DAO bean.
   */
  private DeviceClassDAO deviceClassDAO;

  /**
   * Reference to the DeviceConfigHandler.
   */
  private DeviceConfigHandler deviceConfigHandler;

  /**
   * Default constructor.
   *
   * @param deviceClassCache reference to the DeviceClass cache.
   * @param deviceClassFacade reference to he DeviceClass facade bean.
   * @param deviceClassDAO reference to the DeviceClass DAO bean.
   * @param deviceConfigHandler reference to the DeviceConfigHandler
   */
  @Autowired
  public DeviceClassConfigTransactedImpl(final DeviceClassCache deviceClassCache,
                                         final DeviceClassFacade deviceClassFacade,
                                         final DeviceClassDAO deviceClassDAO,
                                         final DeviceConfigHandler deviceConfigHandler) {
    this.deviceClassCache = deviceClassCache;
    this.deviceClassFacade = deviceClassFacade;
    this.deviceClassDAO = deviceClassDAO;
    this.deviceConfigHandler = deviceConfigHandler;
  }

  @Override
  @Transactional(value = "cacheTransactionManager")
  public ProcessChange doCreateDeviceClass(final ConfigurationElement element) throws IllegalAccessException {
    deviceClassCache.acquireWriteLockOnKey(element.getEntityId());

    try {
      LOGGER.trace("Creating DeviceClass " + element.getEntityId());

      // Check if the device class already exists
      if (deviceClassCache.hasKey(element.getEntityId())) {
        throw new ConfigurationException(ConfigurationException.ENTITY_EXISTS, "Attempting to create a DeviceClass with an already existing id: "
            + element.getEntityId());
      }

      // Create the cache object
      DeviceClass deviceClass = deviceClassFacade.createCacheObject(element.getEntityId(), element.getElementProperties());

      // Insert the device class into the DB
      try {
        deviceClassDAO.insert(deviceClass);

      } catch (Exception e) {
        LOGGER.error("Exception caught while inserting a new DeviceClass into the DB - rolling back changes", e);
        throw new UnexpectedRollbackException("Unexpected exception while creating a DeviceClass: rolling back the change", e);
      }

      // Insert the device class into the cache
      try {
        deviceClassCache.putQuiet(deviceClass);
        return new ProcessChange();

      } catch (Exception e) {
        deviceClassCache.remove(deviceClass.getId());
        LOGGER.error("Exception caught when attempting to create a DeviceClass - rolling back the DB transaction and undoing cache changes.");
        throw new UnexpectedRollbackException("Unexpected exception while creating a DeviceClass: rolling back the change", e);
      }

    } finally {
      deviceClassCache.releaseWriteLockOnKey(element.getEntityId());
    }
  }

  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public ProcessChange doUpdateDeviceClass(Long id, Properties properties) {
    deviceClassCache.acquireWriteLockOnKey(id);

    try {
      LOGGER.trace("Updating DeviceClass " + id);

      DeviceClass deviceClass = deviceClassCache.get(id);
      deviceClassDAO.updateConfig(deviceClass);
      deviceClassFacade.updateConfig(deviceClass, properties);

      // No event for DAQ layer
      return new ProcessChange();

    } catch (CacheElementNotFoundException e) {
      throw e;

    } catch (Exception e) {
      LOGGER.error("Exception caught while updating a DeviceClass - rolling back DB transaction", e);
      throw new UnexpectedRollbackException("Unexpected exception caught while updating a DeviceClass configuration", e);

    } finally {
      deviceClassCache.releaseWriteLockOnKey(id);
    }
  }

  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public ProcessChange doRemoveDeviceClass(Long id, ConfigurationElementReport elementReport) {
    LOGGER.trace("Removing DeviceClass " + id);

    try {
      DeviceClassCacheObject deviceClass = (DeviceClassCacheObject) deviceClassCache.get(id);

      // Remove all Devices dependent on this class (using DeviceConfigHandler)
      List<Long> deviceIds = deviceClass.getDeviceIds();
      if (!deviceIds.isEmpty()) {
        LOGGER.trace("Removing Devices dependent on DeviceClass " + id);

        for (Long deviceId : deviceIds) {
          ConfigurationElementReport newReport = new ConfigurationElementReport(Action.REMOVE, Entity.DEVICE, deviceId);
          elementReport.addSubReport(newReport);
          deviceConfigHandler.removeDevice(deviceId, newReport);
        }
      }

      // TODO: Remove all properties and commands of this class (does this mean
      // remove the DataTags?)

      deviceClassCache.acquireWriteLockOnKey(id);
      try {
        deviceClassDAO.deleteItem(deviceClass.getId());
        return new ProcessChange();

      } catch (Exception e) {
        LOGGER.error("Exception caught while removing a DeviceClass.", e);
        elementReport.setFailure("Unable to remove DeviceClass with id " + id);
        throw new UnexpectedRollbackException("Unable to remove DeviceClass " + id, e);

      } finally {
        if (deviceClassCache.isWriteLockedByCurrentThread(id)) {
          deviceClassCache.releaseWriteLockOnKey(id);
        }
      }

    } catch (CacheElementNotFoundException e) {
      LOGGER.warn("Attempting to remove a non-existent DeviceClass - no action taken.");
      elementReport.setWarning("Attempting to remove a non-existent DeviceClass");
      return new ProcessChange();
    }
  }
}
