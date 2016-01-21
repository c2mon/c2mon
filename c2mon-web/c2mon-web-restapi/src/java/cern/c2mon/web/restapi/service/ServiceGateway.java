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
package cern.c2mon.web.restapi.service;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.ConfigurationService;
import cern.c2mon.client.core.TagService;
import cern.c2mon.client.core.AlarmService;
import cern.c2mon.client.core.CommandService;
import cern.c2mon.client.ext.history.C2monHistoryGateway;
import cern.c2mon.client.ext.history.C2monHistoryManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Service wrapper bean around {@link C2monServiceGateway} for accessing C2MON
 * manager singleton beans.
 *
 * @author Justin Lewis Salmon
 */
@Service
@Slf4j
public class ServiceGateway {

  @Getter
  private TagService tagService;

  @Getter
  private AlarmService alarmService;

  @Getter
  private CommandService commandService;

  @Getter
  private ConfigurationService configurationService;

  @Getter
  private C2monHistoryManager historyManager;

  /**
   * Called by Spring when the service has been created. Starts the C2MON client
   * and initialises references to the manager beans.
   */
  @PostConstruct
  public void init() {
    log.info("Starting C2MON service gateway...");
    C2monServiceGateway.startC2monClientSynchronous();

    tagService = C2monServiceGateway.getTagService();
    alarmService = C2monServiceGateway.getAlarmService();
    configurationService = C2monServiceGateway.getConfigurationService();
    commandService = C2monServiceGateway.getCommandService();
    historyManager = C2monHistoryGateway.getHistoryManager();
  }
}
