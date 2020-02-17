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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Franz Ritter
 */
@Named
@Singleton
class ProcessFactory extends EntityFactory<Process> {

  private ProcessDAO processDAO;
  private final AliveTagFactory aliveTagFactory;
  private final SupervisionStateTagFactory stateTagFactory;
  private SequenceDAO sequenceDAO;

  @Inject
  public ProcessFactory(C2monCache<cern.c2mon.server.common.process.Process> processCache, SequenceDAO sequenceDAO, ProcessDAO processDAO,
                        AliveTagFactory aliveTagFactory, SupervisionStateTagFactory stateTagFactory) {
    super(processCache);
    this.sequenceDAO = sequenceDAO;
    this.processDAO = processDAO;
    this.aliveTagFactory = aliveTagFactory;
    this.stateTagFactory = stateTagFactory;
  }


  @Override
  public List<ConfigurationElement> createInstance(Process entity) {
    List<ConfigurationElement> configurationElements = new ArrayList<>();

    // build the process configuration element. This also set the id of the process
    ConfigurationElement createProcess = doCreateInstance(entity);

    // If the user specified any custom tag info, use it (otherwise it will be created by the handler
    if (entity.getAliveTag() != null) {
      configurationElements.addAll(aliveTagFactory.createInstance(entity.getAliveTag()));
      createProcess.getElementProperties().setProperty("aliveTagId", entity.getAliveTag().getId().toString());
    }
    if (entity.getStatusTag() != null) {
      configurationElements.addAll(stateTagFactory.createInstance(entity.getStatusTag()));
      createProcess.getElementProperties().setProperty("stateTagId", entity.getStatusTag().getId().toString());
    }

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
