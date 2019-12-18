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
import cern.c2mon.shared.client.configuration.api.tag.AliveTag;
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Franz Ritter
 */
@Service
public class ProcessFactory extends EntityFactory<Process> {

  private ProcessDAO processDAO;
  private SequenceDAO sequenceDAO;
  private ControlTagFactory controlTagFactory;

  @Autowired
  public ProcessFactory(C2monCache<cern.c2mon.server.common.process.Process> processCache, SequenceDAO sequenceDAO, ControlTagFactory controlTagFactory,
                        ProcessDAO processDAO) {
    super(processCache);
    this.sequenceDAO = sequenceDAO;
    this.controlTagFactory = controlTagFactory;
    this.processDAO = processDAO;
  }


  @Override
  public List<ConfigurationElement> createInstance(Process entity) {
    List<ConfigurationElement> configurationElements = new ArrayList<>();

    // build the process configuration element. This also set the id of the process
    ConfigurationElement createProcess = doCreateInstance(entity);

    // build the configuration entities for the control tags.
    // This need to be done after the process id is create (see above)
    entity = setDefaultControlTags(entity);

    configurationElements.addAll(controlTagFactory.createInstance(entity.getAliveTag()));
    configurationElements.addAll(controlTagFactory.createInstance(entity.getStatusTag()));

    createProcess.getElementProperties().setProperty("aliveTagId", entity.getAliveTag().getId().toString());
    createProcess.getElementProperties().setProperty("statusTagId", entity.getStatusTag().getId().toString());

    configurationElements.add(createProcess);

    return configurationElements;
  }

  /**
   * Checks if the Process has a defined {@link AliveTag} or {@link StatusTag}.
   * If not a automatic Status tag will be created and attached to the process configuration.
   *
   * @param process The Process which contains the information of an create.
   * @return The same process from the parameters attached with the status tag information.
   */
  public static Process setDefaultControlTags(Process process) {

    if (process.getAliveTag() == null) {

      AliveTag aliveTag = AliveTag.create(process.getName() + ":ALIVE")
          .description("Alive tag for process " + process.getName())
          .build();
      process.setAliveTag(aliveTag);
    }

    if (process.getStatusTag() == null) {

      StatusTag statusTag = StatusTag.create(process.getName() + ":STATUS")
          .description("Status tag for process " + process.getName())
          .build();
      process.setStatusTag(statusTag);
    }

    process.getAliveTag().setProcessId(process.getId());
    process.getStatusTag().setProcessId(process.getId());

    return process;
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
  ConfigConstants.Entity getEntity() {
    return ConfigConstants.Entity.PROCESS;
  }
}
