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
package cern.c2mon.server.configuration.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.client.configuration.ConfigConstants.Status;
import cern.c2mon.shared.daq.config.ChangeReport;

/**
 * Utility class for converting between Process and Client configuration report formats.
 * @author Mark Brightwell
 *
 */
public final class ConfigurationReportConverter {
  
  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationReportConverter.class);
  
  /**
   * Cannot be instantiated.
   */
  private ConfigurationReportConverter() {    
  }
  
  /**
   * Given a Process ChangeReport and the applied configuration element,
   * returns a {@link ConfigurationElementReport} with
   * the same info.
   * 
   * @param changeReport the report from the DAQ
   * @param element the configuration element report for the equivalent 
   *                  action on the server (used to include extra info)
   * @return the report for the client
   */
  public static ConfigurationElementReport fromProcessReport(final ChangeReport changeReport, final ConfigurationElementReport parentReport) {
    Status status = Status.FAILURE;
    switch (changeReport.getState()) {
      case SUCCESS : status = Status.OK; break;
      case FAIL : status = Status.FAILURE; break;
      case REBOOT : status = Status.RESTART; break;
      case PENDING : status = Status.FAILURE; break;
      default : LOGGER.warn("Unrecognized Process status received - setting the report to failure!");
                changeReport.appendWarn("Server failed to recognized the Process status flag - setting the report to failure.");
    }
    String statusMessage = "DAQ INFO REPORT: " + changeReport.getInfoMessage() + "\n"
                         + "DAQ WARNING: " + changeReport.getWarnMessage() + "\n"
                         + "DAQ ERROR: " + changeReport.getErrorMessage();
    
    return new ConfigurationElementReport(parentReport.getAction(), 
                                          parentReport.getEntity(), 
                                          parentReport.getId(), 
                                          status, statusMessage);
  }
  
}
