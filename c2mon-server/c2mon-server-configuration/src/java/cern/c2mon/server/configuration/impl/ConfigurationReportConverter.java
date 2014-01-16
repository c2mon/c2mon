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
package cern.c2mon.server.configuration.impl;

import org.apache.log4j.Logger;

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
  private static final Logger LOGGER = Logger.getLogger(ConfigurationReportConverter.class);
  
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
