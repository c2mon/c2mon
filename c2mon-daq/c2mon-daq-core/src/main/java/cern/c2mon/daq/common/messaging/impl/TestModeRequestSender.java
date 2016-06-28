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
package cern.c2mon.daq.common.messaging.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import cern.c2mon.daq.common.messaging.ProcessRequestSender;
import cern.c2mon.shared.common.process.ProcessConfiguration;
import cern.c2mon.shared.daq.process.ProcessConfigurationResponse;
import cern.c2mon.shared.daq.process.ProcessConnectionResponse;

public class TestModeRequestSender implements ProcessRequestSender {

  private final ProcessRequestSender wrapperRequestSender;

  @Autowired
  public TestModeRequestSender(@Qualifier("activeRequestSender") final ProcessRequestSender wrapper) {
    this.wrapperRequestSender = wrapper;
  }

  @Override
  public ProcessConfigurationResponse sendProcessConfigurationRequest(final String processName) {
    return wrapperRequestSender.sendProcessConfigurationRequest(processName);
  }

  @Override
  public ProcessConnectionResponse sendProcessConnectionRequest(final String processName) {
    return new ProcessConnectionResponse(processName, 123456L);
  }

  @Override
  public void sendProcessDisconnectionRequest(ProcessConfiguration processConfiguration, long startupTime) {
    // TODO Auto-generated method stub

  }
}
