/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.configuration;

import cern.tim.shared.client.configuration.ConfigurationException;
import cern.tim.shared.client.configuration.ConfigurationReport;

/**
 * Public bean that can be accessed to load configurations into
 * the server at runtime.
 * 
 * <p>Another option provided by this module for activating a reconfiguration 
 * is through the ${tim.config} JMS queue. Clients can send reconfiguration
 * requests directly to this queue (the ConfigurationLoader is then called
 * internally).
 * 
 * @author Mark Brightwell
 *
 */
public interface ConfigurationLoader {

  /**
   * Applies the configuration with the given id to the server. This involves
   * retrieving the configuration details from the Config database and making
   * the appropriate changes in the server cache and cache database. If necessary,
   * configuration elements are also passed on to the DAQ layer for further action.
   * 
   * <p>The returned report is made up of reports for each individual configuration
   * element, including reports returned by the DAQ.
   * 
   * <p>Configuration are made up of a number of configuration elements. Configuration
   * elements are either applied successfully on the server or not at all. However,
   * if a configuration element fails on the DAQ layer for some reason, but applied
   * successfully on the server, the change will be committed on the server (DAQ
   * may need a restart).
   * 
   * <p>TODO NOT DECIDED YET: If a reconfiguration of a point, rule or alarm fails for 
   * some reason, the element mode is switched to UNCONFIGURED and a new configuration 
   * should be applied. Notice that if an equipment or process reconfiguration fails, 
   * the state tag is set to RECONFIGURED until a new successful reconfiguration is applied.
   * 
   * @param configId the id of the configuration to apply to the server
   * @param sessionId authorization to run the config loader
   * @return a report with details of the success/failure of the reconfiguration
   * @throws ConfigurationException if the configuration fails (report is attached)
   */
  ConfigurationReport applyConfiguration(int configId, String sessionId);
  
}
