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

import cern.c2mon.cache.actions.device.DeviceCacheObjectFactory;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.loading.DeviceDAO;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceClass;
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

  private C2monCache<Device> deviceCache;

  private DeviceDAO deviceDAO;

  private DeviceCacheObjectFactory deviceCacheObjectFactory;

  private C2monCache<DeviceClass> deviceClassCache;

  /**
   * Default constructor.
   * @param deviceCache reference to the Device cache.
   * @param deviceDAO
   * @param deviceCacheObjectFactory reference to the Device DAO bean.
   * @param deviceClassCache reference to the DeviceClass cache.
   */
  @Autowired
  public DeviceConfigTransactedImpl(final C2monCache<Device> deviceCache,
                                    final DeviceDAO deviceDAO, final DeviceCacheObjectFactory deviceCacheObjectFactory,
                                    final C2monCache<DeviceClass> deviceClassCache) {
    this.deviceCache = deviceCache;
    this.deviceDAO = deviceDAO;
    this.deviceCacheObjectFactory = deviceCacheObjectFactory;
    this.deviceClassCache = deviceClassCache;
  }

  @Override
  @Transactional(value = "cacheTransactionManager")
  public ProcessChange create(ConfigurationElement element) {
    log.trace("Creating Device " + element.getEntityId());

    if (deviceCache.containsKey(element.getEntityId())) {
      throw new ConfigurationException(ConfigurationException.ENTITY_EXISTS, "Attempting to create a Device with an already existing id: "
        + element.getEntityId());
    }

    Device device = deviceCacheObjectFactory.createCacheObject(element.getEntityId(), element.getElementProperties());

    // DB insert
    deviceDAO.insert(device);

    // Cache insert
    deviceCache.putQuiet(device.getId(), device);

    // Update the device class so that it knows about the new device
    deviceClassCache.loadFromDb(device.getDeviceClassId());

    return new ProcessChange();
  }

  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public ProcessChange update(Long id, Properties properties) {
    log.trace("Updating Device " + id);

    Device device = deviceCache.get(id);

    deviceCacheObjectFactory.updateConfig(device, properties);

    deviceDAO.updateConfig(device);

    deviceCache.put(id, device);

    // No event for DAQ layer
    return new ProcessChange();
  }

  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public ProcessChange remove(Long id, ConfigurationElementReport elementReport) {
    log.trace("Removing Device " + id);

    if (deviceCache.containsKey(id)) {
      try {
        deviceDAO.deleteItem(id);

        deviceCache.remove(id);

      } catch (RuntimeException e) {
        log.error("Exception caught while removing a Device.", e);
        elementReport.setFailure("Unable to remove Device with id " + id);
        throw new UnexpectedRollbackException("Unable to remove Device " + id, e);
      }
    }
    else {
      log.warn("Attempting to remove a non-existent Device - no action taken.");
      elementReport.setWarning("Attempting to remove a non-existent Device");
      return new ProcessChange();
    }

    return new ProcessChange();
  }

}
