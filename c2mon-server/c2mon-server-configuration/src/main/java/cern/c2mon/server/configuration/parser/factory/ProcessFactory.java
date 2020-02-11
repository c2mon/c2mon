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

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.loading.ProcessDAO;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.process.Process;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Franz Ritter
 */
@Service
class ProcessFactory extends EntityFactory<Process> {

  private ProcessDAO processDAO;
  private SequenceDAO sequenceDAO;

  @Autowired
  public ProcessFactory(C2monCache<cern.c2mon.server.common.process.Process> processCache, SequenceDAO sequenceDAO, ProcessDAO processDAO) {
    super(processCache);
    this.sequenceDAO = sequenceDAO;
    this.processDAO = processDAO;
  }


  @Override
  public List<ConfigurationElement> createInstance(Process entity) {
    List<ConfigurationElement> configurationElements = new ArrayList<>();

    // build the process configuration element. This also set the id of the process
    ConfigurationElement createProcess = doCreateInstance(entity);

    configurationElements.add(createProcess);

    return configurationElements;
  }

  @Override
  Long getId(Process entity) {
    return entity.getId() != null ? entity.getId() : processDAO.getIdByName(entity.getName());
  }

  @Override
  Long createId(Process entity) {
    if (entity.getName() != null && processDAO.getIdByName(entity.getName()) != null) {
      throw new ConfigurationParseException("Error creating process " + entity.getName() + ": " +
          "Name already exists");
    } else {
      return entity.getId() != null ? entity.getId() : sequenceDAO.getNextProcessId();
    }
  }

  @Override
  public ConfigConstants.Entity getEntity() {
    return ConfigConstants.Entity.PROCESS;
  }
}
