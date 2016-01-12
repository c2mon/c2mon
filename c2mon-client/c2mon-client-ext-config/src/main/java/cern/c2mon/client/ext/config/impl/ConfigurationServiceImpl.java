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
package cern.c2mon.client.ext.config.impl;

import cern.c2mon.client.ext.config.ConfigurationService;
import cern.c2mon.client.ext.config.request.ConfigurationRequestSender;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.configuration.Configuration;
import cern.c2mon.shared.client.configuration.configuration.ConfigurationListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Justin Lewis Salmon
 */
@Service
public class ConfigurationServiceImpl implements ConfigurationService {

  @Autowired
  private ConfigurationRequestSender requestSender;

  @Override
  public ConfigurationReport applyConfiguration(Configuration configuration, ConfigurationListener listener) {
    return requestSender.applyConfiguration(configuration, listener);
  }
}
