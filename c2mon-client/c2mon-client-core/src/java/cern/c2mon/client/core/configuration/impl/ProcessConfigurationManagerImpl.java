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
package cern.c2mon.client.core.configuration.impl;

import cern.c2mon.client.core.configuration.ConfigurationRequestSender;
import cern.c2mon.client.core.configuration.ProcessConfigurationManager;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.process.Process;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cern.c2mon.client.core.configuration.util.ConfigurationUtil.*;

/**
 * @author Franz Ritter
 */
@Service("processConfigurationManager")
public class ProcessConfigurationManagerImpl implements ProcessConfigurationManager {

  private ConfigurationRequestSender configurationRequestSender;

  @Autowired
  ProcessConfigurationManagerImpl(ConfigurationRequestSender configurationRequestSender) {
    this.configurationRequestSender = configurationRequestSender;
  }

  public ConfigurationReport createProcess(String processName) {

    return createProcess(Process.create(processName).build());
  }

  public ConfigurationReport createProcess(Process process) {

    List<Process> processes = new ArrayList<>();
    processes.add(process);

    validateIsCreate(processes);

    Configuration config = new Configuration();
    config.setEntities(processes);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport updateProcess(Process process) {

    List<Process> processes = new ArrayList<>();
    processes.add(process);

    validateIsUpdate(processes);

    Configuration config = new Configuration();
    config.setEntities(processes);

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeProcessById(Long id) {

    Process deleteProcess = new Process();
    deleteProcess.setId(id);
    deleteProcess.setDeleted(true);

    Configuration config = new Configuration();
    config.setEntities(Collections.singletonList(deleteProcess));

    return configurationRequestSender.applyConfiguration(config, null);
  }

  @Override
  public ConfigurationReport removeProcess(String name) {

    Process deleteProcess = new Process();
    deleteProcess.setName(name);
    deleteProcess.setDeleted(true);

    Configuration config = new Configuration();
    config.setEntities(Collections.singletonList(deleteProcess));

    return configurationRequestSender.applyConfiguration(config, null);
  }


}
