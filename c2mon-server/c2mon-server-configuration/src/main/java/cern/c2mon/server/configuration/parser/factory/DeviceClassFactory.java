/*******************************************************************************
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
 ******************************************************************************/

package cern.c2mon.server.configuration.parser.factory;

import cern.c2mon.server.cache.DeviceClassCache;
import cern.c2mon.server.cache.loading.DeviceClassDAO;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.device.DeviceClass;
import cern.c2mon.shared.client.device.Command;
import cern.c2mon.shared.client.device.Property;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Elisabeth Stockinger
 */
@Service
@Slf4j
public class DeviceClassFactory extends EntityFactory<DeviceClass> {

  private DeviceClassDAO deviceClassDAO;
  private SequenceDAO sequenceDAO;

  @Autowired
  public DeviceClassFactory(DeviceClassCache deviceClassCache, SequenceDAO sequenceDAO, DeviceClassDAO deviceClassDAO) {
    super(deviceClassCache);
    this.sequenceDAO = sequenceDAO;
    this.deviceClassDAO = deviceClassDAO;
  }


  @Override
  public List<ConfigurationElement> createInstance(DeviceClass entity) {
    List<ConfigurationElement> configurationElements = new ArrayList<>();

    // Build the process configuration element. This also sets the device class id
    ConfigurationElement createDeviceClass = doCreateInstance(entity);
    // TODO: should properties / commands go here?
    configurationElements.add(createDeviceClass);

    return configurationElements;
  }


  @Override
  Long getId(DeviceClass entity) {
    return entity.getId() != null ? entity.getId() : deviceClassDAO.getIdByName(entity.getName());
  }

  @Override
  Long createId(DeviceClass entity) {
    if (entity.getName() != null && deviceClassDAO.getIdByName(entity.getName()) != null) {
      throw new ConfigurationParseException("Error creating deviceClass " + entity.getName() + ": " +
              "Name already exists");
    } else {
      createAndSetPropertyIds(entity);
      createAndSetCommandIds(entity);
      return entity.getId() != null ? entity.getId() : sequenceDAO.getNextDeviceClassId();
    }
  }

  @Override
  ConfigConstants.Entity getEntity() {
    return ConfigConstants.Entity.DEVICECLASS;
  }

  private void createAndSetPropertyIds(DeviceClass entity) {
    entity.getProperties().getProperties().stream()
            .filter(property -> property.getId() != null)
            .forEach(property -> property.setId(sequenceDAO.getNextPropertyId()));
  }

  private void createAndSetCommandIds(DeviceClass entity) {
    entity.getCommands().getCommands().stream()
            .filter(command -> command.getId() != null)
            .forEach(command -> command.setId(sequenceDAO.getNextCommandId()));
  }
}
