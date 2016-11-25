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
package cern.c2mon.daq.config;

import cern.c2mon.daq.filter.dynamic.CounterTimeDeadbandActivator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * This configuration class is responsible for instantiating the various
 * {@link CounterTimeDeadbandActivator} beans used within the DAQ core.
 *
 * @author Justin Lewis Salmon
 */
public class DeadbandConfig {

  @Autowired
  private DaqProperties properties;

  @Bean
  public CounterTimeDeadbandActivator lowDynamicTimeDeadbandFilterActivator() {
    DaqProperties.Filter.DynamicDeadband deadband = properties.getFilter().getDynamicDeadband();
    return new CounterTimeDeadbandActivator(deadband.getWindowSize(), deadband.getCheckInterval(),
        deadband.getActivationThreshold(), deadband.getDeactivationThreshold(), deadband.getForcedDeadbandInterval());
  }

  @Bean
  public CounterTimeDeadbandActivator medDynamicTimeDeadbandFilterActivator() {
    DaqProperties.Filter.DynamicDeadband deadband = properties.getFilter().getDynamicDeadband();
    return new CounterTimeDeadbandActivator(deadband.getWindowSize(), deadband.getCheckInterval(),
        deadband.getActivationThreshold(), deadband.getDeactivationThreshold(), deadband.getForcedDeadbandInterval());
  }
}
